<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%
	List users=(List)request.getAttribute("userlist");
	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
	<title>请选择退回人员并填写退回原因</title>
	<link href="<%=request.getContextPath()%>/css/form.css" type="text/css" rel="stylesheet">
		<link href="<%=request.getContextPath()%>/css/shell.css" type="text/css" rel="stylesheet">
		<script type="text/javascript" src="../js/jquery/jquery.js"></script>
		<script type="text/javascript" src="../js/jquery/_lib.js"></script>
		
	<script type="text/javascript">
	$(document).ready(function(){
  		initTemplateList();
	});
	function showTxt(txt)
	{
		$("#idContent").val($("#idContent").val()+txt);
	}
	function del(id)
	{
		var url="<%=request.getContextPath()%>/FlowJson/OpinionTemplateDel.action";
		var params="ID="+id;
		sendAjaxOpinion(url, params, function (value) {
			var data = eval('(' + value + ')'); 
			if(data.templateList.length>0)
				{
					$("#"+id).remove();
				} 
		}); 
	}
	function add()
	{
		var yy=$.trim($("#idContent").val());
			if(yy=="")
			{
				alert("请填写退回原因。");
				return;
			}
		var url="<%=request.getContextPath()%>/FlowJson/OpinionTemplateAdd.action";
		var params="content="+yy;
		sendAjaxOpinion(url, params, function (value) {
			var data = eval('(' + value + ')'); 
			if(data.templateList.length>0)
				{
					initTemplateList();
				} 
		}); 
	}
	function initTemplateList()
	{
		var url="<%=request.getContextPath()%>/FlowJson/OpinionTemplateList.action";
		var params="";
		sendAjaxOpinion(url, params, function (value) {
			var data = eval('(' + value + ')'); 
			if(data.templateList.length>0)
			{
			$("#divMainId").html("");
			for(i=0;i<data.templateList.length;i++)
			{
				var divhtml="";
				if (i%2 == 0) 
				{
					divhtml+="<div id='"+data.templateList[i].ID+"' class='divOpinionMessage' style='background-color: #C0CBFF;'>";
				}
				else
				{
					divhtml+="<div id='"+data.templateList[i].ID+"' class='divOpinionMessageWhite' style='background-color: #E9E9E9;'>";
				}
				divhtml+="<div class='divOpinionLeft' onclick=showTxt('"+data.templateList[i].CONTENT+"'); >"+data.templateList[i].CONTENT+"</div><div class='divOpinionRight'><button class='delOpinionButton' onclick='del("+data.templateList[i].ID+");'></button></div>";
				divhtml+="</div>";
				$("#divMainId").append(divhtml);
			}
			} 
		}); 
	}
	
	function sendAjaxOpinion(url, data, functionbk) {
	$.ajax({url:url, data:data || "", type:"POST", async:false, dataType:"xml", contentType:"application/x-www-form-urlencoded;charset=utf-8", complete:function (data, Status) {
		if (functionbk) {
			functionbk(data.responseText);
		}
	}});
	}
	
	function cleanContent(sernder){
	$("#idContent").val("");
	}
		function send()
		{
			var yy=$.trim($("#idContent").val());
			if(yy=="")
			{
				alert("请填写退回原因。");
				return;
			}
			var nodeid= document.getElementById("nodeid").value;
			var userid=document.getElementById("userid").value;
			var deptid=document.getElementById("deptid").value;
			if(nodeid=="")
			{
				alert("请选择要退回的人。");
				return false;
			}
			fm.submit();
		}
		function selectUser()
		{
			var user= document.all.ddlusers.value;
			var list= user.split(":");
			document.getElementById("nodeid").value=list[0];
			document.getElementById("userid").value=list[1];
			document.getElementById("deptid").value=list[2];
		}
	</script>
	
		<style type="text/css">
.divMain {
	width: 100%;
	height: 100%;
	margin: 0px;
	padding-top: 0px;
	margin-left: auto;
	margin-right: auto;
	position: relative;
	font-size: 14px;
	background: #E0EAF3;
}

.divHeader {
	font-size: 16px;
	padding-left: 10px;
	padding-top: 10px;
	padding-bottom: 10px;
	background: #A2C1DD;
}

.divMainContent {
	margin-left:20px;
	height: 300px;
	vertical-align: top;
	OVERFLOW-y: auto;
	background: white;
}

.divOpinionMessage {
	background: #E9E9E9;
	height: 30px;
}

.divOpinionMessageOver {
	background: #C0CBFF;
}

.divOpinionLeft {
	float: left;
	margin-left: 10px;
	width: 150px;
	margin-top: 8px;
	overflow: hidden;
	cursor: hand;
}

