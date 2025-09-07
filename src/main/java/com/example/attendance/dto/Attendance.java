package com.example.attendance.dto;

import java.time.LocalDateTime;

public class Attendance {
	private String userId;
	private LocalDateTime checkInTime;
	private LocalDateTime checkOutTime;
	private int breakDurationMinutes;
	
	public Attendance(String userId) {
		this.userId = userId;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public LocalDateTime getCheckInTime() {
		return checkInTime;
	}
	
	public void setCheckInTime(LocalDateTime CheckInTime) {
		this.checkInTime = CheckInTime;
	}
	
	public LocalDateTime getCheckOutTime() {
		return checkOutTime;
	}
	
	public void setCheckOutTime(LocalDateTime checkOutTime) {
		this.checkOutTime = checkOutTime;
	}
	
	public int getBreakDurationMinutes() {
		return breakDurationMinutes;
	}
	
	public void setBreakDurationMinutes(int breakDurationMinutes) {
		this.breakDurationMinutes = breakDurationMinutes;
	}
}
