package com.example.attendance.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.User;

@WebServlet("/attendance")
@MultipartConfig
public class AttendanceServlet extends HttpServlet {
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		HttpSession session = req.getSession(false);
		User user = (User) session.getAttribute("user");
		
		if (user == null) {
			resp.sendRedirect(req.getContextPath() + "login.jsp");
			return;
		}
		
		String message = (String) session.getAttribute("successMessage");
		if (message != null) {
			req.setAttribute("successMessage", message);
			session.removeAttribute("successMessage");
		}
		
		if ("export_csv".equals(action) && "admin".equals(user.getRole())) {
			exportCsv(req, resp);
			return;
		}
		
		if ("admin".equals(user.getRole())) {
			String filterUserId = req.getParameter("filterUserId");
			String startDateStr = req.getParameter("startDate");
			String endDateStr = req.getParameter("endDate");
			List<Attendance> filteredRecords;
			Map<YearMonth, Long> monthlyOvertimeHours;
			Map<YearMonth, Long> monthlyWorkingHours;
			Map<YearMonth, Long> monthlyCheckInCounts;
			Map<String, Long> totalHoursByUser;
			
			if ("filter".equals(action)) {
				LocalDate startDate = null;
				LocalDate endDate = null;
				try {
					if (startDateStr != null && !startDateStr.isEmpty()) {
						startDate = LocalDate.parse(startDateStr);
					}
					if (endDateStr != null && !endDateStr.isEmpty()) {
						endDate = LocalDate.parse(endDateStr);
					}
				} catch (DateTimeParseException e) {
					req.setAttribute("errorMessage", "日付の形式が不正です。");
				}
				filteredRecords = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);
				monthlyOvertimeHours = attendanceDAO.getMonthlyOvertimeHours(filterUserId);
				monthlyWorkingHours = attendanceDAO.getMonthlyWorkingHours(filterUserId);
				monthlyCheckInCounts = attendanceDAO.getMonthlyCheckInCounts(filterUserId);
				totalHoursByUser = attendanceDAO.getTotalHoursByUserWithBreak(filterUserId, startDate, endDate);
			} else {
				filteredRecords = attendanceDAO.findAll();
				monthlyOvertimeHours = attendanceDAO.getMonthlyOvertimeHours(null);
				monthlyWorkingHours = attendanceDAO.getMonthlyWorkingHours(null);
				monthlyCheckInCounts = attendanceDAO.getMonthlyCheckInCounts(null);
				totalHoursByUser = attendanceDAO.getTotalHoursByUserWithBreak(null, null, null);
			}
			
			req.setAttribute("allAttendanceRecords", filteredRecords);
			req.setAttribute("monthlyOvertimeHours", monthlyOvertimeHours);
			req.setAttribute("monthlyWorkingHours", monthlyWorkingHours);
			req.setAttribute("monthlyCheckInCounts", monthlyCheckInCounts);
			req.setAttribute("totalHoursByUser", totalHoursByUser);
			req.setAttribute("filterUserId", filterUserId);
			req.setAttribute("startDate", startDateStr);
			req.setAttribute("endDate", endDateStr);
			
