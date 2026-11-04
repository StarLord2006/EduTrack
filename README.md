<<<<<<< HEAD
# Attendance Management System — Days 1–10

A small Spring Boot project for learning Java, MySQL, JPA relationships and REST CRUD. The webpage is plain HTML, CSS and JavaScript.

## What is working so far

- **Day 1 — Spring Boot + database:** Spring Boot connects to MySQL on port `3307`.
- **Day 2 — JPA relationships:** a class can have several subjects; an enrollment connects a student to a class. JPA creates `students`, `subjects`, `classes`, `class_subjects` and `enrollments` tables and their foreign keys.
- **Day 3 — REST + CRUD:** create, read, update and delete student records; add/list subjects and classes; enroll a student into a class.
- **Day 4 — DTOs + validation:** API requests use small request records; responses use response records. Invalid names, emails and related-record IDs are rejected before service/database work.
- **Day 5 — Attendance:** record `PRESENT`, `ABSENT` or `LATE` for a student's class enrollment and date; choose a class before choosing its enrolled student; filter records by date, class or subject.
- **Day 6 — Business logic:** calculate total, present, late and absent marks plus the attendance rate for the selected filters. `PRESENT` and `LATE` count as attended.
- **Day 7 — Exception handling:** return one understandable JSON error format for validation, duplicate, missing-record, malformed-request and unexpected server errors.
- **Day 8 — Spring Security:** register/login with a BCrypt-hashed password; attendance APIs require authentication, while login and registration stay public.
- **Day 9 — JWT:** login issues a signed, short-lived access token; protected requests authenticate through the `Authorization: Bearer <token>` header.
- **Day 10 — Role authorization:** `ADMIN`, `TEACHER` and `STUDENT` have different permissions; teachers are assigned classes and students are linked to their student record.

## 1. Prepare MySQL

Make sure your MySQL service is running and configured for port `3307`. In MySQL Workbench, connect with port `3307` and run:

```sql
CREATE DATABASE attendance_db;
```

The MySQL user in the next step needs permission to use this database and create tables.

## 2. Set your database password

Open PowerShell in this project folder. Set the username and password for your MySQL account in that terminal; the password is not saved in the project:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-MySQL-password"
```

Replace the example password with the one you use to sign in to MySQL. Keep this PowerShell window open for the next step.

For the first administrator, also set these before starting the app:

```powershell
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "choose-a-strong-password"
```

If that username already has an account, startup promotes it to `ADMIN` and keeps its existing password. For a new account, both values are required. Leave the variables unset on later starts if you do not want startup to promote an account.

## 3. Start the project

You need Java 21 and Maven. Run:

```powershell
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). Use the page in order: add subjects, add a class and choose its subjects, add a student, enroll the student in the class, then mark attendance for a selected class, subject and date. The first Maven run downloads the project dependencies.

## API endpoints

The browser calls these endpoints. You can also use Postman or PowerShell.

| Method | URL | What it does |
| --- | --- | --- |
| POST | `/api/auth/register` | Create an account; password is saved as a BCrypt hash |
| POST | `/api/auth/login` | Verify credentials and return a signed JWT access token |
| GET | `/api/auth/me` | Return the current signed-in username and role (send the bearer token) |
| GET | `/api/admin/accounts` | Admin: list accounts, roles and student links |
| PATCH | `/api/admin/accounts/1` | Admin: set an account role and, for STUDENT, its student ID |
| PATCH | `/api/admin/classes/1/teacher` | Admin: assign a TEACHER account to a class |
| GET | `/api/students?page=0&size=20` | Return a page of students (default 20 per page; maximum 100) |
| GET | `/api/students/1` | Read student with ID 1 |
| POST | `/api/students` | Create a student |
| PUT | `/api/students/1` | Replace a student's name and email |
| DELETE | `/api/students/1` | Delete a student |
| GET | `/api/subjects` | List subjects |
| POST | `/api/subjects` | Add a subject |
| DELETE | `/api/subjects/1` | Delete a subject with no classes |
| GET | `/api/classes` | List classes with their subjects |
| POST | `/api/classes` | Add a class with one or more existing subjects |
| DELETE | `/api/classes/1` | Delete a class with no enrollments |
| GET | `/api/enrollments` | List enrollments with student and class names |
| POST | `/api/enrollments` | Enroll a student in an existing class |
| DELETE | `/api/enrollments/1` | Remove an enrollment |
| GET | `/api/attendance` | List attendance, newest dates first |
| GET | `/api/attendance?page=0&size=20` | Return a page of attendance records (default 20 per page; maximum 100) |
| GET | `/api/attendance?date=2026-10-02&page=0&size=20` | Page records for one date |
| GET | `/api/attendance?classId=1&page=0&size=20` | Page records for one class |
| GET | `/api/attendance?subjectId=1&page=0&size=20` | Page records for one subject |
| GET | `/api/attendance?date=2026-10-02&classId=1&subjectId=1&page=0&size=20` | Combine attendance filters and pagination |
| GET | `/api/attendance/summary?classId=1&subjectId=1` | Calculate filtered attendance counts and rate |
| GET | `/api/attendance/available-students?classId=1&subjectId=1&date=2026-10-02` | List enrolled students not yet marked for this subject and date |
| POST | `/api/attendance` | Mark attendance for an enrollment and date |
| DELETE | `/api/attendance/1` | Remove an attendance record |

