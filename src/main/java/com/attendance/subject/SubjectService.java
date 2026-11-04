package com.attendance.subject;

import java.util.List;

import com.attendance.schoolclass.SchoolClassRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;

    public SubjectService(SubjectRepository subjectRepository, SchoolClassRepository schoolClassRepository) {
        this.subjectRepository = subjectRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findAll() {
        return subjectRepository.findAll().stream()
                .map(subject -> new SubjectResponse(subject.getId(), subject.getName()))
                .toList();
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        String name = request.name().trim();
        if (subjectRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subject already exists");
        }

        Subject saved = subjectRepository.save(new Subject(name));
        return new SubjectResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public void delete(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        if (schoolClassRepository.existsBySubject_Id(id) || schoolClassRepository.existsBySubjects_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Delete the subject's classes before deleting the subject");
        }
        subjectRepository.delete(subject);
    }
}
