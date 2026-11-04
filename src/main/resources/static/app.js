const statusTitle = document.querySelector("#status-title");
const statusMessage = document.querySelector("#status-message");
const statusDot = document.querySelector("#status-dot");
const studentNameField = document.querySelector("#student-name");
const studentEmailField = document.querySelector("#student-email");
const attendanceDateField = document.querySelector("#attendance-date");
const attendanceFilterDateField = document.querySelector("#attendance-filter-date");
const attendanceClassField = document.querySelector("#attendance-class");
const attendanceSubjectField = document.querySelector("#attendance-subject");
const attendanceEnrollmentField = document.querySelector("#attendance-enrollment");
const attendanceClassFilter = document.querySelector("#attendance-filter-class");
const attendanceSubjectFilter = document.querySelector("#attendance-filter-subject");
let loadedClasses = [];
let loadedSubjects = [];
let loadedAccounts = [];
let availabilityRequestNumber = 0;
let studentPageNumber = 0;
let attendancePageNumber = 0;
const pageSize = 20;
let signedIn = false;
let currentRole = null;
let accessToken = sessionStorage.getItem("attendance-access-token");

function showSignedIn(username, role) {
    signedIn = true;
    currentRole = role;
    document.querySelector("#login-panel").hidden = true;
    document.querySelector("#signed-in-bar").hidden = false;
    document.querySelector("#signed-in-name").textContent = `${username} (${role})`;
    const nameParts = username.trim().split(/[\s._-]+/).filter(Boolean);
    const initials = nameParts.length > 1
        ? nameParts.slice(0, 2).map(part => part[0]).join("")
        : username.trim().slice(0, 2);
    document.querySelector(".profile-avatar").textContent = initials.toUpperCase() || "U";
    document.querySelector(".page-nav").hidden = false;
    const defaultPanel = role === "ADMIN" ? "setup-panel" : "attendance-panel";
    document.querySelectorAll(".nav-button").forEach(button => {
        button.hidden = role !== "ADMIN" && button.dataset.panel !== "attendance-panel";
        const active = button.dataset.panel === defaultPanel;
        button.classList.toggle("active", active);
        if (active) button.setAttribute("aria-current", "page");
        else button.removeAttribute("aria-current");
    });
    document.querySelector(".page-header h1").textContent =
        document.querySelector(`.nav-button[data-panel="${defaultPanel}"]`).textContent.trim();
    document.querySelectorAll(".view-panel").forEach(panel => {
        panel.hidden = role === "ADMIN" ? panel.id !== "setup-panel" : panel.id !== "attendance-panel";
    });
    document.querySelector("#attendance-form").hidden = role === "STUDENT";
    document.querySelector("#attendance-filter-class").hidden = role === "STUDENT";
    document.querySelector("#attendance-filter-subject").hidden = role === "STUDENT";
    document.querySelectorAll("label[for='attendance-filter-class'], label[for='attendance-filter-subject']")
        .forEach(label => label.hidden = role === "STUDENT");
}

function showSignedOut() {
    signedIn = false;
    currentRole = null;
    document.querySelector("#login-panel").hidden = false;
    document.querySelector("#signed-in-bar").hidden = true;
    document.querySelector(".page-nav").hidden = true;
    document.querySelector(".page-header h1").textContent = "Attendance management";
    document.querySelectorAll(".view-panel").forEach(panel => panel.hidden = true);
    document.querySelectorAll(".nav-button").forEach(button => button.hidden = false);
    document.querySelector("#attendance-form").hidden = false;
    document.querySelector("#attendance-filter-class").hidden = false;
    document.querySelector("#attendance-filter-subject").hidden = false;
    document.querySelectorAll("label[for='attendance-filter-class'], label[for='attendance-filter-subject']")
        .forEach(label => label.hidden = false);
}

function updateStudentLinkVisibility() {
    const isStudent = document.querySelector("#account-role").value === "STUDENT";
    document.querySelector("#student-link-field").hidden = !isStudent;
    document.querySelector("#linked-student").required = isStudent;
}