Example subject request:

```json
{
  "name": "Mathematics"
}
```

Example class request (use subject IDs returned by `GET /api/subjects`):

```json
{
  "name": "Grade 10 A",
  "subjectIds": [1, 2]
}
```

Example student request:

```json
{
  "name": "Asha Sharma",
  "email": "asha@example.com"
}
```

Example enrollment request (use IDs returned by the student and class lists):

```json
{
  "studentId": 1,
  "classId": 1
}
```

Example attendance request (use the enrollment ID shown by the enrollment API/page):

```json
{
  "enrollmentId": 1,
  "subjectId": 1,
  "attendanceDate": "2026-10-02",
  "status": "PRESENT"
}
```

Allowed statuses are `PRESENT`, `ABSENT` and `LATE`. A student needs an enrollment before attendance can be marked. Each class can have several subjects. Attendance is unique for one enrollment, subject and date. The student selector only offers students with an enrollment in the selected class who do not already have a mark for that subject and date. Remove attendance records before removing an enrollment; remove enrollments before deleting their class; remove classes before deleting their subjects. A student must be unenrolled from all classes before that student can be deleted.

## Follow the layers

1. The browser calls an endpoint such as `/api/classes`.
2. Its controller handles the HTTP request.
3. The service finds related records and applies the operation.
4. A Spring Data repository uses JPA to read or write rows in MySQL.

`@Entity` makes a Java class a database table. `@ManyToMany` on `SchoolClass.subjects` allows each class to have several subjects and each subject to be taught in several classes. The `class_subjects` join table stores those pairs. The two `@ManyToOne` fields in `Enrollment` link an enrollment to its student and class. `@JoinColumn` names a foreign-key column.

## Day 5: Attendance

### What was added and why

- `AttendanceStatus` — an enum with `PRESENT`, `ABSENT` and `LATE`. An enum prevents spelling variations and only allows these three values.
- `Attendance` — the JPA entity that becomes the MySQL `attendance` table. It stores the enrollment, attendance date and status.
- Attendance links to `Enrollment`, rather than accepting separate student and class IDs. That way, the server can only mark someone who is actually enrolled in that class.
- A unique database constraint on `(enrollment_id, subject_id, attendance_date)` — protects the rule that one student can have one result per subject per day, even if two requests arrive together.
- `AttendanceRequest` — the create-request shape. Its validation requires an enrollment ID, subject ID, date and status; the service verifies the subject belongs to that class, and future dates are rejected.
- `AttendanceResponse` — returns attendance together with student, class and subject IDs/names, so the webpage can group/filter records without making extra requests.
- `AttendanceRepository` — saves attendance, checks for duplicates, and filters records by date, class and/or subject, newest-first.
- `AttendanceService` — looks up the enrollment and subject, checks that the subject is assigned to the class, prevents duplicate marks, saves the entity and maps it to a response.
- `AttendanceController` — provides attendance list filters, an available-students endpoint, `POST` and `DELETE` under `/api/attendance`.
- `AttendanceSummaryResponse` and the summary service method — calculate counts and an attendance rate for the same date/class/subject filters. Rate formula: `(present + late) / total marks * 100`; an empty result has a rate of `0.00%`.
- Attendance section in the webpage — asks for a class, then offers that class's subjects. After choosing a date and subject, the student list includes enrolled students who do not yet have an attendance mark for that combination. The records table groups attendance under class/subject headings and can filter by date, class and subject. The date defaults to the user's local today; the backend independently rejects future dates.
- `DatabaseMigration` — copies each old class's single subject into `class_subjects` and links existing attendance rows to that subject. It keeps old class/attendance data available while the relationship grows to support multiple subjects.
- Attendance deletion — lets you correct/remove a mistaken mark and is required before its enrollment can be deleted. Foreign-key checks keep the cleanup order safe.

