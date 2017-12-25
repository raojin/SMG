<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="dsoap.dsflow.DS_FLOWClass"%>
<%@page import="java.net.URLDecoder"%>
<%
	request.setCharacterEncoding("UTF-8");
	try {
		// 获取发送参数，创建发送对象
		if (request.getParameter("flowParms") != null) {
			String flowParms = URLDecoder.decode(request
					.getParameter("flowParms"), "utf-8");
			//发送参数格式：<Root><Flow><Type>0</Type><Key>1346727307282000</Key><Objclass>651059</Objclass><UserID>737911</UserID><Pid>199592208</Pid><Pnid>2</Pnid><WfID>844</WfID></Flow></Root>
			DS_FLOWClass dsFlow = new DS_FLOWClass(flowParms);
			//System.out.println("提交类型:" + dsFlow.iSendType);
			//将后续可发送的节点和人员信息的XML输出到页面 
			out.print(dsFlow.NextNodeInfoXml.asXML());
			//System.out.println(dsFlow.NextNodeInfoXml.asXML());
			//request.setAttribute("usersXML", dsFlow.NextNodeInfoXml.asXML());

		} else {
			out.print("流程信息错误。");
		}
	} catch (Exception ex) {
		out.print("流程信息错误。");
		ex.printStackTrace();
	}
%>
