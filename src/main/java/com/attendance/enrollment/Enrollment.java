package com.attendance.enrollment;

import java.time.LocalDate;

import com.attendance.schoolclass.SchoolClass;
import com.attendance.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Connects one student with one class and records when they enrolled. */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollment_student_class", columnNames = {"student_id", "class_id"})
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "enrolled_on", nullable = false)
    private LocalDate enrolledOn = LocalDate.now();

    protected Enrollment() {
    }

    public Enrollment(Student student, SchoolClass schoolClass) {
        this.student = student;
        this.schoolClass = schoolClass;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }
}
