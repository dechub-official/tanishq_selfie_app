package com.dechub.tanishq.repository;

import com.dechub.tanishq.entity.Invitee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteeRepository extends JpaRepository<Invitee, Long> {
    List<Invitee> findByEventId(String eventId);
}
