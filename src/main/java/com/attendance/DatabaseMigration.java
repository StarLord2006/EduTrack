package com.attendance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Copies the original one-subject class data into the new class-subject relationship. */
@Component
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        jdbcTemplate.update("UPDATE user_accounts SET role = 'STUDENT' WHERE role IS NULL OR role = ''");
        jdbcTemplate.update("""
                INSERT IGNORE INTO class_subjects (class_id, subject_id)
                SELECT id, subject_id FROM classes WHERE subject_id IS NOT NULL
                """);
        jdbcTemplate.update("""
                UPDATE attendance a
                JOIN enrollments e ON e.id = a.enrollment_id
                JOIN classes c ON c.id = e.class_id
                SET a.subject_id = c.subject_id
                WHERE a.subject_id IS NULL OR a.subject_id = 0
                """);

        Integer oldIndexCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = 'attendance'
                  AND index_name = 'uk_attendance_enrollment_date'
                """, Integer.class);
        if (oldIndexCount != null && oldIndexCount > 0) {
            jdbcTemplate.execute("ALTER TABLE attendance DROP INDEX uk_attendance_enrollment_date");
        }

        Integer subjectForeignKeyCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.key_column_usage
                WHERE table_schema = DATABASE()
                  AND table_name = 'attendance'
                  AND column_name = 'subject_id'
                  AND referenced_table_name = 'subjects'
                """, Integer.class);
        if (subjectForeignKeyCount != null && subjectForeignKeyCount == 0) {
            jdbcTemplate.execute("""
                    ALTER TABLE attendance
                    ADD CONSTRAINT fk_attendance_subject
                    FOREIGN KEY (subject_id) REFERENCES subjects(id)
                    """);
        }
    }
}
