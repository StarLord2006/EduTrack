package com.attendance.attendance;

/** The student and enrollment identifiers needed by the attendance form. */
public record AvailableAttendanceStudent(Long enrollmentId, String studentName) {
}
