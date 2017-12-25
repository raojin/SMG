<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="dsoap.dsflow.model.DataTable"%>
<%@page import="dsoap.dsflow.model.DataRow"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>文件流转情况列表图</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<LINK href="../css/main.css" type="text/css" rel="stylesheet">
		<script type="text/javascript" src="../js/jquery/jquery.js"></script>
		<style>
			<!-- 
			table{
			    table-layout:fixed;/* 只有定义了表格的布局算法为fixed，下面td的定义才能起作用。 */
			}
			table td {
				word-break: keep-all;
				white-space: nowrap;
				text-overflow: ellipsis;
				overflow: hidden;
			}
			.alink{
				color:blue;
				text-decoration: underline;
			}
			
			.titleHead{
				background-image: url("../images/popup_title.gif");
				background-repeat: repeat-x;
				background-color: transparent;
				height:30px;
				color:#fff;
				line-height: 30px;
				font-weight: bold;
			}
			
			.closeDiv{
				position:absolute;
				right:5px;
				top:7;
				width:auto;
				height:16px;
				overflow:hidden;
				line-height: 30px;
			}
			
			.closeImg{
				background-image: url("../images/popup_close.png");
				background-repeat: no-repeat;
				display: inline-block;
				vertical-align: top;
				height:16px;
				width:16px;
				margin:0px 0px 0px 2px;
			}
			
			.showTitle{
				width:300px;
				border:1px gray solid;
				display: none;
				background: #FFFFFF;
				position: absolute;
				z-index: 1000;
			}
			.titleBody{
				margin:5px 2px 5px 2px;
			}
			-->
		</style>
		<script type="text/javascript">
			$(function(){
				$("tr").attr("title","");//屏蔽原有的提示实现方式，暂不删除，待观察		

				$(".title").each(function(){//截取意见，根据长度只显示16个字
					var title = $(this).html() + "";
					var titleShow = title;
					var aLink = "<a class='alink' onclick='showTitle(this)' href='javascript:void()'>>>></a>";
					if(title.length > 20){
						titleShow = titleShow.substring(0,18);
						titleShow = titleShow + aLink;
					}
					$(this).html(titleShow);
					$(this).attr("allTitle",title);
				});
				
				$(".closeImg").click(function(){//意见弹出框关闭事件
					$("#showTitle").hide();
				});
			})
			
			function showTitle(obj){
				var tdObj = $(obj).parent();//获取td对象
				var title = $(tdObj).attr("allTitle");//获取文本信息
				$("#titleBody").html(title);//
				
				var offset = $(obj).offset();//定位
				var left = offset.left;
				var top = offset.top;
				$("#showTitle").css({"left":left,"top":top});
				$("#showTitle").show();//显示DIV
			}
		</script>
	</head>
	<body>
		<div class="showTitle" id="showTitle">
			<div class="titleHead">审批意见</div>
			<div class="closeDiv">
				<a class="closeImg"></a>
			</div>
			<div class="titleBody" id="titleBody"></div>
		</div>
		<form id="Form1" method="post">
			<div align="center">
				<table id="tbl" style="BORDER-COLLAPSE: collapse"
					borderColor="#000000" width="100%" border="0">
					<tr>
						<td align="center" width="100%" height="29">
							<b><font size="2">文件流转情况列表图</font> </b>
							<!-- <a
								href="<%=dsoap.tools.ConfigurationSettings.getServerInfo(null)%>/action?WorkFlow=&Action=export&infoId=<%=request.getParameter("info_id")%>">导出EXCEL
							</a>  -->
						</td>
					</tr>
					<tr>
						<td style="WIDTH: 100%">
							<table id="TblList" cellspacing="0" cellpadding="3" rules="all"
								bordercolor="Black" border="1"
								style="border-color: Black; border-width: 1px; border-style: solid; width: 100%; border-collapse: collapse; BORDER-COLLAPSE: collapse">
								<tr style="background-color: #B1E0FF;">
									<td align="Center" style="width: 50px;">
										序号
									</td>
									<td align="Center" style="width: 50px;">
										状态
									</td>
									<%--<td align="Center" style="width: 150px;">
										处理环节
									</td>--%>
									<td align="Center" style="width: 150px;">
										发送人
									</td>
									<td align="Center" style="width: 120px;">
										发送时间
									</td>
									<td align="Center">
										意见
									</td>
									<td align="Center" style="width: 200px;">
										接收人
									</td>
									<td align="Center" style="width: 120px;">
										阅读时间
									</td>
									<%--<td align="Center" style="width: 100px;">
										处理时间
									</td>--%>
								</tr>
								<%
								    DataTable dt = (DataTable) request.getAttribute("tbl");
								    for (DataRow dr : dt.rows) {
								        String userId = dr.get("USER_ID");
								        String muserId = dr.get("MUSER_ID");
								        String fsr = dr.get("FSR");
								        if (!userId.equals(muserId)) {
								            fsr += "（" + dr.get("DBR") + " 代）";
								        }
								%>
								<%--<tr title="<%=dr.get("title") != null ? dr.get("title") : ""%>"
									style='background-color: <%=dr.getBackColor()%>                  
								                 <%=dr.getForeColor() != null ? ";color:" + dr.getForeColor() : ""%><%=dr.getStyle() != null ? ";text-decoration:underline;" + dr.getStyle() : ""%>;'>
									--%>
								<tr title="<%=dr.get("title") != null ? dr.get("title") : ""%>"
									style='background-color: <%=dr.getBackColor()%>                   
								                  <%=dr.getForeColor() != null ? ";color:" + dr.getForeColor() : ""%><%=dr.getStyle() != null ? ";" + dr.getStyle() : ""%>;'>
									<td align="Center">
										<%=dr.get("ordinal")%>
									</td>
									<td align="Center">
										<%=dr.get("status")%>
									</td>
									<%--<td align="Center">
										<%=dr.get("ACTNAME")%>
									</td>--%>
									<td align="Center"><%=fsr%></td>
									<td align="Center">
										<%=dr.get("SDATE")%>
									</td>
									<td class="title"><%=dr.get("title") != null ? dr.get("title") : ""%></td>
									<td align="Center">
										<%=dr.get("USER") + "（" + dr.get("ACTNAME") + "）"%>
									</td>
									<td align="Center"><%=dr.get("PDATE")%></td>
									<%--
									<td align="Center">
										<%=dr.get("SDATE")%>
									</td>--%>
								</tr>
								<%
								    }
								%>
							</table>
						</td>
					</tr>
				</table>
			</div>
		</form>
	</body>
</HTML>
