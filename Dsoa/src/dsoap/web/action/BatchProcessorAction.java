package dsoap.web.action;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import xsf.data.DBManager;
import dsoap.dsflow.DS_FLOWClass;
import dsoap.log.SystemLog;
import dsoap.tools.tree.Tree;
import dsoap.tools.tree.TreeNode;
import dsoap.tools.webwork.Action;

public class BatchProcessorAction extends Action {
    private static final long serialVersionUID = 1L;
    private static final String FLOW_XML = "<Root><Flow><Type>0</Type><Key>{INFO_ID}</Key><Objclass>{OBJCLASS}</Objclass><UserID>{USER_ID}</UserID><Pid>{PID}</Pid><Pnid>{PNID}</Pnid><WfID>{WFID}</WfID></Flow></Root>";

    public String execute() throws Exception {
        // Date a = new Date();
        super.execute();
        String flowsStr = request.getParameter("flowsStr");
        String autoSend = request.getParameter("autoSend");
        String nextWFNodeId = request.getParameter("nextWFNodeId");
        String nextUserId = request.getParameter("nextUserId");
        String checkSendType = request.getParameter("checkSendType");
        // --------------------------------------------------------------批量处理
        if (flowsStr != null && !"".equals(flowsStr)) {
            flowsStr = URLDecoder.decode(flowsStr, "utf-8");
            String[] flows = flowsStr.split("\\|");
            if (flows[0] != null && !"".equals(flows[0])) {
                String[] flowInfos = flows[0].split(",");
                String xml = FLOW_XML.replace("{INFO_ID}", flowInfos[0]).replace("{OBJCLASS}", flowInfos[1]).replace("{USER_ID}", flowInfos[2]).replace("{PID}", flowInfos[3]).replace("{PNID}", flowInfos[4]).replace("{WFID}", flowInfos[5]);
                DS_FLOWClass dsFlow = new DS_FLOWClass(xml);
                // System.out.println(dsFlow.NextNodeInfoXml.asXML());
                dsFlow.isBatch = true;
                // 选结点
                try {
                    dsFlow.setSelectNodeID(getSelectNodeID(dsFlow, getNextNodeIds(dsFlow.iWfID, dsFlow.iWfNodeID)));
                } catch (Exception e) {
                    if ("NOT UNIQUE".equals(e.getMessage())) {
                        System.out.println("后续结点不唯一,无法批量处理！");
                        request.setAttribute("status", "后续结点不唯一,无法批量处理！");
                    } else {
                        request.setAttribute("status", "处理后续结点出错！");
                    }
                    return SUCCESS;
                }
                // System.out.println(dsFlow.NextNodeInfoXml.asXML());
                // 选人
                Page p = selectUser(dsFlow);
                if (p.iSelectCount > 1) {
                    System.out.println("人员不唯一,无法批量处理！");
                    request.setAttribute("status", "人员不唯一,无法批量处理！");
                    return SUCCESS;
                }
                // 发送
                String users = p.getUsers(null, dsFlow.isBatch);
                String sSendMethod = p.SendMethod;
                this.send(dsFlow, users, sSendMethod);
            }
            if (flows[1] != null && !"".equals(flows[1])) {
                request.setAttribute("index", flows[1]);
            }
        }
        // --------------------------------------------------------------推送
        else if (autoSend != null && !"".equals(autoSend)) {
            autoSend = URLDecoder.decode(autoSend, "utf-8");
            DS_FLOWClass dsFlow = new DS_FLOWClass(autoSend);
            // System.out.println(dsFlow.NextNodeInfoXml.asXML());
            // 选结点
            try {
                dsFlow.setSelectNodeID(getSelectNodeID(dsFlow, nextWFNodeId));
                if (checkSendType != null && !"".equals(checkSendType)) {
                    request.setAttribute("index", String.valueOf(dsFlow.iSendType));
                    return SUCCESS;
                }
            } catch (Exception e) {
                if ("NOT UNIQUE".equals(e.getMessage())) {
                    System.out.println("后续结点不唯一,无法批量处理！");
                    request.setAttribute("status", "后续结点不唯一,无法批量处理！");
                } else {
                    request.setAttribute("status", "处理后续结点出错！");
                }
                return SUCCESS;
            }
            // System.out.println(dsFlow.NextNodeInfoXml.asXML());
            // 选人
            Page p = selectUser(dsFlow);
            // 发送
            String users = p.getUsers(nextUserId, dsFlow.isBatch);
            String sSendMethod = p.SendMethod;
            try {
                this.send(dsFlow, users, sSendMethod);
                request.setAttribute("index", "1");
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("status", "发送失败！");
            }
        } else {
            request.setAttribute("status", "参数出错！");
            return SUCCESS;
        }
        // System.out.println("\n\n\n\n\n\n-------" + (new Date().getTime() - a.getTime()));
        return SUCCESS;
    }

