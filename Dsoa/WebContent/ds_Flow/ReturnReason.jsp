<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>退回原因</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<link href="../css/main.css" rel=stylesheet>
	</head>
	<body background="../images/bg_body_right.gif" topmargin="0"
		style="border: 0px solid #ff0000;">
		<center>
			<form id="From1" name="From1" action="ReturnInBox.action"
				method="post" onsubmit="javascript:return doit();">
				<fieldset>
					<legend>
						退回原因
					</legend>
					<table width="90%" align=center>
						<tr>
							<td>
								<textarea name="TxtReason" rows="12" id="TxtReason" ${requestScope.TxtReason.readOnly==true?" readonly":""}
									style="width: 100%;">${requestScope.TxtReason.text}</textarea>
								<input type="hidden" name="pid" value="${requestScope.pid}" />
								<input type="hidden" name="pnid" value="${requestScope.pnid}" />
								<input type="hidden" name="info_id"
									value="${requestScope.info_id}" />
								<input type="hidden" name="isall" value="${requestScope.isall}" />
								<input type="hidden" name="fpnid" value="${requestScope.fpnid}" />
								<input type="hidden" name="userid"
									value="${requestScope.userid}" />
								<input type="hidden" name="isth" value="${requestScope.isth}" />
							</td>
						</tr>
						<tr>
							<td vAlign=bottom align=center height=40>
								<input class="xpbuttonclass2" name="BtnOk" id="BtnOk" style='display:${requestScope.BtnOk.visible==true?"inline":"none"}'
									type="submit" value="确  定" />
								<input class="xpbuttonclass2" onclick='javascript:top.window.sendFormWin.hide();'
									type="button" value="取  消">
							</td>
						</tr>
					</table>
				</fieldset>
			</form>
		</center>
		<script language="javascript">
	function doit(){
		var strReason = document.all("TxtReason").value
		if(strReason == ""){
			alert("请输入退回原因！")
			return false;
		}
		return true;
	}
</script>
	</body>
</html>
