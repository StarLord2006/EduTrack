package com.attendance.schoolclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** A class name and the subjects taught to that class. */
public record SchoolClassRequest(
        @NotBlank(message = "Class name is required")
        @Size(max = 100, message = "Class name must be 100 characters or fewer")
        String name,

        @NotEmpty(message = "Choose at least one subject")
        List<@NotNull(message = "Subject ID is required") @Positive(message = "Subject ID must be positive") Long> subjectIds) {
}
