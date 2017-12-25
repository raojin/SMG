<%@ page language="java" pageEncoding="UTF-8"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<base href="<%=basePath%>">
		<title>人员列表</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<link href="<%=request.getContextPath()%>/css/main.css" type="text/css" rel="stylesheet" />
		<link href="<%=request.getContextPath()%>/My97DatePicker/skin/WdatePicker.css" type="text/css" rel="stylesheet" />
		<script language="JavaScript" type="text/javascript" src="<%=request.getContextPath()%>/js/jquery/jquery.js"></script>
		<script language="JavaScript" type="text/javascript" src="<%=request.getContextPath()%>/My97DatePicker/WdatePicker.js"></script>
		<script language="javascript">
		function del(strdel){
			if(strdel.split(":").length<9 || <%=dsoap.tools.ConfigurationSettings.removeAble==true%>){
				//必选的节点不能 删除  杨龙修改 2012/9/18
				if(strdel.indexOf('必选')<0)
				{
					parent.SendUserList.UList.value = parent.SendUserList.UList.value.replace(strdel,"");
					//location.href="SelectUserList.jsp";
					window.location.reload();
				}
			}
		}
		</script>
		<style>
			.delete{
				background:url(<%=request.getContextPath()%>/img/delete.png) no-repeat;
				width: 16px;
				height: 16px;
				display: inline-block;
				cursor: pointer;
				vertical-align: -3px;
			}
		</style>
		<script language="javascript">
				$(function(){
					var userlist = parent.SendUserList.UList.value.split(";");
					index = 0;
					$("#Form1").html("");
					var html ="<table cellspacing=\"0\" cellpadding=\"0.5\" rules=\"cols\" bordercolor=\"#DEDFDE\" border=\"1\" style=\"border-color:#DEDFDE;border-width:1px;border-style:solid;width:100%;border-collapse:collapse;BORDER-RIGHT:#dedfde 1px; BORDER-TOP:#dedfde 1px; BORDER-LEFT:#dedfde 1px; COLOR:black; BORDER-BOTTOM:#dedfde 0px; BORDER-COLLAPSE:collapse; BACKGROUND-COLOR:white\">";
					html = html + "<tr class='line_title_right' height=45 style='COLOR:white;'>";
					html = html + "<td align=center>办理动作</td>";
					html = html + "<td width=100 align=center>办理部门</td>";
					html = html + "<td width=75 align=center>办理人员</td>";
					html = html + "<td width=110 align=center>办理时限</td>";
					html = html + "<td width=50 align=center>操作</td>";
					html = html + "</tr>";
					for(var i=0;i<userlist.length;i++){
						var s = userlist[i];
						if(s == ""){
							continue;
						}
						var slist = s.split(":");
						//0:sNodeID 1:sUserID 2:sNodeCaption 3:sfName 4:sUserName
						if (eval(index) % 2 == 0){
							html = html + "<tr  id=\"usr_"+slist[1]+"_"+slist[2]+"\" style=\"CURSOR: hand;background-color:#F4F6F6;\" height=20 align=center valign=middle >";
						}else{
							html = html + "<tr  id=\"usr_"+slist[1]+"_"+slist[2]+"\" style=\"CURSOR: hand;background-color:white;\" height=20 align=center valign=middle >";
						}
						html = html + "<td>";
						html = html + slist[3];
						html = html + "</td>";
						html = html + "<td >";
						html = html + slist[4];
						html = html + "</td>";
						html = html + "<td>";
						// ---吴红亮 添加 开始
						slist[5]=slist[5].split("(")[0];
	            		// ---吴红亮 添加 结束
						html = html + slist[5];
						html = html + "</td>";
						html = html + "<td>";
						var qx = "";
						if(slist.length>7){
							qx = slist[7];
						}
						html = html + "<div><input id=\"blqx_"+slist[1]+"_"+slist[2]+"\" class=\"Wdate\" style=\"width: 100px;height: 20px; margin: 0 2px; cursor: pointer;\" readonly=\"readonly\" onfocus=\"WdatePicker()\" type=\"text\" value=\""+qx+"\"  /></div>";
						html = html + "</td>";
						html = html + "<td >";
						html = html + "<i style=\"margin-left:10px;\" onclick=\"del(';"+s+"')\" class=\"delete\"></i>";
						html = html + "</td>";
						html = html + "</tr>";
						html = html + "<tr>";
						html = html + "<td height=1 colspan=5 background=\"images/compartline.gif\">";
						html = html + "</td>";
						html = html + "</tr>";
						index = eval(index) + 1;
					}
					if (index != "0"){
						html = html + "<tr class='line_title_right' height=45 style='COLOR:white;'>";
						html = html + "<td colspan=5 align=right>";
						html = html + "共" + index + "人";
						html = html + "</td>";
						html = html + "</tr>";
					}
					html = html + "</Table>";	
					html = html + "<input type=hidden name=ucount value=\"" + index + "\">";
					$("#Form1").html(html);
				})
				
				window.onbeforeunload = function(){
					var ulist = parent.SendUserList.UList.value.split(";");
					var newulist = "";
					for(var i=0;i<ulist.length;i++){
						usr = ulist[i];
						param = usr.split(":");
						var blqx = "";
						if($("#blqx_"+param[1]+"_"+param[2]).length>0){
							blqx = $("#blqx_"+param[1]+"_"+param[2]).val();
						}
						var newstr="";
						for(var j=0;j<param.length;j++){
							if(j==0){
								newstr+=param[j];
							}else if(j==7){
								newstr+=":"+blqx;
							}else{
								newstr+=":"+param[j];
							}
						}
						
						if(i==0){
							newulist+=newstr;
						}else{
							newulist+=";"+newstr;
						}
					}
					parent.SendUserList.UList.value = newulist;
				} 
		</script>
	</head>
	<body style="margin: 0px; border: 0px solid #ff0000;">
		<form id="Form1" method="post">
			
		</form>
	</body>
</html>
