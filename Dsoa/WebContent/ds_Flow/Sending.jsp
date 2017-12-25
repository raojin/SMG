<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="dsoap.dsflow.DS_FLOWClass"%>
<%
	request.setCharacterEncoding("UTF-8");
    String sustr = request.getParameter("UList");// 用户列表 （SelectUser.jsp 提交格式【:节点ID:用户ID:流程节点名:部门名:用户名（部门名）:部门ID】多个时以“;”分割）
    String sSendMethod = request.getParameter("SendMethod");// 发送方式 （SelectUserAction 中设置的格式【节点ID:发送方式】多个时以“,”分割）【节点ID:0】
    String sNodeDate = request.getParameter("txtNode");// （SelectUser.jsp 提交）大写：NULL
    String strPriSend = request.getParameter("TxtPriSend"); //默认用户ID 
    String SMS = request.getParameter("SMSContent");	//
    String isHideYJ= request.getParameter("isHideYJ"); //是否隐藏意见
    String flowParms= request.getParameter("flowParms");
    String backurl= request.getParameter("backurl");
    session.setAttribute("sendBackUrl", backurl);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link type="text/css" rel="stylesheet" href="../css/main.css">
		<link type="text/css" rel="stylesheet" href="../css/SOA.css">
	</head>
	<body background="../images/bg_body_right.gif"
		style="border: 0px solid #0000ff; margin: 0px;">
		<form id='fm' action="Sending.action" method="post">
			<input type="hidden" value='<%=sustr%>' name="UList">
			<input type="hidden" value='<%=sSendMethod%>' name="SendMethod">
			<input type="hidden" value='<%=sNodeDate%>' name="txtNode">
			<input type="hidden" value='<%=strPriSend%>' name="TxtPriSend">
			<input type="hidden" value='<%=SMS%>' name="SMSContent">
			<input type="hidden" value='<%=isHideYJ%>' name="isHideYJ">
			<input type="hidden" value='<%=flowParms%>' name="flowParms">
			<input type="hidden" value='<%=backurl%>' name="backurl">
			<input type="hidden" value='true' name="isWebSend">
		</form>
		<div id="backDlg"
			style="border: 0px solid #00ff00; padding-top: 15px; font-size: 12px; padding-left: 15px;">
			<div id="TabCaption" style="width: 100%; font-size: 14px;">
				正在发送中，请稍后。。。
			</div>
			<script>
				fm.submit();
			</script>
		</div>
	</body>
</html>