function populateAccessManagement(accounts, students, classes) {
    loadedAccounts = accounts;
    setOptions(document.querySelector("#role-account"), accounts,
        account => account.id, account => `${account.username} · ${account.role}`, "Choose an account");
    setOptions(document.querySelector("#linked-student"), students,
        student => student.id, student => `${student.name} · ${student.email}`, "Choose a student record", true);
    setOptions(document.querySelector("#teacher-class"), classes,
        schoolClass => schoolClass.id, schoolClass => schoolClass.name, "Choose a class");

    const teacherAccounts = accounts.filter(account => account.role === "TEACHER");
    const teacherSelect = document.querySelector("#class-teacher");
    setOptions(teacherSelect, teacherAccounts,
        account => account.id, account => account.username, "Unassigned", true);
    const unassignedOption = teacherSelect.options[0];
    unassignedOption.value = "none";
    unassignedOption.textContent = "Unassigned";
    unassignedOption.disabled = false;
    teacherSelect.value = "none";

    const accountList = document.querySelector("#access-account-list");
    accountList.replaceChildren();
    if (accounts.length === 0) addTextItem(accountList, "No accounts yet");
    accounts.forEach(account => {
        const linkedStudent = account.studentId == null
            ? "no student linked"
            : students.find(student => student.id === account.studentId)?.name || `student #${account.studentId}`;
        const assignedClasses = classes.filter(item => item.teacherAccountId === account.id).map(item => item.name);
        const details = [account.role, linkedStudent];
        if (assignedClasses.length) details.push(`classes: ${assignedClasses.join(", ")}`);
        addTextItem(accountList, `${account.username} · ${details.join(" · ")}`);
    });

    updateStudentLinkVisibility();
    syncTeacherClassSelection();
}

function syncTeacherClassSelection() {
    const classId = Number(document.querySelector("#teacher-class").value);
    const schoolClass = loadedClasses.find(item => item.id === classId);
    document.querySelector("#class-teacher").value = schoolClass?.teacherAccountId == null
        ? "none"
        : String(schoolClass.teacherAccountId);
}

const studentNamePattern = /^[\p{L}\p{M}]+(?:[ .’'-][\p{L}\p{M}]+)*$/u;
const studentEmailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateStudentFields() {
    const name = studentNameField.value;
    const email = studentEmailField.value;
    studentNameField.setCustomValidity(name === "" || studentNamePattern.test(name)
        ? ""
        : "Use letters, spaces, apostrophes, hyphens or periods in a name; do not use numbers.");
    studentEmailField.setCustomValidity(email === "" || studentEmailPattern.test(email)
        ? ""
        : "Email cannot contain spaces and should look like name@example.com.");
}

studentNameField.addEventListener("input", validateStudentFields);
studentEmailField.addEventListener("input", validateStudentFields);

function localDateString() {
    const now = new Date();
    const localNow = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);
    return localNow.toISOString().slice(0, 10);
}

const today = localDateString();
attendanceDateField.value = today;
attendanceDateField.defaultValue = today;
attendanceDateField.max = today;
attendanceFilterDateField.max = today;

async function requestJson(url, options = {}) {
    const headers = new Headers(options.headers || {});
    if (accessToken && !["/api/auth/login", "/api/auth/register"].includes(url.split("?")[0])) {
        headers.set("Authorization", `Bearer ${accessToken}`);
    }
    let response;
    try {
        response = await fetch(url, { ...options, headers });
    } catch (error) {
        throw new Error(`Cannot reach ${url}. Is Spring Boot running? ${error.message}`);
    }
    if (response.status === 401 && url !== "/api/auth/login" && url !== "/api/auth/register") {
        accessToken = null;
        sessionStorage.removeItem("attendance-access-token");
        showSignedOut();
        document.querySelector("#auth-message").textContent = "Please log in to continue.";
    }
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        const validationMessage = body.fieldErrors
            ? Object.entries(body.fieldErrors).map(([field, message]) => `${field}: ${message}`).join(" ")
            : "";
        const message = validationMessage || body.message || body.detail || response.statusText || "Request failed";
        throw new Error(`${response.status} from ${url}: ${message}`);
    }
    if (response.status === 204) return null;
    return response.json();
}

