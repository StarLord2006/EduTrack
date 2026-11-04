package com.attendance.enrollment;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudent_IdAndSchoolClass_Id(Long studentId, Long schoolClassId);
    boolean existsByStudent_Id(Long studentId);
    boolean existsBySchoolClass_Id(Long schoolClassId);

    @Query("""
            select enrollment from Enrollment enrollment
            join enrollment.schoolClass schoolClass
            join schoolClass.subjects subject
            where schoolClass.id = :classId and subject.id = :subjectId
              and not exists (
                  select attendance.id from Attendance attendance
                  where attendance.enrollment = enrollment
                    and attendance.subject.id = subject.id
                    and attendance.attendanceDate = :attendanceDate
              )
            order by enrollment.student.name
            """)
    List<Enrollment> findAvailableForAttendance(@Param("classId") Long classId,
                                                @Param("subjectId") Long subjectId,
                                                @Param("attendanceDate") LocalDate attendanceDate);
}
