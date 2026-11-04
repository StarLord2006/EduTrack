package com.attendance.schoolclass;

import com.attendance.subject.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.LinkedHashSet;
import java.util.Set;

/** One class offering, for example "Grade 10 - Mathematics". */
@Entity
@Table(name = "classes")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "teacher_account_id")
    private Long teacherAccountId;

    // Many classes may be associated with the same subject.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "class_subjects",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<Subject> subjects = new LinkedHashSet<>();

    protected SchoolClass() {
    }

    public SchoolClass(String name, Subject subject) {
        this.name = name;
        this.subject = subject;
        this.subjects.add(subject);
    }

    public SchoolClass(String name, Subject primarySubject, Set<Subject> subjects) {
        this.name = name;
        this.subject = primarySubject;
        this.subjects.addAll(subjects);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Subject getSubject() {
        return subject;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public Long getTeacherAccountId() { return teacherAccountId; }

    public void setTeacherAccountId(Long teacherAccountId) { this.teacherAccountId = teacherAccountId; }
}
