package com.attendance.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Fields a client may provide when creating or updating a student. */
public record StudentRequest(
        @NotBlank(message = "Name is required")
        @Pattern(
                regexp = "^[\\p{L}\\p{M}]+(?:[ .’'-][\\p{L}\\p{M}]+)*$",
                message = "Name can contain letters, spaces, apostrophes, hyphens and periods, but no numbers")
        @Size(max = 100, message = "Name must be 100 characters or fewer")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
                message = "Email must not contain spaces and must look like name@example.com")
        @Size(max = 150, message = "Email must be 150 characters or fewer")
        String email) {
}
