package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    List<Attendee> findByEventId(String eventId);
    long countByEventId(String eventId);
}
