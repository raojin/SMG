<%@ page language="java" pageEncoding="UTF-8"%>
<html>
	<head>
		<link type="text/css" rel="stylesheet" href="../css/main.css">
		<link type="text/css" rel="stylesheet" href="../css/SOA.css">
	</head>
	<body background="../images/bg_body_right.gif"
		style="border: 0px solid #0000ff; margin: 0px;">
		<form id="backDlg"
			style="border: 0px solid #00ff00; padding-top: 15px; font-size: 12px; padding-left: 15px;">
			<div style="width: 100%; font-size: 14px;padding-bottom:10px;">
				<b>您好，该文件已办结，不能修改流程。</b>
			</div>
			<center>
				<div id="BtnClose" class="select_node_cancel"
					onclick='javascript:closeWin();'>
					确 定
				</div>
			</center>
		</form>
	</body>
	<script language="javascript">
	window.onload = function(){
		try{
			var win = top.window.sendFormWin;
			win.setTitle("提示");
			var c = document.getElementById("backDlg");
			var h = c.clientHeight>c.scrollHeight?c.clientHeight:c.scrollHeight;
			
			/**
			win.element.dialog('option', 'height', h+85);
			win.element.dialog('option', 'width', 350);
			win.element.dialog('option', 'position', "center");
			win.setWH();
			**/
			
			win.setWH({
				height:h+85,
				width:350
			});
			
			
			win.element.bind('dialogclose', function(){
				closeWin();
			});
		}catch(e){}
	}
	function closeWin(){
		top.window.opener.location.reload();
		top.window.close();
	}
</script>
</html>