package com.attendance.attendance;

import java.time.LocalDate;

import com.attendance.enrollment.Enrollment;
import com.attendance.subject.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** One attendance decision for one enrolled student, subject and date. */
@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_enrollment_subject_date",
                columnNames = {"enrollment_id", "subject_id", "attendance_date"})
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Using Enrollment ensures the student is actually registered in the class.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    // Nullable for schema migration of existing rows; the service always supplies it.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "subject_id", nullable = true,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Subject subject;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    protected Attendance() {
    }

    public Attendance(Enrollment enrollment, Subject subject, LocalDate attendanceDate, AttendanceStatus status) {
        this.enrollment = enrollment;
        this.subject = subject;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public Subject getSubject() {
        return subject;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }
}
