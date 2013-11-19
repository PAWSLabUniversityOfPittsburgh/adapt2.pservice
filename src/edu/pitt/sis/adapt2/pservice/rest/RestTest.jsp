<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<h2>Rest User by Login</h2>
<form action="http://localhost:8080/portal_client/rest/user/peterb" method="post">
<input type="text" name="tt" value="" />
<input type="Submit" value="Submit" />
</form>
<p />
<h2>Rest User by ID</h2>
<form action="http://localhost:8080/portal_client/rest/user/id/1122" method="post">
<input type="text" name="tt" value="" />
<input type="Submit" value="Submit" />
</form>
<p />
<h3>Request parameters</h3>
<p>request.getContextPath()= <%=request.getContextPath()%></p>
<p>request.getLocalAddr()= <%=request.getLocalAddr()%></p>
<p>request.getServerName()= <%=request.getServerName()%></p>
<p>request.getPathTranslated()= <%=request.getPathTranslated()%></p>
<p>request.getQueryString()= <%=request.getQueryString()%></p>
<p>request.getRequestURI()= <%=request.getRequestURI()%></p>
<p>request.getPathInfo()= <%=request.getPathInfo()%></p>
<p>request.getRemoteAddr()= <%=request.getRemoteAddr()%></p>
<p>request.getRemoteHost()= <%=request.getRemoteHost()%></p>
<p>request.getServletPath()= <%=request.getServletPath()%></p>
<p>request.getServerName()= <%=request.getServerName()%></p>
<p>request.getProtocol()= <%=request.getProtocol()%></p>
</body>
</html>