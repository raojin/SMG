package dsoap.web.action;

import org.dom4j.Document;
import org.dom4j.Node;

import xsf.data.DBManager;
import dsoap.dsflow.DS_FLOWClass;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.tree.Tree;
import dsoap.tools.tree.TreeNode;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
@SuppressWarnings("serial")
public class SelectUserAction extends Action {

    @Override
    public String execute() throws Exception {
        super.execute();
        Page pageInfo = new Page();
        if (session.get("DSFLOW") == null) {
            throw new Exception("流程信息错误！");// error.jsp
        }
        DS_FLOWClass dsFlow = (DS_FLOWClass) session.get("DSFLOW");
      //将线上条件判断的提示提交到页面  杨龙修改 2012/9/11 开始
        if(!"".equals(dsFlow.wfTip))
        {
        	request.setAttribute("wfTip", dsFlow.wfTip);
        }
      //将线上条件判断的提示提交到页面 杨龙修改 2012/9/11 结束
      //判断用户是否选择了多个不一样的节点 杨龙修改 2012/9/13 开始
        //将出线类型传给选人页面
        	request.setAttribute("iOutLineFlag", dsFlow.iOutLineFlag);
      //判断用户是否选择了多个不一样的节点 杨龙修改 2012/9/13 结束
        	 //增加批量发送 流程参数 杨龙修改 2012/9/26 开始
        	//如果流程中有批量发送参数 往选人页面发送一个批量的标识
        	if(!"".equals(dsFlow.batchFlowXML))
        	{
        		request.setAttribute("sendBatch", "true");
        	}
        	//增加批量发送 流程参数 杨龙修改 2012/9/26 结束
        	//往选人页面 传递抄送发送标识  杨龙修改 2012/11/3 开始
        	request.setAttribute("isCS",String.valueOf(dsFlow.isCS));
        	//往选人页面 传递抄送发送标识 杨龙修改 2012/11/3 结束
        // System.out.println(dsFlow.NextNodeInfoXml.asXML());
        // --------------------------------------------------------------与分支，分区域 2012.2.8 吴红亮
        if (dsFlow.iOutLineFlag == 1) {
            String forkID = request.getParameter("forkID");// 取值 null,1,0
            if (forkID == null) {// 进入与分支
                for (Object obj : dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                    Node nextWorkFlowNode = (Node) obj;
                    if ("0".equals(nextWorkFlowNode.valueOf("@Enabled"))) {
                        continue;
                    }
                    String sLineType = nextWorkFlowNode.valueOf("@LineType");
                    if ("3".equals(sLineType)) {
                        return "fork";
                    }
                }
            } else {// 进入与分支的不同区域
                for (Object obj : dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                    Node nextWorkFlowNode = (Node) obj;
                    if ("0".equals(nextWorkFlowNode.valueOf("@Enabled"))) {
                        continue;
                    }
                    String sLineType = nextWorkFlowNode.valueOf("@LineType");
                    ((org.dom4j.Element) nextWorkFlowNode).addAttribute("showNode", "3".equals(sLineType) ? forkID : ("1".equals(forkID) ? "0" : "1"));
                }
            }
        }
        // --------------------------------------------------------------
        String _cmdStr = "SELECT MJ FROM G_INFOS WHERE ID=" + dsFlow.iInfoID;
        String MJ = DBManager.getFieldStringValue(_cmdStr);
        if (MJ != null && !"".equals(MJ)) {
            pageInfo.strMj = MJ;
        }
        try {
            // //短信
            // String strScript = "<script
            // language=javascript>document.Form1.Content.value='"+dsFlow.sSmsContent+"'</script>";
            // Page.RegisterStartupScript("SMSCONTENT",strScript);
            // //this.Content.Value=dsFlow.sSmsContent;
            // this.ISSMS.Checked=dsFlow.isSendSMS;
            // this.ISMAIL.Checked = dsFlow.isSendEmail;
            // this.ISTRAY.Checked = dsFlow.isSendTray;
            // -------------------
            // 是否显示签名
            if (dsFlow.isHideYJ == 0) {
                pageInfo.chIsHideYJ = false;
            } else {
                pageInfo.chIsHideYJ = true;
            }
            // ------------------
            if (dsFlow.iSendType >= 10) {
                ShowTree(dsFlow.ds_ParentFlow.NextNodeInfoXml, pageInfo);
                // 办结时间
                // SydateNode.DateOnly = "No";
                // SydateFlow.DateOnly = "No";
                // String sNodeDate=dsFlow.ds_ParentFlow.getNodeDate();
                // if(sNodeDate=="0"){
                // SydateNode.Visible=false;
                // LabDate.Visible=false;
                // }else{
                // SydateNode.Value = sNodeDate;
                // }
                // SydateNode.DateOnly = "No";
            } else {
                ShowTree(dsFlow.NextNodeInfoXml, pageInfo);// ----------------------------------------------------设置pageInfo的发送方式，向pageInfo中的树添加节点
                // //办结时间
                // SydateNode.DateOnly = "No";
                // SydateFlow.DateOnly = "No";
                // String sNodeDate=dsFlow.getNodeDate();
                // if(sNodeDate=="0"){
                // SydateNode.Visible=false;
                // LabDate.Visible=false;
                // }else{
                // SydateNode.Value = sNodeDate;
                // }
                // SydateNode.DateOnly = "No";
            }
            // SydateFlow.Value = dsFlow.getFlowDate();
            // SydateFlow.DateOnly = "No";
            // if(dsFlow.iSendType==5||dsFlow.iSendType==15){
            // BtnCancel.Visible=true;
            // }else{
            // BtnCancel.Visible=false;
            // }
        } catch (Exception es) {
            dsFlow.sErrorMessage = es.getMessage();
        }
        if (pageInfo.iSelectCount == 0) {
            throw new Exception("<b>文件接收人需要调整，请联系管理员!</b>");// error.jsp
        }
        
        //dongchj 20160217 发角色可以静默发送,默认自动发送可以多发自动
        pageInfo.isSendByRole = dsFlow.isSendByRole;
        pageInfo.isAutoSendAll = dsFlow.isAutoSendAll;
        
        request.setAttribute("JavaScriptLab", pageInfo.getJs());
        request.setAttribute("chIsHideYJ", pageInfo.chIsHideYJ);
        request.setAttribute("TvUsers", pageInfo.TvUsers);
        request.setAttribute("strMj", pageInfo.strMj);
        request.setAttribute("SendMethod", pageInfo.SendMethod);
        request.setAttribute("OutLineFlag", dsFlow.iOutLineFlag);
        request.setAttribute("sms", dsFlow.isSendSMS ? dsFlow.sSmsContent : null);
        if(dsFlow.msg_lock){
        	 request.setAttribute("sms",dsFlow.sSmsContent);
        }else{
        	 request.setAttribute("sms","");
        }
        request.setAttribute("isForword", dsFlow.isForword);
        request.setAttribute("isEMail", dsFlow.isEMail);
        request.setAttribute("isTray", dsFlow.isTray);
        request.setAttribute("msg_lock", dsFlow.msg_lock);
        // System.out.println(dsFlow.NextNodeInfoXml.asXML());
        //判断是否补发，换人发送
        if(dsFlow.customParameter.containsKey("isAddSend"))
        {
        	if("true".equals(dsFlow.customParameter.get("isAddSend")))
            {
            	return "addsend";
            }
        }
        return SUCCESS;
    }

