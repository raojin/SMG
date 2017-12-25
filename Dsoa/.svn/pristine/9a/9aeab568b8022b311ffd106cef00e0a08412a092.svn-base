<%@ page language="java" pageEncoding="UTF-8"%>
<%
    boolean showCountdown = dsoap.tools.ConfigurationSettings.showCountdown;
    String countdownTime = dsoap.tools.ConfigurationSettings.AppSettings("COUNT_DOWN_TIME");
    int closeMode = dsoap.tools.ConfigurationSettings.closeMode;
    boolean isCloseWin = ((dsoap.dsflow.DS_FLOWClass) session.getAttribute("DSFLOW")).iSendType != 3;
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
		<form id="SuccessList"
			style="border: 0px solid #00ff00; padding-top: 15px; font-size: 12px; padding-left: 15px; width: 338px;">
			<div id="TabCaption" style="width: 100%; font-size: 14px;">
				<%=request.getAttribute("TabCaption") != null ? request.getAttribute("TabCaption") : ""%>
				<%=request.getAttribute("labSendMsg") != null ? request.getAttribute("labSendMsg") : ""%>
				<%=request.getAttribute("LabErrorMsg") != null ? request.getAttribute("LabErrorMsg") : ""%>
			</div>
			<div
				<%=" style='padding-top: 15px;" + (showCountdown && !"0".equals(countdownTime)? "" : " display:none;") + "' "%>>
				页面会在&nbsp;
				<span id="countDown"><%=countdownTime%></span>&nbsp;秒钟后自动关闭！
			</div>
			<center>
				<div class="send_close" onclick="javascript:closeWin();">
					关 闭
				</div>
			</center>
			<input id="txtRetualValue" title="记录是否发送成功，以便返回给调用页面"
				style="display: none;" type="text"
				value="<%=request.getAttribute("txtRetualValue") != null ? request.getAttribute("txtRetualValue") : "CANCEL"%>"
				name="txtRetualValue">
		</form>
	</body>
	<script language="javascript">
	var closeTime=<%=countdownTime%>;
	window.onload = function(){
		try{
			var win = top.window.sendFormWin;
			win.setTitle("");
			var c = document.getElementById("SuccessList");
			var h = c.clientHeight > c.scrollHeight ? c.clientHeight : c.scrollHeight;
			
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
			
			win.element.bind('dialogclose', function(){
				closeWin();
			});
		}catch(e){}
		if(closeTime>0){
			setTimeout(function(){
			<%=showCountdown ? "closeWin();" : ""%>
			},<%=countdownTime+"000"%>);
		}else{
			closeWin();
		}
	}
	setInterval(function(){
		var timer = document.getElementById("countDown");
		var i = parseInt(timer.innerHTML);
		if(i>0){
			timer.innerHTML = i-1;
		}
	},1000);
	function closeWin(){
	    var isNew = <%=session.getAttribute("isNewFile")%>;
        var sendBackUrl = '<%=session.getAttribute("sendBackUrl")%>';
        if(!isNew){
	        var DaiBanBackUrl = top.window.Cookie.get("DaiBanBack");
	        top.window.Cookie.set("DaiBanBack","");
	        if(DaiBanBackUrl != null && DaiBanBackUrl != ""){
	        	sendBackUrl = DaiBanBackUrl;
	        }
        }
		<%
		if (isCloseWin) {
		    if (closeMode == 0) {
		        out.println("top.window.location.href = sendBackUrl;");
		    } else {
		        out.println("if(top.window.opener){try {top.window.opener.location.reload();}catch(eee){} top.window.close();}else{top.sendFormWin.dialog('close');}");
		    }
	    }else{
	    	out.println("top.window.location.href = top.window.location.href;");
	    }
		%>
	}
</script>
</HTML>