## Day 6: Attendance calculations

The Attendance page shows total marks, present, late, absent and an attendance rate. The summary API uses the same optional date, class and subject filters as the attendance list. `PRESENT` and `LATE` count as attended; `ABSENT` does not. The backend calculates the percentage and rounds it to two decimal places, so the browser only displays the result.

## Day 7: Friendly API errors

`ApiExceptionHandler` catches common API errors in one place. Responses share `timestamp`, `status`, `error`, `message`, `path` and `fieldErrors` fields. Validation errors include a map of field names to messages; conflicts and missing records keep their appropriate HTTP status; unexpected server errors return a safe message without database internals. The browser reads `message` and shows field-level messages when available.

## Day 8: Spring Security

`spring-boot-starter-security` protects the existing API endpoints. The public `/api/auth/register` endpoint creates an account in `user_accounts`; BCrypt encodes the password before it is stored in `password_hash`. Passwords must be at least 8 characters. `/api/auth/login` checks credentials with Spring Security. Day 9 changes API authentication to JWT; role-based endpoint rules are planned for Day 10.

For the checkpoint, open the page, create a username and password, log in, and add or view a record. Try `/api/students` without logging in; it should return `401 Unauthorized`. Log in and the request should succeed. In MySQL Workbench, `user_accounts.password_hash` should contain a BCrypt hash, never the submitted password.

## Day 9: JSON Web Tokens (JWT)

`spring-boot-starter-oauth2-resource-server` provides Spring Security's bearer-token filter and JWT verification support. `/api/auth/login` authenticates the username/password and returns a signed HS256 JWT with the username as its subject, an issue time, issuer and a 30-minute expiry. Protected API requests must send `Authorization: Bearer <token>`. Missing, malformed, expired or incorrectly signed tokens are rejected with `401 Unauthorized`. The security filter chain is stateless: it does not create a server session. `/api/auth/me` shows the username established from the token. The browser keeps the access token in `sessionStorage`, adds it to protected requests, checks it on page reload, and clears it on logout or expiry. There is no refresh token or server-side token revocation in this learning step; log in again after expiry.

Set `JWT_SECRET` to a private Base64-encoded secret that decodes to at least 32 bytes before using the app outside local learning. `application.properties` includes a clearly marked development fallback so the project runs locally. Keep production secrets out of source control. To check the flow, log in and copy the `accessToken` from the JSON response; send it as a Bearer token to `/api/students`. A missing token and a deliberately altered token should both be rejected.

## Day 10: Role-based authorization

