# Attendance (前期審査会)

KADOKAWAドワンゴ情報工科学院　2025年前期審査会提出用課題　上級課題　勤怠管理システム
従業員の **出勤／退勤／休憩の打刻**、**履歴の閲覧**、管理者による **一覧・修正承認・CSV 出力** を行う学習用 Web アプリです。  
Jakarta Servlet (Servlet 5) + JSP をベースに、Eclipse での学習や演習に最適化しています。

> **注**: 本 README は公開リポジトリ構成（Java/CSS 比率、Eclipse プロジェクト）と一般的な前期カリキュラム要件に沿って作成したテンプレートです。実装クラス名・パスが異なる場合は適宜読み替えてください。

---

## 機能

**従業員**
- 出勤／退勤／休憩開始／休憩終了の打刻
- 自分の勤怠履歴の検索・閲覧（期間・並び替え）

**管理者**
- 全社員の勤怠一覧（フィルタ／ソート）
- 打刻修正依頼の承認
- CSV 出力（期間・部署などの条件付き想定）

---

## ディレクトリ構成

> 代表的な構成

```text
src/
└─ main
   ├─ java/
   │  └─ com/example/attendance/
   │     ├─ entity/
   │     │  ├─ User.java
   │     │  └─ Attendance.java
   │     ├─ dao/
   │     │  ├─ UserDAO.java
   │     │  └─ AttendanceDAO.java
   │     ├─ servlet/
   │     │  ├─ LoginServlet.java
   │     │  ├─ AttendanceServlet.java
   │     │  └─ AdminServlet.java
   │     ├─ filter/
   │     │  └─ AuthenticationFilter.java
   │     └─ util/
   │        └─ DB.java            // DataSource/接続ユーティリティ想定
   └─ webapp/
      ├─ WEB-INF/
      │  ├─ web.xml
      │  └─ views/                // JSP
      │     ├─ login.jsp
      │     ├─ menu_employee.jsp
      │     ├─ menu_admin.jsp
      │     ├─ attendance_list.jsp
      │     └─ attendance_history.jsp
      ├─ style.css
      └─ index.jsp
```
