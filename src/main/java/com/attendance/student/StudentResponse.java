package com.attendance.student;

/** The student fields the API returns to a client. */
public record StudentResponse(Long id, String name, String email) {
}