    private void ShowTree(Document nextNodeInfoXml, Page pageInfo) {
        pageInfo.TvUsers.Nodes.clear();
        this.ClearAutoSend(nextNodeInfoXml);
        // System.out.println("----------------");
        // System.out.println("选人测试：" + nextNodeInfoXml.asXML());
        // System.out.println(myDoc.selectNodes("Nodes/Node").size());
        for (Object obj : nextNodeInfoXml.selectNodes("Nodes/Node")) {
            Node nextWorkFlowNode = (Node) obj;
            // System.out.println(nextWorkFlowNode.asXML());
            if ("0".equals(nextWorkFlowNode.valueOf("@Enabled")) || "0".equals(nextWorkFlowNode.valueOf("@showNode"))) {
                continue;
            }
            // 取节点信息
            String sID = nextWorkFlowNode.valueOf("@ID");
            String sSendMethod = nextWorkFlowNode.valueOf("@SendMethod");
            String sSelected = nextWorkFlowNode.valueOf("@Selected");
            String sMultiUser = nextWorkFlowNode.valueOf("@MultiUser");
            String sNodeID = nextWorkFlowNode.valueOf("@NodeID");
            String sTimeType = nextWorkFlowNode.valueOf("@TimeType");
            String sTimeSpan = nextWorkFlowNode.valueOf("@TimeSpan");
            String sNodeCaption = nextWorkFlowNode.valueOf("@NodeCaption");
            String lineCaption = nextWorkFlowNode.valueOf("@LineCaption");
            //如果线上的显示完全包含节点的显示名，并且含有一对括号说明这是个退回节点操作
            if(!(lineCaption.contains("(") && lineCaption.contains(")") && lineCaption.contains(sNodeCaption))){
            	sNodeCaption = lineCaption == null || "".equals(lineCaption) ? sNodeCaption : lineCaption;
            }
            String sNodeType = nextWorkFlowNode.valueOf("@NodeType");
            String IsAutoExpand = nextWorkFlowNode.valueOf("@IsAutoExpand");
            String IsAutoSend = nextWorkFlowNode.valueOf("@IsAutoSend");
            // ---------------------------------------------------------------
            TreeNode node = new TreeNode();
            node.setId("0");
            node.setFid("-1");
            node.setText(sNodeCaption);
            node.setType("Root");
            // 存储节点动作(根节点,同时该节点还包含一些WFNodeList中查找的Node信息)
            node.setNodeData(sID + "," + sSendMethod + "," + sSelected + "," + sMultiUser + "," + sNodeID + "," + sTimeType + "," + sTimeSpan + "," + sNodeCaption);
            node.setImageUrl("../img/jstree/root.gif");
            node.setExpanded(true);
            // ---吴红亮 添加 开始
            pageInfo.sendNodeCount++;
            String wfId = nextWorkFlowNode.valueOf("@WfId");
            boolean isMustSend = isDefaultSend(wfId, sNodeID);
            boolean isAutoExpand = "1".equals(IsAutoExpand) ? true : false;
            if ("0".equals(sNodeType)) {// 办结节点
                insertFinishNode(node);
            }
            // ---吴红亮 添加 结束
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
            pageInfo.autoSend = pageInfo.autoSend || "1".equals(IsAutoSend);
            // 取节点下人员信息
            ShowSubTree(nextWorkFlowNode, node, node.getNodeData(), pageInfo, isMustSend, isAutoExpand, 1);
        }
        for (int iDel = 0; iDel < 6; iDel++) {
            ClearTree(pageInfo.TvUsers.Nodes.get(0));
        }
    }

