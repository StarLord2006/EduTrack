package com.attendance.schoolclass;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Comparator;

import com.attendance.subject.Subject;
import com.attendance.subject.SubjectRepository;
import com.attendance.enrollment.EnrollmentRepository;
import com.attendance.security.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserAccountRepository userAccountRepository;

    public SchoolClassService(SchoolClassRepository schoolClassRepository, SubjectRepository subjectRepository,
                              EnrollmentRepository enrollmentRepository, UserAccountRepository userAccountRepository) {
        this.schoolClassRepository = schoolClassRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<SchoolClassResponse> findAll(String username, boolean admin) {
        List<SchoolClass> classes;
        if (admin) {
            classes = schoolClassRepository.findAll();
        } else {
            Long teacherId = userAccountRepository.findByUsername(username).orElseThrow().getId();
            classes = schoolClassRepository.findAllByTeacherAccountId(teacherId);
        }
        return classes.stream().map(this::toResponse).toList();
    }

    @Transactional
    public SchoolClassResponse create(SchoolClassRequest request) {
        LinkedHashSet<Subject> subjects = new LinkedHashSet<>();
        for (Long subjectId : request.subjectIds()) {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Subject not found: " + subjectId));
            subjects.add(subject);
        }
        Subject primarySubject = subjects.iterator().next();
        SchoolClass schoolClass = schoolClassRepository.save(
                new SchoolClass(request.name().trim(), primarySubject, subjects));
        return toResponse(schoolClass);
    }

    @Transactional
    public void delete(Long id) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        if (enrollmentRepository.existsBySchoolClass_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Remove this class's enrollments before deleting the class");
        }
        schoolClassRepository.delete(schoolClass);
    }

    private SchoolClassResponse toResponse(SchoolClass schoolClass) {
        List<Subject> subjects = schoolClass.getSubjects().stream()
                .sorted(Comparator.comparing(Subject::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new SchoolClassResponse(schoolClass.getId(), schoolClass.getName(),
                schoolClass.getSubject().getId(), schoolClass.getSubject().getName(),
                subjects.stream().map(Subject::getId).toList(),
                subjects.stream().map(Subject::getName).toList(),
                schoolClass.getTeacherAccountId());
    }
}
