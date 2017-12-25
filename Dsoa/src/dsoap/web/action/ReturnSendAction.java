package dsoap.web.action;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Node;

import xsf.Config;
import xsf.data.DBManager;
import xsf.data.IDataSource;
import xsf.log.Log;
import xsf.log.LogManager;
import xsf.platform.Logger;
import dsoap.dsflow.DS_FLOWClass;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.SysDataSource;
import dsoap.tools.webui.HtmlInputText;
import dsoap.tools.webui.Label;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class ReturnSendAction extends Action {
	public String errStr = "";

	private DS_FLOWClass dsFlow;

	@Override
    public String execute() throws Exception
		{
		super.execute();
		boolean isEnd = false;
		try {
			// 获取发送参数，创建发送对象
			if (session.get("DSFLOW") != null) {
				dsFlow = (DS_FLOWClass) session.get("DSFLOW");
			} else {
				errStr = "<script language='javascript'>window.alert('流程信息错误！');top.window.close();</script>";
			}
			// 要发送到的节点ID
			String toNodeID = request.getParameter("nodeid");
			String userID = request.getParameter("userid");
			String DeptID = request.getParameter("deptid");
			String BACKREASON = request.getParameter("idContent");
			dsFlow.send_BACKREASON=BACKREASON;
					// 设置要发送到的节点信息
					dsFlow.setSelectCurNodeID(Long.parseLong(toNodeID));
						String sustr = "";
						String sSendMethod = "";
							sustr += ";:" + toNodeID + ":"
									+ userID + "::::"
									+ DeptID + "";
							sSendMethod += "," + toNodeID + ":" + "0";
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
		} catch (Exception e) {
			e.printStackTrace();
			errStr = e.getMessage();
			return ERROR;
		}
		request.setAttribute("errStr", errStr);
		return "success";
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
