<%@ page language="java" pageEncoding="UTF-8"%>
<%
session.setAttribute("sendBackUrl", request.getParameter("backurl"));
String queryStr = request.getQueryString();
queryStr = new String(queryStr.getBytes("ISO-8859-1"),"UTF-8");
//增加批量发送 流程参数 POST提交参数的中转  杨龙修改 2013/1/8 开始
if(request.getParameter("batchFlowXML") != null)
{
    session.setAttribute("batchFlowXML",request.getParameter("batchFlowXML"));
}
//解决批量选人后正常发送seesion清空batchFlowXML--lvxd--20140225
else
{
	session.setAttribute("batchFlowXML","");
}
//-----------结束--------------------------------------
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>获取后续节点</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link type="text/css" rel="stylesheet" href="../css/main.css">
		<script language="javascript">
	//设置目标窗口
	function GoPageOne(){
		//var srcForm = window.dialogArguments;
		var url = "<%=request.getParameter("Page") + "?" + queryStr%>";
		window.frames["mainFrm"].location.href = url;
		window.clearInterval(timeGoPage); 
	}
	//选择关闭按钮后所完成的工作
	function setReturnValue(){
		var ret = "OK";
		try{
			ret = mainFrm.document.forms[0].txtRetualValue.value;
		}catch(e){
			ret="OK";
		} 
		window.returnValue = ret;
	}
	window.onload = function(){
		window.timeGoPage = window.setInterval(GoPageOne,10);
		try{
			var win = top.window.sendFormWin;
			win.setTitle("请选择您要发送的环节");
			win.setWH();
		}catch(e){}
	}
		</script>
	</HEAD>
	<frameset id="frmTop" rows="0,100%"
		onunload="javascript:setReturnValue();">
		<frame name="headerFrm" src="" scrolling="no" noresize frameborder="0">
		<frame name="mainFrm" src="<%=request.getContextPath()%>/start.htm" frameborder="0" scrolling="no"
			noresize>
		<noframes>
			<p id="pFrameInfo">
				此 HTML 框架集显示多个 Web 页。若要查看此框架集，请使用支持 HTML 4.0 及更高版本的 Web 浏览器。
			</p>
		</noframes>
	</frameset>
</HTML>
