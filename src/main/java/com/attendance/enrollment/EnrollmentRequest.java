package com.attendance.enrollment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** IDs select an existing student and an existing class. */
public record EnrollmentRequest(
        @NotNull(message = "Choose a student")
        @Positive(message = "Student ID must be a positive number")
        Long studentId,

        @NotNull(message = "Choose a class")
        @Positive(message = "Class ID must be a positive number")
        Long classId) {
}
