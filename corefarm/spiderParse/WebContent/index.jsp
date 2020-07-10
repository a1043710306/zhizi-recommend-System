<%@ page language="java" pageEncoding="UTF-8"%>
<%
    String SSOLoginPage =request.getSession().getServletContext().getInitParameter("SSOLoginPage");
String CookieName =request.getSession().getServletContext().getInitParameter("CookieName");
CookieName =CookieName.toLowerCase().trim();
Cookie[] cookies=   request.getCookies();
Cookie loginCookie =null;
String cookname ="";
if(cookies!=null){
    for(Cookie cookie:cookies){
        cookname =cookie.getName().trim().toLowerCase();
       if(CookieName.equals(cookname)){
           loginCookie =cookie;
           break;
       }
    }
}

if(loginCookie==null){
    String url =request.getRequestURL().toString();
    response.sendRedirect(SSOLoginPage+"?goto="+url);
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Monitor - Parser</title>
<script type='text/javascript' src='dwr/engine.js'></script>
<script type='text/javascript' src='dwr/util.js'></script>
<script type='text/javascript' src='dwr/interface/Parser.js'></script>

<script>
	function init() {
		Parser.isStarted(function callback(data) {
			if (data) {
				dwr.util.setValue("btn", "Stop");
				$("#btn").removeClass();
				$("#btn").toggleClass("btn btn-danger");
			} else {
				dwr.util.setValue("btn", "Start");
				$("#btn").removeClass();
				$("#btn").toggleClass("btn btn-success");
			}
		});

	}
	function btnClick() {
		var op = dwr.util.getValue("btn");
		if (op == "Start") {
			Parser.start();
			$("#btn").removeClass();
			$("#btn").toggleClass("btn btn-danger");
			dwr.util.setValue("btn", "Stop");
		} else {
			Parser.stop();
			$("#btn").removeClass();
			$("#btn").toggleClass("btn btn-success");
			dwr.util.setValue("btn", "Start");
		}
	}

	function btnStatusClick() {
		Parser.getAllWaitingJobs(function(result) {
			dwr.util.setValue("waiting", result, {
				escapeHtml : false
			});
		});

		Parser.getRunningJobs(function(result) {
			dwr.util.setValue("running", result, {
				escapeHtml : false
			});
		});
	}
</script>

<!-- Bootstrap -->
<link rel="stylesheet" href="css/bootstrap.min.css">
<link href="css/sticky-footer.css" rel="stylesheet">

<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
        <script src="js/html5shiv.min.js"></script>
        <script src="js/respond.min.js"></script>
    <![endif]-->
</head>
<body onload="init()">

	<div class="container">
		<div class="page-header">
			<h1>
				Spider <small>Parser</small>
			</h1>
		</div>
		<div>
			<button type="button" class="btn btn-default navbar-btn" id="btn"
				onclick="btnClick();">Start</button>
			<button type="button" class="btn btn-default navbar-btn"
				id="btnStatus" onclick="btnStatusClick();">Status</button>
		</div>

	</div>
	<div class="panel panel-primary">
		<div class="panel-heading">Running</div>
		<div class="panel-body" id="running"></div>
	</div>
	<div class="panel panel-info">
		<div class="panel-heading">Waiting</div>
		<div class="panel-body" id="waiting"></div>
	</div>




	<div class="footer">
		<div class="container">
			<p class="text-muted">&copy;inveno.cn version:$version$</p>
		</div>
	</div>

	<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
	<script src="js/jquery.min.js"></script>
	<!-- Include all compiled plugins (below), or include individual files as needed -->
	<script src="js/bootstrap.min.js"></script>
</body>
</html>