function setOptions(select, rows, getValue, getLabel, placeholder, allowPlaceholder = false) {
    const previousValue = select.value;
    select.replaceChildren();
    const firstOption = document.createElement("option");
    firstOption.value = "";
    firstOption.textContent = placeholder;
    firstOption.disabled = !allowPlaceholder;
    firstOption.selected = true;
    select.append(firstOption);

    for (const row of rows) {
        const option = document.createElement("option");
        option.value = getValue(row);
        option.textContent = getLabel(row);
        select.append(option);
    }
    if (previousValue && [...select.options].some(option => option.value === previousValue)) {
        select.value = previousValue;
    }
}

function populateClassSubjectChoices(rows) {
    const container = document.querySelector("#class-subject-options");
    container.replaceChildren();
    for (const row of rows) {
        const label = document.createElement("label");
        label.className = "checkbox-option";
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.name = "subjectIds";
        checkbox.value = row.id;
        const name = document.createElement("span");
        name.textContent = row.name;
        label.append(checkbox, name);
        container.append(label);
    }
    if (rows.length === 0) container.textContent = "Add a subject first.";
}

function updateAttendanceFormChoices() {
    const classId = attendanceClassField.value;
    const schoolClass = loadedClasses.find(item => String(item.id) === classId);
    const classSubjects = schoolClass
        ? loadedSubjects.filter(subject => schoolClass.subjectIds.includes(subject.id))
        : [];
    setOptions(attendanceSubjectField, classSubjects,
        subject => subject.id, subject => subject.name,
        schoolClass ? "Choose a subject" : "Choose a class first");
}

function updateAttendanceSubjectFilter() {
    const classId = attendanceClassFilter.value;
    const subjectIds = new Set(loadedClasses
        .filter(item => !classId || String(item.id) === classId)
        .flatMap(item => item.subjectIds));
    const subjects = loadedSubjects.filter(subject => subjectIds.has(subject.id));
    setOptions(attendanceSubjectFilter, subjects, item => item.id, item => item.name,
        "All subjects", true);
}

async function updateAvailableStudents() {
    const requestNumber = ++availabilityRequestNumber;
    const classId = attendanceClassField.value;
    const subjectId = attendanceSubjectField.value;
    const date = attendanceDateField.value;
    if (!classId || !subjectId || !date) {
        setOptions(attendanceEnrollmentField, [], item => item.id, item => item.studentName,
            "Choose class, subject and date first");
        return;
    }

    setOptions(attendanceEnrollmentField, [], item => item.id, item => item.studentName,
        "Loading students…");
    try {
        const params = new URLSearchParams({ classId, subjectId, date });
        const available = await requestJson(`/api/attendance/available-students?${params}`);
        if (requestNumber !== availabilityRequestNumber) return;
        setOptions(attendanceEnrollmentField, available,
            enrollment => enrollment.enrollmentId, enrollment => enrollment.studentName,
            available.length ? "Choose a student" : "All enrolled students are marked");
    } catch (error) {
        if (requestNumber === availabilityRequestNumber) {
            setOptions(attendanceEnrollmentField, [], item => item.id, item => item.studentName,
                "Could not load students");
            document.querySelector("#attendance-message").textContent = error.message;
        }
    }
}

attendanceClassField.addEventListener("change", () => {
    updateAttendanceFormChoices();
    updateAvailableStudents();
});
attendanceSubjectField.addEventListener("change", updateAvailableStudents);
attendanceDateField.addEventListener("change", updateAvailableStudents);
attendanceClassFilter.addEventListener("change", updateAttendanceSubjectFilter);

function addTextItem(list, text, actions = []) {
    const item = document.createElement("li");
    const details = document.createElement("span");
    details.textContent = text;
    item.append(details);

    if (actions.length > 0) {
        const actionGroup = document.createElement("span");
        actionGroup.className = "item-actions";
        for (const action of actions) actionGroup.append(action);
        item.append(actionGroup);
    }
    list.append(item);
}

function makeButton(label, onClick, className = "secondary-button") {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.className = className;
    button.addEventListener("click", onClick);
    return button;
}

