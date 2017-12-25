package dsoap.web.action;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.opensymphony.xwork2.ActionContext;

import xsf.Config;
import xsf.data.DBManager;
import xsf.data.IDataSource;
import dsoap.dsflow.DS_FLOWClass;
import dsoap.log.SystemLog;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.SysDataSource;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class AddSendingAction extends Action {

    public String errStr = "";
    private DS_FLOWClass dsFlow;
    private String iPid="";
    private String iPnid="";
    private String iG_InboxID="";
    private String iInfoID="";
    
    @Override
    public String execute() throws Exception {
        super.execute();
        if (session.get("DSFLOW") == null) {
            errStr = "<script language='javascript'>alert('流程信息错误！');top.window.close();</script>";
            return ERROR;
        }
        DS_FLOWClass dsFlow = (DS_FLOWClass) session.get("DSFLOW");
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        try {
        	_myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            // System.out.println("send_0 事务隔离级别:" + _myConn.getTransactionIsolation() + "    自动提交：" + _myConn.getAutoCommit());
             _myConn.setAutoCommit(false);
             _myRead = _myConn.createStatement();
            String sustr = request.getParameter("UList");// 用户列表 （SelectUser.jsp 提交格式【:节点ID:用户ID:流程节点名:部门名:用户名（部门名）:部门ID】多个时以“;”分割）
            String sSendMethod = request.getParameter("SendMethod");// 发送方式 （SelectUserAction 中设置的格式【节点ID:发送方式】多个时以“,”分割）
            String sNodeDate = request.getParameter("txtNode");// （SelectUser.jsp 提交）
            dsFlow.strPriSend = "," + request.getParameter("TxtPriSend");
            String SMS = request.getParameter("SMSContent");
            String strUserIp = request.getServerName();
            String sendtype =request.getParameter("sendtype");	//发送类型1：换人，2：补发
            //根据ID查询传入的G_INBOX数据
            this.iG_InboxID=dsFlow.customParameter.get("g_inboxID").toString();
            xsf.data.DataTable dt = DBManager.getDataTable("select INFO_ID,OBJCLASS,USER_ID,PID,PNID,WF_ID,WFNODE_ID from G_INBOX where ID=" + iG_InboxID);
            if (dt.getRows().size() > 0) {
                xsf.data.DataRow dr = dt.getRows().get(0);
                this.iInfoID=dr.getString("INFO_ID");
                this.iPid=dr.getString("PID");
                this.iPnid=dr.getString("PNID");
            }
            // ------------------------------------------------------------------------
            dsFlow.strIP = strUserIp;
            dsFlow.strMAC = SystemLog.GetNetCardAddress(strUserIp);
            dsFlow.sSmsContent = SMS;
            // ---吴红亮 添加 开始
            String sendMethod = processSendMethod(dsFlow.NextNodeInfoXml, sSendMethod, sustr);
            if (!sSendMethod.equals(sendMethod)) {
                int index = sendMethod.indexOf("$");
                sSendMethod = sendMethod.substring(0, index);
                sustr = sendMethod.substring(index + 1, sendMethod.length());
            }
            // ---吴红亮 添加 结束
            if (!"".equals(sustr)) {
                    try {
                    	dsFlow.setSendInfo(sustr, sSendMethod, sNodeDate);
                    	Map sendParms=new HashMap<String, String>();
                    	String[] sUserList = sustr.split(";");
                        for (String s : sUserList) {
                            if ("".equals(s)) {
                                continue;
                            }
                            //选人页面选择的人和部门
                            String sUserID = s.split(":")[2];
                            String sDeptID = s.split(":")[6];
                            sendParms.put("userid", sUserID);
                            sendParms.put("deptid", sDeptID);
                            if("1".equals(sendtype))	 //换人发送
                            {
                            	if(reUserSend(_myConn, sendParms))
                            	{
                            		_myConn.commit();
                            	}
                            	else
                            	{
                            		_myConn.rollback();
                            	}
                            }
                            else if("2".equals(sendtype))	//补发发送
                            {
                            	if(addSend(_myConn, sendParms))
                            	{
                            		_myConn.commit();
                            	}
                            	else
                            	{
                            		_myConn.rollback();
                            	}
                            }
                        }
                    } catch (Exception ex) {
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
                        ex.printStackTrace();
                        request.setAttribute("msg", "发送失败："+ex.getMessage());
                        return "SendBack";
                        // throw e;
                    }
            }
            return SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            errStr = dsFlow.sErrorMessage;
            return ERROR;
        }
    }
    /**
     * 换人发送
     * @param _myRead
     * @param sendParms
     * @return
     * @throws Exception 
     */
    private boolean reUserSend(Connection _myConn,Map sendParms) throws Exception
    {
    	Statement _myRead = null;
        ResultSet _myDs = null;
        boolean result=false;
		try {
			_myRead = _myConn.createStatement();
			String _cmdStr = "";
			//修改待办接收人
			_cmdStr = "UPDATE  G_INBOX t SET t.RECEIVE_USERNAME='"+this.getUserName(Long.parseLong(sendParms.get("userid").toString()))+"',t.USER_ID="+sendParms.get("userid")+",t.DEPT_ID="+sendParms.get("deptid")+"  WHERE t.ID="+this.iG_InboxID;
			_myRead.executeUpdate(_cmdStr);
			//修改G_Pnodes 接收人
			_cmdStr = "UPDATE  G_PNODES t SET t.UNAME='"+this.getUserName(Long.parseLong(sendParms.get("userid").toString()))+"',t.USER_ID="+sendParms.get("userid")+",t.DEPT_ID="+sendParms.get("deptid")+" ,t.MUSER_ID="+sendParms.get("userid")+",t.MDEPT_ID="+sendParms.get("deptid")+" WHERE t.INFO_ID="+this.iInfoID+" AND t.PID="+this.iPid+" AND t.ID  ="+this.iPnid;
			_myRead.executeUpdate(_cmdStr);
			result =true;
		} catch (Exception e) {
			e.printStackTrace();
			if (_myConn != null) {
                _myConn.rollback();
            }
            throw e;
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
		}
		return result;
    }
    /**
     * 补发发送
     * @param _myRead
     * @param sendParms
     * @return
     * @throws Exception 
     */
    private boolean addSend(Connection _myConn,Map sendParms) throws Exception
    {
    	Statement _myRead = null;
        ResultSet _myDs = null;
        boolean result=false;
		try {
			_myRead = _myConn.createStatement();
			StringBuffer _cmdStr = new StringBuffer();
			long g_pnodesID=this.getMaxPNID(_myConn)+1;
			//插入补发的g_pnodes数据
			_cmdStr.append("insert into g_pnodes(PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,");
			_cmdStr.append("	COMPLISHDATE,WHOHANDLE,SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,");
			_cmdStr.append("	FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,QSRQ,WAITCOUNT,LOCKFLOW,");
			_cmdStr.append("	FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,");
			_cmdStr.append("	WFLINE_TYPE,ISCS,OPINIONSTATUS,HZSTOP)");
			_cmdStr.append(" select PID,"+g_pnodesID+",ACT_ID,ATYPE,"+sendParms.get("userid")+",DAYS,TYJ,PDATE,"+sendParms.get("deptid")+",HANDLEWAY,1,COMPLISHDATE,");
			_cmdStr.append("	WHOHANDLE,SIGNED,"+sendParms.get("userid")+",RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,");
			_cmdStr.append("	ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,QSRQ,WAITCOUNT,LOCKFLOW,");
			_cmdStr.append("	FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,'"+this.getUserName(Long.parseLong(sendParms.get("userid").toString()))+"',ISSIGN,SENDTIME,");
			_cmdStr.append("	"+sendParms.get("deptid")+",WFLINE_TYPE,ISCS,OPINIONSTATUS,HZSTOP");
			_cmdStr.append(" from g_pnodes");
			_cmdStr.append(" where INFO_ID = "+this.iInfoID+" AND PID = "+this.iPid+" AND ID = "+this.iPnid);
			_myRead.executeUpdate(_cmdStr.toString());
			//插入补发的G_INBOX数据
			_cmdStr = new StringBuffer();
			long G_INBOX_ID = this.getMaxValue("G_INBOX", 1,_myConn);
			_cmdStr.append("insert into g_inbox (ID, INFO_ID, USER_ID, PID, PNID, PRIORY, RDATE, HANDLEWAY, DEPT_ID, FUSER_ID, ");
			_cmdStr.append("	UTYPE, STATUS, ACTNAME, WF_ID, WFNODE_ID, WFNODE_CAPTION, WFNODE_WAIT, ");
			_cmdStr.append("	TIMETYPE,TIMESPAN,DAYS,SENDTYPE, QSRQ, WAITCOUNT, LOCKFLOW, FNODE, EDATE, ISZNG, ");
			_cmdStr.append("	ATYPE, ISSQHS, BT, DELETED, HASCONTENT, URGENT, SWH, WH, OBJCLASS, FPNID, HSVIEWDATE, ");
			_cmdStr.append("	SMSSTATUS, FUSERID, INTIME, MODULE_ID, SUBJECT, USERID, RECEIVE_USERNAME, SEND_USERNAME) ");
			_cmdStr.append(" select 	"+G_INBOX_ID+", INFO_ID, "+sendParms.get("userid")+", PID, PNID, PRIORY, RDATE, HANDLEWAY, "+sendParms.get("deptid")+", FUSER_ID, UTYPE, 1,");
			_cmdStr.append("	ACTNAME, WF_ID, WFNODE_ID, WFNODE_CAPTION, WFNODE_WAIT, TIMETYPE, TIMESPAN, DAYS, ");
			_cmdStr.append("	SENDTYPE, QSRQ, WAITCOUNT, LOCKFLOW, FNODE, EDATE, ISZNG, ATYPE, ISSQHS, BT, ");
			_cmdStr.append("	DELETED, HASCONTENT, URGENT, SWH, WH, OBJCLASS, FPNID, HSVIEWDATE, SMSSTATUS, ");
			_cmdStr.append("	FUSERID, INTIME, MODULE_ID, SUBJECT, USERID,'"+this.getUserName(Long.parseLong(sendParms.get("userid").toString()))+"', SEND_USERNAME ");
			_cmdStr.append(" from g_inbox ");
			_cmdStr.append(" where ID= "+this.iG_InboxID);
			_myRead.executeUpdate(_cmdStr.toString());
			result=true;
		} catch (Exception e) {
            e.printStackTrace();
            if (_myConn != null) {
                _myConn.rollback();
            }
            throw e;
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
		}
		return result;
    }
    private String processSendMethod(Document nextNodeInfoXml, String sendMethod, String users) {
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

 // 获取G_PNODES中最大PNID(共享事务)
    private long getMaxPNID(Connection _myConn) throws Exception {
    	Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        long maxId = 0;
        try {
        	_myRead = _myConn.createStatement();
            _cmdStr = "SELECT MAX(ID) MAXID FROM G_PNODES WHERE PID=" + this.iPid;
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                if (_myDs.getString("MAXID") != null) {
                    maxId = _myDs.getInt("MAXID");
                }
            }
        } catch (Exception e) {
        	e.printStackTrace();
            throw e;
        } finally {
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
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
        }
        return maxId;
    }
    
    // 获取MAXVALUE(查询，更新必须在一个事务中完成，所以沒用 DBManager)
    private long getMaxValue(String sTag, int iCount,Connection _myConn) throws Exception {
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        long returnValue = -1;
        try {
        	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 开始
        	String DBMS = ConfigurationSettings.AppSettings("DBMS");
            _myRead = _myConn.createStatement();
            if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
            	_cmdStr = "SELECT TAG,MAXID FROM `MAXVALUE` WHERE TAG='" + sTag + "'";
            } else {
            	_cmdStr = "SELECT TAG,MAXID FROM MAXVALUE WHERE TAG='" + sTag + "'";
            }
            //_cmdStr = "SELECT TAG,MAXID FROM MAXVALUE WHERE TAG='" + sTag + "'";
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                returnValue = _myDs.getInt("MAXID");
            }
            if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
            	_cmdStr = "UPDATE `MAXVALUE` SET MAXID=MAXID + " + iCount + " WHERE TAG='" + sTag + "'";
            } else {
            	_cmdStr = "UPDATE MAXVALUE SET MAXID=MAXID + " + iCount + " WHERE TAG='" + sTag + "'";
            }
            //_cmdStr = "UPDATE MAXVALUE SET MAXID=MAXID + " + iCount + " WHERE TAG='" + sTag + "'";
            _myRead.executeUpdate(_cmdStr);
            _myConn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
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
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        if (returnValue == -1) {
            throw new Exception("获取MAXVALUE：" + sTag + "出错");
        }
        return returnValue;
    }
    private String getUserName(long uid) throws Exception {
        // String _cmdStr = "";
        String sName = "";
        // _cmdStr = "SELECT UNAME FROM G_USERS WHERE ID=" + uid;
        // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("USER_ID", uid);
        xsf.data.DataTable dt = sqlDataSource.query("getUserName");
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            sName = dr.getString("UNAME") != null ? dr.getString("UNAME") : "";
        }
        return sName;
    }

}
