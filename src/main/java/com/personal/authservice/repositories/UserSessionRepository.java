package com.personal.authservice.repositories;

import com.personal.authservice.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<Session, Long> {
    Session save(Session session);
}