Accounts now store an `ADMIN`, `TEACHER` or `STUDENT` role. Public registration can only create `STUDENT` accounts. Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` before the first startup to create the initial administrator; if that username already exists, startup promotes that account without changing its password. Existing Day 8–9 accounts are migrated to `STUDENT` unless selected as the configured administrator. Log in again after role changes so the new role is included in the JWT.

Only `ADMIN` can create/update/delete students, subjects, classes and enrollments. `ADMIN` and `TEACHER` can read subjects and mark or remove attendance. A teacher can only list assigned classes and view/mark/remove attendance for those classes. `STUDENT` can only view attendance and summary rows for the linked student record. Unauthenticated requests still return `401`; authenticated users without the required role receive `403`.

Admins can do account setup in the webpage's **Access & roles** tab:

1. Create a login with a username and temporary password. New accounts start as `STUDENT`.
2. Select an account and role. For `STUDENT`, link its student record; each student record can be linked to one account.
3. Select a class and teacher to assign them. Choose **Unassigned** to remove a class assignment.

The matching admin API endpoints remain available for Postman: `GET /api/admin/accounts`, `PATCH /api/admin/accounts/{id}` and `PATCH /api/admin/classes/{classId}/teacher`.

The webpage adapts by role: admins see setup and people management, teachers see attendance for their assigned classes, and students see their own attendance records and summary. Check that a teacher cannot access another teacher's class and that a student sees no other student's attendance. Role updates take effect after a fresh login; tokens last 30 minutes.

## Day 11: Verify security and role access

Day 11 is a verification and hardening checkpoint for Days 8–10. It does not add a new product feature. The checks below exercise registration, password hashing, JWT validation, administrator setup, role permissions, and the browser login flow. Record each result in the checkpoint list after running it against the local MySQL database.

### Changes reviewed for this checkpoint

- `SecurityConfiguration` disables server sessions, exposes only the page assets and registration/login without a token, and validates the JWT signature, issuer, expiry, and single allowed role claim.
- `AuthController` creates public registrations as `STUDENT`, encodes passwords with BCrypt, and issues a 30-minute bearer token after successful login.
- `AdminAccountBootstrap` creates or promotes the configured administrator at startup. Promoting an existing account changes its role and preserves its password.
- `AdminManagementController` restricts account and class-teacher setup to `ADMIN`, prevents linking one student record to multiple accounts, and only assigns accounts with the `TEACHER` role.
- Attendance and class services apply teacher class assignments and student record links on the server. Browser tab visibility is only a usability aid; API authorization and data filtering are enforced by the backend.
- The browser stores the access token in `sessionStorage`, sends it on protected requests, checks it on reload, and clears it on logout or when an API request returns `401`.

### How to verify

1. Start MySQL on port `3307`, create `attendance_db`, and start the application as described above. For the first admin, set `ADMIN_USERNAME` and an 8–72 character `ADMIN_PASSWORD` in the same PowerShell window before starting the app. Then run `mvn package`. A successful build and application startup are prerequisites for the API checks.
2. Open `http://localhost:8080`. Register a new user, then inspect MySQL: `user_accounts.password_hash` should contain a BCrypt hash, not the submitted password. Registering the same username again should return `409 Conflict`.
3. Call `GET http://localhost:8080/api/students` without logging in. Expect `401 Unauthorized`. Log in through the page or `POST /api/auth/login` with `{"username":"...","password":"..."}`; expect an `accessToken`, `tokenType` of `Bearer`, `expiresIn` of `1800`, and the account role.
4. Send the token as `Authorization: Bearer <accessToken>` to `GET /api/auth/me` and `/api/students`; expect success. Repeat without the header and with one character changed in the token; expect `401` for both. A request with a valid student token to an admin-only endpoint such as `GET /api/admin/accounts` should return `403`.
5. Log in as the configured admin. Create a teacher account through **Access & roles** (new accounts start as `STUDENT`), change its role to `TEACHER`, and assign it to a class. Create or choose a student account, link it to a student record, then log in again to refresh its role claim.
6. With the teacher token, call `GET /api/classes` and confirm only assigned classes appear. Try viewing, marking, and deleting attendance for an assigned class, then try the same operations for a class assigned to a different teacher; the unrelated class must be denied (or omitted from the class list).
7. With the linked student's token, call `GET /api/attendance` and `GET /api/attendance/summary`. Confirm rows and counts cover only that student. An unlinked student should get an empty attendance list and a zero summary. Student requests to mark or delete attendance must return `403`.
8. In the browser, refresh after login and confirm the session remains signed in. Log out and confirm protected API calls return `401`. To check expiry handling without waiting 30 minutes, use an expired token in an API request; expect `401` and confirm the browser returns to the login panel.

Expected behavior is summarized here:

| Check | Expected result |
| --- | --- |
| Public registration | Account is created with `STUDENT` role; password stored as BCrypt hash |
| Missing, altered, or expired access token | `401 Unauthorized` |
| Authenticated request without required role | `403 Forbidden` |
| Teacher reading classes or attendance | Limited to classes assigned to that teacher |
| Student reading attendance or summary | Limited to the linked student record; unlinked account sees no records |
| Student creating or deleting attendance | `403 Forbidden` |
| Admin setup endpoints | Accessible to `ADMIN`; denied to `TEACHER` and `STUDENT` |

Do not mark the Day 11 checkpoint complete until the build and the live API/browser checks above have actually passed. If a check fails, capture the endpoint, role, request, and actual status/body, then fix and rerun that check.

## Day 12: Paginated lists and attendance dashboard

Student and attendance list endpoints now accept Spring pagination parameters: `page` is zero-based and `size` defaults to `20`, capped at `100`. Their JSON response contains `content` for the current page and metadata such as `totalElements`, `totalPages`, `number`, and `size`. For example, request `GET /api/attendance?classId=1&page=1&size=10` to retrieve the second page of a class's marks. Attendance date, class, subject, teacher, and student scopes are applied by the database query before paging.

