package com.eventhive.backend.repository;

import com.eventhive.backend.entity.Event;
import com.eventhive.backend.entity.User;
import com.eventhive.backend.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    // 🔥 අලුත් කරපු පේළිය: දැනටමත් පෝලිමේ ඉන්නවාද සහ තාම නොටිෆයි කරලා නැද්ද කියලා බලනවා
    boolean existsByUserAndEventAndNotifiedFalse(User user, Event event);

    Optional<Waitlist> findTopByEventOrderByPositionDesc(Event event);

    Optional<Waitlist> findTopByEventAndNotifiedFalseOrderByPositionAsc(Event event);

    java.util.List<Waitlist> findByUser_Email(String email);
}