    private void send(DS_FLOWClass dsFlow, String users, String sSendMethod) throws Exception {
        System.out.println("提交类型:" + dsFlow.iSendType);// ---------------------------------------
        if (dsFlow.iSendType == 9) {// 办结
            synchronized (DS_FLOWClass.lock) {
                // System.out.println("批量LOCK:--------------------------------" + DS_FLOWClass.lock);
                if (dsFlow.isExist()) {
                    dsFlow.isBatch = true;
                    dsFlow.sendToEnd();
                }
            }
        } else {
            String sNodeDate = "NULL";
            dsFlow.strPriSend = ",";
            dsFlow.strIP = request.getServerName();
            dsFlow.strMAC = SystemLog.GetNetCardAddress(dsFlow.strIP);
            dsFlow.strBT = getBT(dsFlow.iInfoID);
            dsFlow.setSendInfo(users, sSendMethod, sNodeDate);
            dsFlow.isHideYJ = 0;
            synchronized (DS_FLOWClass.lock) {
                // System.out.println("批量LOCK:--------------------------------" + DS_FLOWClass.lock);
                if (dsFlow.isExist()) {
                    dsFlow.send();
                }
            }
        }
        // System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + dsFlow.iInfoID);
    }

    private String getBT(long infoId) {
        String BT = "";
        String _cmdStr = "SELECT BT FROM G_INFOS WHERE ID=" + infoId;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            BT = dt.getRows().get(0).getString("BT");
        }
        return BT;
    }

    private String getNextNodeIds(long WF_ID, long WFNODE_ID) throws Exception {
        String IDs = "";
        String _cmdStr = "";
        _cmdStr = "SELECT DEFAULT_NEXT_NODE FROM WFNODELIST WHERE IS_BATCH=1 AND WFNODE_ID=" + WFNODE_ID + " AND WF_ID=" + WF_ID;
        // System.out.println(_cmdStr);
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            IDs = dt.getRows().get(0).getString("DEFAULT_NEXT_NODE");
        }
        if (IDs == "") {
            throw new Exception();
        }
        return IDs;
    }

    @SuppressWarnings("unchecked")
    private long getSelectNodeID(DS_FLOWClass dsFlow, String nextWfNodeIds) throws Exception {
        String[] ids = nextWfNodeIds.split(",");
        long selectId = 0;
        List<Object> list = dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node");
        int count = 0;
        for (Object obj : list) {
            Node myNode = (Node) obj;
            for (String id : ids) {
                if (myNode.valueOf("@NodeID").equals(id)) {
                    selectId = Long.parseLong(myNode.valueOf("@ID"));
                    count++;
                }
            }
        }
        if (count == 0) {
            throw new Exception();
        }
        if (count > 1) {
            throw new Exception("NOT UNIQUE");
        }
        return selectId;
    }

    private Page selectUser(DS_FLOWClass dsFlow) {
        Page pageInfo = new Page();
        String _cmdStr = "SELECT MJ FROM G_INFOS WHERE ID=" + dsFlow.iInfoID;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            String MJ = dt.getRows().get(0).getString("MJ");
            if (MJ != null) {
                pageInfo.strMj = MJ;
            }
        }
        try {
            if (dsFlow.isHideYJ == 0) {
                pageInfo.chIsHideYJ = false;
            } else {
                pageInfo.chIsHideYJ = true;
            }
            if (dsFlow.iSendType >= 10) {
                ShowTree(dsFlow.ds_ParentFlow.NextNodeInfoXml, pageInfo);
            } else {
                ShowTree(dsFlow.NextNodeInfoXml, pageInfo);
            }
        } catch (Exception e) {
            dsFlow.sErrorMessage = e.getMessage();
        }
        return pageInfo;
    }

    private void ShowTree(Document nextNodeInfoXml, Page pageInfo) {
        // System.out.println(myDoc.asXML());
        pageInfo.TvUsers.Nodes.clear();
        for (Object obj : nextNodeInfoXml.selectNodes("Nodes/Node")) {
            Node nextWorkFlowNode = (Node) obj;
            if ("0".equals(nextWorkFlowNode.valueOf("@Enabled"))) {
                continue;
            }
            String sID = nextWorkFlowNode.valueOf("@ID");
            String sSendMethod = nextWorkFlowNode.valueOf("@SendMethod");
            String sSelected = nextWorkFlowNode.valueOf("@Selected");
            String sMultiUser = nextWorkFlowNode.valueOf("@MultiUser");
            String sNodeID = nextWorkFlowNode.valueOf("@NodeID");
            String sTimeType = nextWorkFlowNode.valueOf("@TimeType");
            String sTimeSpan = nextWorkFlowNode.valueOf("@TimeSpan");
            String sNodeCaption = nextWorkFlowNode.valueOf("@NodeCaption");
            // ---------------------------------------------------------------
            TreeNode node = new TreeNode();
            node.setId("0");// 添加id对应
            node.setFid("-1");
            node.setText(sNodeCaption);
            // 创建根结点-------------------------------------------------------------
            node.setType("Root");
            // 存储节点动作(根节点,同时该节点还包含一些WFNodeList中查找的Node信息)
            node.setNodeData(sID + "," + sSendMethod + "," + sSelected + "," + sMultiUser + "," + sNodeID + "," + sTimeType + "," + sTimeSpan + "," + sNodeCaption);
            node.setImageUrl("../img/jstree/root.gif");
            node.setExpanded(true);
            if ("1".equals(sMultiUser)) {
                int sTemp = -1;
                if (sSendMethod != null && !"".equals(sSendMethod)) {
                    sTemp = Integer.parseInt(sSendMethod);
                }
                // 可多人办理
                switch (sTemp) {
                case 0: // 只允许并行
                    pageInfo.SendMethod += "," + sID + ":" + 0;
                    break;
                case 1: // 只允许串行
                    pageInfo.SendMethod += "," + sID + ":" + 1;
                    break;
                case 2: // 默认并行
                    node.setText(node.getText() + "(<input type='radio' value='0' id='Node" + sID + "' name='Node" + sID + "' title='接收者同时收到该信息' onclick='setSendMethod(" + sID + ",0)' checked>并发");
                    node.setText(node.getText() + " <input type='radio' value='1' id='Node" + sID + "' name='Node" + sID + "' title='接收者按顺序收到该信息' onclick='setSendMethod(" + sID + ",1)'>串发)");
                    pageInfo.SendMethod += "," + sID + ":" + 0;
                    break;
                case 3: // 默认串行
                    node.setText(node.getText() + "(<input type='radio' value='0' id='Node" + sID + "' name='Node" + sID + "' title='接收者同时收到该信息' onclick='setSendMethod(" + sID + ",0)'>并发");
                    node.setText(node.getText() + " <input type='radio' value='0' id='Node" + sID + "' name='Node" + sID + "' title='接收者按顺序收到该信息' onclick='setSendMethod(" + sID + ",1)' checked>串发)");
                    pageInfo.SendMethod += "," + sID + ":" + 1;
                    break;
                default:
                    break;
                }
            } else {
                pageInfo.SendMethod += "," + sID + ":" + 0;
            }
            pageInfo.TvUsers.Nodes.add(node);
            // 取节点下人员信息
            ShowSubTree(nextWorkFlowNode, node, node.getNodeData(), pageInfo);
        }
        for (int iDel = 0; iDel < 6; iDel++) {
            ClearTree(pageInfo.TvUsers.Nodes.get(0));
        }
    }

    // Node:流程节点；TreeNode：选人树结点；sNodeInfo：选人树结点信息；Page：容器；
    private void ShowSubTree(Node workFlowNode, TreeNode parentNode, String workFlowNodeData, Page pageInfo) {
        for (Object obj : workFlowNode.selectNodes("Node")) {
            Node userNode = (Node) obj;
            String id = userNode.valueOf("@Id");
            String uType = userNode.valueOf("@UType");
            String name = userNode.valueOf("@UName");
            String pId = userNode.valueOf("@fId");
            String pName = userNode.valueOf("@fName");
            String topName = userNode.valueOf("@topname");
            // 创建子节点-------------------------------------------------------------
            TreeNode subNode = new TreeNode();
            subNode.setId(id);
            subNode.setText(name);
            subNode.setNodeData(uType + "," + id + "," + name + "," + workFlowNodeData + "," + pName + "," + pId + "," + topName);
            if ("8".equals(uType) || "2".equals(uType) || "5".equals(uType)) {
                subNode.setFid("0");
                subNode.setType("Dept");
                ShowSubTree(userNode, subNode, workFlowNodeData, pageInfo);
                subNode.setImageUrl("../img/jstree/dept.gif");
                subNode.setExpanded(true);
            } else {
                subNode.setFid(pId);
                subNode.setImageUrl("../img/jstree/user.gif");
                subNode.setType("User");
                pageInfo.iSelectCount++;
                pageInfo.sSelectStr = ";:" + workFlowNodeData.split(",")[0] + ":" + id + ":" + workFlowNodeData.split(",")[7] + ":" + pName + ":" + name + ":" + pId;
            }
            parentNode.Nodes.add(subNode);
        }
    }

    private void ClearTree(TreeNode tnFather) {
        int j = 0;
        int iCount = tnFather.Nodes.size();
        for (int i = 0; i < iCount; i++) {
            if (tnFather.Nodes.get(j).Nodes.size() == 0 && "DEPT".equals(tnFather.Nodes.get(j).getType())) {
            } else {
                ClearTree(tnFather.Nodes.get(j));
                j++;
            }
        }
    }

    public static void main(String[] args) {
        // INFO_ID,OBJCLASS,USER_ID,PID,PNID,WFID|";
        // String s = "991292996367187022,217015,73011908,199590882,2,740";
        BatchProcessorAction a = new BatchProcessorAction();
        // a.setFlowsStr(s);
        try {
            a.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected class Page {
        protected String strMj = "0";
        protected String SendMethod = "";
        protected int iSelectCount = 0;
        protected String sSelectStr = "";
        protected boolean chIsHideYJ = false;
        protected Tree TvUsers = new Tree();

        public String getUsers(String userId, boolean isBatch) {
            String sustr = "";
            userId = "," + userId + ",";
            List<String> userList = new ArrayList<String>();
            for (TreeNode node : TvUsers.Nodes) {
                if (!node.Nodes.isEmpty() && userList.size() < 1) {
                    processChild(userList, node, userId, isBatch);
                }
            }
            for (String t : userList) {
                String[] dataList = t.split(",");
                String sNodeID = dataList[3];
                String sUserID = dataList[1];
                String sUserName = dataList[2];
                try {
                    sUserName += "(" + dataList[13] + ")";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String sNodeCaption = dataList[10];
                String sfName = dataList[11];
                String sFid = dataList[12];
                sustr += ";:" + sNodeID + ":" + sUserID + ":" + sNodeCaption + ":" + sfName + ":" + sUserName + ":" + sFid;
            }
            return sustr;
        }

        private void processChild(List<String> userList, TreeNode node, String userId, boolean isBatch) {
            for (TreeNode child : node.Nodes) {
                if (child.getType().equals("User")) {
                    if (isBatch) {
                        userList.add(child.getNodeData());
                    } else {
                        String sUserID = child.getNodeData().split(",")[1];
                        if (userId.indexOf("," + sUserID + ",") > -1) {
                            userList.add(child.getNodeData());
                        }
                    }
                } else {
                    if (!isBatch || (isBatch && userList.size() == 0)) {
                        processChild(userList, child, userId, isBatch);
                    }
                }
            }
        }
    }

}