			RequestDispatcher rd = req.getRequestDispatcher("/jsp/admin_menu.jsp");
			rd.forward(req, resp);
		} else {
			req.setAttribute("attendanceRecords", attendanceDAO.findByUserId(user.getUsername()));
			RequestDispatcher rd = req.getRequestDispatcher("/jsp/employee_menu.jsp");
			rd.forward(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession(false);		
		User user = (User) session.getAttribute("user");
		
		if (user == null) {
			resp.sendRedirect("login.jsp");
			return;
		}
		
		String action = req.getParameter("action");
		
		if ("importCSV".equals(action)) {
			importCSV(req, resp);
		} else if ("check_in".equals(action)) {
			attendanceDAO.checkIn(user.getUsername());
			session.setAttribute("successMessage", "出勤を記録しました。");
		} else if ("check_out".equals(action)) {
			attendanceDAO.checkOut(user.getUsername());
			session.setAttribute("successMessage", "退勤を記録しました。");
		} else if ("add_manual".equals(action) && "admin".equals(user.getRole())) {
			String userId = req.getParameter("userId");
			String checkInStr = req.getParameter("checkInTime");
			String checkOutStr = req.getParameter("checkOutTime");
			User userToAddAttendance = UserDAO.findByUsername(userId);
			if (userToAddAttendance == null) {
				req.setAttribute("errorMessage", "指定されたユーザーIDは存在しません。");
				req.setAttribute("allAttendanceRecords", attendanceDAO.findAll());
				req.setAttribute("users", UserDAO.getAllUsers());
				req.getRequestDispatcher("/jsp/admin_menu.jsp").forward(req, resp);
				return;
			}
			try {
				if (checkInStr == null || checkInStr.isEmpty()) {
					req.setAttribute("errorMessage", "出勤時刻は必須です。");
				} else {
					LocalDateTime checkIn = LocalDateTime.parse(checkInStr);
					LocalDateTime checkOut = checkOutStr != null && !checkOutStr.isEmpty() ?
					LocalDateTime.parse(checkOutStr) : null;
					attendanceDAO.addManualAttendance(userId, checkIn, checkOut);
					session.setAttribute("successMessage", "勤怠記録を手動で追加しました。");
				}
			} catch (DateTimeParseException e) {
				req.setAttribute("errorMessage", "日付/時刻の形式が不正です。正しい形式で入力してください。");
				req.setAttribute("allAttendanceRecords", attendanceDAO.findAll());
				req.setAttribute("users", UserDAO.getAllUsers());
				req.getRequestDispatcher("/jsp/admin_menu.jsp").forward(req, resp);
				return;
			} catch (Exception e) {
				req.setAttribute("errorMessage", "勤怠記録の追加中に予期せぬエラーが発生しました。");
				req.setAttribute("allAttendanceRecords", attendanceDAO.findAll());
				req.setAttribute("users", UserDAO.getAllUsers());
				req.getRequestDispatcher("/jsp/admin_menu.jsp").forward(req, resp);
				return;
			}
		} else if ("update_manual".equals(action) && "admin".equals(user.getRole())) {
			String userId = req.getParameter("userId");
			String oldCheckInStr = req.getParameter("oldCheckInTime");
			String oldCheckOutStr = req.getParameter("oldCheckOutTime");
			String newCheckInStr = req.getParameter("newCheckInTime");
			String newCheckOutStr = req.getParameter("newCheckOutTime");
			try {
				LocalDateTime oldCheckIn = parseDateTimeRelaxed(oldCheckInStr);
				LocalDateTime oldCheckOut = parseDateTimeRelaxed(oldCheckOutStr);
				LocalDateTime newCheckIn = parseDateTimeRelaxed(newCheckInStr);
				LocalDateTime newCheckOut = parseDateTimeRelaxed(newCheckOutStr);
				
				if (attendanceDAO.updateManualAttendance(userId, oldCheckIn, oldCheckOut, newCheckIn, newCheckOut)) {
					session.setAttribute("successMessage", "勤怠記録を手動で更新しました。");
				} else {
					session.setAttribute("errorMessage", "勤怠記録の更新に失敗しました。");
				}
			} catch (DateTimeParseException e) {
				session.setAttribute("errorMessage", "日付/時刻の形式が不正です。正しい形式で入力してください。");
				e.printStackTrace();
			}
			req.setAttribute("allAttendanceRecords", attendanceDAO.findAll());
			req.setAttribute("users", UserDAO.getAllUsers());
			req.getRequestDispatcher("/jsp/admin_menu.jsp").forward(req, resp);
		} else if ("delete_manual".equals(action) && "admin".equals(user.getRole())) {
			String userId = req.getParameter("userId");
			String checkInStr = req.getParameter("checkInTime");
			String checkOutStr = req.getParameter("checkOutTime");
			
			try {
				LocalDateTime checkIn = parseAndTruncate(checkInStr);
				LocalDateTime checkOut = parseAndTruncate(checkOutStr);
				if (attendanceDAO.deleteManualAttendance(userId, checkIn, checkOut)) {
					session.setAttribute("successMessage", "勤怠記録を削除しました。");
				} else {
					session.setAttribute("errorMessage", "勤怠記録の削除に失敗しました。");
				}
			} catch (DateTimeParseException e) {
				session.setAttribute("errorMessage", "日付/時刻の形式が不正です。");
			}
		}
		if ("admin".equals(user.getRole())) {			
			resp.sendRedirect("attendance?action=filter");
		} else {
			resp.sendRedirect("attendance");
		}
	}
	
	private LocalDateTime parseAndTruncate(String dateString) {
		if (dateString == null || dateString.isEmpty()) {
			return null;
		}
		return LocalDateTime.parse(dateString).truncatedTo(ChronoUnit.SECONDS);
	}

	private LocalDateTime parseDateTimeRelaxed(String dateString) {
		if (dateString == null || dateString.isEmpty()) {
			return null;
		}
		
		DateTimeFormatter[] formatters = {
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
		};
		for (DateTimeFormatter Formatter : formatters) {
			try {
				return LocalDateTime.parse(dateString, Formatter);
			} catch (DateTimeParseException e) {
				
			}
		}
		throw new DateTimeParseException("Invalid date/time format", dateString, 0);
	}

	private void importCSV(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		try {
			Part filePart = req.getPart("file");
			if (filePart == null) {
				req.setAttribute("errorMessage", "ファイルが選択されていません。");
				return;
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream()))) {
				reader.readLine();
				String line;
				DateTimeFormatter[] formatters = {
						DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"),
						DateTimeFormatter.ofPattern("yyyy/M/d H:m"),
						DateTimeFormatter.ofPattern("yyyy/M/d"),
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
						DateTimeFormatter.ofPattern("yyyy-MM-dd")
				};
				int lineCount = 1;
				while ((line = reader.readLine()) != null) {
					lineCount++;
					String[] cols = line.split(",");
					try {
						if (cols.length >= 3) {
							String userId = cols[0].trim();
							LocalDateTime checkIn = parseDateTime(cols[1].trim(), formatters);
							LocalDateTime checkOut = parseDateTime(cols[2].trim(), formatters);
							attendanceDAO.addManualAttendance(userId, checkIn, checkOut);
						}
					} catch (DateTimeParseException e) {
						req.setAttribute("errorMessage",
								String.format("CSVファイルの%d行目で日付/時刻の形式が不正です。: %s", lineCount, line));
						return;
					} catch (Exception e) {
						req.setAttribute("errorMessage",
								String.format("CSVファイルの%d行目でデータのインポート中に予期せぬエラーが発生しました。: %s", 
								lineCount, e.getMessage()));
						e.printStackTrace();
						return;
					}
				}
				req.setAttribute("successMessage", "CSVファイルのインポートが完了しました。");
			}
		} catch (IOException | ServletException e) {
			req.setAttribute("errorMessage", "ファイルのアップロード中にエラーが発生しました。");
			e.printStackTrace();
		}
	}
	
	private LocalDateTime parseDateTime(String dateString, DateTimeFormatter[] formatters) {
		if (dateString == null || dateString.isEmpty()) {
			return null;
		}
		
		for (DateTimeFormatter formatter : formatters) {
			try {
				return LocalDateTime.parse(dateString, formatter);
			} catch (DateTimeParseException e) {
				
			}
		}
		throw new DateTimeParseException("Invalid date format", dateString, 0);
	}

	private void exportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/csv; charset=UTF-8");
		resp.setHeader("Content-Disposition", "attachment; filename=\"attendance_records.csv\"");
		
		PrintWriter writer = resp.getWriter();
		writer.append("User ID, Check-in Time, Check-out Time\n");
		
		String filterUserId = req.getParameter("filterUserId");
		String startDateStr = req.getParameter("startDate");
		String endDateStr = req.getParameter("endDate");
		LocalDate startDate = null;
		LocalDate endDate = null;
		
		try {
			if (startDateStr != null && !startDateStr.isEmpty()) {
				startDate = LocalDate.parse(startDateStr);
			}
			if (endDateStr != null && !endDateStr.isEmpty()) {
				endDate = LocalDate.parse(endDateStr);
			}
		} catch (DateTimeParseException e) {
			System.err.println("Invalid date format for CSV export: " + e.getMessage());
		}
		
		List<Attendance> records = attendanceDAO.findFilteredRecords(filterUserId, startDate, endDate);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		for (Attendance record : records) {
			writer.append(String.format("%s,%s,%s\n",
					record.getUserId(),
					record.getCheckInTime() != null ? 
					record.getCheckInTime().format(formatter) : "",
					record.getCheckOutTime() != null ? 
					record.getCheckOutTime().format(formatter) : ""));
		}
		writer.flush();
	}
}
