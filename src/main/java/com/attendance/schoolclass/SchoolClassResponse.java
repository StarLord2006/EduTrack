package com.attendance.schoolclass;

import java.util.List;

public record SchoolClassResponse(Long id, String name, Long subjectId, String subjectName,
                                  List<Long> subjectIds, List<String> subjectNames,
                                  Long teacherAccountId) {
}
