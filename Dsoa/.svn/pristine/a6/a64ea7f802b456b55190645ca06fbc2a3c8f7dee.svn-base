package dsoap.web.action;

import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class ShowFlowAction extends Action {
    private static final long serialVersionUID = -9073681757992744656L;

    @Override
    public String execute() throws Exception {
        super.execute();
        String TxtId = request.getParameter("id");
        request.setAttribute("TxtId", TxtId);
        if (request.getParameter("wf_id") != null) {
            // String TxtWF_ID = request.getParameter("wf_id");
            // Button1.Visible=true;
        } else {
            // Button1.Visible=false;
        }
        // request.setAttribute("LblJs", "<script language='javascript'>click2();</script>");
        return SUCCESS;
    }

}
