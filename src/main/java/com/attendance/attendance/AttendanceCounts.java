package com.attendance.attendance;

/** Aggregate counts for the current attendance filters and access scope. */
public record AttendanceCounts(Long total, Long present, Long absent, Long late) { }
