<%@ page language="java" pageEncoding="UTF-8"%>
<%
    out.println(request.getAttribute("tabView") == null ? "" : request.getAttribute("tabView"));
    java.util.Map<String, String> nodesInfo = (java.util.Map<String, String>) request.getAttribute("nodesInfo");
    if (nodesInfo != null) {
        for (String key : nodesInfo.keySet()) {
            out.println(nodesInfo.get(key));
        }
    }
%>
