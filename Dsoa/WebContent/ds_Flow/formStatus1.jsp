<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="dsoap.tools.SysDataSource" %>
<%@ page import="xsf.data.IDataSource" %>
<%@ page import="xsf.data.Sql" %>
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
    StringBuilder message = new StringBuilder();
    xsf.data.DataTable dt = xsf.data.DBManager.getDataTable(sql);
    if (dt != null) {
        for (xsf.data.DataRow dr : dt.getRows()) {
            PROCESS_USER = dr.getString("UNAME");
            PROCESS_USER_ORG = dr.getString("ORGNAME");
            PROCESS_USER_ORG_TEL = dr.getString("TEL");
            
            if(count > 0){
            	message.append("、") ;
            }
            message.append(PROCESS_USER).append("（").append(PROCESS_USER_ORG).append("）") ;
            count++;
            //break;
        }
    }
    
    String othertext = "" ;
    if(count > 1){
    	othertext = "等" ;
    }
    String show = request.getParameter("formStatus");
%>
<div style="border: 0px solid #00ff00;">
	<div id="form_status_node_YJ" class="form_status_node_YJ"></div>
	<div class="form_status_view_bor">
		<span class="form_status_inner_left">当前状态：<%
		    if (count > 0) {//正在流转
		%>
		该申请单正在由<span class="form_status_color_red"
			onmouseover="javascript:try{com.syc.oa.formStatus.showUserInfo(this,<%=show%>);}catch(e){};"
			onmouseout="javascript:try{com.syc.oa.formStatus.hideUserInfo(this,<%=show%>);}catch(e){};">
			<%=PROCESS_USER_ORG == null ? "" : PROCESS_USER_ORG + "："%></span>
			<span  title="<%= message.toString()%>"
				class="form_status_color_red"
				onmouseover="javascript:try{com.syc.oa.formStatus.showUserInfo(this,<%=show%>);}catch(e){};"
				onmouseout="javascript:try{com.syc.oa.formStatus.hideUserInfo(this,<%=show%>);}catch(e){};">
				<%=PROCESS_USER == null ? "" : PROCESS_USER %>	
			</span><span><%=othertext %>办理</span></span>
		<%
		    if (show == null) {
		%>
		<span class="form_status_view" 
			onclick="javascript:try{com.syc.oa.formStatus.showFlow(this);}catch(e){};">
			显示流程 </span>
		<%
		    }
		    } else {//流转结束
		%>
		<span>该申请单</span>
		<span class="form_status_org">已处理完毕</span></span>
		<%
		    if (show == null) {
		%>
		<span class="form_status_view"
			onclick="javascript:try{com.syc.oa.formStatus.showFlow(this);}catch(e){};">显示流程</span>
		<%
		    }
		    }
		%>
	</div>
	<div style="position: absolute; display: none;">
		<table cellspacing='0' cellpadding='0' border='0'>
			<tr>
				<td class="form_status_user_info1"></td>
				<td class="form_status_user_info2">
					<div class="form_status_user_info_div3"><%=PROCESS_USER_ORG == null ? "" : PROCESS_USER_ORG%>&nbsp;&nbsp;<%=PROCESS_USER == null ? "" : PROCESS_USER%></div>
				</td>
				<td class="form_status_user_info4"></td>
				<td class="form_status_user_info2">
					<div class="form_status_user_info_div4" style="border: 0px;">
						电话：<%=PROCESS_USER_ORG_TEL == null ? "" : PROCESS_USER_ORG_TEL%></div>
				</td>
				<td class="form_status_user_info3"
					onclick="javascript:com.syc.oa.formStatus.hideUserInfo(this,<%=show%>);"></td>
			</tr>
		</table>
	</div>
	<div class="form_status_chart"></div>
</div>