The webpage displays **Previous** and **Next** controls for students and attendance records. Changing an attendance filter returns to its first page. The student page selector also controls the student options shown in the enrollment and account-link forms, so an admin can move to the page containing the required student. The attendance totals and rate are calculated by a database aggregate over the full filtered, role-scoped result set; they do not change just because the user moves between pages. `Present + Late` counts as attended.

### Day 12 verification

1. Log in as ADMIN and request `/api/students?page=0&size=2`, then `/api/students?page=1&size=2`. Confirm each response has at most two items in `content`, page `number` changes from `0` to `1`, and the response reports correct `totalElements` and `totalPages`.
2. Request `/api/attendance?page=0&size=2` and `/api/attendance?page=1&size=2`. Confirm there are at most two attendance rows per page, newest records come first, and the `totalElements` count covers all matching records.
3. Add `date`, `classId`, or `subjectId` to the attendance URL while paging. Confirm only matching rows are returned and `totalElements` reflects the filtered result. Confirm an empty page/filter has an empty `content` and a valid zero summary.
4. In the webpage, add enough records to exceed 20, then use Previous/Next for the student and attendance lists. Check that the displayed ranges and page numbers update, and that Previous/Next disable at the first/last page.
5. Apply and clear attendance filters while on a later page. Confirm the list returns to page 1 and the filter controls match the displayed rows.
6. Compare dashboard total, present, late, absent, and rate values on page 1 and page 2. They should remain the same for the same filters, and should match the counts across all matching records. Changing filters should update the dashboard.
7. Repeat the attendance checks as a teacher with assigned classes and as a linked student. Confirm each page and its summary remain limited to that role's data, regardless of requested page/size. Try a size over 100 and confirm the server caps the response at 100.

## Navigation

The page is separated into role-specific tabs: admins see **Setup**, **Students & enrollment**, **Access & roles**, and **Attendance**; teachers see attendance for their assigned classes; students see their own attendance and summary.

### Follow one attendance record

1. The form loads classes and subjects. After selecting a class, the subject list is limited to subjects assigned to it. The student list is loaded for the selected class, subject and date, excluding students who already have a mark. The form sends `enrollmentId`, `subjectId`, `attendanceDate` and `status` as JSON.
2. `AttendanceController` validates the request.
3. `AttendanceService` finds the enrollment and subject, verifies the class-subject relationship, and checks for a duplicate for that subject and date.
4. `AttendanceRepository` saves the `Attendance` entity. MySQL enforces the same unique-per-subject-per-day rule.
5. The response includes the student/class names; JavaScript refreshes the table.

Expected API results: successful create returns `201`; a duplicate mark returns `409`; a future date or missing field returns `400`; a nonexistent enrollment returns `404`.

## Your Day 1–12 checkpoint

