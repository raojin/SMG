<%@ page language="java" pageEncoding="UTF-8"%>
<%
	
	String success =  "" ;
	String pid = "" ;
	String pnid = "" ;
	
	if(request.getAttribute("success") != null){
		success = (String) request.getAttribute("success") ;
	}
	
	if(request.getAttribute("pid") != null){
		pid = (String) request.getAttribute("pid") ;
	}
	
	if(request.getAttribute("pnid") != null){
		pnid = (String) request.getAttribute("pnid") ;
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<style type="text/css">
.user_tree_btn0 {
	margin-top: 30px;
	color: white;
	width: 63px;
	height: 31px;
	line-height: 31px;
	background: url(../images/but04.gif) no-repeat;
	cursor: pointer;
	border: 0px solid #ff0000;
}
</style>
	</head>
	<body background="../images/bg_body_right.gif"
		style="border: 0px solid #0000ff; margin: 0px;">
		<form id="backDlg"
			style="border: 0px solid #00ff00; padding-top: 15px; font-size: 12px; padding-left: 15px;">
			<div style="width: 100%; font-size: 14px;">
				<b><%=request.getAttribute("errStr") %></b>
			</div>
			<center>
				<div id="BtnClose" class="user_tree_btn0"
					onclick='javascript:top.window.sendFormWin.hide();'>
					确 定
				</div>
			</center>
		</form>
	</body>
	<script language="javascript">
	function showErrorMsg(){
		try{
			var win = top.window.sendFormWin;
			win.setTitle("错误信息");
			var c = document.getElementById("backDlg");
			var h = c.clientHeight>c.scrollHeight?c.clientHeight:c.scrollHeight;
			/**
			win.element.dialog('option', 'height', h+85);
			win.element.dialog('option', 'width', 400);
			win.element.dialog('option', 'position', "center");
			win.setWH();
			*/
			
			win.setWH({
				height:h+85,
				width:400
			});
			
		}catch(e){}
	}
	
	window.onload = function(){
		var success = "<%=success%>";
		if("1" == success){
			if(top.callbackFormDataTs){
				top.callbackFormDataTs("<%=pid%>","<%=pnid%>");
			}else{
				top.window.opener.location.reload();
				top.window.close();
			}
		}else{
			showErrorMsg();
		}
	}
</script>
</html>

