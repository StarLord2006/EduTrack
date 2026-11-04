package com.attendance.enrollment;

import java.time.LocalDate;

public record EnrollmentResponse(Long id, Long studentId, String studentName,
                                 Long classId, String className, String subjectName,
                                 LocalDate enrolledOn) {
}
