package com.eventhive.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ticket_categories")
@Data
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // මේ කාණ්ඩය අයිති මොන ඉවෙන්ට් එකටද කියලා
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore // JSON එද්දී අනවශ්‍ය විදියට loop වෙන එක නවත්වන්න
    private Event event;

    // Organizer දෙන නම (උදා: VIP, Balcony, Box)
    @Column(nullable = false)
    private String name;

    // ටිකට් එකේ මිල
    @Column(nullable = false)
    private Double price;

    // මේක පුටු තියෙන එකක්ද, නැත්නම් හිටගෙන ඉන්න (Standing) එකක්ද කියලා
    @Column(name = "is_standing", nullable = false, columnDefinition = "boolean default false")
    private boolean isStanding;

    // නව වෙනස්කම: Ticket Capacity එක
    @Column(name = "capacity")
    private Integer capacity;

    // UI එකේ පෙන්නන්න ඕන පාට (උදා: VIP වලට රන්වන් පාට #FFD700)
    @Column(name = "color_code")
    private String colorCode;
}