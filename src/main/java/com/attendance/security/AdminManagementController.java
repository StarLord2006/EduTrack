package com.attendance.security;

import java.util.List;

import com.attendance.schoolclass.SchoolClass;
import com.attendance.schoolclass.SchoolClassRepository;
import com.attendance.student.StudentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminManagementController {

    private final UserAccountRepository accounts;
    private final StudentRepository students;
    private final SchoolClassRepository classes;

    public AdminManagementController(UserAccountRepository accounts, StudentRepository students,
                                     SchoolClassRepository classes) {
        this.accounts = accounts;
        this.students = students;
        this.classes = classes;
    }

    @GetMapping("/accounts")
    public List<AccountResponse> getAccounts() {
        return accounts.findAll().stream()
                .map(account -> new AccountResponse(account.getId(), account.getUsername(),
                        account.getRole(), account.getStudentId()))
                .toList();
    }

    @PatchMapping("/accounts/{id}")
    public AccountResponse updateAccount(@PathVariable Long id, @Valid @RequestBody AccountUpdate request) {
        UserAccount account = accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getRole() == AccountRole.TEACHER && request.role() != AccountRole.TEACHER &&
                classes.existsByTeacherAccountId(account.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Unassign this teacher from their classes before changing their role.");
        }
        if (request.role() == AccountRole.STUDENT) {
            if (request.studentId() != null && !students.existsById(request.studentId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The selected student record does not exist.");
            }
            if (request.studentId() != null && accounts.existsByStudentId(request.studentId()) &&
                    !request.studentId().equals(account.getStudentId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "That student record is already linked to another account.");
            }
            account.setStudentId(request.studentId());
        } else {
            if (request.studentId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only STUDENT accounts can be linked to a student record.");
            }
            account.setStudentId(null);
        }
        account.setRole(request.role());
        accounts.save(account);
        return new AccountResponse(account.getId(), account.getUsername(), account.getRole(), account.getStudentId());
    }

    @PatchMapping("/classes/{classId}/teacher")
    public ClassTeacherResponse assignTeacher(@PathVariable Long classId,
                                             @Valid @RequestBody TeacherAssignment request) {
        SchoolClass schoolClass = classes.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        if (request.teacherAccountId() == null) {
            schoolClass.setTeacherAccountId(null);
        } else {
            UserAccount teacher = accounts.findById(request.teacherAccountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher account not found"));
            if (teacher.getRole() != AccountRole.TEACHER) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected account does not have the TEACHER role.");
            }
            schoolClass.setTeacherAccountId(teacher.getId());
        }
        classes.save(schoolClass);
        return new ClassTeacherResponse(classId, schoolClass.getTeacherAccountId());
    }

    public record AccountUpdate(@NotNull AccountRole role, @Positive Long studentId) { }
    public record TeacherAssignment(@Positive Long teacherAccountId) { }
    public record AccountResponse(Long id, String username, AccountRole role, Long studentId) { }
    public record ClassTeacherResponse(Long classId, Long teacherAccountId) { }
}