    //如果主送节点设置了自动发送，其中包含抄送节点，则取消自动发送，由用户自己选择发送主送还是抄送  杨龙修改 2013/10/8 开始
    private void ClearAutoSend(Document nextNodeInfoXml) {
    	//是否需要清除自动发送标识
    	boolean isClear=false;
    	for (Object obj : nextNodeInfoXml.selectNodes("Nodes/Node"))
    	{
    		Node nextNode = (Node) obj;
    		String sNodeType = nextNode.valueOf("@NodeType");
    		String IsAutoSend = nextNode.valueOf("@IsAutoSend");
    		if("1".equals(nextNode.valueOf("@Enabled")))
    		{
    			//如果后续发送节点中包含抄送节点，则标识需要清除自动发送
    			if("9".equals(sNodeType)&&"0".equals(IsAutoSend))
    			{
    				isClear=true;
    				break;
    			}
    		}
    		
    	}
    	if(isClear)
    	{
    		//设置自动发送属性为，不需要自动发送
    		for (Object obj : nextNodeInfoXml.selectNodes("Nodes/Node"))
        	{
        		Node nextNode = (Node) obj;
        		String sNodeType = nextNode.valueOf("@NodeType");
        		String IsAutoSend = nextNode.valueOf("@IsAutoSend");
        		if("1".equals(nextNode.valueOf("@Enabled"))&&"1".equals(IsAutoSend))
        		{
        			nextNode.selectSingleNode("@IsAutoSend").setText("0");
        		}
        		
        	}
    	}
    	
    }
   //如果主送节点设置了自动发送，其中包含抄送节点，则取消自动发送，由用户自己选择发送主送还是抄送 杨龙修改 2013/10/8 结束
    
