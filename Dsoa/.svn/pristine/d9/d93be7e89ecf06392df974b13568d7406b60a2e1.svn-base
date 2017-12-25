package dsoap.web.action;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.dsflow.model.DataTable;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class SelectNodeAction extends Action {
    private static final long serialVersionUID = 6355442008391688088L;
    public String errStr = "";

    @Override
    public String execute() throws Exception {
        super.execute();
        if (session.get("DSFLOW") == null) {
            errStr = "<script language='javascript'>alert('流程信息错误！');top.window.close();</script>";
            return ERROR;
        }
        DS_FLOWClass dsFlow = (DS_FLOWClass) session.get("DSFLOW");
        try {
            if (request.getParameter("id") != null) {// 离开待选节点列表页
                if (dsFlow.iSendType >= 10) {
                    dsFlow.setSelectNodeID_P(Long.parseLong(request.getParameter("id")));
                } else {
                    dsFlow.setSelectNodeID(Long.parseLong(request.getParameter("id")));
                }
                return "index";
            } else {// 进入待选节点列表页
                DataTable dt = null;
                // 显示待选节点
                if (dsFlow.iSendType >= 10) {
                    // 显示父流程节点
                    dt = dsFlow.ds_ParentFlow.getSelectNodesViewTable();
                } else {
                    dt = dsFlow.getSelectNodesViewTable();
                }
                request.setAttribute("nextNodes", dt);
            }
        } catch (Exception e) {
            dsFlow.sErrorMessage = e.getMessage();
            e.printStackTrace();
            throw e;// error.jsp
        }
        return SUCCESS;
    }

}
