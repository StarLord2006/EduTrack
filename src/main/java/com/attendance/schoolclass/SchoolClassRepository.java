package com.attendance.schoolclass;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    java.util.List<SchoolClass> findAllByTeacherAccountId(Long teacherAccountId);
    boolean existsByIdAndTeacherAccountId(Long id, Long teacherAccountId);
    boolean existsByTeacherAccountId(Long teacherAccountId);
    boolean existsBySubject_Id(Long subjectId);
    boolean existsBySubjects_Id(Long subjectId);
}