async function refreshData() {
    try {
        const selectedDate = attendanceFilterDateField.value;
        const attendanceParams = new URLSearchParams({ page: String(attendancePageNumber), size: String(pageSize) });
        if (selectedDate) attendanceParams.set("date", selectedDate);
        if (attendanceClassFilter.value) attendanceParams.set("classId", attendanceClassFilter.value);
        if (attendanceSubjectFilter.value) attendanceParams.set("subjectId", attendanceSubjectFilter.value);
        const query = attendanceParams.toString();
        const attendanceUrl = `/api/attendance?${query}`;
        const summaryUrl = query ? `/api/attendance/summary?${query}` : "/api/attendance/summary";
        const isAdmin = currentRole === "ADMIN";
        const isTeacher = currentRole === "TEACHER";
        const studentParams = new URLSearchParams({ page: String(studentPageNumber), size: String(pageSize) });
        const [studentsPage, subjects, classes, enrollments, attendancePage, summary, accounts] = await Promise.all([
            isAdmin ? requestJson(`/api/students?${studentParams}`) : Promise.resolve({ content: [], totalPages: 0, totalElements: 0, number: 0 }),
            isAdmin || isTeacher ? requestJson("/api/subjects") : Promise.resolve([]),
            isAdmin || isTeacher ? requestJson("/api/classes") : Promise.resolve([]),
            isAdmin ? requestJson("/api/enrollments") : Promise.resolve([]),
            requestJson(attendanceUrl),
            requestJson(summaryUrl),
            isAdmin ? requestJson("/api/admin/accounts") : Promise.resolve([])
        ]);
        const students = studentsPage.content;

        statusTitle.textContent = "Backend and MySQL are connected";
        statusMessage.textContent = "Your saved records are loaded below.";
        statusDot.classList.add("online");
        statusDot.classList.remove("offline");

        setOptions(document.querySelector("#enrollment-student"), students,
            student => student.id, student => `${student.name} (${student.email})`, "Choose a student");
        setOptions(document.querySelector("#enrollment-class"), classes,
            schoolClass => schoolClass.id,
            schoolClass => `${schoolClass.name} · ${schoolClass.subjectNames.join(", ")}`,
            "Choose a class");
        loadedClasses = classes;
        loadedSubjects = subjects;
        if (isAdmin) populateAccessManagement(accounts, students, classes);
        populateClassSubjectChoices(subjects);
        setOptions(attendanceClassField, classes, item => item.id,
            item => item.name, "Choose a class");
        updateAttendanceFormChoices();
        setOptions(attendanceClassFilter, classes, item => item.id,
            item => item.name, "All classes", true);
        updateAttendanceSubjectFilter();
        if (currentRole !== "STUDENT") void updateAvailableStudents();

        const subjectList = document.querySelector("#subject-list");
        subjectList.replaceChildren();
        if (subjects.length === 0) addTextItem(subjectList, "No subjects yet");
        subjects.forEach(subject => {
            const deleteButton = makeButton("Delete", () => deleteSubject(subject.id));
            addTextItem(subjectList, subject.name, [deleteButton]);
        });

        const classList = document.querySelector("#class-list");
        classList.replaceChildren();
        if (classes.length === 0) addTextItem(classList, "No classes yet");
        classes.forEach(schoolClass => {
            const deleteButton = makeButton("Delete", () => deleteClass(schoolClass.id));
            addTextItem(classList, `${schoolClass.name} · ${schoolClass.subjectNames.join(", ")}`, [deleteButton]);
        });

        const studentList = document.querySelector("#student-list");
        studentList.replaceChildren();
        if (students.length === 0) addTextItem(studentList, "No students yet");
        students.forEach(student => {
            const editButton = makeButton("Edit", () => editStudent(student));
            const deleteButton = makeButton("Delete", () => deleteStudent(student.id));
            addTextItem(studentList, `${student.name} · ${student.email}`, [editButton, deleteButton]);
        });
        renderPagination("student-pagination", studentsPage, page => {
            studentPageNumber = page;
            refreshData();
        }, "students");

        const enrollmentList = document.querySelector("#enrollment-list");
        enrollmentList.replaceChildren();
        if (enrollments.length === 0) addTextItem(enrollmentList, "No enrollments yet");
        enrollments.forEach(enrollment => {
            const deleteButton = makeButton("Remove", () => deleteEnrollment(enrollment.id));
            addTextItem(enrollmentList,
                `${enrollment.studentName} → ${enrollment.className} · ${enrollment.subjectName} · ${enrollment.enrolledOn}`,
                [deleteButton]);
        });

        renderAttendance(attendancePage.content);
        renderPagination("attendance-pagination", attendancePage, page => {
            attendancePageNumber = page;
            refreshData();
        }, "attendance records");
        renderAttendanceSummary(summary);
    } catch (error) {
        console.error("Could not load project data:", error);
        statusTitle.textContent = "Could not load the project data";
        statusMessage.textContent = error.message;
        statusDot.classList.add("offline");
        statusDot.classList.remove("online");
    }
}