    private void ShowSubTree(Node workFlowNode, TreeNode parentNode, String workFlowNodeData, Page pageInfo, boolean isMustSend, boolean isAutoExpand, int level) {
        level--;
        for (Object obj : workFlowNode.selectNodes("Node")) {
            Node userNode = (Node) obj;
            // System.out.println(myUserNode.asXML());
            String id = userNode.valueOf("@Id");
            String uType = userNode.valueOf("@UType");
            String name = userNode.valueOf("@UName");
            String pId = userNode.valueOf("@fId");
            String pName = userNode.valueOf("@fName");
            // String sgId = myUserNode.valueOf("@gId");
            // myUserNode.Attributes["gId"].Value;
            String topName = userNode.valueOf("@topname");
            // --------------------------------------------------------
            TreeNode subNode = new TreeNode();
            subNode.setId(id);
            subNode.setText(name);
            // 格式：节点类型,ID,NAME,流程节点数据,父亲名,父亲ID,顶级名
            subNode.setNodeData(uType + "," + id + "," + name + "," + workFlowNodeData + "," + pName + "," + pId + "," + topName);
            // if ("8".equals(uType) || "2".equals(uType)) {
            if ("8".equals(uType) || "2".equals(uType) || "5".equals(uType)) {
                subNode.setType("Dept");
                subNode.setFid("0");
                ShowSubTree(userNode, subNode, workFlowNodeData, pageInfo, isMustSend, isAutoExpand, level);
                subNode.setImageUrl("../img/jstree/dept.gif");
                // ---吴红亮 添加 开始
                subNode.setExpanded(isAutoExpand || (!isAutoExpand && level > 0));
                // ---吴红亮 添加 结束
                // subNode.setExpandedImageUrl("/images/icon6.gif");
            } else {
                subNode.setType("User");
                subNode.setFid(pId);
                subNode.setImageUrl("../img/jstree/user.gif");
                if (topName != null && !"".equals(topName)) {
                    name += "(" + topName + ")";
                }
                String u = ";:" + workFlowNodeData.split(",")[0] + ":" + id + ":" + workFlowNodeData.split(",")[7] + ":" + pName + ":" + name + ":" + pId;
                pageInfo.sSelectStr = u;
                // ---吴红亮 添加 开始
                if (isMustSend) {
                    pageInfo.mustSendUsers += u + ":1";
                }
                pageInfo.sSelectStr1 += u;
                // ---吴红亮 添加 开始
                // subNode.setExpandedImageUrl("/images/4.gif");
                pageInfo.iSelectCount++;
            }
            parentNode.Nodes.add(subNode);
        }
    }

