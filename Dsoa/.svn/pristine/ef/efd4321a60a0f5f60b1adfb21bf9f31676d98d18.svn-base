package dsoap.tools.webwork;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;

public class StringResultType extends StrutsResultSupport {
    private static final long serialVersionUID = -6334401233107466650L;
    private String contentTypeName;
    private String stringName = "";

    public StringResultType() {
        super();
    }

    public StringResultType(String location) {
        super(location);
    }

    @Override
    protected void doExecute(String str, ActionInvocation invocation) throws Exception {
        HttpServletResponse response = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);
        String contentType = conditionalParse(contentTypeName, invocation);
        if (contentType == null) {
            contentType = "text/plain; charset=UTF-8";
        }
        response.setContentType(contentType);
        PrintWriter out = response.getWriter();
        String result = (String) invocation.getStack().findValue(stringName);
        out.println(result);
        out.flush();
        out.close();
    }

    public String getContentTypeName() {
        return contentTypeName;
    }

    public void setContentTypeName(String contentTypeName) {
        this.contentTypeName = contentTypeName;
    }

    public String getStringName() {
        return stringName;
    }

    public void setStringName(String stringName) {
        this.stringName = stringName;
    }

}
