package dsoap.web.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import net.sf.json.JSONObject;

import xsf.Config;
import xsf.data.DBManager;

import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class OpinionTemplateAction extends Action {
	
	public String errStr = "";

	private List templateList;

	public List getTemplateList() {
		return templateList;
	}

	public void setTemplateList(List templateList) {
		this.templateList = templateList;
	}

	@Override
	public String execute() throws Exception {
		super.execute();
		return SUCCESS;
	}
	
	public String GetAll() throws Exception {
		Connection _myConn = null;
		Statement _myRead = null;
		ResultSet _myDs = null;
		String strSql = "";
		super.execute();
		try {
			 _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
	         _myRead = _myConn.createStatement();
	         String userID=session.get("userID").toString();
	         strSql="select ID, USERID, CONTENT, ISPUBLIC, ORDERINDEX, type from G_OPINION_TEMPLATE t where t.ISPUBLIC= 1 or t.USERID="+userID+"";
	         _myDs= _myRead.executeQuery(strSql);
	         templateList=new ArrayList();
	         while (_myDs.next()) {
	        	 Map map=new HashMap();
	        	 map.put("ID", _myDs.getString("ID"));
	        	 map.put("USERID", _myDs.getString("USERID"));
	        	 map.put("CONTENT", _myDs.getString("CONTENT"));
	        	 map.put("ISPUBLIC", _myDs.getString("ISPUBLIC"));
	        	 map.put("ORDERINDEX", _myDs.getString("ORDERINDEX"));
	        	 map.put("type", _myDs.getString("type"));
	        	 templateList.add(map);
			}
	         return Action.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			errStr = e.getMessage();
		}
		finally
		{

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
		return ERROR;
	}

	public String Add() throws Exception {
		super.execute();
		Connection _myConn = null;
		Statement _myRead = null;
		String strSql = "";
		templateList=new ArrayList();
		Map map=new HashMap();
		try {
			 _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
	         _myRead = _myConn.createStatement();
	         long id= xsf.ID.get16bID();
	         String userID=session.get("userID").toString();
	         String content=request.getParameter("content");
	         strSql="INSERT INTO G_OPINION_TEMPLATE (ID,USERID,CONTENT,ISPUBLIC,ORDERINDEX,TYPE) VALUES ("+id+","+userID+",'"+content+"',0,0,1)";
	         _myRead.executeUpdate(strSql);
	         map.put("result", "0");
        	 templateList.add(map);
		} catch (Exception e) {
			e.printStackTrace();
			errStr = e.getMessage();
			map.put("result", "-999");
       	 	templateList.add(map);
			return ERROR;
		}
		finally
		{
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
		return SUCCESS;
	}
	
	public String Del() throws Exception {
		super.execute();
		Connection _myConn = null;
		Statement _myRead = null;
		String strSql = "";
		templateList=new ArrayList();
		Map map=new HashMap();
		try {
			 _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
	         _myRead = _myConn.createStatement();
	         String id= request.getParameter("ID");
	         strSql="delete from G_OPINION_TEMPLATE where id="+id+"";
	         _myRead.executeUpdate(strSql);
        	 map.put("result", "0");
        	 templateList.add(map);
		} catch (Exception e) {
			e.printStackTrace();
			errStr = e.getMessage();
			 map.put("result", "-999");
        	 templateList.add(map);
			return ERROR;
		}
		finally
		{
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
		return SUCCESS;
	}
}
