<%@ page language="java" pageEncoding="UTF-8"%>
<html>
	<head>
		<LINK href="../css/main.css" type="text/css" rel="stylesheet">
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
			<center>
				<div style="width: 100%; font-size: 14px;">
					<b><%=request.getAttribute("msg").toString()%></b>
				</div>
			</center>
			<center>
				<div id="BtnClose" class="user_tree_btn0"
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
			win.setTitle("");
			var c = document.getElementById("backDlg");
			var h = c.clientHeight>c.scrollHeight?c.clientHeight:c.scrollHeight;
			win.element.dialog('option', 'height', h+85);
			win.element.dialog('option', 'width', 400);
			win.element.dialog('option', 'position', "center");
			win.setWH();
			win.element.bind('close', function(){
				closeWin();
			});
		}catch(e){}
	}
	
	function closeWin(){
		var obj=top.window.document.getElementById('mainIframe');
		if(obj&&obj.contentWindow){
     	   var w=obj.contentWindow;
     	   w.location.href=w.location.href;
     	  }else{
     		 top.window.location.reload() ;   
     	 }
        top.window.sendFormWin.hide();
	}
</script>
</html>