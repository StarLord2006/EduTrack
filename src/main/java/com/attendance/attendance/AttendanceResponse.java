package com.attendance.attendance;

import java.time.LocalDate;

public record AttendanceResponse(Long id, Long enrollmentId, String studentName,
                                 Long classId, String className, Long subjectId, String subjectName,
                                 LocalDate attendanceDate, AttendanceStatus status) {
}
