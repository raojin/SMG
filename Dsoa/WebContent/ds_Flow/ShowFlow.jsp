<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>流转情况</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<LINK href="../css/inter.css" rel="stylesheet">
		<script language="javascript">
		function click1(){
			//document.all.Td1.className="tab_down";
			document.all.Td2.className="tab";
			document.all.Td3.className="tab";
			document.all.Ifra1.src="ShowFlow2.action?id="+document.Form1.TxtId.value+"&wf_id="+document.Form1.TxtWF_ID.value;
		}
		function click2(){
			//document.all.Td1.className="tab";
			document.all.Td2.className="tab_down";
			document.all.Td3.className="tab";
			document.all.Ifra1.src="ViewFlow1.action?info_id="+document.Form1.TxtId.value+"&wf_id="+document.Form1.TxtWF_ID.value;
		}
		function click3(){
			//document.all.Td1.className="tab";
			document.all.Td2.className="tab";
			document.all.Td3.className="tab_down";
			document.all.Ifra1.src="FormStatus.action?id="+document.Form1.TxtId.value+"&wf_id="+document.Form1.TxtWF_ID.value+"&formStatus=1";
			//document.all.Ifra1.src="ViewFlow.action?info_id="+document.Form1.TxtId.value+"&wf_id="+document.Form1.TxtWF_ID.value; //这个是节点控件图
		}
		window.onload = function(){
			click2();
			try{
				var win = top.window.sendFormWin;
				/**
				win.element.dialog('option', 'height', window.screen.availHeight-150);
				win.element.dialog('option', 'width', window.screen.availWidth-100);
				win.element.dialog('option', 'position', "center");
				win.setWH();
				*/
				
				win.setWH({
					height:window.screen.availHeight-150,
					width:window.screen.availWidth-100
				});
				
			}catch(e){}
		}
		</script>
	</HEAD>
	<body>
		<form id="Form1" name="Form1" method="post">
			<input id="TxtId" type="hidden" name="TxtId"
				value="${requestScope.TxtId}">
			<input id="TxtWF_ID" type="hidden" name="TxtWF_ID" value="0">
			<table height="100%" cellSpacing="0" cellPadding="0" width="100%"
				border="0">
				<tr>
					<td align="center">
						<fieldset style="POSITION: static; HEIGHT: 92%">
							<legend>
								流转情况
							</legend>
							<table height="100%" cellSpacing="0" cellPadding="5" width="100%"
								align="center" border="0">
								<tr>
									<td>
										<table height="100%" cellSpacing="0" cellPadding="0"
											width="100%" border="0">
											<tr>
												<td height="20">
													<table cellSpacing="0" cellPadding="5" width="100%"
														border="0">
														<tr>
															<td id="Td2" width="33%" onclick="javascript:click2();"
																align="center" height="20">
																流程列表
															</td>
															<!-- td id="Td1" width="33%" onclick="javascript:click1();"
																align="center" height="20">
																顺序图
															</td -->
															<td id="Td3" width="34%" onclick="javascript:click3();"
																align="center" height="20">
																流程图
															</td>
														</tr>
													</table>
												</td>
											</tr>
											<tr>
												<td>
													<table class="list_all" height="100%" cellSpacing="0"
														cellPadding="0" width="100%" border="0">
														<tr>
															<td>
																<iframe id="Ifra1" frameBorder="0" width="100%"
																	height="100%"></iframe>
															</td>
														</tr>
													</table>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</fieldset>
					</td>
				</tr>
				<tr style="display:none;">
					<td height="1" align="center">
						<INPUT class="xpbuttonClass2" type="button" value="关 闭"
							onclick="javascript:parent.window.formHeaderWin.hide();">
					</td>
				</tr>
			</table>
		</form>
	</body>
</HTML>
