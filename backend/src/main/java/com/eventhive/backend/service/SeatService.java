package com.eventhive.backend.service;

import com.eventhive.backend.dto.SeatGenerationRequest;
import com.eventhive.backend.entity.Event;
import com.eventhive.backend.entity.Seat;
import com.eventhive.backend.entity.TicketCategory;
import com.eventhive.backend.repository.EventRepository;
import com.eventhive.backend.repository.SeatRepository;
import com.eventhive.backend.repository.TicketCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    // Dynamic Seat Generation Logic
    public List<Seat> generateDynamicSeats(SeatGenerationRequest request) {

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        TicketCategory category = ticketCategoryRepository.findById(request.getTicketCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Seat> seatsToSave = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < request.getNumberOfRows(); rowIndex++) {

            // පේළියේ නම (A, B, C...) හදාගන්නවා (ASCII අගය පාවිච්චි කරලා)
            char rowLetter = (char) ('A' + (rowIndex % 26));

            // පේළි 26කට වඩා තිබ්බොත් AA, AB වගේ යන්න වෙනම ලොජික් එකක් ඕන, දැනට A-Z ඇතියි කියලා හිතමු

            for (int seatIndex = 0; seatIndex < request.getSeatsPerRow(); seatIndex++) {
                Seat seat = new Seat();
                seat.setEvent(event);
                seat.setTierName(category.getName());
                seat.setPrice(category.getPrice()); // Category එකෙන් Base Price එක ගන්නවා
                seat.setStatus("AVAILABLE");

                // Type එක Standing ද Chair ද කියලා Category එකෙන් බලලා තීරණය කරනවා
                seat.setSeatType(category.isStanding() ? "STANDING" : "CHAIR");

                // Seat Number එක හැදීම (උදා: A1, A2, B1...)
                seat.setSeatNumber(rowLetter + String.valueOf(seatIndex + 1));

                // 📐 X සහ Y ඛණ්ඩාංක ගණනය කිරීම
                seat.setXCoordinate(request.getStartX() + (seatIndex * request.getSeatGap()));
                seat.setYCoordinate(request.getStartY() + (rowIndex * request.getRowGap()));

                seatsToSave.add(seat);
            }
        }

        // ලූප් එකෙන් හැදුණු පුටු ඔක්කොම එකපාර Database එකට Save කරනවා (මේක performance වලට ගොඩක් හොඳයි)
        return seatRepository.saveAll(seatsToSave);
    }

    public List<Seat> getSeatsByEvent(Long eventId) {
        return seatRepository.findByEventId(eventId);
    }

    public List<Seat> saveCustomSeats(com.eventhive.backend.dto.SeatCustomSaveRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<Seat> seatsToSave = new ArrayList<>();
        
        // Remove existing seats for this event (optional, depending on if we are fully replacing the layout)
        seatRepository.deleteAll(seatRepository.findByEventId(event.getId()));

        int i = 1;
        for (com.eventhive.backend.dto.CustomSeatDTO dto : request.getSeats()) {
            Seat seat = new Seat();
            seat.setEvent(event);
            seat.setSeatType(dto.getType()); // CHAIR, TABLE, STANDING, STAGE, WALKWAY
            seat.setXCoordinate(dto.getX());
            seat.setYCoordinate(dto.getY());

            if ("STAGE".equals(dto.getType()) || "WALKWAY".equals(dto.getType())) {
                seat.setTierName("INFRASTRUCTURE");
                seat.setPrice(0.0);
                seat.setStatus("LOCKED");
                seat.setSeatNumber(dto.getType() + "_" + i);
            } else {
                TicketCategory category = ticketCategoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                seat.setTierName(category.getName());
                seat.setPrice(category.getPrice());
                seat.setStatus("AVAILABLE");
                seat.setSeatNumber("C" + i); // Custom numbering logic could go here
            }

            seatsToSave.add(seat);
            i++;
        }

        return seatRepository.saveAll(seatsToSave);
    }
    public com.eventhive.backend.dto.SeatStatsDTO getSeatStats(Long eventId) {
        List<Seat> allSeats = seatRepository.findByEventId(eventId);
        com.eventhive.backend.dto.SeatStatsDTO stats = new com.eventhive.backend.dto.SeatStatsDTO();
        
        int tablesCount = 0;
        int standingCount = 0;
        int standaloneChairs = 0;
        int tableChairs = 0;
        
        java.util.Set<Long> tableChairIds = new java.util.HashSet<>();
        
        // First pass: Find tables and their surrounding chairs
        for (Seat seat : allSeats) {
            if ("TABLE".equals(seat.getSeatType())) {
                tablesCount++;
                for (Seat other : allSeats) {
                    if ("CHAIR".equals(other.getSeatType())) {
                        double dx = Math.abs(other.getXCoordinate() - seat.getXCoordinate());
                        double dy = Math.abs(other.getYCoordinate() - seat.getYCoordinate());
                        if (dx <= 1.5 && dy <= 1.5) {
                            tableChairIds.add(other.getId());
                        }
                    }
                }
            } else if ("STANDING".equals(seat.getSeatType())) {
                standingCount++;
            }
        }
        
        // Second pass: Count standalone vs table chairs
        for (Seat seat : allSeats) {
            if ("CHAIR".equals(seat.getSeatType())) {
                if (tableChairIds.contains(seat.getId())) {
                    tableChairs++;
                } else {
                    standaloneChairs++;
                }
            }
        }
        
        stats.setTables(tablesCount);
        stats.setStandingZones(standingCount);
        stats.setTableChairs(tableChairs);
        stats.setStandaloneChairs(standaloneChairs);
        
        return stats;
    }
}