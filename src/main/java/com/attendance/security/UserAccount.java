package com.attendance.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A login account. Passwords are stored as BCrypt hashes, never as submitted text. */
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'STUDENT'")
    private AccountRole role = AccountRole.STUDENT;

    @Column(name = "student_id", unique = true)
    private Long studentId;

    protected UserAccount() { }

    public UserAccount(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public UserAccount(String username, String passwordHash, AccountRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public AccountRole getRole() { return role; }
    public Long getStudentId() { return studentId; }
    public void setRole(AccountRole role) { this.role = role; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
