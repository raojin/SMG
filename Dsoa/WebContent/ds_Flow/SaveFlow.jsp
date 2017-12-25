<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
    String path = request.getServletPath();
    System.out.println(path);
%>
<html>
	<head>
		<title>流程显示</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<LINK href="../css/main.css" type="text/css" rel="stylesheet">
	</HEAD>
	<body bottomMargin="0" topMargin="0" MS_POSITIONING="GridLayout">
		<table id="AutoNumber1"
			style="WIDTH: 100%; BORDER-COLLAPSE: collapse; HEIGHT: 99%"
			borderColor="#111111" height="99%" cellSpacing="0" cellPadding="0"
			border="1">
			<tr>
				<td height="90%">
					<OBJECT id="DS_Pane" style="WIDTH: 100%; HEIGHT: 100%"
						codeBase="DS_WF_UI.CAB#version=2,4,0,0" height="100%" width="100%"
						classid="CLSID:300BF8C7-1570-4DD3-86DC-D62C98A9E020" VIEWASTEXT>
						<PARAM NAME="_ExtentX" VALUE="23495">
						<PARAM NAME="_ExtentY" VALUE="18812">
					</OBJECT>
				</td>
			</tr>
			<tr>
				<td vAlign="middle" align="center">
					<table id="table1"
						style="WIDTH: 100%; BORDER-COLLAPSE: collapse; HEIGHT: 100%"
						borderColor="#111111" height="100%" cellSpacing="0"
						cellPadding="0" border="0">
						<tr>
							<td width="16%" align="center">
								<img id="Image7" Width="32px" Height="32px"
									src="<%=request.getContextPath()%>/images/FlowIcon/2.jpg" />
							</td>
							<TD align="center" width="12%">
								<img id="Image8" Width="32px" Height="32px"
									src="<%=request.getContextPath()%>/images/FlowIcon/3.jpg" />
							</TD>
							<TD align="center" width="12%">
								<img id="Image9" Width="32px" Height="32px"
									src="<%=request.getContextPath()%>/images/FlowIcon/4.jpg" />
							</TD>
							<TD align="center" width="12%">
								<img id="Image1"
									src="<%=request.getContextPath()%>/images/FlowIcon/6.jpg"
									Height="32px" Width="32px" />
							</TD>
							<TD align="center" width="12%">
								<img id="Image2"
									src="<%=request.getContextPath()%>/images/FlowIcon/1.jpg"
									Height="32px" Width="32px" />
							</TD>
							<TD align="center" width="12%">
								<img id="Image2"
									src="<%=request.getContextPath()%>/images/FlowIcon/7.gif"
									Height="32px" Width="31px" />
							</TD>
							<TD align="center" width="12%">
								<img id="Image2"
									src="<%=request.getContextPath()%>/images/FlowIcon/8.gif"
									Height="32px" Width="31px" />
							</TD>
						</tr>
						<TR>
							<TD align="center" width="12%">
								开始节点
							</TD>
							<TD align="center" width="12%">
								一般节点
							</TD>
							<TD align="center" width="12%">
								远程节点
							</TD>
							<TD align="center" width="12%">
								汇总节点
							</TD>
							<TD align="center" width="12%">
								结束节点
							</TD>
							<TD align="center" width="12%">
								文件汇总节点
							</TD>
							<TD align="center" width="12%">
								文件抢办节点
							</TD>
						</TR>
					</table>
				</td>
			</tr>
		</table>
		${requestScope.LblScript}
	</body>
</HTML>

