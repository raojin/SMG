<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="dsoap.dsflow.model.DataTable"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>顺序图</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<link rel="stylesheet" href="../css/main.css">
		<STYLE>
TD {
	FONT-SIZE: 9pt;
	FONT-FAMILY: 宋体;
}

A {
	TEXT-DECORATION: none;
}

A:hover {
	TEXT-DECORATION: underline;
}

.index1 {
	COLOR: #ffffff;
}

.index1e {
	COLOR: #ffffff;
	FONT-FAMILY: Arial;
}

.index2 {
	COLOR: #808080;
}

.index2:hover {
	COLOR: #000000;
}

.index2e {
	COLOR: #808080;
	FONT-FAMILY: Arial;
}

.index2e:hover {
	COLOR: #000000;
	FONT-FAMILY: Arial;
}

.index5 {
	TEXT-DECORATION: underline;
}

.index5:hover {
	TEXT-DECORATION: none;
}

.gd {
	COLOR: #ffffff;
}

INPUT {
	FONT-SIZE: 9pt;
	FONT-FAMILY: 宋体;
}

SELECT {
	FONT-SIZE: 9pt;
	FONT-FAMILY: 宋体;
}

.point {
	FONT-SIZE: 5pt;
}

.title2 {
	FONT-WEIGHT: bold;
	FONT-SIZE: 10.5pt;
	COLOR: #000ac9;
}

.title3 {
	FONT-WEIGHT: bold;
	FONT-SIZE: 9pt;
	COLOR: #000ac9;
}

.big {
	FONT-WEIGHT: bold;
}

.title4 {
	
}

.title4:hover {
	COLOR: #000ac9;
}

.jia {
	TEXT-DECORATION: underline;
}

.jia:hover {
	TEXT-DECORATION: none;
}

