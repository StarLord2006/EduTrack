package com.attendance.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The small amount of data needed to create a subject. */
public record SubjectRequest(
        @NotBlank(message = "Subject name is required")
        @Size(max = 100, message = "Subject name must be 100 characters or fewer")
        String name) {
}
