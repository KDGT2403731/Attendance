# Attendance (前期審査会)

企業の従業員と人事部向けに作成した動的Webプロジェクトです。

## プロジェクト構成

> 代表的な構成

```text
src/
└── main/
    └── java/
        └── com/
            └── example/
                └── attendance/
                    ├── controller/
                    │ 　├── AttendanceServlet.java
                    │ 　├── LoginServlet.java
　　　　　　　　　　　　│ 　├── LogoutServlet.java
　　　　　　　　　　　　│ 　└── UserServlet.java
　　　　　　　　　　　　├── dao/
　　　　　　　　　　　　│ 　├── AttendanceDAO.java
　　　　　　　　　　　　│ 　└── UserDAO.java
　　　　　　　　　　　　├── dto/
　　　　　　　　　　　　│　 ├── Attendance.java
　　　　　　　　　　　　│　 └── User.java
　　　　　　　　　　　　└── filter/
　　　　　　　　　　　　　　└── AuthenticationFilter.java
webapp/
├── login.jsp
├── style.css
└── jsp/
　　├── admin_menu.jsp
　　├── employee_menu.jsp
　　├── error.jsp
　　└── user_management.jsp
```
