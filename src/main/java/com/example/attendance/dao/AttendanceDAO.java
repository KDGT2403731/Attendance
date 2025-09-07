package com.example.attendance.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.example.attendance.dto.Attendance;

public class AttendanceDAO {
	private static final List<Attendance> attendanceRecords = new CopyOnWriteArrayList<>();
	
	public void checkIn(String userId) {
		Attendance attendance = new Attendance(userId);
		attendance.setCheckInTime(LocalDateTime.now());
		attendanceRecords.add(attendance);
	}
	
	public void checkOut(String userId) {
		attendanceRecords.stream()
			.filter(att -> userId.equals(att.getUserId()) && att.getCheckOutTime() == null)
			.findFirst()
			.ifPresent(att -> att.setCheckOutTime(LocalDateTime.now()));
	}
	
	public List<Attendance> findByUserId(String userId) {
		return attendanceRecords.stream()
				.filter(att -> userId.equals(att.getUserId()))
				.collect(Collectors.toList());
	}
	
	public List<Attendance> findAll() {
		return new ArrayList<>(attendanceRecords);
	}
	
	public List<Attendance> findFilteredRecords(String userId, LocalDate startDate, LocalDate endDate) {
		return attendanceRecords.stream()
				.filter(att -> (userId == null || userId.isEmpty() || att.getUserId().equals(userId)))
				.filter(att -> (startDate == null || (att.getCheckInTime() != null
				&& !att.getCheckInTime().toLocalDate().isBefore(startDate))))
				.filter(att -> (endDate == null || (att.getCheckInTime() != null
				&& !att.getCheckInTime().toLocalDate().isAfter(endDate))))
				.collect(Collectors.toList());
	}
	
	public Map<YearMonth, Long> getMonthlyWorkingHours(String userId) {
		return attendanceRecords.stream()
				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
				.filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
				.collect(Collectors.groupingBy(
						att -> YearMonth.from(att.getCheckInTime()),
						Collectors.summingLong(att -> ChronoUnit.HOURS.between(att.getCheckInTime(), 
									att.getCheckOutTime()))
				));
	}
	
	public Map<YearMonth, Long> getMonthlyCheckInCounts(String userId) {
		return attendanceRecords.stream()
				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
				.filter(att -> att.getCheckInTime() != null)
				.collect(Collectors.groupingBy(
						att -> YearMonth.from(att.getCheckInTime()),
						Collectors.counting()
				));		
	}
	
	public Map<YearMonth, Long> getMonthlyOvertimeHours(String userId) {
		return attendanceRecords.stream()
				.filter(att -> userId == null || userId.isEmpty() || att.getUserId().equals(userId))
				.filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
				.collect(Collectors.groupingBy(
						att -> YearMonth.from(att.getCheckInTime()),
						Collectors.summingLong(att -> {
							LocalDateTime checkIn = att.getCheckInTime();
							LocalDateTime checkOut = att.getCheckOutTime();
							LocalDateTime workStart = checkIn.withHour(9).withMinute(0).withSecond(0).withNano(0);
							LocalDateTime workEnd = checkOut.withHour(17).withMinute(0).withSecond(0).withNano(0);
							long regularHours = 0;
							if (checkIn.isBefore(workEnd) && checkOut.isAfter(workStart)) {
								LocalDateTime start = checkIn.isAfter(workStart) ? checkIn : workStart;
								LocalDateTime end = checkOut.isBefore(workEnd) ? checkOut : workEnd;
								regularHours = ChronoUnit.HOURS.between(start, end);
							}
							long totalHours = ChronoUnit.HOURS.between(checkIn, checkOut);
							return Math.max(0, totalHours - regularHours);
						})
					));
	}
	
	public void addManualAttendance(String userId, LocalDateTime checkIn, LocalDateTime checkOut) {
		Attendance newRecord = new Attendance(userId);
		newRecord.setCheckInTime(checkIn);
		newRecord.setCheckOutTime(checkOut);
		attendanceRecords.add(newRecord);
	}
	
	public boolean updateManualAttendance(String userId, LocalDateTime oldCheckIn, LocalDateTime oldCheckOut,
			LocalDateTime newCheckIn, LocalDateTime newCheckOut) {
		for (int i = 0; i < attendanceRecords.size(); i++) {
			Attendance att = attendanceRecords.get(i);
			boolean isCheckInEqual = att.getCheckInTime().truncatedTo(ChronoUnit.SECONDS)
					.isEqual(oldCheckIn.truncatedTo(ChronoUnit.SECONDS));
			boolean isCheckOutEqual = (att.getCheckOutTime() == null ? oldCheckOut == null : att.getCheckOutTime()
					.truncatedTo(ChronoUnit.SECONDS).isEqual(oldCheckOut.truncatedTo(ChronoUnit.SECONDS)));
			if (att.getUserId().equals(userId) &&
				isCheckInEqual &&
				isCheckOutEqual) {
				att.setCheckInTime(newCheckIn);
				att.setCheckOutTime(newCheckOut);
				return true;
			}
		}
		return false;
	}
	
	public boolean deleteManualAttendance(String userId, LocalDateTime checkIn, LocalDateTime checkOut) {
		return attendanceRecords.removeIf(att -> 
			att.getUserId().equals(userId) &&
			att.getCheckInTime() != null && checkIn != null &&
			att.getCheckInTime().truncatedTo(ChronoUnit.SECONDS).isEqual(checkIn.truncatedTo(ChronoUnit.SECONDS)) &&
			(att.getCheckOutTime() == null ? checkOut == null :
			(att.getCheckOutTime() != null ? checkOut != null : 
			att.getCheckOutTime().truncatedTo(ChronoUnit.SECONDS).isEqual(checkOut.truncatedTo(ChronoUnit.SECONDS))))
		);
	}

	public Map<String, Long> getTotalHoursByUser(String userId, LocalDate startDate, LocalDate endDate) {
	    return attendanceRecords.stream()
	    		.filter(att -> (userId == null || userId.isEmpty() || att.getUserId().equals(userId)))
	    		.filter(att -> (startDate == null || (att.getCheckInTime() != null 
	    		&& !att.getCheckInTime().toLocalDate().isBefore(startDate))))
	    		.filter(att -> (endDate == null || (att.getCheckInTime() != null 
	    		&& !att.getCheckInTime().toLocalDate().isAfter(endDate))))
	    		.filter(att -> att.getCheckInTime() != null && att.getCheckOutTime() != null)
	    		.collect(Collectors.groupingBy(
	    				Attendance::getUserId,
	    				Collectors.summingLong(att -> 
	    				ChronoUnit.HOURS.between(att.getCheckInTime(), att.getCheckOutTime()))
	    		));
	}
}
