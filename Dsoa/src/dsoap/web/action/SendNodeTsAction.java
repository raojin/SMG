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
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class SendNodeTsAction extends Action {
	public String errStr = "";

	private DS_FLOWClass dsFlow;
	private List depUserList;

	@Override
	public String execute() throws Exception {
		super.execute();
		boolean isEnd = false;
		try {
			// 获取发送参数，创建发送对象
			if (request.getParameter("toNodeID") != null) {
				// 要发送到的节点ID
				String toNodeID = URLDecoder.decode(request.getParameter("toNodeID"), "utf-8");
				String userID = request.getParameter("userID").toString();
				String userDeptID = request.getParameter("userDeptID").toString();
				String infoID = request.getParameter("infoID").toString();
				String wfID = request.getParameter("wfID").toString();
				String objclass = request.getParameter("objclass").toString();
				StringBuffer sb = new StringBuffer();
				
				
				try {
					sb.append("<Root>");
					sb.append("<Flow>");
					sb.append("<Type>0</Type>");
					sb.append("<Key>" + infoID + "</Key>");
					sb.append("<Objclass>"+objclass+"</Objclass>");
					sb.append("<UserID>" + userID + "</UserID>");
					sb.append("<Pid>-1</Pid>");
					sb.append("<Pnid>" + 0 + "</Pnid>");
					sb.append("<WfID>" + wfID + "</WfID>");
					sb.append("<RUserID></RUserID>");
					sb.append("</Flow>");
					sb.append("</Root>");
				} catch (Exception ex) {
					errStr += "</br>流程参数错误：" + ex.getMessage();
					return "RESULT";
				}
				try {
					dsFlow = new DS_FLOWClass(sb.toString());
					// System.out.println("提交类型:" + dsFlow.iSendType);
					// 设置要发送到的节点信息
					dsFlow.setSelectCurNodeID(Long.parseLong(toNodeID));
					Node nextNode = dsFlow.NextNodeInfoXml
							.selectSingleNode("Nodes/Node");
					String nodeType = nextNode.valueOf("@NodeType");
					// System.out.println("节点类型:" + nodeType);
					if ("0".equals(nodeType)) {
						// 办结发送
						dsFlow.sendToEnd();
					} else {
						String sustr = "";
						String sSendMethod = "";
						sustr += ";:" + toNodeID + ":"
						+ userID + ":" + "" + ":::" + userDeptID;
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
						
						//初始化数据信息 taolb 2012.9.5  start
						clearFlowDatas(infoID);
						//初始化数据信息 taolb 2012.9.5  end
						
						
						// 一般发送
						dsFlow.setSendInfoIsTS(sustr, sSendMethod, sNodeDate);
						dsFlow.send();
						//将该文件的isold改为0
						Connection _myConn = null;
						Statement _myRead = null;
						try {
							_myConn = DBManager.getConnection(Config.CONNECTION_KEY);
							_myRead = _myConn.createStatement();
							String infoIDSQL = "update G_INFOS  set ISOLD = 2 where ID= " + infoID + " and ISOLD =1 and STATUS = 1";
							//System.out.println("=====将该文件的isold改为0的SQL：" + infoIDSQL);
							int updateInfo = _myRead.executeUpdate(infoIDSQL);
							if(updateInfo>0)
							{
								try {
								String infoSQL = "SELECT D.NAME AS MODULE_NAME,A.BT,A.STATUS,A.RDATE,C.NAME AS F_USERNAME,B.MODULE_ID,A.INFO_ID,A.PID,A.PNID FROM G_INBOX A,G_INFOS B ,G_USERINFO C,G_MODULE D WHERE A.INFO_ID = B.ID AND B.MODULE_ID = D.ID AND A.FUSER_ID = C.ID AND A.STATUS IN (1,2) and a.info_id="+infoID+" AND A.USER_ID="+userID+" AND B.ROWSTATE >-1";
								//System.out.println("=====查询待办的SQL：" + infoSQL);
								ResultSet rs=_myRead.executeQuery(infoSQL);
								if(rs.next())
								{
									String moduleId=rs.getString("MODULE_ID")==null?"":rs.getString("MODULE_ID").toString();
									String pid=rs.getString("PID")==null?"":rs.getString("PID").toString();
									String pnid=rs.getString("PNID")==null?"":rs.getString("PNID").toString();
//									String ezweb=ConfigurationSettings.getServerInfo(request);
//									String url=ezweb+"/modules/system/formControl.jsp?moduleId="+moduleId+"&pid="+pid+"&pnid="+pnid+"&Info_ID="+infoID;
//									response.sendRedirect(url);
									
									//跳转页面的处理
									request.setAttribute("pid", pid) ;
									request.setAttribute("pnid", pnid) ;
									request.setAttribute("success", "1") ;
								}
								} catch (Exception e) {
									errStr += "</br>发送成功，跳转待办文件失败。"+e.getMessage();
								}
							}
							else
							{
								errStr += "</br>修改isold状态失败";
							}
						} catch (Exception e) {
							errStr += "</br>修改isold状态失败：" + e.getMessage();
						}
						finally
						{
							if (_myRead != null) {
								try {
									_myRead.close();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
							if (_myConn != null) {
								try {
									_myConn.close();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						
						}
					}
				} catch (Exception e) {
					errStr += "</br>发送失败：" + e.getMessage();
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
