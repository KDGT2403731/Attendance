package com.example.attendance.controller;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.User;

@WebServlet("/users")
public class UserServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		HttpSession session = req.getSession(false);
		User currentUser = (User) session.getAttribute("user");
		
		if (session == null || session.getAttribute("user") == null) {
			resp.sendRedirect(req.getContextPath() + "login.jsp");
			return;
		}
		
		if (!"admin".equals(currentUser.getRole())) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "アクセス権がありません。");
			return;
		}
		
		String message = (String) session.getAttribute("successMessage");
		if (message != null) {
			req.setAttribute("successMessage", message);
			session.removeAttribute("successMessage");
		}
		if ("list".equals(action) || action == null) {
			Collection<User> users = UserDAO.getAllUsers();
			req.setAttribute("users", users);
			RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
			rd.forward(req, resp);
		} else if ("edit".equals(action)) {
			String username = req.getParameter("username");
			User user = UserDAO.findByUsername(username);
			req.setAttribute("userToEdit", user);
			Collection<User> users = UserDAO.getAllUsers();
			req.setAttribute("users", users);
			RequestDispatcher rd = req.getRequestDispatcher("/jsp/user_management.jsp");
			rd.forward(req, resp);
		} else {
			resp.sendRedirect("users?action=list");
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		HttpSession session = req.getSession(false);
		User currentUser = (User) session.getAttribute("user");
		
		if (currentUser == null || !"admin".equals(currentUser.getRole())) {
			resp.sendRedirect("login.jsp");
			return;
		}
		
		if ("add".equals(action)) {
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			String role = req.getParameter("role");
			if (UserDAO.findByUsername(username) == null) {
				UserDAO.addUser(new User(username, UserDAO.hashPassword(password), role));
				session.setAttribute("successMessage", "ユーザーを追加しました");
			} else {
				req.setAttribute("errorMessage", "ユーザーIDはすでに存在します。");
			}
		} else if ("update".equals(action)) {
			String username = req.getParameter("username");
			String role = req.getParameter("role");
			boolean enabled = req.getParameter("enabled") != null;
			
			User existingUser = UserDAO.findByUsername(username);
			if (existingUser != null) {
				UserDAO.updateUser(new User(username, existingUser.getPassword(), role, enabled));
				session.setAttribute("successMessage", "ユーザー情報を更新しました。");
			}
		} else if ("delete".equals(action)) {
			String username = req.getParameter("username");
			UserDAO.deleteUser(username);
			session.setAttribute("successMessage", "ユーザーを削除しました。");
		} else if ("reset_password".equals(action)) {
			String username = req.getParameter("username");
			String newPassword = req.getParameter("newPassword");
			UserDAO.resetPassword(username, newPassword);
			session.setAttribute("successMessage", username + "のパスワードをリセットしました。 (デフォルトパスワード: "
					+ newPassword + ")");
		} else if ("toggle_enabled".equals(action)) {
			String username = req.getParameter("username");
			boolean enabled = Boolean.parseBoolean(req.getParameter("enabled"));
			UserDAO.toggleUserEnabled(username, enabled);
			session.setAttribute("successMessage", username + "のアカウントを" +
			(enabled ? "有効" : "無効") + "にしました。");
		}
		session.setAttribute("allUsers", UserDAO.getAllUsers());
		resp.sendRedirect("users?action=list");
	}
}