    private void insertFinishNode(TreeNode node) {
        TreeNode subNode = new TreeNode();
        subNode.setId("0");
        subNode.setText("办结");
        subNode.setNodeData(2 + "," + 0 + "," + "办结" + "," + node.getNodeData() + "," + "办结" + "," + 0 + "," + "办结");
        subNode.setType("Dept");
        subNode.setFid("0");
        subNode.setImageUrl("../img/jstree/dept.gif");
        subNode.setExpanded(true);
        node.Nodes.add(subNode);

        TreeNode subNode1 = new TreeNode();
        subNode1.setId("0");
        subNode1.setText("办结");
        subNode1.setNodeData(2 + "," + 0 + "," + "办结" + "," + node.getNodeData() + "," + "办结" + "," + 0 + "," + "办结");
        subNode1.setType("User");
        subNode1.setFid("0");
        subNode1.setImageUrl("../img/jstree/user.gif");
        subNode.Nodes.add(subNode1);
    }

    private void ClearTree(TreeNode tnFather) {
        int j = 0;
        int iCount = tnFather.Nodes.size();
        for (int i = 0; i < iCount; i++) {
            if (tnFather.Nodes.get(j).Nodes.size() == 0 && "DEPT".equals(tnFather.Nodes.get(j).getType())) {
                // tnFather.Nodes.get(j).Remove();
            } else {
                ClearTree(tnFather.Nodes.get(j));
                j++;
            }
        }
    }

    protected class Page {
        protected String strMj = "0";
        protected String SendMethod = "";
        protected int iSelectCount = 0;// 可选目标用户数
        protected String sSelectStr = "";//
        protected boolean chIsHideYJ = false;
        protected Tree TvUsers = new Tree();
        // ---吴红亮 添加 开始
        protected String mustSendUsers = "";
        protected int sendNodeCount = 0;
        protected String sSelectStr1 = "";// 自动发送用户数
        protected boolean autoSend = false;
        protected boolean isSendByRole = false;  //是否选择了按角色发送
        protected boolean isAutoSendAll = false;  //是否自动发送多发

        // ---吴红亮 添加 结束

        // 判断是否只有一个待发人员，如果是，则跳过选人界面，直接发送
        protected String getJs() {
            String javascript = "";
            // System.out.println("<<<>>>唯一可选人员时自动发送：" + ConfigurationSettings.AppSettings("唯一可选人员时自动发送") + " 自动发送可选节点数:" + this.sendNodeCount + " 自动发送可选人数：" + this.iSelectCount);
            if ("1".equals(ConfigurationSettings.AppSettings("唯一可选人员时自动发送"))) {
                if (this.iSelectCount == 1 && this.sendNodeCount == 1) {
                    javascript = "<script language='javascript'>document.all.UList.value='" + this.sSelectStr + "';Send();</script>";
                } else {
                    if (!"".equals(this.mustSendUsers)) {
                        javascript = "<script language='javascript'>document.all.UList.value=('" + this.mustSendUsers + "');document.all.List.src='SelectUserList.jsp';</script>";
                    }
                }
            } else {
                if (this.autoSend && (this.iSelectCount <= this.sendNodeCount || this.isSendByRole || this.isAutoSendAll )) {
                    javascript = "<script language='javascript'>document.all.UList.value='" + this.sSelectStr1 + "';Send();</script>";
                } else {
                    if (!"".equals(this.mustSendUsers)) {
                        javascript = "<script language='javascript'>document.all.UList.value=('" + this.mustSendUsers + "');document.all.List.src='SelectUserList.jsp';</script>";
                    } else {
                        if (this.iSelectCount == 1 && this.sendNodeCount == 1) {
                            javascript = "<script language='javascript'>document.all.UList.value='" + this.sSelectStr + "';document.all.List.src='SelectUserList.jsp';</script>";
                        }
                    }
                }
            }
            return javascript;
        }
    }

    private boolean isDefaultSend(String wfId, String wfnoeId) {
        boolean result = false;
        String _cmdStr = "";
        _cmdStr = "select MUST_SEND from WFNODELIST where WF_ID=" + wfId + " and WFNODE_ID=" + wfnoeId;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            int msstSend = dt.getRows().get(0).getInt("MUST_SEND");
            result = msstSend == 1 ? true : false;
        }
        return result;
    }
}
