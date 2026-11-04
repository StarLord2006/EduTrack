package com.attendance.enrollment;

import java.util.List;
import java.util.stream.Collectors;

import com.attendance.attendance.AttendanceRepository;
import com.attendance.schoolclass.SchoolClass;
import com.attendance.schoolclass.SchoolClassRepository;
import com.attendance.student.Student;
import com.attendance.student.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository schoolClassRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository,
                             SchoolClassRepository schoolClassRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findAll() {
        return enrollmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(request.classId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));

        if (enrollmentRepository.existsByStudent_IdAndSchoolClass_Id(student.getId(), schoolClass.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student is already enrolled in this class");
        }

        return toResponse(enrollmentRepository.save(new Enrollment(student, schoolClass)));
    }

    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        if (attendanceRepository.existsByEnrollment_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Remove this enrollment's attendance records before removing the enrollment");
        }
        enrollmentRepository.delete(enrollment);
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(enrollment.getId(), enrollment.getStudent().getId(),
                enrollment.getStudent().getName(), enrollment.getSchoolClass().getId(),
                enrollment.getSchoolClass().getName(), enrollment.getSchoolClass().getSubjects().stream()
                        .map(subject -> subject.getName()).sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.joining(", ")),
                enrollment.getEnrolledOn());
    }
}
