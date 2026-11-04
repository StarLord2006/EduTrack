package com.attendance.attendance;

import java.util.List;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.attendance.security.AccountRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

/** HTTP endpoints for recording and viewing attendance. */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public Page<AttendanceResponse> getAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return attendanceService.findAll(date, classId, subjectId, authentication.getName(), role(authentication), pageable);
    }

    @GetMapping("/summary")
    public AttendanceSummaryResponse getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId, Authentication authentication) {
        return attendanceService.getSummary(date, classId, subjectId, authentication.getName(), role(authentication));
    }

    @GetMapping("/available-students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public List<AvailableAttendanceStudent> getAvailableStudents(
            @RequestParam @NotNull Long classId,
            @RequestParam @NotNull Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        return attendanceService.findAvailableStudents(classId, subjectId, date,
                authentication.getName(), isAdmin(authentication));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse markAttendance(@Valid @RequestBody AttendanceRequest request, Authentication authentication) {
        return attendanceService.create(request, authentication.getName(), isAdmin(authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttendance(@PathVariable Long id, Authentication authentication) {
        attendanceService.delete(id, authentication.getName(), isAdmin(authentication));
    }

    private AccountRole role(Authentication authentication) {
        return AccountRole.valueOf(authentication.getAuthorities().stream().findFirst().orElseThrow()
                .getAuthority().replace("ROLE_", ""));
    }

    private boolean isAdmin(Authentication authentication) { return role(authentication) == AccountRole.ADMIN; }
}
