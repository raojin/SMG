<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="xsf.data.IDataSource"%>
<%@page import="dsoap.tools.SysDataSource"%>
<%@page import="xsf.data.Sql"%>
<%
    String info = request.getParameter("id") ;
    // taolb 2012.8.26  将原来程序 直接写SQL 换成了 通过读取配置文件来 
    //String _cmdStr = "SELECT A.*,C.NAME ORGNAME,D.NAME POSTNAME FROM (SELECT A.ID,A.NAME AS UNAME, A.MOBILE AS TEL FROM G_USERINFO A,G_INBOX B WHERE A.ID=B.USER_ID AND B.INFO_ID=" + request.getParameter("id") + ")A left join G_ORGUSER B on A.ID=B.USERINFOID left join G_ORGANIZE C on B.ORGANIZEID=C.ID LEFT JOIN G_ORGANIZEPOST D ON D.ORGANIZEID=C.ID LEFT JOIN G_USERORGANIZEPOST E ON E.USERID=A.ID";
    //String _cmdStr = "SELECT A.*,U.id,U.UNAME ORGNAME,L.LEVEL_NAME POSTNAME FROM (SELECT A.* FROM G_USERS A,G_INBOX B WHERE A.ID=B.USER_ID AND B.INFO_ID=" + request.getParameter("id") + ")A LEFT JOIN G_LEVEL L ON (A.ULEVEL=L.LEVEL_ID) LEFT JOIN G_DEPT D ON (A.ID=D.USER_ID) LEFT JOIN G_DEPT D1 ON (D1.ID=D.FID) LEFT JOIN G_USERS U ON (U.ID=D1.USER_ID)";
    
    IDataSource sqlDataSource = SysDataSource.getSysDataSource();
    sqlDataSource.setParameter("INFO_ID", info);
    Sql sql = (Sql) sqlDataSource.getSelectCommands().get("getCurrentNodeUserInfo");
  
    String PROCESS_USER = "";
    String PROCESS_USER_ORG = "";
    String PROCESS_USER_ORG_TEL = "";
    int count = 0;
    xsf.data.DataTable dt = xsf.data.DBManager.getDataTable(sql);
    if (dt != null) {
        for (xsf.data.DataRow dr : dt.getRows()) {
            PROCESS_USER = dr.getString("UNAME");
            PROCESS_USER_ORG = dr.getString("ORGNAME");
            PROCESS_USER_ORG_TEL = dr.getString("TEL");
            count++;
            break;
        }
    }
    String show = request.getParameter("formStatus");
%>
<html>
	<head>
		<style>
body {
	scrollbar-face-color: #d9d9d9;
	scrollbar-arrow-color: #ffffff;
	scrollbar-highlight-color: #ffffff;
	scrollbar-3dlight-color: #d9d9d9;
	scrollbar-shadow-color: #ffffff;
	scrollbar-darkshadow-color: #d9d9d9;
	scrollbar-track-color: #ffffff;
}

.form_status_user_info1 {
	background: url(../images/form_status1.gif) no-repeat;
	height: 47px;
	width: 3px;
	font-size: 12px;
	border: 0px solid #red;
}

.form_status_user_info2 {
	background: url(../images/form_status2.gif) repeat-x;
	height: 47px;
	font-size: 12px;
	border: 0px solid #red;
}

.form_status_user_info3 {
	background: url(../images/form_status3.gif) no-repeat;
	height: 47px;
	width: 16px;
	border: 0px solid #red;
	cursor: pointer;
}

.form_status_user_info4 {
	background: url(../images/form_status4.gif) no-repeat;
	height: 47px;
	width: 22px;
	height: 47px;
	border: 0px solid #red;
}

.form_status_user_info5 {
	height: 47px;
}

.form_status_user_info_div1 {
	font-size: 12px;
	margin-top: 10px;
	margin-left: 10px;
	border: 0px solid #red;
}

.form_status_user_info_div2 {
	font-size: 12px;
	margin-top: 8px;
	margin-right: 30px;
	border: 0px solid #red;
}

