<%@ page language="java" pageEncoding="UTF-8"%>
<html>
	<head>
		<style>
		.flow_node{
			background: url(../images/flow_node.gif) no-repeat;
			padding-left:10px;
			color:#396c6d;
		}
		.flow_node_1{
			background: url(../images/flow_node1.gif) no-repeat 0px 8px;
			width:8px;
			height:4px;
			border:0px solid #ff0000;
			float:left;
			margin-top:-4px;
		}
		.flow_node_2{
			background: url(../images/flow_node1.gif) no-repeat 0px -8px;
			width:8px;
			height:4px;
			border:0px solid #ff0000;
			float:left;
			margin-top:4px;
		}
		.flow_node_3{
			border:0px solid #00ff00;
			float:left;
		}
</style>
	</head>
	<body style="margin: 0px; border: 0px solid #ff0000; width: 100%;">
		<%
		    java.util.List<String> t = (java.util.List<String>) request.getAttribute("itemList");
		    for (String i : t) {
		%>
		<div><%=i%></div>
		<%
		    }
		%>
	</body>
</html>