.divOpinionLeft input {
	width: 100%;
}

.divOpinionRight {
	float: right;
	padding-top: 5px;
	padding-right: 10px;
	text-align: right;
}

.editOpinionButton {
	width: 25px;
	height: 20px;
	BORDER-TOP-STYLE: none;
	BORDER-RIGHT-STYLE: none;
	BORDER-LEFT-STYLE: none;
	BORDER-BOTTOM-STYLE: none;
	background: url(resources/images/editor.gif) no-repeat left top;
	padding-right: 20px;
	cursor: hand;
	margin-top: auto;
	margin-bottom: auto;
}

.delOpinionButton {
	width: 25px;
	height: 20px;
	BORDER-TOP-STYLE: none;
	BORDER-RIGHT-STYLE: none;
	BORDER-LEFT-STYLE: none;
	BORDER-BOTTOM-STYLE: none;
	background: url(../images/del.gif) no-repeat left top;
	cursor: hand;
	padding-right: 20px;
	margin-top: auto;
	margin-bottom: auto;
}

.divOpinionMessageWhite {
	background: white;
	height: 30px;
}

.divMainBottom {
	margin: 0px;
	width: 100%;
	height: 30px;
	padding-top: 10px;
	background: white;
}
.divOperation {
	margin-top:25px;
	padding-bottom: 10px;
}
.divOpinionContent {
	border: 0px solid #00ff00;
	margin-bottom: 15px;
	height: 260px;
	background: #E0EAF3;
}

.divOpinionContent textarea {
	margin: 10px;
	width: 90%;
	height: 230px;
	font-size: 14px;
	color: #666666;
	border-top: 1px solid #aaadb2;
	border-right: 1px solid #dcdfe6;
	border-bottom: 1px solid #e2e9ef;
	border-left: 1px solid #e3e3eb;
	scrollbar-shadow-color: #ffffff;
	scrollbar-highlight-color: #ffffff;
	scrollbar-face-color: #d9d9d9;
	scrollbar-3dlight-color: #d9d9d9;
	scrollbar-darkshadow-color: #d9d9d9;
	scrollbar-track-color: #ffffff;
	scrollbar-arrow-color: #ffffff;
}
.headCaption{
padding-left: 5px;
padding-bottom: 5px;
font-size: 14px;
font-weight: bold;
}


</style>
	</head>
		<body style="margin: 0px; overflow: hidden;  border: 0px solid #ff0000;">
		<%out.print("<div style='display:none;'><input type='text' onchange='javascript:setVALUES(this);' /><input type='hidden' id='VALUES' /></div>");%>
		<table width="100%" cellpadding="0" cellspacing="0" border="0"> 
		<tr style="height: 30px;">
			<td class="headCaption" colspan="2">
				请选择退回人员：<select id="ddlusers" onchange="selectUser()">
				<%
				for (int i = 0; i < users.size(); i++) {
            	HashMap map = new HashMap<String, String>();
            	map=(HashMap)users.get(i);
            	String user=map.get("nodeid")+":"+map.get("userid")+":"+map.get("deptid");
            	out.print("<option  value='"+user+"'>"+map.get("username")+"("+map.get("nodename")+")</option>");
				}
				 %>
				 <option selected="selected">请选择</option>
			</select>
			</td>
		</tr>
			<tr>
				<td width="55%" class="headCaption">退回原因</td>
				<td  class="headCaption" style="padding-left:25px;">模板：</td>
			</tr>
			<tr>
				<td height="250" align="center" valign="top">
					<div class="divOpinionContent">
						<center>
							<form id="fm" action="ReturnNodesSend.action" method="post">
							<input type="hidden" id="nodeid" name="nodeid" value=""/>
							<input type="hidden" id="userid" name="userid" value=""/>
							<input type="hidden" id="deptid" name="deptid" value=""/>
							<textarea id="idContent" rows="300" name="idContent"></textarea>
							</form>
						</center>
					</div>
					<table width="100%" cellpadding="0" cellspacing="0" border="0">
					
					</table>
				</td>
				<td rowspan="1" valign="top">
					<div class="divMainContent" id="divMainId">
						
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="2" align="center">
					<div class="divOperation" >
						<input class="button2" type="button" value="清空"
							onclick="cleanContent(this)" />
						<span style="margin-left: 50px;">&nbsp;</span>
						<input class="button2" type="button" value="确定"
							onclick="send()" />
						<span style="margin-left: 50px;">&nbsp;</span>
						<input class="button4_ext" type="button" value="新增为模版"
							onclick="add()" />
					</div>
				</td>
			</tr>
		</table>
	</body>
	
</html>

