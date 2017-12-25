package dsoap.web.action;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import xsf.Config;
import xsf.data.CommandCollection;
import xsf.data.DBManager;
import xsf.data.Parameter;
import xsf.data.Sql;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.dsflow.model.DataTable;
import dsoap.log.SystemLog;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.webwork.Action;

/**
 * 特送发送类
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class SendingTsAction extends Action {
	public String errStr = "";

	private DS_FLOWClass dsFlow;

	@Override
	public String execute() throws Exception {
		super.execute();
		boolean isEnd = false;
		try {
			// 获取发送参数，创建发送对象
			//杨龙修改根据业务系统发送的流程参数 初始化流程 2013-3-25 开始
	        if (request.getParameter("flowParms") != null&&request.getParameter("toNodeID") != null) {
	            String FlowParms = URLDecoder.decode(request.getParameter("flowParms"), "utf-8");
	            Document parmsXml = DocumentHelper.parseText(FlowParms);
	            dsFlow = new DS_FLOWClass(parmsXml.asXML());
				try {
					// 要发送到的节点ID
		            String toNodeID = URLDecoder.decode(request.getParameter("toNodeID"), "utf-8");
		            dsFlow.setSelectCurNodeID(Long.parseLong(toNodeID));
					// System.out.println("提交类型:" + dsFlow.iSendType);
					// 设置要发送到的节点信息
					Node nextNode = dsFlow.NextNodeInfoXml
							.selectSingleNode("Nodes/Node");
					String nodeType = nextNode.valueOf("@NodeType");
					// System.out.println("节点类型:" + nodeType);
					if ("0".equals(nodeType)) {
						// 办结发送
						dsFlow.sendToEnd();
					} else {
						 String sustr = request.getParameter("UList");// 用户列表 （SelectUser.jsp 提交格式【:节点ID:用户ID:流程节点名:部门名:用户名（部门名）:部门ID】多个时以“;”分割）
				         String sSendMethod = request.getParameter("SendMethod");// 发送方式 （SelectUserAction 中设置的格式【节点ID:发送方式】多个时以“,”分割）
				         String sNodeDate = request.getParameter("txtNode");// （SelectUser.jsp 提交）
				         dsFlow.strPriSend = "," + request.getParameter("TxtPriSend");
				         String SMS = request.getParameter("SMSContent");
				         String strUserIp = request.getServerName();
				            // ------------------------------------------------------------------------
				         dsFlow.strIP = strUserIp;
				         dsFlow.strMAC = SystemLog.GetNetCardAddress(strUserIp);
				         dsFlow.sSmsContent = SMS;
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
						 try {
		                        synchronized (DS_FLOWClass.lock) {
		                            // System.out.println("一般LOCK:--------------------------------" + DS_FLOWClass.lock);
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
				} catch (Exception e) {
					e.printStackTrace();
					errStr += "</br>发送失败：" + e.getMessage();
					return ERROR;
				}

			} else {
				errStr = "<script language='javascript'>window.alert('流程信息错误！');top.window.close();</script>";
				return ERROR;
			}
		} catch (Exception e) {
			e.printStackTrace();
			errStr = e.getMessage();
			return ERROR;
		}
		request.setAttribute("errStr", errStr);
		return "SUCCESS";
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
	
	/**
	 * 清除数据
	 * @param info
	 */
	private void clearFlowDatas(String info){
		Sql sql = new Sql("select ISOLD from G_INFOS where ID = ? ");
		sql.getParameters().add(new Parameter("ID",info)) ;
		String isOld = DBManager.getFieldStringValue(sql) ;
		//清除数据数据 2012.9.5 taolb  每次重新都需要删除之前分送的数据
		CommandCollection sqls = new CommandCollection();
		if("1".equals(isOld)){ //删除记录
			sql = new Sql("delete from G_INBOX where INFO_ID = ? ");
			sql.getParameters().add(new Parameter("INFO_ID",info)) ;
			sqls.add(sql) ;
		}else if("2".equals(isOld)){//更新记录
			sql = new Sql("update G_INBOX set  PRIORY = NULL , RDATE = NULL , DEPT_ID = NULL , FUSER_ID = 0 , ACTNAME = NULL , WF_ID = 0 , WFNODE_ID = 0 , WFNODE_CAPTION = NULL , SENDTYPE = 0  where INFO_ID = ? ");
			sql.getParameters().add(new Parameter("INFO_ID",info)) ;
			sqls.add(sql) ;
		}
		
		//清除G_INBOX
		sql = new Sql("select PID from G_INBOX where INFO_ID = ? ");
		sql.getParameters().add(new Parameter("INFO_ID",info)) ;
		String pid = DBManager.getFieldStringValue(sql) ;
		
		//清除G_PROUTE
		sql = new Sql("delete from G_PROUTE where PID = ? ");
		sql.getParameters().add(new Parameter("PID",pid)) ;
		sqls.add(sql) ;
		
		//清除G_PNODES
		sql = new Sql("delete from G_PNODES where INFO_ID = ? and ID <> 1");
		sql.getParameters().add(new Parameter("INFO_ID",info)) ;
		sqls.add(sql) ;
		
		//修改状态
		sql = new Sql("update G_INFOS set ISOLD = 1 where ID = ?");
		sql.getParameters().add(new Parameter("INFO_ID",info)) ;
		sqls.add(sql) ;
		
		if(sqls.size() > 0){
			DBManager.execute(sqls) ;
		}
	}
}
