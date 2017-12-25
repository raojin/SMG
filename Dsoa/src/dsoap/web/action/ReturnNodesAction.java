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
public class ReturnNodesAction extends Action {
	public String errStr = "";

	private DS_FLOWClass dsFlow;

	@Override
    public String execute() throws Exception
		{
		super.execute();
			Connection _myConn = null;
	        Statement _myRead = null;
	        ResultSet _myDs = null;
	        StringBuffer strSql = new StringBuffer();
	        String flowParms="";
			try {
				// 获取发送参数，创建发送对象
			 if (request.getParameter("flowParms") != null) {
				flowParms = URLDecoder.decode(request
						.getParameter("flowParms"), "utf-8");
				// 发送参数格式：<Root><Flow><Type>0</Type><Key>1346727307282000</Key><Objclass>651059</Objclass><UserID>737911</UserID><Pid>199592208</Pid><Pnid>2</Pnid><WfID>844</WfID></Flow></Root>
				// 要发送到的节点ID
				dsFlow = new DS_FLOWClass(flowParms);
				session.put("DSFLOW", dsFlow);
			}
			 else if (session.get("DSFLOW") != null) {
					dsFlow = (DS_FLOWClass) session.get("DSFLOW");
				}else {
				errStr = "<script language='javascript'>window.alert('流程信息错误！');top.window.close();</script>";
			}
			String userID=String.valueOf(dsFlow.iUserID);
			session.put("userID",userID);
			 _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
	         _myRead = _myConn.createStatement();
	         strSql.append("select * from (");	
	         strSql.append(" select t.WFNODE_ID, t.actname, t.UNAME, t.USER_ID, t.ID, t.DEPT_ID,t.pid");	
	         strSql.append(" from g_pnodes t where  t.PID = "+dsFlow.iPID+" and t.ID != "+dsFlow.iPnID+" ");	
	         strSql.append(" union all");	
	         strSql.append(" select t.WFNODE_ID, t.actname, t.UNAME, t.USER_ID, t.ID, t.DEPT_ID,t.pid");	
	         strSql.append(" from g_pnodes_history t where  t.PID = "+dsFlow.iPID+" and t.ID != "+dsFlow.iPnID+" ");	
	         strSql.append(" ) t order by t.ID desc");		
			_myDs = _myRead.executeQuery(strSql.toString());
			List list = new ArrayList();
			List listTemp = new ArrayList();
            while (_myDs.next()) {
            	HashMap map = new HashMap<String, String>();
            	if(listTemp.contains(_myDs.getString("WFNODE_ID").trim()+_myDs.getString("USER_ID").trim()))
            	{
            		continue;
            	}
            	listTemp.add(_myDs.getString("WFNODE_ID").trim()+_myDs.getString("USER_ID").trim());
            	map.put("nodeid",_myDs.getString("WFNODE_ID"));
            	map.put("nodename",_myDs.getString("actname"));
            	map.put("username",_myDs.getString("UNAME"));
            	map.put("userid",_myDs.getString("USER_ID"));
            	map.put("deptid",_myDs.getString("DEPT_ID"));
            	list.add(map);
            }
            _myDs.close();
            if(list.size()<1)
            {
            	errStr="没有可发送的节点";
            	return ERROR;
            }
            request.setAttribute("userlist", list);
			} catch (Exception e) {
				e.printStackTrace();
				errStr = e.getMessage();
				return ERROR;
			}
			finally {
				if (_myDs != null) {
	                try {
	                	_myDs.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }
	            }
	            if (_myRead != null) {
	                try {
	                    _myRead.close();
	                } catch (SQLException e1) {
	                    e1.printStackTrace();
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
			request.setAttribute("errStr", errStr);
			return "success";
		}
		public String Send()
		{
			
			return "success";
		}
  }
