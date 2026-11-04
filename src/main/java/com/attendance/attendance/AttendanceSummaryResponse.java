package com.attendance.attendance;

import java.math.BigDecimal;

/** Counts and the attended percentage for the selected attendance filters. */
public record AttendanceSummaryResponse(long totalRecords, long presentCount, long absentCount,
                                        long lateCount, long attendedCount, BigDecimal attendanceRate) {
}