.form_status_user_info_div3 {
	font-size: 12px;
	margin-top: 0px;
	margin-left: 10px;
	border: 0px solid #red;
}

.form_status_user_info_div4 {
	font-size: 12px;
	margin-top: 0px;
	margin-right: 30px;
	border: 0px solid #red;
}

.form_status_table {
	font-size: 12px;
}

.form_status_dqzt {
	width: 100%;
	font-size: 12px;
	font-weight: bold;
	color: #989898;
	padding-left: 20px;
	padding-top: 2px;
	border: 0px solid #0000ff;
}

.form_status_view {
	color: #fe7201;
	font-size: 12px;
	font-weight: bold;
	text-decoration: underline;
	cursor: pointer;
}

.form_status_sp {
	font-size: 12px;
	padding-right: 30px;
	padding-left: 10px;
	border: 0px solid #ff0000;
}

.form_status_org {
	font-size: 12px;
	color: #2b69a7;
	padding-right: 10px;
	border: 0px solid #ff0000;
}

.form_status_user {
	font-size: 12px;
	color: #2b69a7;
	border: 0px solid #ff0000;
}

.form_status_node_info {
	background: url(../images/showNodeInfo.gif) no-repeat;
	height: 15px;
	width: 15px;
	border: 0px solid #red;
	cursor: pointer;
}

.form_status_node_info1 {
	background: url(../images/hideNodeInfo.gif) no-repeat;
	height: 15px;
	width: 15px;
	border: 0px solid #red;
	cursor: pointer;
}

.form_status_node_YJ {
	z-index: 200;
	position: absolute;
	width: 160px;
	border: 0px solid #ff0000;
	position:absolute; 
	height:160px; 
	overflow: auto;
}

.form_status_node_YJ1 {
	width: 100%;
	white-space: normal;
	overflow: auto;
	border: 1px solid #f9b620;
	font-size: 12px;
	padding: 5px;
	word-break: break-all;
}

.form_status_node_YJ2 {
	background: url(../images/talk.gif) no-repeat;
}

a {
	color: #83bfe6;
}
</style>
		<script src="../js/jquery/jquery.js"></script>
		<script src="../js/formStatus.js"></script>
	</head>
	<body style="margin: 0px; border: 0px solid #ff0000; width: 100%;">
		<div id="form_status_node_YJ" class="form_status_node_YJ"></div>
		<div class="form_status_dqzt">
			当前状态:
		</div>
		<div style="border: 0px solid #00ff00; padding-left: 78px;">
			<%
			    if (count > 0) {
			%>
			<span style="font-size: 12px; border: 0px solid #ff0000;">
				该申请单正在由</span><span class="form_status_org"
				onmouseover="javascript:try{com.syc.oa.formStatus.showUserInfo(this,1);}catch(e){};"
				onmouseout="javascript:try{com.syc.oa.formStatus.hideUserInfo(this,1);}catch(e){};"><%=PROCESS_USER_ORG == null ? "" : PROCESS_USER_ORG%></span><span
				class="form_status_user"
				onmouseover="javascript:try{com.syc.oa.formStatus.showUserInfo(this,1);}catch(e){};"
				onmouseout="javascript:try{com.syc.oa.formStatus.hideUserInfo(this,1);}catch(e){};"><%=PROCESS_USER == null ? "" : PROCESS_USER%></span><span
				class="form_status_sp">审批</span>
			<%
			    } else {
			%>
			<span style="font-size: 12px;">该申请单</span>
			<span class="form_status_org">已处理完毕</span>
			<%
			    }
			%>
		</div>
		<div class="form_status_chart">
			<%
			    out.println(request.getAttribute("tabView") == null ? "" : request.getAttribute("tabView"));
			    java.util.Map<String, String> nodesInfo = (java.util.Map<String, String>) request.getAttribute("nodesInfo");
			    if (nodesInfo != null) {
			        for (String key : nodesInfo.keySet()) {
			            out.println(nodesInfo.get(key));
			        }
			    }
			%>
		</div>
	</body>
</html>
