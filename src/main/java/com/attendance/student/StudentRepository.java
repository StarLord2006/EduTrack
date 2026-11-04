package com.attendance.student;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring creates the database implementation for us at startup.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
