package dsoap.web.action;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import xsf.data.DBManager;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.dsflow.model.DataTable;
import dsoap.log.SystemLog;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class SendTsMoreAction extends Action {
	public String errStr = "";

	private DS_FLOWClass dsFlow;
	private List depUserList;

	@Override
	public String execute() throws Exception {
		super.execute();
		boolean isEnd = false;
		try {
			// 获取发送参数，创建发送对象
			if (request.getParameter("flowParmsTS") != null
					&& request.getParameter("toNodeID") != null) {
				String flowParmsTS = URLDecoder.decode(request
						.getParameter("flowParmsTS"), "utf-8");
				// 要发送到的节点ID
				String toNodeID = URLDecoder.decode(request
						.getParameter("toNodeID"), "utf-8");
				ArrayList flowParmsXML = new ArrayList<String>();
				String[] flowParmsList = null;
				if (flowParmsTS.contains("#")) {
					flowParmsList = flowParmsTS.split("#");
				} else {
					flowParmsList = new String[1];
					flowParmsList[0] = flowParmsTS;
				}
				for (int i = 0; i < flowParmsList.length; i++) {
					try{
					String[] flowParms = flowParmsList[i].split(",");
					StringBuffer sb = new StringBuffer();
					sb.append("<Root>");
					sb.append("<Flow>");
					sb.append("<Type>" + flowParms[0] + "</Type>");
					sb.append("<Key>" + flowParms[1] + "</Key>");
					sb.append("<Objclass>" + flowParms[2] + "</Objclass>");
					sb.append("<UserID>" + flowParms[3] + "</UserID>");
					sb.append("<Pid>-1</Pid>");
					sb.append("<Pnid>" + flowParms[5] + "</Pnid>");
					sb.append("<WfID>" + flowParms[6] + "</WfID>");
					sb.append("<RUserID>" + flowParms[7] + "</RUserID>");
					sb.append("</Flow>");
					sb.append("</Root>");
					flowParmsXML.add(sb.toString());
					}
					catch(Exception ex)
					{
						errStr+="</br>流程参数flowParmsTS错误：第"+i+1+"条";
						return "RESULT";
					}
				}
				for (int i = 0; i < flowParmsXML.size(); i++) {
					try{
						dsFlow = new DS_FLOWClass(flowParmsXML.get(i).toString());
						//System.out.println("提交类型:" + dsFlow.iSendType);
						// 设置要发送到的节点信息
						dsFlow.setSelectCurNodeID(Long.parseLong(toNodeID));
						Node nextNode = dsFlow.NextNodeInfoXml
								.selectSingleNode("Nodes/Node");
						String nodeType = nextNode.valueOf("@NodeType");
						//System.out.println("节点类型:" + nodeType);
						if ("0".equals(nodeType)) {
							// 办结发送
							dsFlow.sendToEnd();
						} else {
							this.getNodeUsers(dsFlow, toNodeID);
							if (depUserList == null || depUserList.size() < 1) {
								errStr+="</br>发送失败,该节点下未配置人员：第"+Integer.parseInt(String.valueOf(i+1))+"条";
								continue;
							}
							String sustr = "";
							String sSendMethod = "";
							HashMap userMap = null;
							for (int j = 0; j < depUserList.size(); j++) {
								userMap = (HashMap) depUserList.get(j);
								sustr += ";:" + toNodeID + ":"
										+ userMap.get("UserID") + "::::"
										+ userMap.get("DeptID") + "";
								sSendMethod += "," + toNodeID + ":" + "0";
							}
							String sNodeDate = "NULL";
							// ---吴红亮 添加 开始
							String sendMethod = processSendMethod(
									dsFlow.NextNodeInfoXml, sSendMethod, sustr);
							if (!sSendMethod.equals(sendMethod)) {
								int index = sendMethod.indexOf("$");
								sSendMethod = sendMethod.substring(0, index);
								sustr = sendMethod.substring(index + 1, sendMethod
										.length());
								isEnd = true;
							}
							// 一般发送
							dsFlow.setSendInfoIsTS(sustr, sSendMethod, sNodeDate);
							dsFlow.send();
							//System.out.println("=========第"+i+1+"条发送成功。。");
						}
					}
					catch (Exception e) {
						errStr+="</br>发送失败：第"+Integer.parseInt(String.valueOf(i+1))+"条";
						continue;
					}
				}

			} else {
					errStr = "<script language='javascript'>window.alert('流程信息错误！');top.window.close();</script>";
			}
		} catch (Exception e) {
			e.printStackTrace();
			errStr = e.getMessage();
		}
		request.setAttribute("errStr", errStr);
		return "RESULT";
	}

	public void getNodeUsers(DS_FLOWClass dsFlow, String workflowNodeID) {
		try {
			if (!"".equals(workflowNodeID) && !"0".equals(workflowNodeID)) {
				dsFlow.setSelectNodeIDIsTS(Long.parseLong(workflowNodeID));
			}
			System.out.println(dsFlow.NextNodeInfoXml.asXML());
			for (Object obj : dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
				Node node = (Node) obj;
				if (node != null) {
					System.out.println(node.asXML());
					if ("1".equals(node.valueOf("@Enabled"))) {
						depUserList = new ArrayList<Object>();
						this.getUserByNodeXml(node, workflowNodeID);
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
					map.put("nodeID", workflowNodeID); // 用户选择的下个节点的ID
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
						map.put("nodeID", workflowNodeID); // 用户选择的下个节点的ID
						depUserList.add(map);
					}
					getUserByNodeXml(userNode1, workflowNodeID);
				}
			}
		}
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
