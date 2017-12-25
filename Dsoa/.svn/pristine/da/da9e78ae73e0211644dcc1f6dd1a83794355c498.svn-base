<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>注册页面</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">
		<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
		<meta http-equiv="description" content="This is my page">
		<script type="text/javascript">
	function RequestByGet(data){
        var xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");    
        //Webservice location.   
        var URL="http://192.168.1.12:8080/service/wfservice/GetRegCode";   
        xmlhttp.Open("GET",URL, false);    
        xmlhttp.SetRequestHeader ("Content-Type","text/xml; charset=utf-8");    
        xmlhttp.SetRequestHeader ("SOAPAction","http://192.168.1.12:8080/service/wfservice/GetRegCode");    
        xmlhttp.Send(data);
        var result = xmlhttp.status;
        //OK   
        if(result==200) {    
            document.write("注册码:" + xmlhttp.responseText);
            //alert(xmlhttp.responseText.getElementsByTagName("GetRegCodeResult")[0].childNodes[0].nodeValue);
           	//alert(xmlhttp.responseText);
        }else{
        	alert('请您先注册...');
        }
        //xmlhttp = null;
    }
    //test function with post method   
    function RequestByPost(value)   {   
    	if(document.getElementById("registCode").value == null || document.getElementById("registCode").value == ""){
    		alert("注册码不能为空...");
    		return;
    	}
        var data;   
        data = '<?xml version="1.0" encoding="utf-8"?>';    
        data = data + '<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">';    
        data = data + '<soap:Body>';    
        data = data + '<SetRegCode>';    
        data = data + '<sCode>'+value+'</sCode>';
        data = data + '</SetRegCode>';
        data = data + '</soap:Body>';
        data = data + '</soap:Envelope>';    
        var xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        var URL="http://192.168.1.12:8080/service/wfservice";
        xmlhttp.Open("POST",URL, false);
        xmlhttp.SetRequestHeader ("Content-Type","text/xml; charset=gb2312");
        xmlhttp.SetRequestHeader ("SOAPAction","http://192.168.1.12:8080/service/wfservice/SetRegCode");    
        xmlhttp.Send(data);
        document.write(xmlhttp.responseText);
        window.location.reload();
    } 
</script>
	</head>
	<body>
		<form id="From1" name="From1" action="Regist.action" method="post">
			<table>
				<tr>
					<td>
						请输入注册码:
					</td>
					<td>
						<input id="registCode" type="text" name="registCode" value="" />
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="button" value="注&nbsp;&nbsp;&nbsp;&nbsp;册"
							onclick="RequestByPost('45826156537611480028125042643')" />
						<input type="button" value="验证注册码" onclick="RequestByGet(null)" />
					</td>
				</tr>
				<tr>
					<td>
						<a href="http://192.168.1.12:8080/ds_Flow/ToSendNode.jsp?Page=Index.action&FlowParms=%253cRoot%253e%253cFlow%253e%253cType%253e0%253c%252fType%253e%253cKey%253e90013234%253c%252fKey%253e%253cObjclass%253eFW%253c%252fObjclass%253e%253cUserID%253e11%253c%252fUserID%253e%253cPid%253e90009650%253c%252fPid%253e%253cPnid%253e1%253c%252fPnid%253e%253cWfID%253e596%253c%252fWfID%253e%253c%252fFlow%253e%253c%252fRoot%253e">发送演示链接</a>
						<br>
						<br>
						<a href="http://192.168.1.12:8080/ds_Flow/SaveFlow.action?id=626">流程显示演示链接</a>
						<br>
						<br>
						<a href="http://192.168.1.12:8080/ds_Flow/ShowFlow.action?id=90007381&userid=11">流转情况演示链接</a>
						<br>
						<br>
						<a href="http://192.168.1.12:8080/ds_Flow/ToSendNode.jsp?Page=ReturnReason.action&FlowParms=1%26info_id%3d90013253%26pid%3d90009668%26pnid%3d2%26fpnid%3d1%26isall%3d0%26isth%3d1%26userid%3d73003950">回退演示链接</a>
						<br>
						<br>
						<a href="http://192.168.1.12:8080/ds_Flow/ReturnInBox.action?userid=11&info_id=90013253&pid=90009668&pnid=2&fpnid=1&isall=0">回收演示链接</a>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
