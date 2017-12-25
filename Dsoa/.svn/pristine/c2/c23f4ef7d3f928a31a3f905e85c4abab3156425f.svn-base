<%@ page language="java" pageEncoding="UTF-8"%>
<%
    int closeMode = dsoap.tools.ConfigurationSettings.closeMode;
    String sendStatus = (String) request.getAttribute("SEND_STATUS");
    String INFO_ID = (String) request.getAttribute("INFO_ID");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link type="text/css" rel="stylesheet" href="../css/main.css">
		<link type="text/css" rel="stylesheet" href="../css/SOA.css">
		<script type="text/javascript" src="../js/jquery/jquery.js"></script>
	</head>
	<body background="../images/bg_body_right.gif"
		style="border: 0px solid #0000ff; margin: 0px;">
		<form id="SuccessList"
			style="border: 0px solid #00ff00; padding-top: 15px; font-size: 12px; padding-left: 15px; width: 338px;">
			<div id="TabCaption" style="width: 100%; font-size: 14px;">
				<%=request.getAttribute("TabCaption") != null ? request.getAttribute("TabCaption") : ""%>
				<%=request.getAttribute("labSendMsg") != null ? request.getAttribute("labSendMsg") : ""%>
			</div>
			<%
			    if ("1".equals(sendStatus)) {
			%>
			<div style="width: 100%; font-size: 14px; padding: 15 0 20 0px;">
				<b>是否回收待阅文件</b>
			</div>
			<table cellSpacing="0" cellPadding="0" width="100%" border="0"
				style="padding-top: 6px; border-top: 1px solid #DFDFDF; width: 100%;">
				<tr>
					<td align="right">
						<div id="subBu" onclick="send();" class="select_user_send">
							回收
						</div>
					</td>
					<td align="left">
						<div onclick="javascript:closeWin();" class="select_user_reselect">
							取消
						</div>
					</td>
				</tr>
			</table>
			<%
			    } else {
			%>
			<center>
				<div class="send_close" onclick="javascript:closeWin();">
					关 闭
				</div>
			</center>
			<%
			    }
			%>
			<input id="txtRetualValue" title="记录是否发送成功，以便返回给调用页面"
				style="display: none;" type="text"
				value="<%=request.getAttribute("txtRetualValue") != null ? request.getAttribute("txtRetualValue") : "CANCEL"%>"
				name="txtRetualValue">
		</form>
	</body>
	<script language="javascript">
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
	}
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
		        out.println("top.window.opener.location.reload();top.window.close();");
		    }
		%>
	}
	function send(){
		var url = '<%=dsoap.tools.ConfigurationSettings.getServerInfo(null)%>/action?IssueAction=12&Info_ID=<%=INFO_ID%>';
		var result = $.ajax({url:url,type:"POST",data:null,dataType:null,timeout:2000,async:false}).responseText;
		closeWin();
	}
</script>
</HTML>