<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="dsoap.dsflow.DS_FLOWClass"%>
<%
	String msg = "";
	request.setCharacterEncoding("UTF-8");
    boolean showCountdown = dsoap.tools.ConfigurationSettings.showCountdown;
    int closeMode = dsoap.tools.ConfigurationSettings.closeMode;
    DS_FLOWClass dsFlow=null;
	try{
    if (request.getParameter("flowParms") != null)
    	{
            String FlowParms = URLDecoder.decode(request.getParameter("flowParms"), "utf-8");
                // System.out.println("获取Flowparams对象:"+FlowParms);
             dsFlow = new DS_FLOWClass(FlowParms);
         }
     else
	    {
			dsFlow = (dsoap.dsflow.DS_FLOWClass) session.getAttribute("DSFLOW");
		}
    boolean endFlag = dsFlow.sendToEnd();
    if (endFlag) {
    	msg = "<b>该文件成功处理！</b>";
     /*   if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
            msg = "<b>该文件已成功发送到下列用户：</b>" + dsFlow.joinToUsers;
        } else {
            msg = "<b>该文件成功处理！</b>";
        }*/
    } else {
        msg = "<b>该文件处理中发生错误!</b>";
    }
    }
    catch(Exception ex)
    {
    	ex.printStackTrace();
    	msg = "<b>该文件处理中发生错误!</b>";
    }
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
		<div id="backDlg"
			style="border: 0px solid #00ff00; padding-top: 15px; font-size: 12px; padding-left: 15px;">
			<div id="TabCaption" style="width: 100%; font-size: 14px;">
				<%=msg%>
			</div>
			<div
				<%=" style='padding-top: 15px;" + (showCountdown ? "" : " display:none;") + "' "%>>
				页面会在&nbsp;
				<span id="countDown">3</span>&nbsp;秒钟后自动关闭！
			</div>
			<center>
				<div class="send_close" onclick="javascript:closeWin();">
					关 闭
				</div>
			</center>
		</div>
	</body>
	<script language="javascript">
	window.onload = function(){
		try{
			var win = top.window.sendFormWin;
			win.setTitle("");
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
			
			
			win.element.bind('dialogclose', function(){
				closeWin();
			});
		}catch(e){}
	}
	setInterval(function(){
		var timer = document.getElementById("countDown");
		var i = parseInt(timer.innerHTML);
		if(i>0){
			timer.innerHTML = i-1;
		}
	},1000);
	setTimeout(function(){
	<%=showCountdown ? "closeWin();" : ""%>
	},3000);
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
		    if (closeMode == 0) {
		        out.println("top.window.location.href = sendBackUrl;");
		    } else {
		        out.println("top.window.close();");
		    }
		%>
	}
</script>
</html>