function renderPagination(containerId, page, onPageChange, label) {
    const container = document.querySelector(`#${containerId}`);
    container.replaceChildren();
    const totalPages = page.totalPages;
    const currentPage = page.number;
    const range = document.createElement("span");
    const start = page.totalElements === 0 ? 0 : currentPage * page.size + 1;
    const end = Math.min((currentPage + 1) * page.size, page.totalElements);
    range.textContent = `${start}–${end} of ${page.totalElements} ${label}`;
    const previous = makeButton("Previous", () => onPageChange(currentPage - 1), "secondary-button");
    previous.disabled = currentPage <= 0;
    const next = makeButton("Next", () => onPageChange(currentPage + 1), "secondary-button");
    next.disabled = totalPages === 0 || currentPage >= totalPages - 1;
    const pageLabel = document.createElement("span");
    pageLabel.textContent = `Page ${totalPages === 0 ? 0 : currentPage + 1} of ${totalPages}`;
    container.append(range, previous, pageLabel, next);
}

function renderAttendanceSummary(summary) {
    document.querySelector("#summary-total").textContent = summary.totalRecords;
    document.querySelector("#summary-present").textContent = summary.presentCount;
    document.querySelector("#summary-late").textContent = summary.lateCount;
    document.querySelector("#summary-absent").textContent = summary.absentCount;
    document.querySelector("#summary-rate").textContent = `${Number(summary.attendanceRate).toFixed(2)}%`;
}

function renderAttendance(records) {
    const tableBody = document.querySelector("#attendance-table-body");
    const emptyMessage = document.querySelector("#attendance-empty");
    tableBody.replaceChildren();
    emptyMessage.hidden = records.length > 0;

    const groups = new Map();
    for (const record of records) {
        const key = `${record.classId}:${record.subjectId}`;
        if (!groups.has(key)) groups.set(key, []);
        groups.get(key).push(record);
    }

    for (const groupRecords of groups.values()) {
        const groupHeader = document.createElement("tr");
        groupHeader.className = "attendance-group-row";
        const groupTitle = document.createElement("th");
        groupTitle.colSpan = 6;
        groupTitle.scope = "rowgroup";
        groupTitle.textContent = `${groupRecords[0].className} · ${groupRecords[0].subjectName}`;
        groupHeader.append(groupTitle);
        tableBody.append(groupHeader);

        for (const record of groupRecords) {
        const row = document.createElement("tr");
        for (const value of [record.attendanceDate, record.studentName, record.className, record.subjectName]) {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.append(cell);
        }

        const statusCell = document.createElement("td");
        const badge = document.createElement("span");
        badge.className = `status-badge ${record.status.toLowerCase()}`;
        badge.textContent = record.status[0] + record.status.slice(1).toLowerCase();
        statusCell.append(badge);
        row.append(statusCell);

        const actionCell = document.createElement("td");
        if (currentRole !== "STUDENT") {
            const deleteButton = makeButton("Remove", () => deleteAttendance(record.id));
            actionCell.append(deleteButton);
        }
        row.append(actionCell);
        tableBody.append(row);
        }
    }
}

