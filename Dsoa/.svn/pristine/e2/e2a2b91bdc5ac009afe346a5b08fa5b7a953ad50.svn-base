<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>请选择后续操作</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link type="text/css" rel="stylesheet" href="../css/main.css">
		<link type="text/css" rel="stylesheet" href="../css/SOA.css">
		<script language="javascript">
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
	function showuser()
	{
		var FlowParms="<Root><Flow><Type>0</Type><Key>1381903840189000</Key><Objclass>717112</Objclass><UserID>12</UserID><Pid>199592291</Pid><Pnid>3</Pnid><WfID>944</WfID></Flow></Root>";
		window.location.href="AddSend.action?FlowParms="+FlowParms;
	}
		</script>
	</HEAD>
	
	<body background="../images/bg_body_right.gif"
		style="border: 0px solid #0000ff; margin: 0px;">
		<form id="SuccessList"
			style="border: 0px solid #00ff00; padding-top: 15px; font-size: 12px; padding-left: 15px; width: 338px;">
			<div id="TabCaption" style="width: 100%; font-size: 14px;">
			
			</div>
			<div style="width: 100%; font-size: 14px; padding: 15 0 20 0px;">
				<b>请选择后续操作</b>
			</div>
			<table cellSpacing="0" cellPadding="0" width="100%" border="0"
				style="padding-top: 6px; border-top: 1px solid #DFDFDF; width: 100%;">
				<tr>
					<td align="right">
						<div id="subBu" onclick="showuser()" class="select_user_send">
							换人
						</div>
					</td>
					<td align="right">
						<div onclick="javascript:showuser();" class="select_user_send">
							补发
						</div>
					</td>
					<td align="right">
						<div onclick="javascript:closeWin();" class="select_user_send">
							关闭
						</div>
					</td>
				</tr>
			</table>
		</form>
	</body>
	
	
	
	
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
