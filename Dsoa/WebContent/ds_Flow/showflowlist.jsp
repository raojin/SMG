<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	</head>
	<body style="margin: 0px;">
		<div style="padding-left: 20px;">
			<%
			    String fid = request.getParameter("fid");
			    //String pid = request.getParameter("pid");
			    String info_id = request.getParameter("infoId");
			    String sql = "select A.PID,A.ID,A.UNAME,A.WFNODE_CAPTION,A.SDATE,B.CONTENT,A.STATUS from G_PNODES A left join G_OPINION B on A.PID=B.PID and A.ID=B.PNID where A.INFO_ID=" + info_id + " and A.FID in(" + fid + ")";
			    java.sql.Connection _myConn = new dsoap.connection.JdbcConnection().getConnection();
			    java.sql.ResultSet _myDs1 = _myConn.createStatement().executeQuery(sql);
			    int i = 0;
			    while (_myDs1.next()) {
			        i++;
			        String nodeName = _myDs1.getString("WFNODE_CAPTION");
			        String uName = _myDs1.getString("UNAME");
			        String opinion = _myDs1.getString("CONTENT");
			        opinion = null == opinion || "null".equals(opinion) ? "" : opinion;
			        String sDateStr = _myDs1.getString("SDATE");
			        String status = _myDs1.getString("STATUS");
			        if (sDateStr != null && !"".equals(sDateStr)) {
			            java.util.Calendar cl = java.util.Calendar.getInstance(); // 时间
			            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-M-dd HH:mm");
			            cl.setTime(sdf.parse(sDateStr));
			            sDateStr = sDateStr == null ? "" : sdf.format(cl.getTime());
			        } else {
			            sDateStr = "";
			        }
			        if (i == 1) {
			%>
			<div style="height: 30px;">
				<div style="font-size: 12px;">
					<span style="padding-right: 20px;">节点名称:</span><span><%=nodeName%></span>
				</div>
				<div>
					------------------------------------------------------------
				</div>
			</div>
			<%
			    }
			%>
			<div style="height: 30px;">
				<div style="font-size: 12px;">
					<span style="padding-right: 20px;">办理人员:</span><span><%=uName%></span>
				</div>
				<div style="font-size: 12px;">
					<span style="padding-right: 20px;">办理时间:</span><span><%=sDateStr%></span>
				</div>
				<%
				    if ("-1".equals(status)) {
				%>
				<div style="font-size: 12px;">
					<span style="padding-right: 20px;">意见:</span><span><%=null == opinion || "".equals(opinion) ? "已阅" : opinion%></span>
				</div>
				<%
				    }
				%>
				<div>
					------------------------------------------------------------
				</div>
			</div>
			<%
			    }
			%>
		</div>
	</body>
</html>