.DEK {
	Z-INDEX: 200;
	POSITION: absolute;
	display:none;
	width: 200px;
}
</STYLE>
		<script language="javascript">
			function OpenList(pid,fid){
				var width=450;//screen.availWidth - 10;
				var height=400;//screen.availHeight - 30;
				//alert("showflowlist.jsp?pid="+pid+"&fid="+fid+"&infoId=<%=request.getParameter("id")%>");
				window.open("showflowlist.jsp?pid="+pid+"&fid="+fid+"&infoId=<%=request.getParameter("id")%>",'ShowFlowList','top=200,left=200,width=' + width + ',height=' + height + ',resizable=1,scrollbars=1');
			}
		</script>
	</HEAD>
	<body>
		<DIV class="dek" id="dek" style="Z-INDEX: 101; width: 200px;"></DIV>
		<SCRIPT language="" type="text/javascript">
			Xoffset=-60;
			Yoffset= 20;
			var nav,old,iex=(document.all),yyy=-1000;
			if(navigator.appName == "Netscape"){
				(document.layers)?nav=true:old=true;
			}
			if(!old){
				var skn = (nav)?document.dek:dek.style;
				if(nav){
					document.captureEvents(Event.MOUSEMOVE);
				}
				document.onmousemove = get_mouse;
			}
			function popup(msg,bak){
				if(msg==''){
					msg="已阅";
				}
				var posb=msg.indexOf("[BR]");
				while (posb!=-1){
					msg=msg.substring(0,posb)+"<br>"+msg.substring(posb+4,msg.length);
					posb=msg.indexOf("[BR]");
				}
				var posb=msg.indexOf("[br]");
				while (posb!=-1){
					msg=msg.substring(0,posb)+"<br>"+msg.substring(posb+4,msg.length);
					posb=msg.indexOf("[br]");
				}
				var content="<TABLE BORDER=1 width=200 BORDERCOLOR=#f9b620 CELLPADDING=0 CELLSPACING=0 BGCOLOR="+bak+"><tr width=200><TD nowrap width=200><FONT COLOR=black SIZE=2>"+msg+"</FONT></TD></tr></TABLE>";
				if(old){
					alert(msg);
					return;
				} else{
					yyy=Yoffset;
					if(nav){
						skn.document.write(content);
						skn.document.close();
						//skn.visibility="visible";
						skn.display="block";
					}
					if(iex){
						document.all("dek").innerHTML=content;
						//skn.visibility="visible";
						skn.display="block";
					}
				}
			}
			function get_mouse(e){
				var x = (nav) ? e.pageX:event.x + document.body.scrollLeft;
				skn.left = x + Xoffset;
				var y = (nav) ? e.pageY:event.y + document.body.scrollTop;
				skn.top = y + yyy;
				window.status=x + Xoffset;
			}
			function kill(){
				if(!old){
					yyy=-1000;
					//skn.visibility="hidden";
					skn.display="none";
				}
			}
		</SCRIPT>
		<form id="ShowFlow" method="post">
			<DIV align="center">
				<table id="tabTitle" border="0"
					style="border-width: 0px; width: 80%;">
					<tr>
						<td>
							<%=request.getAttribute("tabTitle") == null ? "" : request.getAttribute("tabTitle")%>
						</td>
					</tr>
				</table>
				<br>
				<%
				    DataTable TblPercent1 = (DataTable) request.getAttribute("TblPercent1");
				    if (TblPercent1.isVisible()) {
				%>
				<table width="600" border="0" cellpadding="0" cellspacing="0"
					id="TblPercent1">
					<tr height="25">
						<td>
							<span
								style="BORDER-RIGHT: black 1px solid; PADDING-RIGHT: 1px; BORDER-TOP: black 1px solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; BORDER-LEFT: black 1px solid; WIDTH: 12px; PADDING-TOP: 1px; BORDER-BOTTOM: black 1px solid; HEIGHT: 18px">
								<span style='BACKGROUND: url(/images/flow1.gif); WIDTH: 100%'>&nbsp;</span>
							</span>&nbsp;剩余时间
							<span
								style="BORDER-RIGHT: black 1px solid; PADDING-RIGHT: 1px; BORDER-TOP: black 1px solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; BORDER-LEFT: black 1px solid; WIDTH: 12px; PADDING-TOP: 1px; BORDER-BOTTOM: black 1px solid; HEIGHT: 18px">
								<span style='BACKGROUND: url(/images/flow2.gif); WIDTH: 100%'>&nbsp;</span>
							</span>&nbsp;已流转时间
							<span
								style="BORDER-RIGHT: black 1px solid; PADDING-RIGHT: 1px; BORDER-TOP: black 1px solid; PADDING-LEFT: 1px; PADDING-BOTTOM: 1px; BORDER-LEFT: black 1px solid; WIDTH: 12px; PADDING-TOP: 1px; BORDER-BOTTOM: black 1px solid; HEIGHT: 18px">
								<span style='BACKGROUND: url(/images/flow3.gif); WIDTH: 100%'>&nbsp;</span>
							</span>&nbsp;超期时间
						</td>
					</tr>
					<tr>
						<td>
							<%
							    DataTable TblPercent = (DataTable) request.getAttribute("TblPercent");
							        if (TblPercent.isVisible()) {
							%>
							<!--  asp:table id="TblPercent" runat="server" BorderWidth="0"
								Width="100%" cellpadding="0" cellspacing="0"></asp:table-->
							<%
							    }
							%>
							<br>
						</td>
					</tr>
				</table>
				<%
				    }
				%>
				<table id="tabView" border="0" style="border-width: 0px;">
					<tr>
						<td>
							<%=request.getAttribute("tabView") == null ? "" : request.getAttribute("tabView")%>
						</td>
					</tr>
				</table>
				<!--br>
				<%
				    //Boolean LblOver = (Boolean) request.getAttribute("LblOver");
				    //if (LblOver) {
				%>
				<asp:Label id="LblOver" Runat="server" ForeColor="#ff0000">超期节点列表</asp:Label>
				<%
				    //}
				%>
				<br>
				<br>
				<%
				    //DataTable TblOver = (DataTable) request.getAttribute("TblOver");
				    //if (TblOver.isVisible()) {
				%>
				<asp:table id="TblOver" runat="server" BorderWidth="1px"
					BorderStyle="Solid" BorderColor="#000000" Width="600"
					cellpadding="4" cellspacing="0">
					<asp:TableRow Height="25">
						<asp:TableCell Text="处理人员" Width="80" VerticalAlign="Middle"
							HorizontalAlign="Center" BorderColor="#000000" BorderWidth="1"
							BorderStyle="Solid"></asp:TableCell>
						<asp:TableCell Text="处理动作" Width="120" VerticalAlign="Middle"
							HorizontalAlign="Center" BorderColor="#000000" BorderWidth="1"
							BorderStyle="Solid"></asp:TableCell>
						<asp:TableCell Text="状态" Width="40" VerticalAlign="Middle"
							HorizontalAlign="Center" BorderColor="#000000" BorderWidth="1"
							BorderStyle="Solid"></asp:TableCell>
						<asp:TableCell Text="收件时间" Width="120" VerticalAlign="Middle"
							HorizontalAlign="Center" BorderColor="#000000" BorderWidth="1"
							BorderStyle="Solid"></asp:TableCell>
						<asp:TableCell Text="预计结束时间" Width="120" VerticalAlign="Middle"
							HorizontalAlign="Center" BorderColor="#000000" BorderWidth="1"
							BorderStyle="Solid"></asp:TableCell>
						<asp:TableCell Text="实际结束时间" Width="120" VerticalAlign="Middle"
							HorizontalAlign="Center" BorderColor="#000000" BorderWidth="1"
							BorderStyle="Solid"></asp:TableCell>
					</asp:TableRow>
				</asp:table>
				<%
				    //}
				%>
				<br-->
				<%
				    Boolean LblErr = (Boolean) request.getAttribute("LblErr");
				    if (LblErr) {
				%>
				<span id="LblErr" style="color: Red;">异常处理列表</span>
				<%
				    }
				%>
				<br>
				<br>
				<%
				    DataTable TblErr = (DataTable) request.getAttribute("TblErr");
				    if (TblErr.isVisible()) {
				%>
				<table id="TblErr" cellspacing="0" cellpadding="4"
					bordercolor="Black" border="0"
					style="border: 1px solid #000000; border-collapse: collapse;">
					<tr style="height: 25px;">
						<td align="Center" valign="Middle"
							style="width: 80px; border: 1px solid #000000;">
							操作类型
						</td>
						<td align="Center" valign="Middle"
							style="width: 80px; border: 1px solid #000000;">
							操作人
						</td>
						<td align="Center" valign="Middle"
							style="width: 80px; border: 1px solid #000000;">
							被操作人
						</td>
						<td align="Center" valign="Middle"
							style="width: 80px; border: 1px solid #000000;">
							处理环节
						</td>
						<td align="Center" valign="Middle"
							style="width: 150px; border: 1px solid #000000;">
							操作日期
						</td>
						<td align="Center" valign="Middle"
							style="width: 130px; border: 1px solid #000000;">
							退回原因
						</td>
					</tr>
					<%
					    for (dsoap.dsflow.model.DataRow row : TblErr.rows) {
					%>
					<tr height="20">
						<td align="center" style="border: 1px solid #000000;">
							<%=row.get("ACT")%>
						</td>
						<td align="center" style="border: 1px solid #000000;">
							<%=row.get("UNAME")%>
						</td>
						<td align="center" style="border: 1px solid #000000;">
							<%=row.get("ACTUNAME")%>
						</td>
						<td align="center" style="border: 1px solid #000000;">
							<%=row.get("ACTNAME")%>
						</td>
						<td align="center" style="border: 1px solid #000000;">
							<%=row.get("ACTTIME")%>
						</td>
						<td align="center" style="border: 1px solid #000000;"><%=row.get("BZ") == null ? "" : row.get("BZ")%></td>
					</tr>
					<%
					    }
					    }
					%>
				</table>
			</DIV>
			<br>
			<div align=center>
				<input type="button" name="aaa" value="切换视图" class="xpbuttonClass2"
					onclick="location.href='ViewFlow.aspx?info_id=<%=request.getParameter("id")%>'"
					style="display: none">
			</div>
		</form>
	</body>
</HTML>
