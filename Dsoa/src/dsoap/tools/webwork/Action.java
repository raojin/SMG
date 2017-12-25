package dsoap.tools.webwork;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class Action extends ActionSupport {
    private static final long serialVersionUID = 6760350488087709840L;
    public HttpServletRequest request = null;
    public HttpServletResponse response = null;
    public Map<String, Object> session = null;

    @Override
    public String execute() throws Exception {
        ActionContext ctx = ActionContext.getContext();
        request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
        response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
        // ServletActionContext.APPLICATION;
        session = ActionContext.getContext().getSession();
        return super.execute();
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public Map<String, Object> getSession() {
        return session;
    }

    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

}
