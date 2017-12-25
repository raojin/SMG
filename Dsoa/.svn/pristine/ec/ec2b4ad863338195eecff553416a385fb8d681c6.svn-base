<%
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    Object o = request.getAttribute("status");
    if (o == null) {
        String index = (String) request.getAttribute("index");
        response.getWriter().print(index);
    } else {
        response.getWriter().print("error:" + o.toString());
    }
%>
