<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link type="text/css" rel="stylesheet" href="../css/main.css">
		<link type="text/css" rel="stylesheet" href="../css/SOA.css">
		<style type="text/css">
a {
	padding-left: 0px;
}
</style>
		<script language="javascript">
			function showSending() {
				sending.style.visibility="visible";
				cover.style.visibility="visible";
			}	
			function hideSending() {
				sending.style.visibility="hidden";
				cover.style.visibility="hidden";
			}	
			function GoSend(id){
				showSending();
				location.href='SelectNode.action?id='+id;
			}
			window.onload = function(){
				try{
					var win = top.window.sendFormWin;
					win.setTitle("请选择您要发送的环节");
					var c = document.getElementById("Send");
					var h = c.clientHeight > c.scrollHeight ? c.clientHeight : c.scrollHeight;
					
					var json = {
						height : h+85,
						width  : 400 
					} ;
					win.setWH(json);
				}catch(e){}
			}
		</script>
	</head>
	<body background="../images/bg_body_right.gif" topmargin="0"
		style="border: 0px solid #ff0000;">
		<form id="Send" method="post" style="border: 0px solid #00ff00;">
			<div id="sending"
				style="width:90%;z-index: 10; padding: 0 20 0 20px; visibility: hidden; position: absolute;">
				<TABLE cellSpacing="0" cellPadding="0" width="100%" border="1">
					<TR>
						<TD bgColor="#ff9900">
							<TABLE height="70" cellSpacing="2" cellPadding="0" width="100%"
								border="0">
								<TR>
									<td align="center" bgColor="#eeeeee">
										正在获取人员列表，请稍候...
									</td>
								</TR>
							</TABLE>
						</TD>
					</TR>
				</TABLE>
			</div>
			<div id="cover"
				style="z-index: 9; left: 0px; visibility: hidden; width: 100%; position: absolute; top: 0px;">
			</div>
			<table width="100%" border="0" cellpadding="0" cellspacing="0"
				align="center">
				<tr>
					<td>
						<table cellspacing="0" cellpadding="3" rules="all"
							bordercolor="#DEDFDE" border="0" id="DGrid_List"
							style="border-color: #DEDFDE; font-family: 宋体; width: 100%; border-collapse: collapse;">
							<%
							    dsoap.dsflow.model.DataTable dt = (dsoap.dsflow.model.DataTable) request.getAttribute("nextNodes");
							    int i = 0;
							    boolean isFirst = false;
							    for (dsoap.dsflow.model.DataRow dr : dt.rows) {
							        String name = dr.get("NAME");
							        String id = dr.get("ID");
							        String lineType = dr.get("LINETYPE");
							        if (!isFirst && "3".equals(lineType)) {
							            isFirst = true;
							%>
							<tr>
								<td style="border-top: 2px dotted #DFDFDF;"></td>
							</tr>
							<%
							    }
							%>
							<tr style='background-color: <%=(i % 2 == 0 ? "white" : "#F1F1F1")%>;'>
								<td style="font-family: 宋体; font-size: 14pt;">
									<a style="font-size: 14px;<%=isFirst ? "color:green;" : "font-weight: bold;"%>"
										href="javascript:GoSend(<%=id%>)"><%=name%></a>
								</td>
							</tr>
							<%
							    i++;
							    }
							%>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<table border="0" cellpadding="0" cellspacing="0" width="100%"
							style="padding-top: 6px; border-top: 1px solid #DFDFDF; width: 100%;">
							<tr>
								<td align="center">
									<div class="select_node_cancel"
										onclick="top.window.sendFormWin.hide();">
										取 消
									</div>
								</td>
							</tr>
						</table>
						<input id="txtRetualValue" title="记录是否发送成功，以便返回给调用页面"
							style="display: none;" type="text" value="CANCEL" name="Text1">
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
