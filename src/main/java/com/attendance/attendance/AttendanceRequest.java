package com.attendance.attendance;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

/** Input needed to mark a student's attendance for one enrolled class. */
public record AttendanceRequest(
        @NotNull(message = "Choose a student and class enrollment")
        @Positive(message = "Enrollment ID must be a positive number")
        Long enrollmentId,

        @NotNull(message = "Choose a subject")
        @Positive(message = "Subject ID must be a positive number")
        Long subjectId,

        @NotNull(message = "Attendance date is required")
        @PastOrPresent(message = "Attendance date cannot be in the future")
        LocalDate attendanceDate,

        @NotNull(message = "Choose an attendance status")
        AttendanceStatus status) {
}
