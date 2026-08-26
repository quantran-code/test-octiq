package com.example.demo.repository;

import com.example.demo.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmailIgnoreCase(String email);

    @Query("SELECT s FROM Student s WHERE "
            + "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR "
            + "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR "
            + "LOWER(s.email) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Student> search(@Param("term") String term, Pageable pageable);
}
