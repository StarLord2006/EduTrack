package com.attendance.student;

import java.util.Locale;

import com.attendance.enrollment.EnrollmentRepository;
import com.attendance.security.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Keeps student database operations out of the HTTP controller. */
@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserAccountRepository userAccountRepository;

    public StudentService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
                          UserAccountRepository userAccountRepository) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public Page<StudentResponse> findAll(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::toResponse);
    }

    public StudentResponse findById(Long id) {
        return toResponse(findStudent(id));
    }

    public StudentResponse create(StudentRequest request) {
        String email = normalizeEmail(request.email());
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A student with this email already exists");
        }
        // Build a new record so a client cannot choose the database ID.
        Student student = new Student(request.name().trim(), email);
        return toResponse(studentRepository.save(student));
    }

    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudent(id);
        String email = normalizeEmail(request.email());
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A student with this email already exists");
        }
        student.setName(request.name().trim());
        student.setEmail(email);
        return toResponse(studentRepository.save(student));
    }

    public void delete(Long id) {
        Student student = findStudent(id);
        if (enrollmentRepository.existsByStudent_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Remove this student's enrollments before deleting the student");
        }
        if (userAccountRepository.existsByStudentId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Unlink this student's login account before deleting the student");
        }
        studentRepository.delete(student);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
