<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>エラー</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
	<h1>エラーが発生しました</h1>
	<p>申し訳ありませんが、処理中にエラーが発生しました。</p>
	<p>エラーメッセージ:
	<% if (exception != null) { %>
			<%= exception.getMessage() %>
	<% } else { %>
			不明なエラーが発生しました。
	<% } %>
	</p>
	<a href="../login.jsp">ログインページに戻る</a>
</body>
</html>