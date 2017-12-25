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
import dsoap.log.SystemLog;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class SendBatchAction extends Action {

	public String errStr = "";
	String sustr = "";// 用户列表 （SelectUser.jsp
	// 提交格式【:节点ID:用户ID:流程节点名:部门名:用户名（部门名）:部门ID】多个时以“;”分割）
	String sSendMethod = "";// 发送方式 （SelectUserAction
	// 中设置的格式【节点ID:发送方式】多个时以“,”分割）
	String sNodeDate = "";// （SelectUser.jsp 提交）
	String strPriSend = "";
	String SMS = "";
	String strUserIp = "";
	DS_FLOWClass dsFlow1;
	//批量发送的类型1:自动发送,不用选择后续节点和人自动发送到后续所有节点所有人2:根据第一条待办选择的节点和人,后续待办做相同操作
	int batchSendType=2;
	private List depUserList;
	@Override
	public String execute() throws Exception {
		super.execute();
		// ---吴红亮 添加 开始
		// ---吴红亮 添加 结束
		// if (session.get("DSFLOW") == null) {
		// errStr =
		// "<script language='javascript'>alert('流程信息错误！');top.window.close();</script>";
		// return ERROR;
		// }
		try {
			// 获取公共参数
			sustr = request.getParameter("UList");// 用户列表 （SelectUser.jsp
			// 提交格式【:节点ID:用户ID:流程节点名:部门名:用户名（部门名）:部门ID】多个时以“;”分割）
			sSendMethod = request.getParameter("SendMethod");// 发送方式
			// （SelectUserAction
			// 中设置的格式【节点ID:发送方式】多个时以“,”分割）
			sNodeDate = request.getParameter("txtNode");// （SelectUser.jsp
			// 提交）
			// dsFlow.strPriSend = "," + request.getParameter("TxtPriSend");
			SMS = request.getParameter("SMSContent");
			strUserIp = request.getServerName();
		} catch (Exception e) {
			errStr = "<script language='javascript'>alert('获取选人参数失败.');//location.href='Selectuser.aspx';</script>";
			return "error";
		}
		int yesCount = 0;
		int notCount = 0;
		String flowXML = "";
		try {
			dsFlow1 = (DS_FLOWClass) session.get("DSFLOW");
			flowXML=dsFlow1.batchFlowXML;
			String[] flowParmsList = null;
			if (flowXML.contains("#")) {
				flowParmsList = flowXML.split("#");
			} else {
				flowParmsList = new String[1];
				flowParmsList[0] = flowXML;
			}
			for (int i = 0; i < flowParmsList.length; i++) {
				try {
					DS_FLOWClass dsFlow = new DS_FLOWClass(flowParmsList[i]);
					String results="";
					if(batchSendType==1)
					{
						results = this.autoSend(dsFlow);					
					}
					else
					{
						results = this.send(dsFlow);
					}
					if ("success".equals(results)) {
						yesCount++;
					} else {
						notCount++;
					}
				} catch (Exception e) {
					notCount++;
				}
			}
		} catch (Exception e) {
			errStr = "<script language='javascript'>alert('获取流程参数失败.');//location.href='Selectuser.aspx';</script>";
			return "error";
		}
		request.setAttribute("msg", "发送成功:" + yesCount + "条;失败:" + notCount+ "条.");
		return "SendBack";
	}
	
	private String autoSend(DS_FLOWClass dsFlow)
	{
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
						return "success";
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
				return "error";
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
							return "error";
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					request.setAttribute("msg", e.getMessage());
					return "error";
				}
				return "success";
			}
			return "error";
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	private String send(DS_FLOWClass dsFlow) {
		boolean isEnd = false;
		try {
			// ------------------------------------------------------------------------
			dsFlow.strPriSend = strPriSend;
			dsFlow.strIP = strUserIp;
			dsFlow.strMAC = SystemLog.GetNetCardAddress(strUserIp);
			dsFlow.sSmsContent = SMS;
			//lvxiaodan-2012-9-27
			dsFlow.NextNodeInfoXml =dsFlow1.NextNodeInfoXml;
			// ---吴红亮 添加 开始
			String sendMethod = processSendMethod(dsFlow.NextNodeInfoXml,
					sSendMethod, sustr);
			if (!sSendMethod.equals(sendMethod)) {
				int index = sendMethod.indexOf("$");
				sSendMethod = sendMethod.substring(0, index);
				sustr = sendMethod.substring(index + 1, sendMethod.length());
				isEnd = true;
			}
			// ---吴红亮 添加 结束
			String _cmdStr = "";
			_cmdStr = "SELECT BT FROM G_INFOS WHERE ID=" + dsFlow.iInfoID;
			xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
			if (dt.getRows().size() > 0) {
				dsFlow.strBT = dt.getRows().get(0).getString("BT");
			}
			if (!"".equals(sustr)) {
				if (dsFlow.iSendType >= 10) {
					dsFlow.ds_ParentFlow.setSendInfo(sustr, sSendMethod,
							sNodeDate);
					// // 短信
					// dsFlow.ds_ParentFlow.sSmsContent=
					// request.getParameter("SMSContent");
					// //Server.UrlDecode(this.Request.Params["SMSContent"]);
					// dsFlow.ds_ParentFlow.isSendSMS=("true".equals(request.getParameter("isSendSMS")));
					// //(this.Request.Params["isSendSMS"]=="true");
					// dsFlow.ds_ParentFlow.isSendEmail=("true".equals(request.getParameter("isSendMAIL")));
					// //(this.Request.Params["isSendMAIL"]=="true");
					// dsFlow.ds_ParentFlow.isSendTray=("true".equals(request.getParameter("isSendTRAY")));
					// //(this.Request.Params["isSendTRAY"]=="true");
					// 是否隐藏意见
					dsFlow.ds_ParentFlow.isHideYJ = ("true".equals(request
							.getParameter("isHideYJ"))) ? 1 : 0;
					// (this.Request.Params["isHideYJ"]=="true")?1:0;
					dsFlow.ds_ParentFlow.send();
					// 结办
					// dsFlow.sendToEnd();
				} else {
					dsFlow.setSendInfo(sustr, sSendMethod, sNodeDate);
					// 短信
					// dsFlow.sSmsContent=
					// request.getParameter("SMSContent");
					// //Server.UrlDecode(this.Request.Params["SMSContent"]);
					// dsFlow.isSendSMS=("true".equals(request.getParameter("isSendSMS")));
					// //(this.Request.Params["isSendSMS"]=="true");
					// dsFlow.isSendEmail=("true".equals(request.getParameter("isSendMAIL")));
					// //(this.Request.Params["isSendMAIL"]=="true");
					// dsFlow.isSendTray=("true".equals(request.getParameter("isSendTRAY")));
					// //(this.Request.Params["isSendTRAY"]=="true");
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
								return "SendBack";
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						request.setAttribute("msg", e.getMessage());
						return "SendBack";
						// throw e;
					}
				}
			}
			// ---吴红亮 添加 开始
			if (isEnd) {
				setEndAble(dsFlow.NextNodeInfoXml);
				dsFlow.iSendType = 9;
				return "index";
			}
			// ---吴红亮 添加 结束
			return SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			if (dsFlow.iSendType >= 10) {
				if (dsFlow.ds_ParentFlow.iErrorCode == 25) {
					dsFlow.ds_ParentFlow.iErrorCode = 0;
					errStr = "<script language='javascript'>alert('当前选择的人员数量为零，请重新选择');//location.href='Selectuser.aspx';</script>";
					return ERROR;
				} else {
					errStr = dsFlow.sErrorMessage;
					return ERROR;
				}
			} else {
				if (dsFlow.iErrorCode == 25) {
					dsFlow.iErrorCode = 0;
					errStr = "<script language='javascript'>alert('当前选择的人员数量为零，请重新选择');//location.href='Selectuser.aspx';</script>";
					return ERROR;
				} else {
					errStr = dsFlow.sErrorMessage;
					return ERROR;
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

}
