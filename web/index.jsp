<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'> 
<html>
<head>
<title>ADAPT&sup2; Personalization Services</title>
<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>
<link rel='StyleSheet' href='assets/rest.css' type='text/css' />  <!-- <%=request.getContextPath()%>/ -->
<%!
	String user_login;
	boolean isLoggedIn;
	String loginout_url;
	boolean invalidated;
%>
<%
	invalidated = false;
	if(request.getParameter("logout") != null)
	{
		session.invalidate();
		invalidated = true;
	}
	
	user_login = request.getRemoteUser();
	isLoggedIn = !invalidated && (user_login!=null) && (user_login.length()>0);
	
	loginout_url = (isLoggedIn)?user_login + "&nbsp;&nbsp;" + "<a href='" + request.getContextPath() + "/index.jsp?logout=1'>Log out</a>" :
		"<span style='color:#999999;'>You are not logged in</span>&nbsp;&nbsp;<a href='" + request.getContextPath() + "/home'>Log in</a>";
%>

</head>
<body>
<table cellpadding='2px' cellspacing='0px' class='dkcyan_table' width='500px'>
	<caption style="text-align:right; padding:3px;" class='login_header'><%=loginout_url%></caption>
	<tr><td colspan="2" class='dkcyan_table_caption'>ADAPT&sup2; Personalization Services</td></tr>
	<tr>
		<td class="dkcyan_table_header">Home</td>
	</tr>
	<tr>
		<td>
			<div>Personalization Services</div>
			<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='<%=request.getContextPath()%>/services'>View all Personalization Services&nbsp;<img border='0' src='<%=request.getContextPath()%>/assets/view_enabled.gif' /></a></div>
			<div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='<%=request.getContextPath()%>/services/new'>Add new Personalization Service&nbsp;<img border='0' src='<%=request.getContextPath()%>/assets/add2_enable.gif' /></a></div>
		</td>
	</tr>
	<tr>
	  <td>&nbsp;</td>
    </tr>
	<tr>
	  <td class='dkcyan_table_footer'>Michael V. Yudelson &copy; 2007</td>
    </tr>
</table>

</body>
</html>
