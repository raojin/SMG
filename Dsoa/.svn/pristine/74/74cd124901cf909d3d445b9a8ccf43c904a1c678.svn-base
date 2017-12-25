package dsoap.web.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import xsf.Config;
import xsf.data.DBManager;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class SaveFlowAction extends Action {
    private static final long serialVersionUID = -1425126045895624773L;

    public String errStr = "";

    @Override
    public String execute() throws Exception {
        super.execute();
        String LblScript = "";
        // 在此处放置用户代码以初始化页面
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        try {
        	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
        	String DBMS = ConfigurationSettings.AppSettings("DBMS");
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myRead = _myConn.createStatement();
            String sql1 = "";
            String strObjclass = "";
            String strFlowName = "";
            String strMaxId = "1";
            String path = request.getContextPath();
            String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
            if (request.getParameter("id") != null) {
                // String sTmp = "http://" + request.getServerName() + ":" + request.getServerPort();
                // sTmp = sTmp.substring(0, sTmp.lastIndexOf("/"));
                // sTmp = sTmp.substring(0, sTmp.lastIndexOf("/"));
                // sTmp = sTmp.substring(0, sTmp.lastIndexOf("/"));
                String str1 = "<script language='javascript'>";
                str1 += "DS_Pane.Url='" + basePath + "service/wfservice?wsdl';";
                str1 += "DS_Pane.SetXmlFromID('" + request.getParameter("id") + "');";
                str1 += "document.all.DS_Pane.Resize();";
                str1 += "</script>";
                LblScript = str1;
                // System.out.println("流程路径 : " + basePath);
                request.setAttribute("LblScript", LblScript);
            } else if (request.getParameter("flowname") != null) {
                strObjclass = request.getParameter("objclass");
                strFlowName = request.getParameter("flowname").trim();
                sql1 = "SELECT WF_ID FROM WFDEFINITION WHERE WF_TYPEID='" + strObjclass + "' AND WF_CAPTION='" + strFlowName + "'";
                _myDs = _myRead.executeQuery(sql1);
                if (_myDs.next()) {
                    _myDs.close();
                    errStr += "<script language='javascript'>";
                    errStr += "alert('此流程名已存在！');";
                    errStr += "location.href='../index/blank.htm';";
                    errStr += "</script>";
                    return ERROR;
                }
                _myDs.close();
              //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
            	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
            		 sql1 = "SELECT MAXID FROM `MAXVALUE` WHERE TAG='WF_ID'";
                } else {
                	 sql1 = "SELECT MAXID FROM MAXVALUE WHERE TAG='WF_ID'";
                }
               // sql1 = "SELECT MAXID FROM MAXVALUE WHERE TAG='WF_ID'";
                _myDs = _myRead.executeQuery(sql1);
                if (_myDs.next()) {
                    strMaxId = _myDs.getString("MAXID");// M_Read["MAXID"].ToString();
                }
                _myDs.close();
                _myConn.setAutoCommit(false);
                try {
                	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                		 sql1 = "UPDATE `MAXVALUE` SET MAXID=MAXID+1 WHERE TAG='WF_ID'";
                    } else {
                    	 sql1 = "UPDATE MAXVALUE SET MAXID=MAXID+1 WHERE TAG='WF_ID'";
                    }
                   // sql1 = "UPDATE MAXVALUE SET MAXID=MAXID+1 WHERE TAG='WF_ID'";
                    _myRead.executeUpdate(sql1);
                    _myConn.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (_myConn != null) {
                        _myConn.rollback();
                    }
                    errStr += "<script language='javascript'>";
                    errStr += "alert('流程保存失败！');";
                    errStr += "location.href='../index/blank.htm';";
                    errStr += "</script>";
                    return ERROR;
                }
                _myConn.setAutoCommit(false);
                try {
                    sql1 = "INSERT INTO WFDEFINITION(WF_ID,WF_TYPEID,WF_CAPTION) VALUES(" + strMaxId + ",'" + strObjclass + "','" + strFlowName + "')";
                    _myRead.executeUpdate(sql1);
                    _myConn.commit();
                    // String sTmp = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getRequestURI();
                    // sTmp = sTmp.substring(0, sTmp.lastIndexOf("/"));
                    // sTmp = sTmp.substring(0, sTmp.lastIndexOf("/"));
                    String str1 = "<script language='javascript'>";
                    str1 += "DS_Pane.Url='" + basePath + "service/wfservice?wsdl';";
                    str1 += "DS_Pane.SetXmlFromID('" + strMaxId + "');";// 依次调用WFServiceImpl.GetSysDate();WFServiceImpl.GetRegCode();WFServiceImpl.GetWF()
                    str1 += "document.all.DS_Pane.Resize();";
                    str1 += "</script>";
                    LblScript = str1;
                    request.setAttribute("LblScript", LblScript);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (_myConn != null) {
                        _myConn.rollback();
                    }
                    errStr += "<script language=javascript>";
                    errStr += "alert('流程保存失败！');";
                    errStr += "location.href='../index/blank.htm';";
                    errStr += "</script>";
                    return ERROR;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        return SUCCESS;
    }

    public String getErrStr() {
        return errStr;
    }

    public void setErrStr(String errStr) {
        this.errStr = errStr;
    }

}
