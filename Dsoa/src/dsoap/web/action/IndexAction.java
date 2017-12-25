package dsoap.web.action;

import java.net.URLDecoder;

import xsf.resource.ResourceManager;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.tools.Escape;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class IndexAction extends Action {

    private static final long serialVersionUID = 1175412321901094803L;

    public String errStr = "";

    private DS_FLOWClass dsFlow;

    @Override
    public String execute() throws Exception {
        super.execute();
        try {
            // 获取发送参数，创建发送对象
            if (request.getParameter("FlowParms") != null) {
            	 //String FlowParms = URLDecoder.decode(request.getParameter("FlowParms"), "utf-8");
            	 String FlowParms = Escape.unescape(request.getParameter("FlowParms"));
                // System.out.println("获取Flowparams对象:"+FlowParms);
                dsFlow = new DS_FLOWClass(FlowParms);
              //增加批量发送 流程参数 杨龙修改 2012/9/26 开始
                if(session.get("batchFlowXML") != null)
            	{
            		String batchFlowXML = URLDecoder.decode(session.get("batchFlowXML").toString(), "utf-8");
            		dsFlow.batchFlowXML=batchFlowXML;
            	}
            	//增加批量发送 流程参数 杨龙修改 2012/9/26 结束
                // -- 吴红亮 添加 开始 --
                session.put("isNewFile", new Boolean(dsFlow.iPID == 0 && dsFlow.iPnID == 0));
                // -- 吴红亮 添加 结束 --
                session.put("DSFLOW", dsFlow);
            } else {
                if (session.get("DSFLOW") == null) {
                    errStr = "<script language='javascript'>window.alert('流程信息错误！');top.window.close();</script>";
                    return ERROR;
                }
                dsFlow = (DS_FLOWClass) session.get("DSFLOW");
            }
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
                	
                    // --吴红亮 修改 开始
                            // // "function sendToEnd(){"+
                            // "if(confirm('您是最后一个处理者，是否办结该文件？')){"
                            // + "document.location='SendResult.action';"
                            // + "}else{top.close();"
                            // + "}" +
                            // // "}"+
                            // --吴红亮 修改 结束
                	errStr = "<script language='javascript'>document.location='SendResult.action';</script>";
                	String endingtips = ResourceManager.getAppKey("EndingTips", "false");
                    if(endingtips.equals("true")){
                    	errStr = "<script language='javascript'>if(confirm('是否办结该文件？')){document.location='SendResult.action';}else{top.window.location.reload();}</script>";
                    	//throw new Exception(errStr); //抛出异常 进入全局error.jsp处理是否办结该文件
                    }
                    
                    return ERROR;
                } else {
                    errStr = "<script language='javascript'>" +
                    // --吴红亮 修改 开始
                             "if(confirm('是否办结该文件？')){document.location='SendResult.action';}else{top.close();}" +
                            /*"document.location='SendResult.action';" +*/
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
