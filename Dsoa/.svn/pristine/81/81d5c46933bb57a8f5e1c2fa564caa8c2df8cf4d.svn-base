package dsoap.web.action;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.dsflow.model.DataTable;
import dsoap.tools.webwork.Action;

@SuppressWarnings("serial")
public class SelectCurNodeAction extends Action {

    public String execute() throws Exception {
        super.execute();
        Object obj = session.get("DSFLOW");
        if (obj == null) {
            throw new Exception("流程信息错误！");
        }
        DS_FLOWClass dsFlow = (DS_FLOWClass) obj;
        String toId = request.getParameter("id");
        if (toId != null) {
            if (dsFlow.iSendType >= 10) {
                dsFlow.setSelectNodeID_P(Long.parseLong(toId));
            } else {
                dsFlow.setSelectCurNodeID(Long.parseLong(toId));
            }
            if (dsFlow.iSendType == 9) {// 增发办结节点
                dsFlow.iSendType = 39;
                return "SendResult";
            }else{
                return "SelectUser";// 增发普通节点
            }
        } else {
            if(dsFlow.isEnd()){
                return "endErr";
            }
            String ids = dsFlow.getTSNodeList();
            if (ids == null || "".equals(ids)) {
                throw new Exception("流程增发节点配置信息错误！");
            }
            String[] idArr = ids.split(",");
            String whereIn = "";
            for (int i = 0; i < idArr.length; i++) {
                if (!"".equals(idArr[i])) {
                    whereIn += idArr[i] + ",";
                }
            }
            whereIn = whereIn.substring(0, whereIn.length() - 1);
            DataTable dt = null;
            // 显示待选节点
            if (dsFlow.iSendType >= 10) {
                // 显示父流程节点
                dt = dsFlow.ds_ParentFlow.getSelectCurNodesViewTable(whereIn);
            } else {
                dt = dsFlow.getSelectCurNodesViewTable(whereIn);
            }
            request.setAttribute("nextNodes", dt);
        }
        return "SelectNode";
    }

}
