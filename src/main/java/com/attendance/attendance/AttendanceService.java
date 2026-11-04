package com.attendance.attendance;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.attendance.enrollment.Enrollment;
import com.attendance.enrollment.EnrollmentRepository;
import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import com.attendance.security.AccountRole;
import com.attendance.security.UserAccount;
import com.attendance.security.UserAccountRepository;
import com.attendance.schoolclass.SchoolClassRepository;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Looks up enrollment records and applies the attendance rules. */
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubjectRepository subjectRepository;
    private final UserAccountRepository userAccountRepository;
    private final SchoolClassRepository schoolClassRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EnrollmentRepository enrollmentRepository,
                             SubjectRepository subjectRepository,
                             UserAccountRepository userAccountRepository,
                             SchoolClassRepository schoolClassRepository) {
        this.attendanceRepository = attendanceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.subjectRepository = subjectRepository;
        this.userAccountRepository = userAccountRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> findAll(LocalDate date, Long classId, Long subjectId,
                                            String username, AccountRole role, Pageable pageable) {
        Scope scope = scopeFor(username, role);
        if (scope.studentId() == null && role == AccountRole.STUDENT) return Page.empty(pageable);
        return attendanceRepository.findFiltered(date, classId, subjectId,
                scope.teacherAccountId(), scope.studentId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getSummary(LocalDate date, Long classId, Long subjectId,
                                                String username, AccountRole role) {
        Scope scope = scopeFor(username, role);
        AttendanceCounts counts = role == AccountRole.STUDENT && scope.studentId() == null
                ? new AttendanceCounts(0L, 0L, 0L, 0L)
                : attendanceRepository.summarizeFiltered(date, classId, subjectId,
                        scope.teacherAccountId(), scope.studentId());
        long total = counts.total() == null ? 0 : counts.total();
        long present = counts.present() == null ? 0 : counts.present();
        long absent = counts.absent() == null ? 0 : counts.absent();
        long late = counts.late() == null ? 0 : counts.late();
        long attended = present + late;
        BigDecimal rate = total == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(attended).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return new AttendanceSummaryResponse(total, present, absent, late, attended, rate);
    }

    @Transactional(readOnly = true)
    public List<AvailableAttendanceStudent> findAvailableStudents(Long classId, Long subjectId, LocalDate date,
                                                                  String username, boolean admin) {
        if (!admin) {
            Long teacherId = userAccountRepository.findByUsername(username).orElseThrow().getId();
            if (!schoolClassRepository.existsByIdAndTeacherAccountId(classId, teacherId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your assigned classes.");
            }
        }
        return enrollmentRepository.findAvailableForAttendance(classId, subjectId, date).stream()
                .map(enrollment -> new AvailableAttendanceStudent(enrollment.getId(),
                        enrollment.getStudent().getName()))
                .toList();
    }

    @Transactional
    public AttendanceResponse create(AttendanceRequest request, String username, boolean admin) {
        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        if (!admin) ensureOwnClass(enrollment.getSchoolClass().getId(), username);

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        boolean subjectBelongsToClass = enrollment.getSchoolClass().getSubjects().stream()
                .anyMatch(assignedSubject -> assignedSubject.getId().equals(subject.getId()));
        if (!subjectBelongsToClass) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This subject is not assigned to the selected class");
        }

        if (attendanceRepository.existsByEnrollment_IdAndSubject_IdAndAttendanceDate(
                enrollment.getId(), subject.getId(), request.attendanceDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Attendance has already been recorded for this student, subject and date");
        }

        Attendance attendance = new Attendance(enrollment, subject, request.attendanceDate(), request.status());
        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public void delete(Long id, String username, boolean admin) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));
        if (!admin) ensureOwnClass(attendance.getEnrollment().getSchoolClass().getId(), username);
        attendanceRepository.delete(attendance);
    }

    private Scope scopeFor(String username, AccountRole role) {
        if (role == AccountRole.ADMIN) return new Scope(null, null);
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return role == AccountRole.TEACHER
                ? new Scope(account.getId(), null)
                : new Scope(null, account.getStudentId());
    }

    private void ensureOwnClass(Long classId, String username) {
        Long accountId = userAccountRepository.findByUsername(username).orElseThrow().getId();
        if (!schoolClassRepository.existsByIdAndTeacherAccountId(classId, accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage attendance for your assigned classes.");
        }
    }

    private record Scope(Long teacherAccountId, Long studentId) { }

    private AttendanceResponse toResponse(Attendance attendance) {
        Enrollment enrollment = attendance.getEnrollment();
        var schoolClass = enrollment.getSchoolClass();
        var subject = attendance.getSubject();
        return new AttendanceResponse(attendance.getId(), enrollment.getId(), enrollment.getStudent().getName(),
                schoolClass.getId(), schoolClass.getName(), subject.getId(), subject.getName(),
                attendance.getAttendanceDate(), attendance.getStatus());
    }
}
