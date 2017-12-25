package dsoap.web.action;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import xsf.data.DBManager;
import xsf.log.LogManager;
import dsoap.dsflow.DS_FLOWClass;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author 根据传入的当前节点的参数，自动发送的后续第一节点的第一个用户。
 */
public class AutoSendingAction extends Action {
	private static final long serialVersionUID = 4516271888926684193L;

	public String errStr = "";
	private DS_FLOWClass dsFlow;
	private List depUserList;
	private String sustr = "";
	@Override
	public String execute() throws Exception {
		super.execute();
		boolean isEnd = false;
        // 杨龙修改根据业务系统发送的流程参数 初始化流程 2013-5-7 开始
		//获取流程参数，初始化流程
		try{
		if (request.getParameter("flowParms") != null) {
			String FlowParms = URLDecoder.decode(request
					.getParameter("flowParms"), "utf-8");
			dsFlow = new DS_FLOWClass(FlowParms);
			session.put("DSFLOW", dsFlow);
		}
		if (session.get("DSFLOW") == null) {
            errStr = "<script language='javascript'>alert('流程信息错误！');top.window.close();</script>";
			return ERROR;
		}
		dsFlow = (DS_FLOWClass) session.get("DSFLOW");
		}
		catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("msg", "初始化流程失败："+e.getMessage());
			return ERROR;
		}
		try {
			// 循环设置后续节点的用户
			for (Object obj : dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
				Node nextWorkFlowNode = (Node) obj;
				String nodeType = nextWorkFlowNode.valueOf("@NodeType");
				String nodeIndex = nextWorkFlowNode.valueOf("@ID");
				String toNodeID = nextWorkFlowNode.valueOf("@NodeID");
				// 设置该节点为选中节点
				dsFlow.setSelectNodeID(Long.parseLong(nodeIndex));
				//办结节点，如果是一个节点发送到办结则办结，如果是多个节点则办结节点被忽略掉
				if ("0".equals(nodeType)) {
					//过滤掉办结节点不做处理
					if(dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node").size()==1)
					{
			            // 办结发送
						dsFlow.sendToEnd();
						return SUCCESS;
					}
					else
					{
						continue;
					}
				}
			}
			//强制所有后续节点都要发送
			for (Object obj : dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
				Node nextWorkFlowNode = (Node) obj;
				nextWorkFlowNode.selectSingleNode("@Enabled").setText("1");
			}
			
			//==============================================获取发送节点的用户
			LogManager.debug(dsFlow.NextNodeInfoXml.asXML());
			depUserList = new ArrayList<Object>();
			this.getNodeUsers(dsFlow);
			if (depUserList == null || depUserList.size() < 1) {
				return ERROR;
			}
			String sustr = "";
            String sSendMethod = ",0:0";
			HashMap userMap = null;
			for (int j = 0; j < depUserList.size(); j++) {
				userMap = (HashMap) depUserList.get(j);
				sustr += ";:" + userMap.get("ID") + ":" + userMap.get("UserID")
						+ "::::" + userMap.get("DeptID") + "";
			if(!sSendMethod.contains(userMap.get("ID").toString()))
			{
				sSendMethod += "," + userMap.get("ID").toString() + ":" + "0";
			}
			}
			String sNodeDate = "NULL";
            // ---吴红亮 添加 开始
            String sendMethod = processSendMethod(dsFlow.NextNodeInfoXml, sSendMethod, sustr);
            if (!sSendMethod.equals(sendMethod)) {
                int index = sendMethod.indexOf("$");
                sSendMethod = sendMethod.substring(0, index);
                sustr = sendMethod.substring(index + 1, sendMethod.length());
                isEnd = true;
            }
			//String sendMethod =sSendMethod;
            // 一般发送
			String _cmdStr = "";
			_cmdStr = "SELECT BT FROM G_INFOS WHERE ID=" + dsFlow.iInfoID;
			xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
			if (dt.getRows().size() > 0) {
				dsFlow.strBT = dt.getRows().get(0).getString("BT");
			}
			//===========================================普通发送
			if (!"".equals(sustr)) {
				dsFlow.setSendInfo(sustr, sSendMethod, sNodeDate);
                // 是否隐藏意见
				dsFlow.isHideYJ = (("true".equals("false")) ? 1 : 0);
				// (this.Request.Params["isHideYJ"]=="true")?1:0;
				try {
					synchronized (DS_FLOWClass.lock) {
                        // System.out.println("一般LOCK:--------------------------------"
						// + DS_FLOWClass.lock);
						if (dsFlow.isExist()) {
							dsFlow.send();
						} else {
                            request.setAttribute("msg", "该文件已被处理!");
							return ERROR;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					request.setAttribute("msg", e.getMessage());
					return ERROR;
				}
				return SUCCESS;
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			errStr = dsFlow.sErrorMessage;
			return ERROR;
		}
		return ERROR;
	}

	private void setUserInfo()
	{
		
	}
	
	private String processSendMethod(Document nextNodeInfoXml,
			String sendMethod, String users) {
		// System.out.println(nextNodeInfoXml.asXML());
		for (Object obj : nextNodeInfoXml.selectNodes("Nodes/Node")) {
			Node nextWorkFlowNode = (Node) obj;
			if ("0".equals(nextWorkFlowNode.valueOf("@Enabled"))) {
				continue;
			}
			String sID = nextWorkFlowNode.valueOf("@ID");
			String sNodeType = nextWorkFlowNode.valueOf("@NodeType");
			String test = "," + sID + ":";
			String test1 = ":" + sID + ":";
            if ("0".equals(sNodeType) && sendMethod.indexOf(test) > -1) {// 办结节点
				nextWorkFlowNode.selectSingleNode("@Enabled").setText("0");
				String[] temp = sendMethod.split(test);
				String tail = "";
				if (temp[1].indexOf(",") > -1) {
					tail = temp[1].substring(temp[1].indexOf(","));
				}
				sendMethod = temp[0] + tail;
				temp = users.split(";");
				users = "";
				for (String u : temp) {
					if (!u.startsWith(test1) && !"".equals(u)) {
						users += ";" + u;
					}
				}
				sendMethod += "$" + users;
				break;
			}
		}
		return sendMethod;
	}

	public void getNodeUsers(DS_FLOWClass dsFlow) {
		try {
            LogManager.debug(dsFlow.NextNodeInfoXml.asXML());
			for (Object obj : dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
				Node node = (Node) obj;
				if (node != null) {
                    LogManager.debug(node.asXML());
					if ("1".equals(node.valueOf("@Enabled"))) {
						this.getUserByNodeXml(node, node.valueOf("@ID"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	                        /**
     * 递归循环XML 获取XML中的人员信息
     * 
     * @param node
     */
	private void getUserByNodeXml(Node node, String workflowNodeID)
			throws Exception {
		for (Object obj : node.selectNodes("Node")) {
			Node userNode = (Node) obj;
			if (userNode != null) {
				System.out.println(userNode.asXML());
				if ("0".equals(userNode.valueOf("@UType"))
						|| "9".equals(userNode.valueOf("@UType"))) {
					HashMap<String, String> map = new HashMap<String, String>();
                    map.put("UserID", userNode.valueOf("@Id")); // 人员ID
                    map.put("UserName", userNode.valueOf("@UName")); // 人员名称
                    map.put("UserType", userNode.valueOf("@UType")); // 人员类型
                    map.put("DeptID", userNode.valueOf("@fId")); // 人员ID
                    map.put("ID", workflowNodeID); // 用户选择的下个节点的索引
					depUserList.add(map);
				}
			}
            // 获取人员信息
			for (Object obj1 : userNode.selectNodes("Node")) {
				Node userNode1 = (Node) obj1;
				if (userNode1 != null) {
					System.out.println(userNode1.asXML());
                    // 获取部门信息
					if ("0".equals(userNode1.valueOf("@UType"))
							|| "9".equals(userNode1.valueOf("@UType"))) {
						HashMap<String, String> map = new HashMap<String, String>();
                        map.put("UserID", userNode1.valueOf("@Id")); // 人员ID
                        map.put("UserName", userNode1.valueOf("@UName")); // 人员名称
                        map.put("UserType", userNode1.valueOf("@UType")); // 人员类型
                        map.put("DeptID", userNode1.valueOf("@fId")); // 人员ID
                        map.put("ID", workflowNodeID); // 用户选择的下个节点的索引
						depUserList.add(map);
					}
					getUserByNodeXml(userNode1, workflowNodeID);
				}
			}
		}
	}

	private void setEndAble(Document nextNodeInfoXml) {
		for (Object obj : nextNodeInfoXml.selectNodes("Nodes/Node")) {
			Node nextWorkFlowNode = (Node) obj;
            if ("0".equals(nextWorkFlowNode.valueOf("@NodeType"))) {// 办结节点
				nextWorkFlowNode.selectSingleNode("@Enabled").setText("1");
			} else {
				nextWorkFlowNode.selectSingleNode("@Enabled").setText("0");
			}
		}
	}

}
