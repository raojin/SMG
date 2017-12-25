<%@ page language="java" pageEncoding="UTF-8"%>
<%
	//错误显示页面
    String strMsg =request.getAttribute("msg")!=null?"暂无相关信息。":request.getAttribute("msg").toString();
%>
<html>
	<head>
		<style>
body {
	scrollbar-face-color: #d9d9d9;
	scrollbar-arrow-color: #ffffff;
	scrollbar-highlight-color: #ffffff;
	scrollbar-3dlight-color: #d9d9d9;
	scrollbar-shadow-color: #ffffff;
	scrollbar-darkshadow-color: #d9d9d9;
	scrollbar-track-color: #ffffff;
}

.form_status_user_info1 {
	background: url(../images/form_status1.gif) no-repeat;
	height: 47px;
	width: 3px;
	font-size: 12px;
	border: 0px solid #red;
}

.form_status_user_info2 {
	background: url(../images/form_status2.gif) repeat-x;
	height: 47px;
	font-size: 12px;
	border: 0px solid #red;
}

.form_status_user_info3 {
	background: url(../images/form_status3.gif) no-repeat;
	height: 47px;
	width: 16px;
	border: 0px solid #red;
	cursor: pointer;
}

.form_status_user_info4 {
	background: url(../images/form_status4.gif) no-repeat;
	height: 47px;
	width: 22px;
	height: 47px;
	border: 0px solid #red;
}

.form_status_user_info5 {
	height: 47px;
}

.form_status_user_info_div1 {
	font-size: 12px;
	margin-top: 10px;
	margin-left: 10px;
	border: 0px solid #red;
}

.form_status_user_info_div2 {
	font-size: 12px;
	margin-top: 8px;
	margin-right: 30px;
	border: 0px solid #red;
}

.form_status_user_info_div3 {
	font-size: 12px;
	margin-top: 0px;
	margin-left: 10px;
	border: 0px solid #red;
}

.form_status_user_info_div4 {
	font-size: 12px;
	margin-top: 0px;
	margin-right: 30px;
	border: 0px solid #red;
}

.form_status_table {
	font-size: 12px;
}

.form_status_dqzt {
	width: 100%;
	font-size: 12px;
	font-weight: bold;
	color: #989898;
	padding-left: 20px;
	padding-top: 2px;
	border: 0px solid #0000ff;
}

.form_status_view {
	color: #fe7201;
	font-size: 12px;
	font-weight: bold;
	text-decoration: underline;
	cursor: pointer;
}

.form_status_sp {
	font-size: 12px;
	padding-right: 30px;
	padding-left: 10px;
	border: 0px solid #ff0000;
}

.form_status_org {
	font-size: 12px;
	color: #2b69a7;
	padding-right: 10px;
	border: 0px solid #ff0000;
}

.form_status_user {
	font-size: 12px;
	color: #2b69a7;
	border: 0px solid #ff0000;
}

.form_status_node_info {
	background: url(../images/showNodeInfo.gif) no-repeat;
	height: 15px;
	width: 15px;
	border: 0px solid #red;
	cursor: pointer;
}

.form_status_node_info1 {
	background: url(../images/hideNodeInfo.gif) no-repeat;
	height: 15px;
	width: 15px;
	border: 0px solid #red;
	cursor: pointer;
}

.form_status_node_YJ {
	z-index: 200;
	position: absolute;
	width: 160px;
	border: 0px solid #ff0000;
	position:absolute; 
	height:160px; 
	overflow: auto;
}

.form_status_node_YJ1 {
	width: 100%;
	white-space: normal;
	overflow: auto;
	border: 1px solid #f9b620;
	font-size: 12px;
	padding: 5px;
	word-break: break-all;
}

.form_status_node_YJ2 {
	background: url(../images/talk.gif) no-repeat;
}

a {
	color: #83bfe6;
}
</style>
		<script src="../js/jquery/jquery.js"></script>
		<script src="../js/formStatus.js"></script>
	</head>
	<body style="margin: 0px; border: 0px solid #ff0000; width: 100%;">
		<div id="form_status_node_YJ" class="form_status_node_YJ"></div>
		<div class="form_status_dqzt">
			提醒:
		</div>
		<div style="border: 0px solid #00ff00; padding-left: 78px;">
			<span class="form_status_org"><%=strMsg%></span>
		</div>
		<div class="form_status_chart">
		</div>
	</body>
</html>