async function submitForm(event, url, makeBody, messageSelector, successMessage) {
    event.preventDefault();
    const form = event.currentTarget;
    const message = document.querySelector(messageSelector);
    const values = new FormData(form);
    const body = makeBody(values);

    try {
        await requestJson(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        if (form.id !== "attendance-form") form.reset();
        if (form.id === "attendance-form") attendancePageNumber = 0;
        if (form.id === "student-form") studentPageNumber = 0;
        message.textContent = successMessage;
        await refreshData();
    } catch (error) {
        message.textContent = error.message;
    }
}

document.querySelector("#subject-form").addEventListener("submit", event =>
    submitForm(event, "/api/subjects", values => ({ name: values.get("name") }),
        "#subject-message", "Subject added."));

document.querySelector("#class-form").addEventListener("submit", event => {
    const values = new FormData(event.currentTarget);
    if (values.getAll("subjectIds").length === 0) {
        event.preventDefault();
        document.querySelector("#class-message").textContent = "Choose at least one subject for the class.";
        return;
    }
    submitForm(event, "/api/classes", formValues => ({
        name: formValues.get("name"),
        subjectIds: formValues.getAll("subjectIds").map(Number)
    }), "#class-message", "Class added.");
});

document.querySelector("#student-form").addEventListener("submit", event =>
    submitForm(event, "/api/students", values => ({
        name: values.get("name"),
        email: values.get("email")
    }), "#student-message", "Student added."));

document.querySelector("#enrollment-form").addEventListener("submit", event =>
    submitForm(event, "/api/enrollments", values => ({
        studentId: Number(values.get("studentId")),
        classId: Number(values.get("classId"))
    }), "#enrollment-message", "Student enrolled in the class."));

document.querySelector("#attendance-form").addEventListener("submit", event =>
    submitForm(event, "/api/attendance", values => ({
        enrollmentId: Number(values.get("enrollmentId")),
        subjectId: Number(values.get("subjectId")),
        attendanceDate: values.get("attendanceDate"),
        status: values.get("status")
    }), "#attendance-message", "Attendance saved."));

document.querySelector("#attendance-filter-form").addEventListener("submit", event => {
    event.preventDefault();
    attendancePageNumber = 0;
    refreshData();
});

document.querySelector("#auth-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = new FormData(form);
    try {
        const result = await requestJson("/api/auth/login", {
            method: "POST", headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: values.get("username"), password: values.get("password") })
        });
        accessToken = result.accessToken;
        sessionStorage.setItem("attendance-access-token", accessToken);
        form.reset();
        document.querySelector("#auth-message").textContent = result.message;
        showSignedIn(result.username, result.role);
        await refreshData();
    } catch (error) {
        document.querySelector("#auth-message").textContent = error.message;
    }
});

document.querySelector("#register-button").addEventListener("click", async () => {
    const form = document.querySelector("#auth-form");
    const values = new FormData(form);
    try {
        const result = await requestJson("/api/auth/register", {
            method: "POST", headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: values.get("username"), password: values.get("password") })
        });
        document.querySelector("#auth-message").textContent = result.message;
    } catch (error) {
        document.querySelector("#auth-message").textContent = error.message;
    }
});

document.querySelector("#access-create-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const values = new FormData(form);
    try {
        const created = await requestJson("/api/auth/register", {
            method: "POST", headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: values.get("username"), password: values.get("password") })
        });
        document.querySelector("#access-create-message").textContent = `${created.username} created as STUDENT. Set a role below.`;
        form.reset();
        await refreshData();
    } catch (error) {
        document.querySelector("#access-create-message").textContent = error.message;
    }
});

document.querySelector("#role-account").addEventListener("change", () => {
    const account = loadedAccounts.find(item => String(item.id) === document.querySelector("#role-account").value);
    if (account) {
        document.querySelector("#account-role").value = account.role;
        document.querySelector("#linked-student").value = account.studentId == null ? "" : String(account.studentId);
    }
    updateStudentLinkVisibility();
});
document.querySelector("#account-role").addEventListener("change", updateStudentLinkVisibility);

document.querySelector("#role-form").addEventListener("submit", async event => {
    event.preventDefault();
    const accountId = document.querySelector("#role-account").value;
    const role = document.querySelector("#account-role").value;
    const studentValue = document.querySelector("#linked-student").value;
    try {
        const updated = await requestJson(`/api/admin/accounts/${accountId}`, {
            method: "PATCH", headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ role, studentId: role === "STUDENT" && studentValue ? Number(studentValue) : null })
        });
        document.querySelector("#role-message").textContent = `${updated.username} is now ${updated.role}. Log in again for the new role to take effect.`;
        await refreshData();
    } catch (error) {
        document.querySelector("#role-message").textContent = error.message;
    }
});

