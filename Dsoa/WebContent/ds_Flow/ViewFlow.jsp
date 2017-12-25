<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" >
<HTML>
	<head>
		<title>流转情况</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<LINK href="../css/main.css" type="text/css" rel="stylesheet">
	</head>
	<body>
		<form id="Form1" method="post">
			<table id="AutoNumber2"
				style="WIDTH: 100%; BORDER-COLLAPSE: collapse; HEIGHT: 100%"
				borderColor="#111111" height="100%" cellSpacing="0" cellPadding="0"
				border="1">
				<tr>
					<td style="HEIGHT: 30px" align="center" colSpan="2"><%=request.getAttribute("labName").toString()%></td>
				</tr>
				<tr>
					<td width="62%">
						<table id="AutoNumber1"
							style="WIDTH: 100%; BORDER-COLLAPSE: collapse; HEIGHT: 100%"
							borderColor="#111111" height="100%" cellSpacing="0"
							cellPadding="0" border="1">
							<tr>
								<td height="90%">
									<%=request.getAttribute("dsPanel").toString()%>
								</td>
							</tr>
							<TR>
								<TD vAlign="middle" align="center">
									<table id="table1"
										style="WIDTH: 100%; BORDER-COLLAPSE: collapse; HEIGHT: 100%"
										borderColor="#111111" height="100%" cellSpacing="0"
										cellPadding="0" border="0">
										<tr>
											<td align="center" width="20%">
												<img id="Image7" Width="32px" src="<%=request.getContextPath() %>/images/FlowIcon/2.jpg"
													Height="32px"></img>
											</td>
											<TD align="center" width="20%">
												<img id="Image8" Width="32px" src="<%=request.getContextPath() %>/images/FlowIcon/3.jpg"
													Height="32px"></img>
											</TD>
											<TD align="center" width="20%">
												<img id="Image9" Width="32px" src="<%=request.getContextPath() %>/images/FlowIcon/1.jpg"
													Height="32px"></img>
											</TD>
										</tr>
										<TR>
											<TD align="center" width="20%">
												已办节点
											</TD>
											<TD align="center" width="20%">
												未办节点
											</TD>
											<TD align="center" width="20%">
												当前节点
											</TD>
										</TR>
									</table>
								</TD>
							</TR>
						</table>
					</td>
				</tr>
			</table>
			<%=request.getAttribute("labScript").toString()%>
			<SCRIPT language="javascript" event="ClickSubFlowNode" for="DS_Pane">
				top.location.href = "ShowFlow.action?id=<%=request.getParameter("info_id")%>&wf_id="+DS_Pane.SelectedSubFLowNodeID+"";
			</SCRIPT>
		</form>
	</body>
</HTML>