- [ ] Start MySQL on port 3307 and create `attendance_db`.
- [ ] Start Spring Boot and confirm the browser reports a database connection.
- [ ] Add a student, refresh the page, edit it, then delete it.
- [ ] Add a subject and class, then enroll a student in that class.
- [ ] Mark the student present for today and confirm the row appears in Attendance.
- [ ] Try marking the same enrollment, subject and date again and confirm it is rejected as a duplicate.
- [ ] Filter attendance by date, class and subject, then choose Clear filters.
- [ ] Confirm the summary counts and rate change when filters change. Rate = `(present + late) / total marks * 100`.
- [ ] Submit an invalid student request and confirm the API returns a field-specific error message.
- [ ] Remove an attendance record before trying to remove its enrollment.
- [ ] In MySQL Workbench, run `SELECT * FROM students;`.
- [ ] Check the `subjects`, `classes` and `enrollments` tables in MySQL Workbench.
- [ ] Explain the path from controller to service to repository.
- [ ] Find `subject_id`, `student_id` and `class_id` in the generated tables.
- [ ] Send a student request with a blank name and confirm the API returns `400 Bad Request`.
- [ ] Send a student request with an invalid email and confirm it is rejected.
- [ ] Explain why `StudentRequest` and `StudentResponse` are separate from the `Student` database entity.
- [ ] Create an account in the webpage and confirm `user_accounts.password_hash` contains a BCrypt hash.
- [ ] Log in and verify the protected student API works; log out and confirm it returns `401 Unauthorized`.
- [ ] Log in and confirm the response contains an access token and `Bearer` token type.
- [ ] Call `/api/students` with `Authorization: Bearer <token>`; confirm missing or altered tokens return `401 Unauthorized`.
- [ ] Configure `JWT_SECRET` with a private Base64-encoded key before using this outside local learning.
- [ ] Start once with `ADMIN_USERNAME` and `ADMIN_PASSWORD`, then confirm the account has the `ADMIN` role.
- [ ] Use admin endpoints to assign a teacher to a class and link a student account to its student record.
- [ ] Confirm ADMIN, TEACHER and STUDENT permissions differ, and teacher/student data stays scoped to their assignments.
- [ ] Build successfully with `mvn package` after dependencies are available.
- [ ] Verify the missing, altered and expired JWT cases return `401 Unauthorized`.
- [ ] Verify authenticated requests without the required role return `403 Forbidden`.
- [ ] Verify the browser session survives refresh and logout clears the token.
- [ ] Record the live Day 11 API and browser verification results in the Day 11 section.
- [ ] Confirm student and attendance APIs return page metadata and no more than the requested page size.
- [ ] Confirm attendance filters, teacher/student scopes, and dashboard aggregates remain correct across pages.
- [ ] Confirm browser pagination controls, page ranges, and filter/page reset behavior work.

## Day 4: DTOs and validation

### What was added and why

- `spring-boot-starter-validation` in `pom.xml` — brings in Jakarta Bean Validation so Spring can check request fields.
- `StudentRequest` — describes the fields clients are allowed to send for student create/update. It deliberately has no database ID field.
- `StudentResponse` — describes the student fields returned to clients. The API no longer sends the JPA entity directly.
- The existing `SubjectRequest`, `SchoolClassRequest` and `EnrollmentRequest` records now have validation rules too. They describe each request's expected fields and reject invalid or missing relationship IDs.
- `@Valid` on controller request bodies — tells Spring to run the request's validation rules before calling the controller method.
- `@NotBlank` — rejects missing, empty, or whitespace-only names and emails.
- `@Pattern` on student name — rejects digits and leading/trailing punctuation while allowing Unicode letters, spaces, apostrophes, hyphens and periods.
- `@Pattern` on email — explicitly rejects whitespace anywhere in the address. The webpage applies the same rule before submitting; the backend remains the final check.
- `@Email` — checks that an email has a basic valid email format.
- `@Size(max = ...)` — limits text to fit the database column and avoid unexpectedly long input.
- `@NotNull` and `@Positive` — require relationship IDs and reject zero or negative IDs. The service still checks that a positive ID actually exists in MySQL.
- Matching `@Column(length = ...)` settings — keep the database text columns in line with the API's length limits.
- Student service mapping — turns database entities into `StudentResponse` records, so internal JPA objects stay behind the service layer.
- Duplicate email check — returns `409 Conflict` with a useful message instead of exposing a database uniqueness error.
- Delete controls and relationship checks — classes can be deleted only after their enrollments are removed; subjects can be deleted only after their classes are removed. This keeps foreign-key relationships valid.

Invalid input returns HTTP `400 Bad Request`; valid input continues through the service and repository. For example, a student name is required and `email` must be in a valid format. Day 7 formats these validation messages into the common API error response.

Existing database rows are not rewritten automatically by validation. If an older student row already has a number in the name or spaces in the email, it remains until you correct that row; these rules reject invalid create/update requests from now on.

### Other current limitations to keep in mind

- Subjects and classes can be added, listed and deleted, but do not yet have edit (`PUT`) endpoints or edit buttons.
- Validation failures use the Day 7 common error response and include field messages.
- Attendance can be marked, viewed, filtered and removed. Editing an existing mark is not in the Day 5 scope yet.

### Follow the student request

1. The JSON body is converted to `StudentRequest`.
2. `@Valid` checks its annotations. If a rule fails, Spring returns `400` and stops the request.
3. If valid, `StudentController` passes the request to `StudentService`.
4. The service saves or updates the `Student` entity and maps it to `StudentResponse`.
5. Spring converts that response record to JSON for the browser.
=======
# EduTrack
This is an Attendance Management System made using core concepts of Spring
>>>>>>> e7fe252943432654d6d267dd7d5a4caa0a63abad
