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
import xsf.data.DBManager;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.dsflow.model.DataTable;
import dsoap.log.SystemLog;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 * 根据待办参数和登陆用户获取登陆用户角色配置的可发送节点信息
 */
public class SelectNodeByRoleAction extends Action {
	public String errStr = "";
	private DS_FLOWClass dsFlow;

	@Override
	public String execute() throws Exception {
		super.execute();
		Connection _myConn = null;
		Statement _myRead = null;
		try {
			_myConn = DBManager.getConnection(Config.CONNECTION_KEY);
			_myRead = _myConn.createStatement();
			// 获取当前登录用户ID
			String userID = request.getParameter("userID").toString();
			String userDeptID = request.getParameter("userDeptID").toString();
			String infoID = request.getParameter("infoID").toString();
			String wfID = request.getParameter("wfID").toString();
			String objclass = request.getParameter("Objclass").toString();
			// 获取当前用户的角色
			String roles = "";
			String roleSQL = "select p.roleid from G_PRIVILEGE p where p.userid="
					+ userID + "";
			System.out.println("=====查询当前用户角色的SQL：" + roleSQL);
			ResultSet rs = _myRead.executeQuery(roleSQL);
			while (rs.next()) {
				if (!"".equals(rs.getString(1))) {
					roles += rs.getString(1);
				}
			}
			if (!"".equals(roles)) {
				// 拟稿人与当前用户的部门是否一致
				boolean inDept = false;
				String sqlInDept = "select count(*) from g_infos t where t.isold=1 and t.status=1 and t.id="
						+ infoID + " and t.maindept=" + userDeptID + "";
				System.out.println("=====查询拟稿人与当前用户的部门是否一致的SQL：" + sqlInDept);
				ResultSet rsInDept = _myRead.executeQuery(sqlInDept);
				if (rsInDept.next()) {
					if (rsInDept.getInt(1) > 0) {
						// 如果当前用户的部门与g_infos的maindept一致inDept=true;
						inDept = true;
					}
				}
				// nodeMap（节点ID，节点名称）
				HashMap nodeMap = new HashMap<String, String>();
				roles = roles.substring(0, roles.length() - 1);
				//查询该角色下配置节点信息
				String nodeSQL = "select t.wf_nodeid,t.wf_nodename,t.indept from role_wfnode t where t.wf_id="
						+ wfID + " and t.role_id in (" + roles + ")";
				System.out.println("=====查询该角色下配置节点信息的SQL：" + nodeSQL);
				ResultSet rsNode = _myRead.executeQuery(nodeSQL);
				while (rsNode.next()) {
					// 校验是否与拟稿人部门一致
					if ("1".equals(rsNode.getString("INDEPT").trim())) {
						if (inDept) {
							nodeMap.put(rsNode.getString("WF_NODEID"), rsNode
									.getString("WF_NODENAME"));
						}
					} else {
						nodeMap.put(rsNode.getString("WF_NODEID"), rsNode
								.getString("WF_NODENAME"));
					}
				}
				if (nodeMap.size() < 1) {
					errStr += "</br>没有可发送的节点。userid=" + userID;
					request.setAttribute("errStr", errStr);
					return "RESULT";
				}
				//将待办参数传递到后续页面
				request.setAttribute("nodes", nodeMap);
				request.setAttribute("userID", userID);
				request.setAttribute("userDeptID", userDeptID);
				request.setAttribute("infoID", infoID);
				request.setAttribute("wfID", wfID);
				request.setAttribute("objclass", objclass);
				return "SELECTNODE";
			} else {
				errStr += "</br>该用户未设置角色信息。userid=" + userID;
				request.setAttribute("errStr", errStr);
				return "RESULT";
			}
		} catch (Exception e) {
			request.setAttribute("errStr", errStr+e.getMessage());
			return "RESULT";
		} finally {
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
}
