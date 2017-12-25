package dsoap.web.action;

import java.net.URLDecoder;

import com.opensymphony.xwork2.ActionContext;

import xsf.data.DBManager;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class AddSendAction extends Action {

    public String errStr = "";

    private DS_FLOWClass dsFlow;
    
    @Override
    public String execute() throws Exception {
        super.execute();
        try {
        	String iPid="";
            String iPnid="";
            String iG_InboxID="";
            String iInfoID="";
            String iObjclass="";
            String iWf_NodeID="";
            // 获取发送参数，创建发送对象
            if (request.getParameter("id") != null&&!"undefined".equals(request.getParameter("id"))) {
                String FlowParms = "";
                iG_InboxID=request.getParameter("id");
                //根据ID查询传入的G_INBOX数据
                xsf.data.DataTable dt = DBManager.getDataTable("select INFO_ID,OBJCLASS,USER_ID,PID,PNID,WF_ID,WFNODE_ID from G_INBOX where ID=" + iG_InboxID);
                if (dt.getRows().size() > 0) {
                    xsf.data.DataRow dr = dt.getRows().get(0);
                    iInfoID=dr.getString("INFO_ID");
                    iPid=dr.getString("PID");
                    iPnid=dr.getString("PNID");
                    iObjclass=dr.getString("OBJCLASS");
                    iWf_NodeID=dr.getString("WFNODE_ID");
                }
                //根据PID和PNID查出FID的父亲G_pnodes数据，使用父亲节点初始化流程，获取发送到当前节点的人员信息
                xsf.data.DataTable dt1 = DBManager.getDataTable("select t.INFO_ID,t.USER_ID,t.PID,t.ID PNID,t.WF_ID from g_pnodes t where t.pid="+iPid+" and  t.id = (select fid from g_pnodes where pid="+iPid+" and id= "+iPnid+")");
                if (dt1.getRows().size() > 0) {
                    xsf.data.DataRow dr = dt1.getRows().get(0);
                    FlowParms="<Root><Flow><Type>0</Type><Key>"+dr.getString("INFO_ID")+"</Key><Objclass>"+iObjclass+"</Objclass><UserID>"+dr.getString("USER_ID")+"</UserID><Pid>"+dr.getString("PID")+"</Pid><Pnid>"+dr.getString("PNID")+"</Pnid><WfID>"+dr.getString("WF_ID")+"</WfID></Flow></Root>";
                }
                session.put("isAddSend", "true");
                session.put("sendBackUrl", request.getParameter("backurl"));
                dsFlow = new DS_FLOWClass(FlowParms);
                session.remove("isAddSend");
                //将传入的G_INBOXID保存到流程该次文件的生命周期中
                dsFlow.customParameter.put("g_inboxID", iG_InboxID);
                dsFlow.customParameter.put("isAddSend", "true");
                session.put("DSFLOW", dsFlow);
            } else {
                if (session.get("DSFLOW") == null) {
                    errStr = "<script language='javascript'>window.alert('流程信息错误！');top.window.close();</script>";
                    return ERROR;
                }
                dsFlow = (DS_FLOWClass) session.get("DSFLOW");
            }
            dsFlow.setSelectNodeIDIsTS(Long.parseLong(iWf_NodeID));
            dsFlow.iSendType=1;
            System.out.println("提交类型:" + dsFlow.iSendType);
            switch ((int) dsFlow.iSendType) {
            case -1:
                errStr = "<script language='javascript'>window.alert('未找到后继节点！');top.window.close();</script>";
                return ERROR;
            case 0:// 或分支
                return "SelectNode";
            case 1:// 与分支
                return "SelectUser";
            case 2:
                return "SendToContinue";
            case 3:
                return "SelectCurNode";
            case 4:// 汇总
                return "SendToEnd";
            case 5:
                return "SelectUser";
            case 9:// 办结
                int iCount = dsFlow.getActiveNodeCount();
                if (iCount <= 1) {
                    errStr = "<script language='javascript'>" +
                    // --吴红亮 修改 开始
                            // // "function sendToEnd(){"+
                            // "if(confirm('您是最后一个处理者，是否办结该文件？')){"
                            // + "document.location='SendResult.action';"
                            // + "}else{top.close();"
                            // + "}" +
                            // // "}"+
                            "document.location='SendResult.action';" +
                            // --吴红亮 修改 结束
                            "</script>";
                    return ERROR;
                } else {
                    errStr = "<script language='javascript'>" +
                    // --吴红亮 修改 开始
                            // "function sendToEnd(){"+
                            // "if(confirm('是否办结该文件？')){document.location='SendResult.action';}else{top.close();}" +
                            // "}"+
                            "document.location='SendResult.action';" +
                            // --吴红亮 修改 结束
                            "</script>";
                    return ERROR;
                }
                // 子流程办结
            case 10:
                return "SelectNode";
            case 11:
                return "SelectUser";
            case 12:
                return "SendToContinue";
            case 13:
                return "SelectCurNode";
            case 14:
                return "SendToEnd";
            case 15:
                return "SelectUser";
            case 19:
                errStr = "<script language='javascript'>if(confirm('是否办结该文件？')){document.location='SendResult.action';}else{top.close();}</script>";
                return ERROR;
            case 29:
                return "SendResult";
            }
        } catch (Exception e) {
            e.printStackTrace();
            errStr = e.getMessage();
            return ERROR;
        }
        return SUCCESS;
    }
    
}
