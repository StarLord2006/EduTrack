package com.attendance.attendance;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsByEnrollment_Id(Long enrollmentId);

    boolean existsByEnrollment_IdAndSubject_IdAndAttendanceDate(Long enrollmentId, Long subjectId,
                                                                 LocalDate attendanceDate);

    @Query(value = """
            select attendance
            from Attendance attendance
            join attendance.enrollment enrollment
            join enrollment.schoolClass schoolClass
            join schoolClass.subjects subject
            join attendance.subject attendanceSubject
            where (:date is null or attendance.attendanceDate = :date)
              and subject.id = attendanceSubject.id
              and (:classId is null or schoolClass.id = :classId)
              and (:subjectId is null or attendanceSubject.id = :subjectId)
              and (:teacherAccountId is null or schoolClass.teacherAccountId = :teacherAccountId)
              and (:studentId is null or enrollment.student.id = :studentId)
            order by attendance.attendanceDate desc, attendance.id desc
            """, countQuery = """
            select count(distinct attendance.id)
            from Attendance attendance
            join attendance.enrollment enrollment
            join enrollment.schoolClass schoolClass
            join schoolClass.subjects subject
            join attendance.subject attendanceSubject
            where (:date is null or attendance.attendanceDate = :date)
              and subject.id = attendanceSubject.id
              and (:classId is null or schoolClass.id = :classId)
              and (:subjectId is null or attendanceSubject.id = :subjectId)
              and (:teacherAccountId is null or schoolClass.teacherAccountId = :teacherAccountId)
              and (:studentId is null or enrollment.student.id = :studentId)
            """)
    Page<Attendance> findFiltered(@Param("date") LocalDate date,
                                  @Param("classId") Long classId,
                                  @Param("subjectId") Long subjectId,
                                  @Param("teacherAccountId") Long teacherAccountId,
                                  @Param("studentId") Long studentId,
                                  Pageable pageable);

    @Query("""
            select new com.attendance.attendance.AttendanceCounts(
                   count(attendance),
                   count(case when attendance.status = com.attendance.attendance.AttendanceStatus.PRESENT then 1 end),
                   count(case when attendance.status = com.attendance.attendance.AttendanceStatus.ABSENT then 1 end),
                   count(case when attendance.status = com.attendance.attendance.AttendanceStatus.LATE then 1 end))
            from Attendance attendance
            join attendance.enrollment enrollment
            join enrollment.schoolClass schoolClass
            join schoolClass.subjects subject
            join attendance.subject attendanceSubject
            where (:date is null or attendance.attendanceDate = :date)
              and subject.id = attendanceSubject.id
              and (:classId is null or schoolClass.id = :classId)
              and (:subjectId is null or attendanceSubject.id = :subjectId)
              and (:teacherAccountId is null or schoolClass.teacherAccountId = :teacherAccountId)
              and (:studentId is null or enrollment.student.id = :studentId)
            """)
    AttendanceCounts summarizeFiltered(@Param("date") LocalDate date,
                                       @Param("classId") Long classId,
                                       @Param("subjectId") Long subjectId,
                                       @Param("teacherAccountId") Long teacherAccountId,
                                       @Param("studentId") Long studentId);
}
