package com.example.attendance.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.attendance.dto.User;

public class UserDAO {
	private static final Map<String, User> users = new ConcurrentHashMap<>();
	
	static {
		users.put("employee1", new User("employee1", hashPassword("password"), "employee", true));
		users.put("admin1", new User("admin", hashPassword("adminpass"), "admin", true));
		users.put("employee2", new User("employee2", hashPassword("password"), "employee", true));
		users.put("yuya", new User("yuya", hashPassword("yuya0616"), "admin", true));
	}
	
	public static User findByUsername(String username) {
		return users.get(username);
	}
	
	public static boolean verifyPassword(String username, String password) {
		User user = findByUsername(username);
		return user != null && user.isEnabled() && user.getPassword().equals(hashPassword(password));
	}
	
	public static Collection<User> getAllUsers() {
		return users.values();
	}
	
	public static void addUser(User user) {
		users.put(user.getUsername(), user);
	}
	
	public static void updateUser(User user) {
		users.put(user.getUsername(), user);
	}
	
	public static void deleteUser(String username) {
		users.remove(username);
	}
	
	public static void resetPassword(String username, String newPassword) {
		User user = users.get(username);
		if (user != null) {
			users.put(username, new User(user.getUsername(), hashPassword(newPassword), user.getRole(),
			user.isEnabled()));
		}
	}
	
	public static void toggleUserEnabled(String username, boolean enabled) {
		User user = users.get(username);
		if (user != null) {
			users.put(username, new User(user.getUsername(), user.getPassword(), user.getRole(), enabled));
		}
	}
	
	public static String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = md.digest(password.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : hashedBytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