document.querySelector("#teacher-class").addEventListener("change", syncTeacherClassSelection);
document.querySelector("#teacher-assignment-form").addEventListener("submit", async event => {
    event.preventDefault();
    const classId = document.querySelector("#teacher-class").value;
    const selectedTeacher = document.querySelector("#class-teacher").value;
    try {
        await requestJson(`/api/admin/classes/${classId}/teacher`, {
            method: "PATCH", headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ teacherAccountId: selectedTeacher === "none" ? null : Number(selectedTeacher) })
        });
        document.querySelector("#teacher-assignment-message").textContent = "Class teacher assignment saved.";
        await refreshData();
    } catch (error) {
        document.querySelector("#teacher-assignment-message").textContent = error.message;
    }
});

document.querySelector("#logout-button").addEventListener("click", () => {
    accessToken = null;
    sessionStorage.removeItem("attendance-access-token");
    showSignedOut();
    statusTitle.textContent = "You are logged out";
    statusMessage.textContent = "Log in again to manage attendance.";
});

document.querySelector("#clear-attendance-filter").addEventListener("click", () => {
    attendanceFilterDateField.value = "";
    attendanceClassFilter.value = "";
    updateAttendanceSubjectFilter();
    attendanceSubjectFilter.value = "";
    attendancePageNumber = 0;
    refreshData();
});

document.querySelectorAll(".nav-button").forEach(button => {
    button.addEventListener("click", () => {
        const selectedPanel = button.dataset.panel;
        document.querySelectorAll(".nav-button").forEach(navButton => {
            const active = navButton === button;
            navButton.classList.toggle("active", active);
            if (active) navButton.setAttribute("aria-current", "page");
            else navButton.removeAttribute("aria-current");
        });
        document.querySelectorAll(".view-panel").forEach(panel => {
            panel.hidden = panel.id !== selectedPanel;
        });
        const pageTitle = document.querySelector(".page-header h1");
        pageTitle.textContent = button.textContent.trim();
    });
});

async function editStudent(student) {
    const name = window.prompt("Student name", student.name);
    if (name === null) return;
    const email = window.prompt("Student email", student.email);
    if (email === null) return;

    try {
        await requestJson(`/api/students/${student.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email })
        });
        document.querySelector("#student-message").textContent = "Student updated.";
        await refreshData();
    } catch (error) {
        document.querySelector("#student-message").textContent = error.message;
    }
}

async function deleteStudent(id) {
    if (!window.confirm("Delete this student?")) return;
    await deleteRecord(`/api/students/${id}`, "#student-message", "Student deleted.");
}

async function deleteEnrollment(id) {
    if (!window.confirm("Remove this student from the class?")) return;
    await deleteRecord(`/api/enrollments/${id}`, "#enrollment-message", "Enrollment removed.");
}

async function deleteAttendance(id) {
    if (!window.confirm("Remove this attendance record?")) return;
    await deleteRecord(`/api/attendance/${id}`, "#attendance-message", "Attendance record removed.");
}

async function deleteClass(id) {
    if (!window.confirm("Delete this class? Remove its enrollments first if needed.")) return;
    await deleteRecord(`/api/classes/${id}`, "#class-message", "Class deleted.");
}

async function deleteSubject(id) {
    if (!window.confirm("Delete this subject? Delete its classes first if needed.")) return;
    await deleteRecord(`/api/subjects/${id}`, "#subject-message", "Subject deleted.");
}

async function deleteRecord(url, messageSelector, successMessage) {
    try {
        await requestJson(url, { method: "DELETE" });
        document.querySelector(messageSelector).textContent = successMessage;
        await refreshData();
    } catch (error) {
        document.querySelector(messageSelector).textContent = error.message;
    }
}

showSignedOut();
if (accessToken) {
    requestJson("/api/auth/me").then(result => {
        showSignedIn(result.username, result.role);
        return refreshData();
    }).catch(() => {
        statusTitle.textContent = "Your login has expired";
        statusMessage.textContent = "Log in again to load attendance data.";
    });
} else {
    statusTitle.textContent = "Log in to load attendance data";
    statusMessage.textContent = "Create an account or enter your username and password.";
}
