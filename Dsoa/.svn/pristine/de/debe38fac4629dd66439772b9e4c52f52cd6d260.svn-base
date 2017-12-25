<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
   
    <link type="text/css" rel="stylesheet" href="../css/main.css">
	<link type="text/css" rel="stylesheet" href="../css/SOA.css">
	<style type="text/css">
	a {
				padding-left: 0px;
	}
	</style>
    
    <title>选择节点</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	
	<script type="text/javascript">
		function ckNode(nodeID)
		{
			showSending();
			document.getElementById("toNodeID").value=nodeID;
			document.getElementById("f1").submit();
		}
		
		function showSending() {
			sending.style.visibility="visible";
			cover.style.visibility="visible";
		}	
		function hideSending() {
			sending.style.visibility="hidden";
			cover.style.visibility="hidden";
		}	
		
		
		window.onload = function(){
			try{
				var win = top.window.sendFormWin;
				win.setTitle("请选择您当前的环节");
				var c = document.getElementById("f1");
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
				
			}catch(e){}
		}
		
	</script>
  </head>
  
  <body background="../images/bg_body_right.gif" topmargin="0" style="border: 0px solid #ff0000;">
    <%
   		//获取待办参数，传递到后续页面
    	String userID = request.getAttribute("userID").toString();
		Object objNodes = request.getAttribute("nodes");
		String infoID = request.getAttribute("infoID").toString();
		String wfID = request.getAttribute("wfID").toString();
		String userDeptID = request.getAttribute("userDeptID").toString();
		String objclass = request.getAttribute("objclass").toString();
		//可选节点的集合
       HashMap<String,String> mapNodes= (HashMap<String,String>)objNodes;
       Iterator keys=mapNodes.keySet().iterator();
     %>
     <form id="f1" method="post" action="<%=request.getContextPath() %>/ds_Flow/SendNodeTs.action" style="border: 0px solid #00ff00;">
     	<input name="userID" id="userID" type="hidden" value="<%=userID %>">
     	<input name="infoID" id="infoID" type="hidden" value="<%=infoID %>">
     	<input name="wfID" id="wfID" type="hidden" value="<%=wfID %>">
     	<input name="userDeptID" id="userDeptID" type="hidden" value="<%=userDeptID %>">
     	<input name="objclass" id="objclass" type="hidden" value="<%=objclass %>">
     	<input name="toNodeID" id="toNodeID" type="hidden" value="">
    
     
     <div id="sending"
				style="z-index: 10; padding: 0 20 0 20px; visibility: hidden; position: absolute;">
				<TABLE cellSpacing="0" cellPadding="0" width="100%" border="1">
					<TR>
						<TD bgColor="#ff9900">
							<TABLE height="70" cellSpacing="2" cellPadding="0" width="100%"
								border="0">
								<TR>
									<td align="center" bgColor="#eeeeee">
										正在获取列表，请稍候...
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
							
							<tr>
								<td style="font-family: 宋体; font-size: 12pt;height:30px;">
									请选择该文件所处的办理环节
								</td>
							</tr>
							
							<%
							   
							    int i = 0;
							    boolean isFirst = false;
							    while(keys.hasNext()){
							    	String strKey=keys.next().toString();
							    	String strValue=mapNodes.get(strKey).toString();
							%>
							
							<tr style='background-color: <%=(i % 2 == 0 ? "white" : "#F1F1F1")%>;'>
								<td style="font-family: 宋体; font-size: 14pt;">
									<a style="font-size: 14px;<%=isFirst ? "color:green;" : "font-weight: bold;"%>"
										href="javascript:ckNode(<%=strKey%>)"><%=strValue%></a>
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
