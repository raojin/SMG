package dsoap.web.action;

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
import xsf.data.CommandCollection;
import xsf.data.DBManager;
import xsf.data.IDataSource;
import xsf.data.Sql;
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
public class ReturnInBoxAction extends Action {
    private static final long serialVersionUID = 4266378012177257107L;
    public Label LabJavaScriptString = new Label();
    public HtmlInputText txtRetualValue = new HtmlInputText();
    public Label labScriptError = new Label();
    public String errStr = "";

    @Override
    public String execute() throws Exception {
        java.util.Date now = new java.util.Date();
        super.execute();
        String strPid = request.getParameter("pid");
        String strPnid = request.getParameter("pnid");
        String strInfoId = request.getParameter("info_id");
        String strIsAll = request.getParameter("isall");
        String sUser_ID = request.getParameter("userid");
        String isth = request.getParameter("isth");
        String reason = request.getParameter("TxtReason");
        // 吴红亮 修改 开始
        // String strFPnid = request.getParameter("fpnid");
        String strFPnid = "";
        String returnType = request.getParameter("returnType");
        //退回的类型 （1：错发回收，null为页面退回）zdz
        String return_type = request.getParameter("return_type");
        // 吴红亮 修改 结束
        // ----------------------------------------------------------
        String strSql = "";
        String strMaxId = "0";
        // String strFnode = "";
        String strFFnode = "";
        String strEdate = "";
        String strFFPnid = "0";
        String strMaxYjId = "0";
        String strMaxActLogId = "0";
        String strAct = "";
        String strSendType = "0";
        String strReason = "";
        String DeptId = "";
        String Caption = "";
        String WfId = "";
        String WfNodeId = "";
        String TimeSpan = "0";
        String TimeType = "0";
        String Fuserid = "";
        String Muserid = "";
        String wfNodeType="";
        String wfLineType="";
        // boolean bChild = false;
        // boolean bBrother = false;
        if (isth != null && "1".equals(isth)) {
            strAct = "退回";
            strSendType = "2";
            if (reason != null) {
                strReason = reason.trim();
            }
        } else {
            strAct = "回收";
        }
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _myStr = "";
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myRead = _myConn.createStatement();
            // -------------------------------------------------------------------------------------- 查找文件拟稿人
            strSql = "SELECT USER_ID FROM G_INFOS WHERE ID=" + strInfoId;
            _myDs = _myRead.executeQuery(strSql);
            if (_myDs.next()) {
                Fuserid = _myDs.getObject("USER_ID") != null ? _myDs.getString("USER_ID") : "";
            }
            _myDs.close();
            //查询要回收的节点信息
            strSql = "select A.WFNODE_CAPTION,A.WFLINE_TYPE,B.WFNODE_NODETYPE From g_pnodes A ,WFNODELIST B WHERE A.WF_ID=B.WF_ID AND A.WFNODE_ID=B.WFNODE_ID AND A.PID="+strPid+" AND A.ID="+strPnid+"";
            _myDs = _myRead.executeQuery(strSql);
            if (_myDs.next()) {
                wfNodeType = _myDs.getObject("WFNODE_NODETYPE") != null ? _myDs.getString("WFNODE_NODETYPE") : "";
                wfLineType = _myDs.getObject("WFLINE_TYPE") != null ? _myDs.getString("WFLINE_TYPE") : "";
            }
            _myDs.close();
            // -------------------------------------------------------------------------------------- 查找上一节点的ID
            // 吴红亮 添加 开始
            strSql = "SELECT FID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid;
            _myDs = _myRead.executeQuery(strSql);
            if (_myDs.next()) {
                strFPnid = _myDs.getObject("FID") != null ? _myDs.getString("FID") : "";
            }
            _myDs.close();
            // 吴红亮 添加 结束
            // -------------------------------------------------------------------------------------- 查找上一节点的详细数据
            String DBMS = ConfigurationSettings.AppSettings("DBMS");
            strSql = "SELECT B.WFNODE_NODETYPE,A.FNODE,A.FID,A.DEPT_ID,A.MDEPT_ID,A.WFNODE_CAPTION,A.WF_ID,A.WFNODE_ID,A.TIMESPAN,A.TIMETYPE,A.USER_ID,A.MUSER_ID,A.EDATE  ";
            strSql += "FROM G_PNODES A,WFNODELIST B WHERE A.WF_ID=B.WF_ID AND A.WFNODE_ID=B.WFNODE_ID AND A.PID=" + strPid + " AND A.ID=" + strFPnid;
            _myDs = _myRead.executeQuery(strSql);
            if (_myDs.next()) {
                strFFnode = _myDs.getObject("FNODE") != null ? _myDs.getString("FNODE") : "";
                strFFPnid = _myDs.getObject("FID") != null ? _myDs.getString("FID") : "";
                // DeptId = _myDs.getObject("DEPT_ID") != null ? _myDs.getString("DEPT_ID") : "";
                DeptId = _myDs.getObject("MDEPT_ID") != null ? _myDs.getString("MDEPT_ID") : "";
                Caption = _myDs.getObject("WFNODE_CAPTION") != null ? _myDs.getString("WFNODE_CAPTION") : "";
                // -----------------------------------------------------------
                Caption = xsf.Value.getString(Caption, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
                WfId = _myDs.getObject("WF_ID") != null ? _myDs.getString("WF_ID") : "";
                WfNodeId = _myDs.getObject("WFNODE_ID") != null ? _myDs.getString("WFNODE_ID") : "";
                TimeSpan = _myDs.getObject("TIMESPAN") != null ? _myDs.getString("TIMESPAN") : "0";
                TimeType = _myDs.getObject("TIMETYPE") != null ? _myDs.getString("TIMETYPE") : "0";
                // Muserid = _myDs.getObject("USER_ID") != null ? _myDs.getString("USER_ID") : "";
                Muserid = _myDs.getObject("MUSER_ID") != null ? _myDs.getString("MUSER_ID") : "";
                Date edate = _myDs.getDate("EDATE");
                // 吴红亮 更新 开始 ---------------------------
                strEdate = dsoap.dsflow.DS_FLOWClass.processDate(edate == null ? "NULL" : "NOW", DBMS, now);
                // 吴红亮 更新 结束 ---------------------------
            }
            _myDs.close();
            // -------------------------------------------------------------------------------------- 查找上上节点的发送人
            if (!"0".equals(strFFPnid)) {
                strSql = "SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strFFPnid;
                _myDs = _myRead.executeQuery(strSql);
                if (_myDs.next()) {
                    Fuserid = _myDs.getObject("USER_ID") != null ? _myDs.getString("USER_ID") : "";
                }
                _myDs.close();
            }
            // -------------------------------------------------------------------------------------- 查找上一节点的最大意见ID
            strSql = "SELECT MAX(ID) MAXID FROM G_OPINION WHERE PID=" + strPid + " AND PNID=" + strFPnid;
            _myDs = _myRead.executeQuery(strSql);
            if (_myDs.next()) {
                if (_myDs.getObject(1) != null) {
                    strMaxYjId = _myDs.getString("MAXID");
                }
            }
            _myDs.close();
            // -------------------------------------------------------------------------------------- 获取G_INBOX最大ID,并+1
          //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
        	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
        		strSql = "SELECT MAXID FROM `MAXVALUE` WHERE TAG='G_INBOX'";
            } else {
            	strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
            }
            //strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
            _myDs = _myRead.executeQuery(strSql);
            if (_myDs.next()) {
                strMaxId = _myDs.getObject("MAXID") != null ? _myDs.getString("MAXID") : "";
            }
            _myDs.close();
          //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
        	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
        		strSql = "UPDATE `MAXVALUE` SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
            } else {
            	strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
            }
            //strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
            _myRead.executeUpdate(strSql);
            // -------------------------------------------------------------------------------------- 全部回收(oa.js的isALL=0 不是全部回收)
            if ("1".equals(strIsAll)) {
                // // 查找已完成儿子节点
                // strSql = "SELECT ID FROM G_PNODES WHERE PID=" + strPid + " AND FID=" + strFPnid + " AND STATUS=-1";
                // _myDs = _myRead.executeQuery(strSql);
                // if (_myDs.next()) {
                // bBrother = true;
                // }
                // _myDs.close();
                // // ---------------------------
                // if (bBrother) {
                // if (returnType == null) {
                // errStr = "<script language=javascript>\n";
                // errStr += "alert('此文件已被后续人员处理完毕,无法全部回收!请使用单步回收!')\n";
                // errStr += "top.close()";
                // errStr += "</script>\n";
                // } else {
                // errStr = "0";
                // }
                // return ERROR;
                // }
                // // ---------------------------
                // // 获取所有字节点列表
                // String strChilds = "";
                // String strChildsUser = "";
                // String strTHChildsUser = "";
                // String strChildsAct = "";
                // strSql = "SELECT A.ID,C.USER_ID,C.ACTNAME,D.USER_ID THUSER_ID FROM G_PROUTE A,G_INBOX B,G_PNODES C,G_PNODES D WHERE A.PID=B.PID AND A.ID=B.PNID AND A.PID=" + strPid + " AND A.PID=C.PID AND A.ID=C.ID AND A.FID=" + strFPnid + " AND D.ID=A.FID AND D.PID=A.PID";
                // _myDs = _myRead.executeQuery(strSql);
                // while (_myDs.next()) {
                // strChilds += _myDs.getString("ID") + ",";
                // strChildsUser += _myDs.getString("USER_ID") + ",";
                // strTHChildsUser += _myDs.getString("USER_ID") + ",";
                // strChildsAct += _myDs.getString("ACTNAME") + ",";
                // }
                // _myDs.close();
                // // ---------------------------
                // if (strChilds.length() > 0) {
                // strChilds = strChilds.substring(0, strChilds.length() - 1);
                // strChildsUser = strChildsUser.substring(0, strChildsUser.length() - 1);
                // strTHChildsUser = strTHChildsUser.substring(0, strTHChildsUser.length() - 1);
                // strChildsAct = strChildsAct.substring(0, strChildsAct.length() - 1);
                // }
                // String[] strArrChilds = strChilds.split(",");
                // String[] strArrChildsUser;
                // if ("退回".equals(strAct)) {
                // strArrChildsUser = strTHChildsUser.split(",");
                // } else {
                // strArrChildsUser = strChildsUser.split(",");
                // }
                // String[] strArrChildsAct = strChildsAct.split(",");
                // // if (strFFPnid == "0") {
                // // strChilds = strChilds + "," + strFPnid;
                // // }
                // // ---------------------------
                // // 取得可用的G_ACT_LOG表MAXID.并加i
                // strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG = 'G_ACT_LOG'";
                // _myDs = _myRead.executeQuery(strSql);
                // if (_myDs.next()) {
                // strMaxActLogId = _myDs.getString("MAXID");
                // }
                // _myDs.close();
                // // ---------------------------
                // strSql = "UPDATE MAXVALUE SET MAXID = MAXID + " + strArrChilds.length + " WHERE TAG ='G_ACT_LOG'";
                // _myRead.executeUpdate(strSql);
                // // ---------------------------
                // /** * 事务开始 * */
                // _myConn.setAutoCommit(false);
                // try {
                // boolean isok = true;
                // // 回收需要执行的ＳＱＬ
                // for (int i = 0; i < strArrChilds.length; i++) {
                // // ----------------------------------------------------------------------
                // DS_FLOWClass.doCommand(GetSqlFromBack(strPid, strArrChilds[i]), _myRead);
                // // ----------------------------------------------------------------------
                // }
                // for (int i = 0; i < strArrChilds.length; i++) {
                // strSql = "INSERT INTO G_ACT_LOG(ID,INFO_ID,USER_ID,ACT_USER_ID,ACTNAME,ACT,ACTTIME,BZ) VALUES (" + (Integer.parseInt(strMaxActLogId) + i) + "," + strInfoId + "," + sUser_ID + "," + strArrChildsUser[i] + ",'" + strArrChildsAct[i] + "','" + strAct + "',";
                // if ("SYBASE".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else if ("ORACLE".equals(DBMS)) {
                // strSql += "SYSDATE,";
                // } else if ("SQLSERVER".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else {
                // strSql += "SYSDATE,";
                // }
                // strSql += "'" + strReason + "')";
                // _myRead.executeUpdate(strSql);
                // // 回收效验
                // strSql = "SELECT ID FROM G_INBOX WHERE PID=" + strPid + " AND PNID=" + strArrChilds[i];
                // _myDs = _myRead.executeQuery(strSql);
                // if (!_myDs.next()) {
                // isok = false;
                // }
                // _myDs.close();
                // }
                // strSql = "DELETE G_PNODES WHERE PID=" + strPid + " AND ID IN (" + strChilds + ")";
                // _myRead.executeUpdate(strSql);
                // strSql = "DELETE G_INBOX WHERE PID=" + strPid + " AND PNID IN (" + strChilds + ")";
                // _myRead.executeUpdate(strSql);
                // strSql = "DELETE G_PROUTE WHERE PID=" + strPid + " AND ID IN (" + strChilds + ")";
                // _myRead.executeUpdate(strSql);
                // strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + strFPnid + " AND ID=" + strMaxYjId;
                // _myRead.executeUpdate(strSql);
                // strSql = "DELETE G_OPINION WHERE PID=" + strPid + " AND PNID IN (" + strChilds + ")";
                // _myRead.executeUpdate(strSql);
                // // 没有未完成儿子，在G_INBOX增加自己记录
                // if (!bBrother) {
                // // if (strFFPnid == "0") {
                // // strSql = "DELETE G_PNODES WHERE PID=" + strPid;
                // // M_Comm.CommandText = strSql;
                // // M_Comm.ExecuteNonQuery();
                // // strSql = "DELETE G_INBOX WHERE PID=" + strPid;
                // // M_Comm.CommandText = strSql;
                // // M_Comm.ExecuteNonQuery();
                // // strSql = "DELETE G_PROUTE WHERE PID=" + strPid;
                // // M_Comm.CommandText = strSql;
                // // M_Comm.ExecuteNonQuery();
                // // strSql = "DELETE G_OPINION WHERE PID=" + strPid;
                // // M_Comm.CommandText = strSql;
                // // M_Comm.ExecuteNonQuery();
                // // strSql = "UPDATE G_INFOS SET STATUS=0 WHERE ID=" + strInfoId + "";
                // // M_Comm.CommandText = strSql;
                // // M_Comm.ExecuteNonQuery();
                // // } else
                // {
                // strSql = "UPDATE G_PNODES SET STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + strFPnid;
                // _myRead.executeUpdate(strSql);
                // strSql = "INSERT INTO G_INBOX ";
                // strSql += "(ID,INFO_ID,DEPT_ID,USER_ID,FUSER_ID,UTYPE,HANDLEWAY,PID,PNID,STATUS,PRIORY,RDATE,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,TIMETYPE,TIMESPAN,DAYS,SENDTYPE,WAITCOUNT,EDATE,FNODE) ";
                // strSql += "VALUES (" + strMaxId + "," + strInfoId + "," + DeptId + "," + Muserid + "," + Fuserid + ",0,0," + strPid + "," + strFPnid + ",1,0,";
                // if ("SYBASE".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else if ("ORACLE".equals(DBMS)) {
                // strSql += "SYSDATE,";
                // } else if ("SQLSERVER".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else {
                // strSql += "GETDATE(),";
                // }
                // strSql += "'" + Caption + "'," + WfId + "," + WfNodeId + ",'" + Caption + "',0," + TimeType + "," + TimeSpan + ",0," + strSendType + ",0," + strEdate + ",'" + strFFnode + "')";
                // _myRead.executeUpdate(strSql);
                // // 取G_INFOS信息
                // _myStr = "select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId;
                // _myDs = _myRead.executeQuery(_myStr);
                // if (_myDs.next()) {
                // strSql = "update g_inbox set BT='" + _myDs.getString("BT") + "',DELETED=" + _myDs.getString("DELETED") + ",HASCONTENT=" + _myDs.getString("HASCONTENT") + ",URGENT=" + _myDs.getString("URGENT") + ",SWH='" + _myDs.getString("SWH") + "',WH='" + _myDs.getString("WH") + "',OBJCLASS='" + _myDs.getString("OBJCLASS") + "' where id=" + strMaxId;
                // }
                // _myDs.close();
                // _myRead.executeUpdate(strSql);
                // }
                // }
                // // 回收校验
                // if (isok) {
                // _myConn.commit();
                // } else {
                // throw (new Exception("该文件已被处理，无法回收！"));
                // }
                // // ----------------------------
                // if (returnType == null) {
                // errStr = "<script language=javascript>\n";
                // if (isth != null && "1".equals(isth)) {
                // txtRetualValue.setValue("OK");
                // errStr += "alert('退回成功!')\n";
                // errStr += "top.close()\n";
                // } else {
                // errStr += "alert('回收成功!')\n";
                // }
                // errStr += "top.close()\n";
                // errStr += "</script>\n";
                // } else {
                // if (isth != null && "1".equals(isth)) {
                // errStr = "1";
                // } else {
                // errStr = "2";
                // }
                // }
                // return ERROR;
                // // ---------------------------
                // } catch (Exception es) {
                // _myConn.rollback();
                // // ---------------------------
                // if (returnType == null) {
                // errStr = "<script language=javascript>\n";
                // if (isth != null && "1".equals(isth)) {
                // errStr += "alert('退回失败!:" + es.getMessage() + "')\n";
                // errStr += "top.close()\n";
                // } else {
                // errStr += "alert('回收失败!" + es.getMessage() + strSql + "')\n";
                // }
                // errStr += "top.close()\n";
                // errStr += "</script>\n";
                // } else {
                // if (isth != null && "1".equals(isth)) {
                // errStr = "3";
                // } else {
                // errStr = "4";
                // }
                // }
                // return ERROR;
                // // ---------------------------
                // }
            }
            // ------------------------------------------------------------------------------------------------------------- 单个回收(***********************)
            else {
                // 查找G_INBOX是否存在该记录（回收效验）
                int STATUS = 1;
                int signStatus = -1;
                strSql = "SELECT ID,STATUS,SIGNSTATUS FROM G_INBOX WHERE PID=" + strPid + " AND PNID=" + strPnid;
                _myDs = _myRead.executeQuery(strSql);
                boolean isok = false;
                if (_myDs.next()) {
                    STATUS = _myDs.getInt("STATUS");
                    signStatus = _myDs.getInt("SIGNSTATUS");
                    isok = STATUS == 2 || STATUS == 1 || STATUS == 3;
                }
                _myDs.close();
                if (!isok && "回收".equals(strAct)) {
                    throw new Exception("该文件已被处理，无法回收！");
                }
                // -----------------------------遍历上一节点的所有儿子(不包括自己)
                int iBrother = 0; // 完成兄弟节点数
                // int iWait = 0; // 等待兄弟节点ID
                // int iWaitCount = 0;// 等待兄弟数
                // strSql = "SELECT A.ID,B.STATUS,B.WAITCOUNT FROM G_PROUTE A,G_PNODES B WHERE B.STATUS<>-1 AND A.PID=B.PID AND A.ID=B.ID AND A.PID=" + strPid + " AND A.FID=" + strFPnid + " AND B.ID<>" + strPnid;
                // _myDs = _myRead.executeQuery(strSql);
                // while (_myDs.next()) {
                // if (_myDs.getInt("WAITCOUNT") > 0) {
                // iWait = _myDs.getInt("ID");
                // iWaitCount = _myDs.getInt("WAITCOUNT");
                // } else {
                // iBrother++;
                // }
                // }
                // _myDs.close();
                // -----------------------------查找当前节点(名，用户)
                String strChildUser = "0";
                String strthChildUser = "0";
                String strChildAct = "";
                strSql = "SELECT A.USER_ID,A.ACTNAME,B.USER_ID THUSER_ID FROM G_PNODES A,G_PNODES B WHERE A.PID=B.PID AND A.FID=B.ID AND A.PID=" + strPid + " AND A.ID=" + strPnid;
                _myDs = _myRead.executeQuery(strSql);
                if (_myDs.next()) {
                    strChildUser = _myDs.getString("USER_ID");
                    strChildAct = _myDs.getString("ACTNAME");
                    // -----------------------------------------------------------
                    strChildAct = xsf.Value.getString(strChildAct, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                    // -----------------------------------------------------------
                    strthChildUser = _myDs.getString("THUSER_ID");
                }
                _myDs.close();
                if ("退回".equals(strAct)) {
                    strChildUser = strthChildUser;
                }
                //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
            	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
            		strSql = "SELECT MAXID FROM `MAXVALUE` WHERE TAG='G_ACT_LOG'";
                } else {
                	strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_ACT_LOG'";
                }
                // 获取G_ACT_LOG最大ID,并+1
                //strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_ACT_LOG'";
                // System.out.println(strSql);// ------------------
                _myDs = _myRead.executeQuery(strSql);
                if (_myDs.next()) {
                    strMaxActLogId = _myDs.getString("MAXID");
                }
                _myDs.close();
              //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
            	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
            		strSql = "UPDATE `MAXVALUE` SET MAXID=MAXID+1 WHERE TAG='G_ACT_LOG'";
                } else {
                	strSql = "UPDATE MAXVALUE SET MAXID=MAXID+1 WHERE TAG='G_ACT_LOG'";
                }
                //strSql = "UPDATE MAXVALUE SET MAXID=MAXID+1 WHERE TAG='G_ACT_LOG'";
                // System.out.println(strSql);// ------------------
                _myRead.executeUpdate(strSql);
                // // 查找上一节点的是否有儿子（没有儿子说明是返回动作，有儿子是一般流程）
                // strSql = "SELECT ID FROM G_PROUTE WHERE PID=" + strPid + " AND FID=" + strFPnid;
                // System.out.println(strSql);// ------------------
                // _myDs = _myRead.executeQuery(strSql);
                // if (_myDs.next()) {
                // bChild = true;
                // }
                // _myDs.close();
                // // --------------------------- 有儿子
                // if (bChild) {
                List<String> httpTask = new ArrayList<String>();
                _myConn.setAutoCommit(false);// ******************************************************
                try {
                    // 回收需要执行的ＳＱＬ
                    DS_FLOWClass.doCommand(GetSqlFromBack(strPid, strPnid), _myRead, httpTask);
                    // 插入 G_ACT_LOG
                    strSql = "INSERT INTO G_ACT_LOG(ID,INFO_ID,USER_ID,ACT_USER_ID,ACTNAME,ACT,ACTTIME,BZ)VALUES(";
                    strSql += strMaxActLogId + ",";
                    strSql += strInfoId + ",";
                    strSql += (sUser_ID == null || "".equals(sUser_ID) ? "NULL" : sUser_ID) + ",";
                    strSql += (strChildUser == null || "".equals(strChildUser) ? "NULL" : strChildUser) + ",";
                    strSql += "?,";
                    strSql += "?,";
                    strSql += dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now) + ",";
                    strSql += "?)";
                    PreparedStatement ps = _myConn.prepareStatement(strSql);
                    // -----------------------------------------------------------
                    strChildAct = xsf.Value.getString(strChildAct, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                    strAct = xsf.Value.getString(strAct == null ? "" : strAct, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                    strReason = strReason == null || "".equals(strReason) ? "" : xsf.Value.getString(strReason, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                    // -----------------------------------------------------------
                    ps.setString(1, strChildAct);
                    ps.setString(2, strAct);
                    ps.setString(3, strReason);
                    ps.executeUpdate();
                    // 删除回收节点
					//要回收的节点是汇总节点并且是汇总线，则不删除G_PNODES而是将状态修改为汇总中
					if("2".equals(wfLineType)&&"4".equals(wfNodeType))
					{
						strSql= "UPDATE G_UFILES SET ISBW=(select case when count(*)>1 then 1 else 0 end from G_PNODES WHERE INFO_ID="+strInfoId+" and USER_ID=(SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")) WHERE INFO_ID="+strInfoId+" AND USER_ID=(SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")";
		                _myRead.executeUpdate(strSql);
		                strSql= "UPDATE G_UFILES SET ISBW=(select case when count(*)>1 then 1 else 0 end from G_PNODES WHERE INFO_ID="+strInfoId+" and USER_ID=(SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")) WHERE INFO_ID="+strInfoId+" AND USER_ID=(SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")";
		                _myRead.executeUpdate(strSql);
						strSql = "DELETE from G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
	                    strSql = "DELETE from G_INBOX WHERE PID=" + strPid + " AND PNID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
	                    strSql = "DELETE from G_PROUTE WHERE PID=" + strPid + " AND ID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
	                    strSql = "DELETE from G_OPINION WHERE PID=" + strPid + " AND PNID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
					}
					else
					{
						strSql= "UPDATE G_UFILES SET ISBW=(select case when count(*)>1 then 1 else 0 end from G_PNODES WHERE INFO_ID="+strInfoId+" and USER_ID=(SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")) WHERE INFO_ID="+strInfoId+" AND USER_ID=(SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")";
		                _myRead.executeUpdate(strSql);
		                strSql= "UPDATE G_UFILES SET ISBW=(select case when count(*)>1 then 1 else 0 end from G_PNODES WHERE INFO_ID="+strInfoId+" and MUSER_ID=(SELECT USER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")) WHERE INFO_ID="+strInfoId+" AND USER_ID=(SELECT MUSER_ID FROM G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid+")";
		                _myRead.executeUpdate(strSql);
						strSql = "DELETE from G_PNODES WHERE PID=" + strPid + " AND ID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
	                    strSql = "DELETE from G_INBOX WHERE PID=" + strPid + " AND PNID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
	                    strSql = "DELETE from G_PROUTE WHERE PID=" + strPid + " AND ID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
	                    strSql = "DELETE from G_OPINION WHERE PID=" + strPid + " AND PNID=" + strPnid;
	                    _myRead.executeUpdate(strSql);
	                   
	                 // 退回，回收 插入日志表g_pnodes_history 杨龙修改 2012/9/8 开始
	                    long HISTORY_ID=0;
						if ("1".equals(isth)) {
							// 退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改
							// 2012/9/8 开始
							// 获取 G_PNODES_HISTORY最大ID,并+1
							HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1,_myConn);
							HISTORY_ID++;
							//复制g_pnodes数据
							strSql = "insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
							strSql += "SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
							strSql += "QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
							strSql += "OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
							strSql += "select PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
							strSql += "FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, ACTNAME,  WF_ID,WFNODE_ID,WFNODE_CAPTION, WFNODE_WAIT,SENDTYPE, QSRQ,";
							strSql += " WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
							strSql += "" + sUser_ID + ", 0,'',"+Muserid+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid
									+ "  and id = "+ strPnid;
							LogManager.debug("==========插入退回日志表g_pnodes_history："
									+ strSql);
							_myRead.executeUpdate(strSql);
							// 获取 G_PNODES_HISTORY最大ID,并+1
							HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1,_myConn);
							HISTORY_ID++;
							//写入退回数据
							strSql = "insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
							strSql += "SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
							strSql += "QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
							strSql += "OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
							strSql += "select PID,ID,ACT_ID,ATYPE,"+Muserid+",DAYS,TYJ,"+ dsoap.dsflow.DS_FLOWClass.processDate("NOW",DBMS, now)+ ",DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, "+ dsoap.dsflow.DS_FLOWClass.processDate("NOW",DBMS, now)+ ",";
							strSql += "FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, '"+Caption+"',  WF_ID,WFNODE_ID,'"+Caption+"', WFNODE_WAIT,SENDTYPE, "+dsoap.dsflow.DS_FLOWClass.processDate("NOW",DBMS, now)+",";
							strSql += " WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
							strSql += "" + sUser_ID + ", 2,'" + strReason+ "',"+sUser_ID+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid+ "  and id = " + strPnid;
							LogManager.debug("==========插入退回日志表g_pnodes_history："+ strSql);
							_myRead.executeUpdate(strSql);
							// 退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改
							// 2012/9/8 结束
						} else {
							// 回收时 将要回收的g_pnodes数据插入日志表g_pnodes_history 杨龙修改
							// 2012/9/8 开始
							// 获取 G_PNODES_HISTORY最大ID,并+1
							HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1,_myConn);
							HISTORY_ID++;
							//复制g_pnodes数据
							strSql = "insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
							strSql += "SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
							strSql += "QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
							strSql += "OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
							strSql += "select PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
							strSql += "FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, ACTNAME,  WF_ID,WFNODE_ID,WFNODE_CAPTION, WFNODE_WAIT,SENDTYPE, QSRQ,";
							strSql += " WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
							strSql += "" + sUser_ID + ", 0,'',"+Muserid+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid
									+ "  and id = " + strPnid;
							LogManager.debug("==========插入回收日志表g_pnodes_history："
									+ strSql);
							_myRead.executeUpdate(strSql);
							// 获取 G_PNODES_HISTORY最大ID,并+1
							HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1,_myConn);
							HISTORY_ID++;
							//写入回收数据
							strSql = "insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
							strSql += "SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
							strSql += "QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
							strSql += "OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
							strSql += "select PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,"+ dsoap.dsflow.DS_FLOWClass.processDate("NOW",DBMS, now)+ ",DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, "+ dsoap.dsflow.DS_FLOWClass.processDate("NOW",DBMS, now)+ ",";
							strSql += "FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,"+ dsoap.dsflow.DS_FLOWClass.processDate("NOW",DBMS, now)+ ",EDATE,FSTATUS, '"+Caption+"',  WF_ID,WFNODE_ID,'"+Caption+"', WFNODE_WAIT,SENDTYPE, "+ dsoap.dsflow.DS_FLOWClass.processDate("NOW",DBMS, now)+ ",";
							strSql += " WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
							strSql += "" + sUser_ID+ ", 1,'回收',"+Muserid+","+HISTORY_ID+" from g_pnodes   where pid = "+ strPid + "  and id = " + strPnid;
							LogManager.debug("==========插入回收日志表g_pnodes_history："+ strSql);
							_myRead.executeUpdate(strSql);
							// 回收时 将要回收的g_pnodes数据插入日志表g_pnodes_history 杨龙修改
							// 2012/9/8 结束
						}
						// 退回，回收 插入日志表g_pnodes_history 杨龙修改 2012/9/8 结束
					}
                    // 如果有等待节点,等待节点-1
                    // if (iWait > 0) {
                    // if (iWaitCount == 1) {
                    // strSql = "UPDATE G_PNODES SET WAITCOUNT=WAITCOUNT-1,WFNODE_WAIT=0,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + iWait;
                    // _myRead.executeUpdate(strSql);
                    // strSql = "UPDATE G_INBOX SET WAITCOUNT=WAITCOUNT-1,WFNODE_WAIT=0 WHERE PID=" + strPid + " AND PNID=" + iWait;
                    // _myRead.executeUpdate(strSql);
                    // } else {
                    // strSql = "UPDATE G_PNODES SET WAITCOUNT=WAITCOUNT-1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + iWait;
                    // _myRead.executeUpdate(strSql);
                    // strSql = "UPDATE G_INBOX SET WAITCOUNT=WAITCOUNT-1 WHERE PID=" + strPid + " AND PNID=" + iWait;
                    // _myRead.executeUpdate(strSql);
                    // }
                    // }
                    // 总是回收文件
                    // iBrother = 0;
                    // _myStr = "SELECT * FROM G_PNODES A WHERE A.PID=" + strPid + " AND A.FID=(SELECT FID FROM G_PNODES WHERE PID=" + strPid + " and ID=" + strPnid + ") AND A.STATUS in(-1,2)";
                    // _myDs = _myRead.executeQuery(_myStr);
                    // if (_myDs.next()) {
                    // iBrother = 1;
                    // }
                    // _myDs.close();
                    
                    
					// 如果当前节点无未读和已读文件，说明当前节点已处理完，查看当前节点有没有已处理文件，如果有查询后续节点汇总中的文件改为未读
					// 杨龙修改 2012/10/16 开始
					// 吴红亮 添加 开始 ---------------------------
//					boolean isOpenedOrProcessed = false;
//					_myStr = "SELECT * FROM G_PNODES A WHERE A.PID=" + strPid
//							+ " AND A.FID=" + strFPnid
//							+ " AND A.STATUS in(-1,2)";
//					_myDs = _myRead.executeQuery(_myStr);
//					if (_myDs.next()) {
//						isOpenedOrProcessed = true;
//					}
//					_myDs.close();
//					if (!isOpenedOrProcessed) {
//						_myStr = "SELECT * FROM G_INBOX A inner join G_PNODES B on A.PID=B.PID and A.PNID=B.ID and B.PID="
//								+ strPid + " AND B.FID=" + strFPnid;
//						_myDs = _myRead.executeQuery(_myStr);
//						if (_myDs.next()) {
//							iBrother = 1;
//						} else {
//							iBrother = 0;
//						}
//						_myDs.close();
//					} else {
//						iBrother = 1;
//					}
					// 吴红亮 添加 结束 ---------------------------
                    
                    boolean isOpenedOrProcessed = false;
					_myStr = "SELECT * FROM G_PNODES A WHERE A.PID=" + strPid
							+ " AND A.FID=" + strFPnid
							+ " AND A.STATUS in(1,2)";
					_myDs = _myRead.executeQuery(_myStr);
					if (_myDs.next()) {
						isOpenedOrProcessed = true;
					}
					_myDs.close();
					if (!isOpenedOrProcessed) {
						_myStr = "SELECT * FROM G_INBOX A inner join G_PNODES B on A.PID=B.PID and A.PNID=B.ID and B.PID="
								+ strPid + " AND B.FID=" + strFPnid;
						_myDs = _myRead.executeQuery(_myStr);
						if (_myDs.next()) {
							iBrother = 1;
						} else {
							//如果兄弟节点已经处理完成，则查询本节点是否有已处理的文件
							_myStr = "SELECT ID FROM G_PNODES A WHERE A.PID="
									+ strPid + " AND A.FID=" + strFPnid
									+ " AND A.STATUS =-1";
							_myDs = _myRead.executeQuery(_myStr);
							String id="";
							while (_myDs.next()) {
								id+=_myDs.getString(1)+",";
							}
							_myDs.close();
							if (!"".equals(id)) {
								id = id.substring(0, id.length() - 1);
								// 如果本节点有已处理的文件，则根据本节点的ID查找后续节点的文件
								_myStr = "SELECT ID FROM G_PNODES A WHERE A.PID="
										+ strPid
										+ " AND A.FID in ("
										+ id
										+ ") AND A.STATUS in (1,2)";
								_myDs = _myRead.executeQuery(_myStr);
								String gPnodes_id = "";
								if (_myDs.next()) {
									gPnodes_id += _myDs.getString(1) + ",";
								}
								_myDs.close();
								if (!"".equals(gPnodes_id)) {
									gPnodes_id = gPnodes_id.substring(0,
											gPnodes_id.length() - 1);
									// 如果后续节点的状态时汇总中，则改为未读
									strSql = "update g_inbox set status=1 where status=3 and pid="
											+ strPid
											+ " and pnid in ("
											+ gPnodes_id + ")";
									_myRead.executeUpdate(strSql);
								}
								iBrother = 1;
							}
							else
							{
							iBrother = 0;
							}
						}
						_myDs.close();
					} else {
						iBrother = 1;
					}
                    
					// 如果当前节点无未读和已读文件，说明当前节点已处理完，查看当前节点有没有已处理文件，如果有查询后续节点汇总中的文件改为未读
					// 杨龙修改 2012/10/16 结束
					// 没有兄弟则需要增加操作
                    if (iBrother == 0) {
                        // // 存在Wait节点则删除Wait节点
                        // if (iWait > 0) {
                        // strSql = "DELETE G_PNODES WHERE PID=" + strPid + " AND ID=" + iWait;
                        // _myRead.executeUpdate(strSql);
                        // strSql = "DELETE G_INBOX WHERE PID=" + strPid + " AND PNID=" + iWait;
                        // _myRead.executeUpdate(strSql);
                        // strSql = "DELETE G_PROUTE WHERE PID=" + strPid + " AND ID=" + iWait;
                        // _myRead.executeUpdate(strSql);
                        // strSql = "DELETE G_OPINION WHERE PID=" + strPid + " AND PNID=" + iWait;
                        // _myRead.executeUpdate(strSql);
                        // }
                        // 在G_INBOX增加父亲记录
                        strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + strFPnid + " AND ID=" + strMaxYjId;
                        _myRead.executeUpdate(strSql);
                        strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + strFPnid;// 添加退回原因
                        _myRead.executeUpdate(strSql);
                        // 插入G_INBOX----------------------------------
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("ID", strMaxId);
                        data.put("PID", strPid);
                        data.put("PNID", strFPnid);
                        data.put("INFO_ID", strInfoId);
                        data.put("DEPT_ID", DeptId);
                        data.put("USER_ID", Muserid);
                        data.put("FUSER_ID", Fuserid);
                        data.put("UTYPE", "0");
                        data.put("STATUS", "1");
                        data.put("ACTNAME", Caption);
                        data.put("WF_ID", WfId);
                        data.put("WFNODE_ID", WfNodeId);
                        data.put("WFNODE_CAPTION", Caption);
                        data.put("WFNODE_WAIT", "0");
                        data.put("SENDTYPE", strSendType);
                        data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now));
                        data.put("EDATE", strEdate);
                        data.put("TIMESPAN", TimeSpan);
                        data.put("TIMETYPE", TimeType);
                        data.put("DAYS", "0");
                        data.put("HANDLEWAY", "0");
                        data.put("WAITCOUNT", "0");
                        data.put("ISZNG", "NULL");
                        data.put("FNODE", strFFnode == null || "".equals(strFFnode) ? "NULL" : strFFnode);
                        data.put("PRIORY", "0");
                      //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                        String RECEIVE_USERNAME="";
                        String SEND_USERNAME="";
                        try{
                        	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                        	SEND_USERNAME=this.getUserName(Long.parseLong(Muserid));
                        }
                        catch (Exception e) {
						}
                        data.put("RECEIVE_USERNAME", SEND_USERNAME);
                        data.put("SEND_USERNAME", RECEIVE_USERNAME);
                        data.put("BACKREASON", strReason);
                      //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                        dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                        // ----------------------------------
                        if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                            strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                        } else if ("ORACLE".equals(DBMS)) {
                            strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                        } else if ("SQLSERVER".equals(DBMS)) {
                            strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                        } else if ("MYSQL".equals(DBMS)) {
                            strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                        } else if ("CLOUD".equals(DBMS)) {
                            strSql = "update g_inbox b set b.BT=a.BT,b.DELETED=a.DELETED,b.HASCONTENT=0,b.URGENT=a.URGENT,b.SWH=a.SWH,b.WH=a.WH,b.OBJCLASS=a.OBJCLASS FROM G_INFOS a  where a.id=" + strInfoId + "and b.id=" + strMaxId;
                        } else {
                            strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                        }
                        _myRead.executeUpdate(strSql);
                        // ----------------------------------
                        if (isth != null && "1".equals(isth) && "1".equals(ConfigurationSettings.AppSettings("退回汇总前所有人"))) {
                            List<Map<String, String>> a = new ArrayList<Map<String, String>>();
                            strSql = "SELECT A.FNODE,A.FID,A.DEPT_ID,A.ID,A.WFNODE_CAPTION,A.WF_ID,A.WFNODE_ID,A.TIMESPAN,A.TIMETYPE,A.USER_ID,A.EDATE ";
                            strSql += "FROM G_PNODES A WHERE A.PID=" + strPid + " AND A.FID=" + strFFPnid + " AND ID<>" + strFPnid;
                            _myDs = _myRead.executeQuery(strSql);
                            if (_myDs.next()) {
                                Map<String, String> b = new HashMap<String, String>();
                                b.put("FID", strFFPnid);
                                b.put("ID", _myDs.getObject("ID") != null ? _myDs.getString("ID") : "");
                                b.put("FNODE", _myDs.getObject("FNODE") != null ? _myDs.getString("FNODE") : "");
                                b.put("DEPT_ID", _myDs.getObject("DEPT_ID") != null ? _myDs.getString("DEPT_ID") : "");
                                String WFNODE_CAPTION = _myDs.getObject("WFNODE_CAPTION") != null ? _myDs.getString("WFNODE_CAPTION") : "";
                                // -----------------------------------------------------------
                                WFNODE_CAPTION = xsf.Value.getString(WFNODE_CAPTION, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                                // -----------------------------------------------------------
                                b.put("WFNODE_CAPTION", WFNODE_CAPTION);
                                b.put("WF_ID", _myDs.getObject("WF_ID") != null ? _myDs.getString("WF_ID") : "");
                                b.put("WFNODE_ID", _myDs.getObject("WFNODE_ID") != null ? _myDs.getString("WFNODE_ID") : "");
                                b.put("TIMESPAN", _myDs.getObject("TIMESPAN") != null ? _myDs.getString("TIMESPAN") : "0");
                                b.put("TIMETYPE", _myDs.getObject("TIMETYPE") != null ? _myDs.getString("TIMETYPE") : "0");
                                b.put("USER_ID", _myDs.getObject("USER_ID") != null ? _myDs.getString("USER_ID") : "");
                                Date edate = _myDs.getDate("EDATE");
                                // 吴红亮 更新 开始 ---------------------------
                                strEdate = dsoap.dsflow.DS_FLOWClass.processDate(edate == null ? "NULL" : "NOW", DBMS, now);
                                b.put("EDATE", strEdate);
                                a.add(b);
                                // 吴红亮 更新 结束 ---------------------------
                            }
                            for (Map<String, String> c : a) {
                            	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                            	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                            		strSql = "SELECT MAXID FROM `MAXVALUE` WHERE TAG='G_INBOX'";
                                } else {
                                	strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                }
                                //strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                _myDs = _myRead.executeQuery(strSql);
                                if (_myDs.next()) {
                                    strMaxId = _myDs.getObject("MAXID") != null ? _myDs.getString("MAXID") : "";
                                }
                                _myDs.close();
                              //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                            	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                            		strSql = "UPDATE `MAXVALUE` SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                } else {
                                	strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                }
                                //strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                _myRead.executeUpdate(strSql);
                                
                              //退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改 2012/9/8 开始
                              //复制g_pnodes数据
                             // 获取 G_PNODES_HISTORY最大ID,并+1
//            					HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1);
//            					HISTORY_ID++;
//        						strSql = "insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
//        						strSql += "SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
//        						strSql += "QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
//        						strSql += "OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
//        						strSql += "select PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
//        						strSql += "FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, ACTNAME,  WF_ID,WFNODE_ID,WFNODE_CAPTION, WFNODE_WAIT,SENDTYPE, QSRQ,";
//        						strSql += " WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
//        						strSql += "" + sUser_ID + ", 0,'',"+Muserid+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid
//        								+ "  and id = "+ c.get("ID");
//        						LogManager.debug("==========插入退回日志表g_pnodes_history："
//        								+ strSql);
//        						_myRead.executeUpdate(strSql);
        						//写入退回数据
        						 // 获取 G_PNODES_HISTORY最大ID,并+1
//            					HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1);
//            					HISTORY_ID++;
//                                strSql="insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
//                                strSql+="SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
//                                strSql+="QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
//                                strSql+="OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
//                                strSql+="select PID,ID,ACT_ID,ATYPE,"+Muserid+",DAYS,TYJ,"+dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now)+",DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
//                                strSql+="FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, '"+Caption+"',  WF_ID,WFNODE_ID,'"+Caption+"', WFNODE_WAIT,SENDTYPE, "+dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now)+",";
//                                strSql+=" WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
//                                strSql+=""+sUser_ID+", 2,'"+strReason+"',"+sUser_ID+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid + "  and id = "+ c.get("ID");
//                                LogManager.debug("==========插入退回日志表g_pnodes_history："+strSql);
//                                _myRead.executeUpdate(strSql);
                                //退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改 2012/9/8 结束
                                
                                // 在G_INBOX增加父亲记录
                                strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + c.get("ID") + " AND ID=" + strMaxYjId;
                                _myRead.executeUpdate(strSql);
                                strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + c.get("ID");// 添加退回原因
                                _myRead.executeUpdate(strSql);
                                // 插入G_INBOX----------------------------------
                                data = new HashMap<String, String>();
                                data.put("ID", strMaxId);
                                data.put("PID", strPid);
                                data.put("PNID", c.get("ID"));
                                data.put("INFO_ID", strInfoId);
                                data.put("DEPT_ID", c.get("DEPT_ID"));
                                data.put("USER_ID", c.get("USER_ID"));
                                data.put("FUSER_ID", Fuserid);
                                data.put("UTYPE", "0");
                                data.put("STATUS", "1");
                                data.put("ACTNAME", c.get("WFNODE_CAPTION"));
                                data.put("WF_ID", c.get("WF_ID"));
                                data.put("WFNODE_ID", c.get("WFNODE_ID"));
                                data.put("WFNODE_CAPTION", c.get("WFNODE_CAPTION"));
                                data.put("WFNODE_WAIT", "0");
                                data.put("SENDTYPE", strSendType);
                                data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW,", DBMS, now));
                                data.put("EDATE", c.get("EDATE"));
                                data.put("TIMESPAN", c.get("TIMESPAN"));
                                data.put("TIMETYPE", c.get("TIMETYPE"));
                                data.put("DAYS", "0");
                                data.put("HANDLEWAY", "0");
                                data.put("WAITCOUNT", "0");
                                data.put("ISZNG", "NULL");
                                data.put("FNODE", c.get("FNODE"));
                                data.put("PRIORY", "0");
                                //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                                RECEIVE_USERNAME="";
                                SEND_USERNAME="";
                                try{
                                	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                                	SEND_USERNAME=this.getUserName(Long.parseLong(c.get("USER_ID")));
                                }
                                catch (Exception e) {
        						}
                                data.put("RECEIVE_USERNAME",RECEIVE_USERNAME );
                                data.put("SEND_USERNAME", SEND_USERNAME);
                                data.put("BACKREASON", strReason);
                              //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                                dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                                if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                    strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else if ("ORACLE".equals(DBMS)) {
                                    strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else if ("SQLSERVER".equals(DBMS)) {
                                    strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else if ("MYSQL".equals(DBMS)) {
                                    strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else {
                                    strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                }
                                _myRead.executeUpdate(strSql);

                            }
                        }
                    }
                    else if(!"1".equals(isth)){
                    	if(!"1".equals(return_type)){
                    		CommandCollection command = new CommandCollection();
                			Sql sql = new Sql("delete from g_inbox where info_id="+strInfoId+" and signstatus=-1");
                			command.add(sql);
                			sql = new Sql("delete from g_pnodes where info_id="+strInfoId+" and status=1");
                			command.add(sql);
                			DBManager.execute(command);
                    		 strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + strFPnid + " AND ID=" + strMaxYjId;
                             _myRead.executeUpdate(strSql);
                             strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + strFPnid;// 添加退回原因
                             _myRead.executeUpdate(strSql);
                             // 插入G_INBOX----------------------------------
                             Map<String, String> data = new HashMap<String, String>();
                             data.put("ID", strMaxId);
                             data.put("PID", strPid);
                             data.put("PNID", strFPnid);
                             data.put("INFO_ID", strInfoId);
                             data.put("DEPT_ID", DeptId);
                             data.put("USER_ID", Muserid);
                             data.put("FUSER_ID", Fuserid);
                             data.put("UTYPE", "0");
                             data.put("STATUS", "1");
                             data.put("ACTNAME", Caption);
                             data.put("WF_ID", WfId);
                             data.put("WFNODE_ID", WfNodeId);
                             data.put("WFNODE_CAPTION", Caption);
                             data.put("WFNODE_WAIT", "0");
                             data.put("SENDTYPE", strSendType);
                             data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now));
                             data.put("EDATE", strEdate);
                             data.put("TIMESPAN", TimeSpan);
                             data.put("TIMETYPE", TimeType);
                             data.put("DAYS", "0");
                             data.put("HANDLEWAY", "0");
                             data.put("WAITCOUNT", "0");
                             data.put("ISZNG", "NULL");
                             data.put("FNODE", strFFnode == null || "".equals(strFFnode) ? "NULL" : strFFnode);
                             data.put("PRIORY", "0");
                           //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                             String RECEIVE_USERNAME="";
                             String SEND_USERNAME="";
                             try{
                             	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                             	SEND_USERNAME=this.getUserName(Long.parseLong(Muserid));
                             }
                             catch (Exception e) {
     						}
                             data.put("RECEIVE_USERNAME", SEND_USERNAME);
                             data.put("SEND_USERNAME", RECEIVE_USERNAME);
                             data.put("BACKREASON", strReason);
                           //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                             dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                             // ----------------------------------
                             if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                 strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                             } else if ("ORACLE".equals(DBMS)) {
                                 strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                             } else if ("SQLSERVER".equals(DBMS)) {
                                 strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                             } else if ("MYSQL".equals(DBMS)) {
                                 strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                             } else if ("CLOUD".equals(DBMS)) {
                                 strSql = "update g_inbox b set b.BT=a.BT,b.DELETED=a.DELETED,b.HASCONTENT=0,b.URGENT=a.URGENT,b.SWH=a.SWH,b.WH=a.WH,b.OBJCLASS=a.OBJCLASS FROM G_INFOS a  where a.id=" + strInfoId + "and b.id=" + strMaxId;
                             } else {
                                 strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                             }
                             _myRead.executeUpdate(strSql);
                             // ----------------------------------
                             if (isth != null && "1".equals(isth) && "1".equals(ConfigurationSettings.AppSettings("退回汇总前所有人"))) {
                                 List<Map<String, String>> a = new ArrayList<Map<String, String>>();
                                 strSql = "SELECT A.FNODE,A.FID,A.DEPT_ID,A.ID,A.WFNODE_CAPTION,A.WF_ID,A.WFNODE_ID,A.TIMESPAN,A.TIMETYPE,A.USER_ID,A.EDATE ";
                                 strSql += "FROM G_PNODES A WHERE A.PID=" + strPid + " AND A.FID=" + strFFPnid + " AND ID<>" + strFPnid;
                                 _myDs = _myRead.executeQuery(strSql);
                                 if (_myDs.next()) {
                                     Map<String, String> b = new HashMap<String, String>();
                                     b.put("FID", strFFPnid);
                                     b.put("ID", _myDs.getObject("ID") != null ? _myDs.getString("ID") : "");
                                     b.put("FNODE", _myDs.getObject("FNODE") != null ? _myDs.getString("FNODE") : "");
                                     b.put("DEPT_ID", _myDs.getObject("DEPT_ID") != null ? _myDs.getString("DEPT_ID") : "");
                                     String WFNODE_CAPTION = _myDs.getObject("WFNODE_CAPTION") != null ? _myDs.getString("WFNODE_CAPTION") : "";
                                     // -----------------------------------------------------------
                                     WFNODE_CAPTION = xsf.Value.getString(WFNODE_CAPTION, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                                     // -----------------------------------------------------------
                                     b.put("WFNODE_CAPTION", WFNODE_CAPTION);
                                     b.put("WF_ID", _myDs.getObject("WF_ID") != null ? _myDs.getString("WF_ID") : "");
                                     b.put("WFNODE_ID", _myDs.getObject("WFNODE_ID") != null ? _myDs.getString("WFNODE_ID") : "");
                                     b.put("TIMESPAN", _myDs.getObject("TIMESPAN") != null ? _myDs.getString("TIMESPAN") : "0");
                                     b.put("TIMETYPE", _myDs.getObject("TIMETYPE") != null ? _myDs.getString("TIMETYPE") : "0");
                                     b.put("USER_ID", _myDs.getObject("USER_ID") != null ? _myDs.getString("USER_ID") : "");
                                     Date edate = _myDs.getDate("EDATE");
                                     // 吴红亮 更新 开始 ---------------------------
                                     strEdate = dsoap.dsflow.DS_FLOWClass.processDate(edate == null ? "NULL" : "NOW", DBMS, now);
                                     b.put("EDATE", strEdate);
                                     a.add(b);
                                     // 吴红亮 更新 结束 ---------------------------
                                 }
                                 for (Map<String, String> c : a) {
                                 	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                                 	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                 		strSql = "SELECT MAXID FROM `MAXVALUE` WHERE TAG='G_INBOX'";
                                     } else {
                                     	strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                     }
                                     //strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                     _myDs = _myRead.executeQuery(strSql);
                                     if (_myDs.next()) {
                                         strMaxId = _myDs.getObject("MAXID") != null ? _myDs.getString("MAXID") : "";
                                     }
                                     _myDs.close();
                                   //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                                 	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                 		strSql = "UPDATE `MAXVALUE` SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                     } else {
                                     	strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                     }
                                     //strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                     _myRead.executeUpdate(strSql);
                                     
                                   //退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改 2012/9/8 开始
                                   //复制g_pnodes数据
                                  // 获取 G_PNODES_HISTORY最大ID,并+1
//                 					HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1);
//                 					HISTORY_ID++;
//             						strSql = "insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
//             						strSql += "SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
//             						strSql += "QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
//             						strSql += "OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
//             						strSql += "select PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
//             						strSql += "FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, ACTNAME,  WF_ID,WFNODE_ID,WFNODE_CAPTION, WFNODE_WAIT,SENDTYPE, QSRQ,";
//             						strSql += " WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
//             						strSql += "" + sUser_ID + ", 0,'',"+Muserid+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid
//             								+ "  and id = "+ c.get("ID");
//             						LogManager.debug("==========插入退回日志表g_pnodes_history："
//             								+ strSql);
//             						_myRead.executeUpdate(strSql);
             						//写入退回数据
             						 // 获取 G_PNODES_HISTORY最大ID,并+1
//                 					HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1);
//                 					HISTORY_ID++;
//                                     strSql="insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
//                                     strSql+="SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
//                                     strSql+="QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
//                                     strSql+="OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
//                                     strSql+="select PID,ID,ACT_ID,ATYPE,"+Muserid+",DAYS,TYJ,"+dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now)+",DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
//                                     strSql+="FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, '"+Caption+"',  WF_ID,WFNODE_ID,'"+Caption+"', WFNODE_WAIT,SENDTYPE, "+dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now)+",";
//                                     strSql+=" WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
//                                     strSql+=""+sUser_ID+", 2,'"+strReason+"',"+sUser_ID+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid + "  and id = "+ c.get("ID");
//                                     LogManager.debug("==========插入退回日志表g_pnodes_history："+strSql);
//                                     _myRead.executeUpdate(strSql);
                                     //退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改 2012/9/8 结束
                                     
                                     // 在G_INBOX增加父亲记录
                                     strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + c.get("ID") + " AND ID=" + strMaxYjId;
                                     _myRead.executeUpdate(strSql);
                                     strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + c.get("ID");// 添加退回原因
                                     _myRead.executeUpdate(strSql);
                                     // 插入G_INBOX----------------------------------
                                     data = new HashMap<String, String>();
                                     data.put("ID", strMaxId);
                                     data.put("PID", strPid);
                                     data.put("PNID", c.get("ID"));
                                     data.put("INFO_ID", strInfoId);
                                     data.put("DEPT_ID", c.get("DEPT_ID"));
                                     data.put("USER_ID", c.get("USER_ID"));
                                     data.put("FUSER_ID", Fuserid);
                                     data.put("UTYPE", "0");
                                     data.put("STATUS", "1");
                                     data.put("ACTNAME", c.get("WFNODE_CAPTION"));
                                     data.put("WF_ID", c.get("WF_ID"));
                                     data.put("WFNODE_ID", c.get("WFNODE_ID"));
                                     data.put("WFNODE_CAPTION", c.get("WFNODE_CAPTION"));
                                     data.put("WFNODE_WAIT", "0");
                                     data.put("SENDTYPE", strSendType);
                                     data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW,", DBMS, now));
                                     data.put("EDATE", c.get("EDATE"));
                                     data.put("TIMESPAN", c.get("TIMESPAN"));
                                     data.put("TIMETYPE", c.get("TIMETYPE"));
                                     data.put("DAYS", "0");
                                     data.put("HANDLEWAY", "0");
                                     data.put("WAITCOUNT", "0");
                                     data.put("ISZNG", "NULL");
                                     data.put("FNODE", c.get("FNODE"));
                                     data.put("PRIORY", "0");
                                     //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                                     RECEIVE_USERNAME="";
                                     SEND_USERNAME="";
                                     try{
                                     	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                                     	SEND_USERNAME=this.getUserName(Long.parseLong(c.get("USER_ID")));
                                     }
                                     catch (Exception e) {
             						}
                                     data.put("RECEIVE_USERNAME",RECEIVE_USERNAME );
                                     data.put("SEND_USERNAME", SEND_USERNAME);
                                     data.put("BACKREASON", strReason);
                                   //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                                     dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                                     if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                         strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                     } else if ("ORACLE".equals(DBMS)) {
                                         strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                     } else if ("SQLSERVER".equals(DBMS)) {
                                         strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                     } else if ("MYSQL".equals(DBMS)) {
                                         strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                     } else {
                                         strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                     }
                                     _myRead.executeUpdate(strSql);

                                 }
                             }
                    	}else{
                    		if(signStatus==1){
                    			CommandCollection command = new CommandCollection();
                    			Sql sql = new Sql("delete from g_inbox where info_id="+strInfoId+" and signstatus=-1");
                    			command.add(sql);
                    			sql = new Sql("delete from g_pnodes where info_id="+strInfoId+" and status=1");
                    			command.add(sql);
                    			DBManager.execute(command);
                    			strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + strFPnid + " AND ID=" + strMaxYjId;
                                _myRead.executeUpdate(strSql);
                                strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + strFPnid;// 添加退回原因
                                _myRead.executeUpdate(strSql);
                                // 插入G_INBOX----------------------------------
                                Map<String, String> data = new HashMap<String, String>();
                                data.put("ID", strMaxId);
                                data.put("PID", strPid);
                                data.put("PNID", strFPnid);
                                data.put("INFO_ID", strInfoId);
                                data.put("DEPT_ID", DeptId);
                                data.put("USER_ID", Muserid);
                                data.put("FUSER_ID", Fuserid);
                                data.put("UTYPE", "0");
                                data.put("STATUS", "1");
                                data.put("ACTNAME", Caption);
                                data.put("WF_ID", WfId);
                                data.put("WFNODE_ID", WfNodeId);
                                data.put("WFNODE_CAPTION", Caption);
                                data.put("WFNODE_WAIT", "0");
                                data.put("SENDTYPE", strSendType);
                                data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now));
                                data.put("EDATE", strEdate);
                                data.put("TIMESPAN", TimeSpan);
                                data.put("TIMETYPE", TimeType);
                                data.put("DAYS", "0");
                                data.put("HANDLEWAY", "0");
                                data.put("WAITCOUNT", "0");
                                data.put("ISZNG", "NULL");
                                data.put("FNODE", strFFnode == null || "".equals(strFFnode) ? "NULL" : strFFnode);
                                data.put("PRIORY", "0");
                              //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                                String RECEIVE_USERNAME="";
                                String SEND_USERNAME="";
                                try{
                                	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                                	SEND_USERNAME=this.getUserName(Long.parseLong(Muserid));
                                }
                                catch (Exception e) {
        						}
                                data.put("RECEIVE_USERNAME", SEND_USERNAME);
                                data.put("SEND_USERNAME", RECEIVE_USERNAME);
                                data.put("BACKREASON", strReason);
                              //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                                dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                                // ----------------------------------
                                if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                    strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else if ("ORACLE".equals(DBMS)) {
                                    strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else if ("SQLSERVER".equals(DBMS)) {
                                    strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else if ("MYSQL".equals(DBMS)) {
                                    strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                } else if ("CLOUD".equals(DBMS)) {
                                    strSql = "update g_inbox b set b.BT=a.BT,b.DELETED=a.DELETED,b.HASCONTENT=0,b.URGENT=a.URGENT,b.SWH=a.SWH,b.WH=a.WH,b.OBJCLASS=a.OBJCLASS FROM G_INFOS a  where a.id=" + strInfoId + "and b.id=" + strMaxId;
                                } else {
                                    strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                }
                                _myRead.executeUpdate(strSql);
                                // ----------------------------------
                                if (isth != null && "1".equals(isth) && "1".equals(ConfigurationSettings.AppSettings("退回汇总前所有人"))) {
                                    List<Map<String, String>> a = new ArrayList<Map<String, String>>();
                                    strSql = "SELECT A.FNODE,A.FID,A.DEPT_ID,A.ID,A.WFNODE_CAPTION,A.WF_ID,A.WFNODE_ID,A.TIMESPAN,A.TIMETYPE,A.USER_ID,A.EDATE ";
                                    strSql += "FROM G_PNODES A WHERE A.PID=" + strPid + " AND A.FID=" + strFFPnid + " AND ID<>" + strFPnid;
                                    _myDs = _myRead.executeQuery(strSql);
                                    if (_myDs.next()) {
                                        Map<String, String> b = new HashMap<String, String>();
                                        b.put("FID", strFFPnid);
                                        b.put("ID", _myDs.getObject("ID") != null ? _myDs.getString("ID") : "");
                                        b.put("FNODE", _myDs.getObject("FNODE") != null ? _myDs.getString("FNODE") : "");
                                        b.put("DEPT_ID", _myDs.getObject("DEPT_ID") != null ? _myDs.getString("DEPT_ID") : "");
                                        String WFNODE_CAPTION = _myDs.getObject("WFNODE_CAPTION") != null ? _myDs.getString("WFNODE_CAPTION") : "";
                                        // -----------------------------------------------------------
                                        WFNODE_CAPTION = xsf.Value.getString(WFNODE_CAPTION, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                                        // -----------------------------------------------------------
                                        b.put("WFNODE_CAPTION", WFNODE_CAPTION);
                                        b.put("WF_ID", _myDs.getObject("WF_ID") != null ? _myDs.getString("WF_ID") : "");
                                        b.put("WFNODE_ID", _myDs.getObject("WFNODE_ID") != null ? _myDs.getString("WFNODE_ID") : "");
                                        b.put("TIMESPAN", _myDs.getObject("TIMESPAN") != null ? _myDs.getString("TIMESPAN") : "0");
                                        b.put("TIMETYPE", _myDs.getObject("TIMETYPE") != null ? _myDs.getString("TIMETYPE") : "0");
                                        b.put("USER_ID", _myDs.getObject("USER_ID") != null ? _myDs.getString("USER_ID") : "");
                                        Date edate = _myDs.getDate("EDATE");
                                        // 吴红亮 更新 开始 ---------------------------
                                        strEdate = dsoap.dsflow.DS_FLOWClass.processDate(edate == null ? "NULL" : "NOW", DBMS, now);
                                        b.put("EDATE", strEdate);
                                        a.add(b);
                                        // 吴红亮 更新 结束 ---------------------------
                                    }
                                    for (Map<String, String> c : a) {
                                    	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                                    	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                    		strSql = "SELECT MAXID FROM `MAXVALUE` WHERE TAG='G_INBOX'";
                                        } else {
                                        	strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                        }
                                        //strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                        _myDs = _myRead.executeQuery(strSql);
                                        if (_myDs.next()) {
                                            strMaxId = _myDs.getObject("MAXID") != null ? _myDs.getString("MAXID") : "";
                                        }
                                        _myDs.close();
                                      //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                                    	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                    		strSql = "UPDATE `MAXVALUE` SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                        } else {
                                        	strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                        }
                                        //strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                        _myRead.executeUpdate(strSql);
                                        
                                      //退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改 2012/9/8 开始
                                      //复制g_pnodes数据
                                     // 获取 G_PNODES_HISTORY最大ID,并+1
//                    					HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1);
//                    					HISTORY_ID++;
//                						strSql = "insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
//                						strSql += "SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
//                						strSql += "QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
//                						strSql += "OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
//                						strSql += "select PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
//                						strSql += "FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, ACTNAME,  WF_ID,WFNODE_ID,WFNODE_CAPTION, WFNODE_WAIT,SENDTYPE, QSRQ,";
//                						strSql += " WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
//                						strSql += "" + sUser_ID + ", 0,'',"+Muserid+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid
//                								+ "  and id = "+ c.get("ID");
//                						LogManager.debug("==========插入退回日志表g_pnodes_history："
//                								+ strSql);
//                						_myRead.executeUpdate(strSql);
                						//写入退回数据
                						 // 获取 G_PNODES_HISTORY最大ID,并+1
//                    					HISTORY_ID = this.getMaxValue("G_PNODES_HISTORY", 1);
//                    					HISTORY_ID++;
//                                        strSql="insert into g_pnodes_history  (PID,ID,ACT_ID,ATYPE,USER_ID,DAYS,TYJ,PDATE,DEPT_ID,HANDLEWAY,STATUS,COMPLISHDATE,WHOHANDLE,";
//                                        strSql+="SIGNED,MUSER_ID,RDATE,FID,INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,";
//                                        strSql+="QSRQ,WAITCOUNT,LOCKFLOW,FNODE,BACKREASON,ISZNG,ISHIDEYJ,PARENTFLOWPNID,PARENTFLOWPID,UNAME,ISSIGN,SENDTIME,MDEPT_ID,WFLINE_TYPE,";
//                                        strSql+="OPERATION_USERID,OPERATION_TYPE,OPERATION_REMARK,FUSERID,HISTORY_ID)";
//                                        strSql+="select PID,ID,ACT_ID,ATYPE,"+Muserid+",DAYS,TYJ,"+dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now)+",DEPT_ID, HANDLEWAY, STATUS,COMPLISHDATE, WHOHANDLE,SIGNED, MUSER_ID, RDATE,";
//                                        strSql+="FID, INFO_ID,UTYPE,TIMETYPE,TIMESPAN,SDATE,EDATE,FSTATUS, '"+Caption+"',  WF_ID,WFNODE_ID,'"+Caption+"', WFNODE_WAIT,SENDTYPE, "+dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now)+",";
//                                        strSql+=" WAITCOUNT, LOCKFLOW, FNODE, BACKREASON,ISZNG,ISHIDEYJ, PARENTFLOWPNID, PARENTFLOWPID,UNAME,ISSIGN, SENDTIME,MDEPT_ID, WFLINE_TYPE,";
//                                        strSql+=""+sUser_ID+", 2,'"+strReason+"',"+sUser_ID+","+HISTORY_ID+" from g_pnodes   where pid = " + strPid + "  and id = "+ c.get("ID");
//                                        LogManager.debug("==========插入退回日志表g_pnodes_history："+strSql);
//                                        _myRead.executeUpdate(strSql);
                                        //退回时 将要退回的g_pnodes数据插入日志表g_pnodes_history 杨龙修改 2012/9/8 结束
                                        
                                        // 在G_INBOX增加父亲记录
                                        strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + c.get("ID") + " AND ID=" + strMaxYjId;
                                        _myRead.executeUpdate(strSql);
                                        strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + c.get("ID");// 添加退回原因
                                        _myRead.executeUpdate(strSql);
                                        // 插入G_INBOX----------------------------------
                                        data = new HashMap<String, String>();
                                        data.put("ID", strMaxId);
                                        data.put("PID", strPid);
                                        data.put("PNID", c.get("ID"));
                                        data.put("INFO_ID", strInfoId);
                                        data.put("DEPT_ID", c.get("DEPT_ID"));
                                        data.put("USER_ID", c.get("USER_ID"));
                                        data.put("FUSER_ID", Fuserid);
                                        data.put("UTYPE", "0");
                                        data.put("STATUS", "1");
                                        data.put("ACTNAME", c.get("WFNODE_CAPTION"));
                                        data.put("WF_ID", c.get("WF_ID"));
                                        data.put("WFNODE_ID", c.get("WFNODE_ID"));
                                        data.put("WFNODE_CAPTION", c.get("WFNODE_CAPTION"));
                                        data.put("WFNODE_WAIT", "0");
                                        data.put("SENDTYPE", strSendType);
                                        data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW,", DBMS, now));
                                        data.put("EDATE", c.get("EDATE"));
                                        data.put("TIMESPAN", c.get("TIMESPAN"));
                                        data.put("TIMETYPE", c.get("TIMETYPE"));
                                        data.put("DAYS", "0");
                                        data.put("HANDLEWAY", "0");
                                        data.put("WAITCOUNT", "0");
                                        data.put("ISZNG", "NULL");
                                        data.put("FNODE", c.get("FNODE"));
                                        data.put("PRIORY", "0");
                                        //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                                        RECEIVE_USERNAME="";
                                        SEND_USERNAME="";
                                        try{
                                        	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                                        	SEND_USERNAME=this.getUserName(Long.parseLong(c.get("USER_ID")));
                                        }
                                        catch (Exception e) {
                						}
                                        data.put("RECEIVE_USERNAME",RECEIVE_USERNAME );
                                        data.put("SEND_USERNAME", SEND_USERNAME);
                                        data.put("BACKREASON", strReason);
                                      //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                                        dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                                        if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                            strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                        } else if ("ORACLE".equals(DBMS)) {
                                            strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                        } else if ("SQLSERVER".equals(DBMS)) {
                                            strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                        } else if ("MYSQL".equals(DBMS)) {
                                            strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                        } else {
                                            strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                        }
                                        _myRead.executeUpdate(strSql);

                                    }
                                }
                    		}
                    	}
                    	
                    }else if("1".equals(isth) && signStatus == 1){
                    	Sql sql = new Sql("select count(ID) from g_inbox where info_id="+strInfoId+" and signstatus=-1");
                    	long i = DBManager.getFieldLongValue(sql); //如果没有signstatus=-1的记录说明这次退回是多发退回不处理 dongchj20151014
                    	if(i > 0){
                    		CommandCollection command = new CommandCollection();
                			sql = new Sql("delete from g_inbox where info_id="+strInfoId+" and signstatus=-1");
                			command.add(sql);
                			sql = new Sql("delete from g_pnodes where info_id="+strInfoId+" and status=1");
                			command.add(sql);
                			DBManager.execute(command);
                			strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + strFPnid + " AND ID=" + strMaxYjId;
                            _myRead.executeUpdate(strSql);
                            strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + strFPnid;// 添加退回原因
                            _myRead.executeUpdate(strSql);
                            // 插入G_INBOX----------------------------------
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("ID", strMaxId);
                            data.put("PID", strPid);
                            data.put("PNID", strFPnid);
                            data.put("INFO_ID", strInfoId);
                            data.put("DEPT_ID", DeptId);
                            data.put("USER_ID", Muserid);
                            data.put("FUSER_ID", Fuserid);
                            data.put("UTYPE", "0");
                            data.put("STATUS", "1");
                            data.put("ACTNAME", Caption);
                            data.put("WF_ID", WfId);
                            data.put("WFNODE_ID", WfNodeId);
                            data.put("WFNODE_CAPTION", Caption);
                            data.put("WFNODE_WAIT", "0");
                            data.put("SENDTYPE", strSendType);
                            data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW", DBMS, now));
                            data.put("EDATE", strEdate);
                            data.put("TIMESPAN", TimeSpan);
                            data.put("TIMETYPE", TimeType);
                            data.put("DAYS", "0");
                            data.put("HANDLEWAY", "0");
                            data.put("WAITCOUNT", "0");
                            data.put("ISZNG", "NULL");
                            data.put("FNODE", strFFnode == null || "".equals(strFFnode) ? "NULL" : strFFnode);
                            data.put("PRIORY", "0");
                          //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                            String RECEIVE_USERNAME="";
                            String SEND_USERNAME="";
                            try{
                            	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                            	SEND_USERNAME=this.getUserName(Long.parseLong(Muserid));
                            }
                            catch (Exception e) {
    						}
                            data.put("RECEIVE_USERNAME", SEND_USERNAME);
                            data.put("SEND_USERNAME", RECEIVE_USERNAME);
                            data.put("BACKREASON", strReason);
                          //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                            dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                            // ----------------------------------
                            if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                            } else if ("ORACLE".equals(DBMS)) {
                                strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                            } else if ("SQLSERVER".equals(DBMS)) {
                                strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                            } else if ("MYSQL".equals(DBMS)) {
                                strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                            } else if ("CLOUD".equals(DBMS)) {
                                strSql = "update g_inbox b set b.BT=a.BT,b.DELETED=a.DELETED,b.HASCONTENT=0,b.URGENT=a.URGENT,b.SWH=a.SWH,b.WH=a.WH,b.OBJCLASS=a.OBJCLASS FROM G_INFOS a  where a.id=" + strInfoId + "and b.id=" + strMaxId;
                            } else {
                                strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                            }
                            _myRead.executeUpdate(strSql);
                            // ----------------------------------
                            if (isth != null && "1".equals(isth) && "1".equals(ConfigurationSettings.AppSettings("退回汇总前所有人"))) {
                                List<Map<String, String>> a = new ArrayList<Map<String, String>>();
                                strSql = "SELECT A.FNODE,A.FID,A.DEPT_ID,A.ID,A.WFNODE_CAPTION,A.WF_ID,A.WFNODE_ID,A.TIMESPAN,A.TIMETYPE,A.USER_ID,A.EDATE ";
                                strSql += "FROM G_PNODES A WHERE A.PID=" + strPid + " AND A.FID=" + strFFPnid + " AND ID<>" + strFPnid;
                                _myDs = _myRead.executeQuery(strSql);
                                if (_myDs.next()) {
                                    Map<String, String> b = new HashMap<String, String>();
                                    b.put("FID", strFFPnid);
                                    b.put("ID", _myDs.getObject("ID") != null ? _myDs.getString("ID") : "");
                                    b.put("FNODE", _myDs.getObject("FNODE") != null ? _myDs.getString("FNODE") : "");
                                    b.put("DEPT_ID", _myDs.getObject("DEPT_ID") != null ? _myDs.getString("DEPT_ID") : "");
                                    String WFNODE_CAPTION = _myDs.getObject("WFNODE_CAPTION") != null ? _myDs.getString("WFNODE_CAPTION") : "";
                                    // -----------------------------------------------------------
                                    WFNODE_CAPTION = xsf.Value.getString(WFNODE_CAPTION, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                                    // -----------------------------------------------------------
                                    b.put("WFNODE_CAPTION", WFNODE_CAPTION);
                                    b.put("WF_ID", _myDs.getObject("WF_ID") != null ? _myDs.getString("WF_ID") : "");
                                    b.put("WFNODE_ID", _myDs.getObject("WFNODE_ID") != null ? _myDs.getString("WFNODE_ID") : "");
                                    b.put("TIMESPAN", _myDs.getObject("TIMESPAN") != null ? _myDs.getString("TIMESPAN") : "0");
                                    b.put("TIMETYPE", _myDs.getObject("TIMETYPE") != null ? _myDs.getString("TIMETYPE") : "0");
                                    b.put("USER_ID", _myDs.getObject("USER_ID") != null ? _myDs.getString("USER_ID") : "");
                                    Date edate = _myDs.getDate("EDATE");
                                    // 吴红亮 更新 开始 ---------------------------
                                    strEdate = dsoap.dsflow.DS_FLOWClass.processDate(edate == null ? "NULL" : "NOW", DBMS, now);
                                    b.put("EDATE", strEdate);
                                    a.add(b);
                                    // 吴红亮 更新 结束 ---------------------------
                                }
                                for (Map<String, String> c : a) {
                                	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                                	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                		strSql = "SELECT MAXID FROM `MAXVALUE` WHERE TAG='G_INBOX'";
                                    } else {
                                    	strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                    }
                                    //strSql = "SELECT MAXID FROM MAXVALUE WHERE TAG='G_INBOX'";
                                    _myDs = _myRead.executeQuery(strSql);
                                    if (_myDs.next()) {
                                        strMaxId = _myDs.getObject("MAXID") != null ? _myDs.getString("MAXID") : "";
                                    }
                                    _myDs.close();
                                  //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 
                                	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                		strSql = "UPDATE `MAXVALUE` SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                    } else {
                                    	strSql = "UPDATE MAXVALUE SET MAXID = MAXID + 1 WHERE TAG ='G_INBOX'";
                                    }
                                    _myRead.executeUpdate(strSql);
                                    
                                    
                                    // 在G_INBOX增加父亲记录
                                    strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + c.get("ID") + " AND ID=" + strMaxYjId;
                                    _myRead.executeUpdate(strSql);
                                    strSql = "UPDATE G_PNODES SET USER_ID=MUSER_ID,MUSER_ID=MUSER_ID,DEPT_ID=MDEPT_ID,MDEPT_ID=MDEPT_ID,STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + c.get("ID");// 添加退回原因
                                    _myRead.executeUpdate(strSql);
                                    // 插入G_INBOX----------------------------------
                                    data = new HashMap<String, String>();
                                    data.put("ID", strMaxId);
                                    data.put("PID", strPid);
                                    data.put("PNID", c.get("ID"));
                                    data.put("INFO_ID", strInfoId);
                                    data.put("DEPT_ID", c.get("DEPT_ID"));
                                    data.put("USER_ID", c.get("USER_ID"));
                                    data.put("FUSER_ID", Fuserid);
                                    data.put("UTYPE", "0");
                                    data.put("STATUS", "1");
                                    data.put("ACTNAME", c.get("WFNODE_CAPTION"));
                                    data.put("WF_ID", c.get("WF_ID"));
                                    data.put("WFNODE_ID", c.get("WFNODE_ID"));
                                    data.put("WFNODE_CAPTION", c.get("WFNODE_CAPTION"));
                                    data.put("WFNODE_WAIT", "0");
                                    data.put("SENDTYPE", strSendType);
                                    data.put("RDATE", dsoap.dsflow.DS_FLOWClass.processDate("NOW,", DBMS, now));
                                    data.put("EDATE", c.get("EDATE"));
                                    data.put("TIMESPAN", c.get("TIMESPAN"));
                                    data.put("TIMETYPE", c.get("TIMETYPE"));
                                    data.put("DAYS", "0");
                                    data.put("HANDLEWAY", "0");
                                    data.put("WAITCOUNT", "0");
                                    data.put("ISZNG", "NULL");
                                    data.put("FNODE", c.get("FNODE"));
                                    data.put("PRIORY", "0");
                                    //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                                    RECEIVE_USERNAME="";
                                    SEND_USERNAME="";
                                    try{
                                    	RECEIVE_USERNAME=this.getUserName(Long.parseLong(Fuserid));
                                    	SEND_USERNAME=this.getUserName(Long.parseLong(c.get("USER_ID")));
                                    }
                                    catch (Exception e) {
            						}
                                    data.put("RECEIVE_USERNAME",RECEIVE_USERNAME );
                                    data.put("SEND_USERNAME", SEND_USERNAME);
                                    data.put("BACKREASON", strReason);
                                  //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                                    dsoap.dsflow.DS_FLOWClass.insertGInbox(_myConn, data);
                                    if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                        strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                    } else if ("ORACLE".equals(DBMS)) {
                                        strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,backreason)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS,'"+strReason+"' from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                    } else if ("SQLSERVER".equals(DBMS)) {
                                        strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                    } else if ("MYSQL".equals(DBMS)) {
                                        strSql = "update g_inbox set BT=(select BT from g_infos where id=" + strInfoId + "),DELETED=(select DELETED from g_infos where id=" + strInfoId + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + strInfoId + "),URGENT=(select URGENT from g_infos where id=" + strInfoId + "),SWH=(select SWH from g_infos where id=" + strInfoId + "),WH=(select WH from g_infos where id=" + strInfoId + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                    } else {
                                        strSql = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId + ") where id=" + strMaxId;
                                    }
                                    _myRead.executeUpdate(strSql);

                                }
                            }
                    	}
            			
            		
                    }
                    // if (true) {
                    // throw (new Exception("该文件已被处理，无法回收！"));
                    // }
                    _myConn.commit();
                    // ---------------------------------------------------------
                    String BT = null;
                    long MODULE_ID = 0;
                    xsf.data.DataTable dt = DBManager.getDataTable("select BT,MODULE_ID from G_INFOS where ID=" + strInfoId);
                    if (dt.getRows().size() > 0) {
                        xsf.data.DataRow dr = dt.getRows().get(0);
                        BT = dr.getString("BT");
                        MODULE_ID = dr.getLong("MODULE_ID");
                    }
                    String formName = strAct + "了文件： " + BT + " ";
                    int action = "回收".equals(strAct) ? 100001006 : 100001007;
                    Log log = new Log(formName);// 操作内容
                    log.setAction(action);// 操作类型
                    log.setBody(formName);// 操作内容
                    if (sUser_ID != null && !"".equals(sUser_ID)) {
                        log.setUserID(Long.parseLong(sUser_ID));// 操作用户ID
                    }
                    log.setDate(new java.util.Date());// 操作时间
                    log.setClasz(MODULE_ID);// 表单模块ID
                    Logger.info(log); // 保存进g_log表
                    // ---------------------------------------------------------
                    DS_FLOWClass.doHttp(httpTask);
                    if (returnType == null) {
                        if (isth != null && "1".equals(isth)) {
                            txtRetualValue.setValue("OK");
                            errStr = "alert('退回成功!')\n";
                            errStr = "<script language=javascript>" + errStr + "top.window.close(); var isNew = " + session.get("isNewFile") + ";var a = '" + session.get("sendBackUrl") + "';if(!isNew){var b = top.window.Cookie.get('DaiBanBack');top.window.Cookie.set('DaiBanBack','');if(b == null || b == ''){b=a;}top.window.location.href = b;}else{top.window.location.href = a;};</script>";
                        } else {
                            errStr = "alert('回收成功!')\n";
                            errStr = "<script language=javascript>" + errStr + " var isNew = " + session.get("isNewFile") + ";var a = '" + session.get("sendBackUrl") + "';if(!isNew){var b = top.window.Cookie.get('DaiBanBack');top.window.Cookie.set('DaiBanBack','');if(b == null || b == ''){b=a;}top.window.location.href = b;}else{top.window.location.href = a;};</script>";
                        }
                    } else {
                        if (isth != null && "1".equals(isth)) {
                            errStr = "1";
                        } else {
                            errStr = "2";
                        }
                    }
                    return ERROR;
                } catch (Exception es) {
                    es.printStackTrace();
                    if (_myConn != null) {
                        _myConn.rollback();
                    }
                    if (returnType == null) {
                        errStr = "<script language=javascript>\n";
                        if (isth != null && "1".equals(isth)) {
                            errStr += "alert('退回失败!:" + es.getMessage() + "')\n";
                            errStr += "top.close()\n";
                        } else {
                            errStr += "alert('回收失败!" + es.getMessage() + strSql + "')\n";
                        }
                        errStr += "top.close()\n";
                        errStr += "</script>\n";
                    } else {
                        if (isth != null && "1".equals(isth)) {
                            errStr = "3";
                        } else {
                            errStr = "4";
                        }
                    }
                    return ERROR;
                }
                // }
                // // --------------------------- 没有儿子
                // else {
                // // 回收校验
                // strSql = "SELECT ID FROM G_INBOX WHERE PID=" + strPid + " AND PNID=" + strPnid;
                // _myDs = _myRead.executeQuery(strSql);
                // isok = false;
                // if (_myDs.next()) {
                // isok = true;
                // }
                // _myDs.close();
                // // 查找等待的兄弟节点的FNODE
                // strSql = "SELECT FNODE FROM G_INBOX WHERE PID=" + strPid + " AND PNID=" + strPnid + " AND FNODE IS NOT NULL AND FNODE<>''";
                // _myDs = _myRead.executeQuery(strSql);
                // if (_myDs.next()) {
                // strFnode = _myDs.getString("FNODE");
                // }
                // _myDs.close();
                // String strNode = "(" + Muserid + "," + strFPnid + ")";
                // strFnode = strFnode.replace(strNode, "");
                // _myConn.setAutoCommit(false);
                // try {
                // // 回收需要执行的ＳＱＬ
                // DS_FLOWClass.doCommand(GetSqlFromBack(strPid, strPnid), _myRead);
                // strSql = "INSERT INTO G_ACT_LOG(ID,INFO_ID,USER_ID,ACT_USER_ID,ACTNAME,ACT,ACTTIME,BZ) VALUES(" + strMaxActLogId + "," + strInfoId + "," + sUser_ID + "," + strChildUser + ",'" + strChildAct + "','" + strAct + "',";
                // if ("SYBASE".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else if ("ORACLE".equals(DBMS)) {
                // strSql += "SYSDATE,";
                // } else if ("SQLSERVER".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else {
                // strSql += "SYSDATE,";
                // }
                // strSql += "'" + strReason + "')";
                // _myRead.executeUpdate(strSql);
                // strSql = "UPDATE G_PNODES SET WAITCOUNT=WAITCOUNT+1,WFNODE_WAIT=1,FNODE='" + strFnode + "',BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + strPnid;
                // _myRead.executeUpdate(strSql);
                // strSql = "UPDATE G_INBOX SET WAITCOUNT=WAITCOUNT+1,WFNODE_WAIT=1,FNODE='" + strFnode + "' WHERE PID=" + strPid + " AND PNID=" + strPnid;
                // _myRead.executeUpdate(strSql);
                // strSql = "DELETE G_OPINION WHERE PID=" + strPid + " AND PNID=" + strPnid;
                // _myRead.executeUpdate(strSql);
                // strSql = "UPDATE G_OPINION SET STATUS=0 WHERE PID=" + strPid + " AND PNID=" + strFPnid + " AND ID=" + strMaxYjId;
                // _myRead.executeUpdate(strSql);
                // strSql = "UPDATE G_PNODES SET STATUS=1,BACKREASON='" + strReason + "' WHERE PID=" + strPid + " AND ID=" + strFPnid;
                // _myRead.executeUpdate(strSql);
                // strSql = "INSERT INTO G_INBOX (ID,INFO_ID,DEPT_ID,USER_ID,FUSER_ID,UTYPE,HANDLEWAY,PID,PNID,STATUS,PRIORY,RDATE,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,TIMETYPE,TIMESPAN,DAYS,SENDTYPE,WAITCOUNT,EDATE,FNODE) ";
                // strSql += "VALUES (" + strMaxId + "," + strInfoId + "," + DeptId + "," + Muserid + "," + Fuserid + ",0,0," + strPid + "," + strFPnid + ",1,0,";
                // if ("SYBASE".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else if ("ORACLE".equals(DBMS)) {
                // strSql += "SYSDATE,";
                // } else if ("SQLSERVER".equals(DBMS)) {
                // strSql += "GETDATE(),";
                // } else {
                // strSql += "GETDATE(),";
                // }
                // strSql += "'" + Caption + "'," + WfId + "," + WfNodeId + ",'" + Caption + "',0," + TimeType + "," + TimeSpan + ",0," + strSendType + ",0," + strEdate + ",'" + strFFnode + "')";
                // _myRead.executeUpdate(strSql);
                // // 取G_INFOS信息
                // _myStr = "select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + strInfoId;
                // _myDs = _myRead.executeQuery(_myStr);
                // if (_myDs.next()) {
                // strSql = "update g_inbox set BT='" + _myDs.getString("BT") + "',DELETED=" + _myDs.getString("DELETED") + ",HASCONTENT=" + _myDs.getString("HASCONTENT") + ",URGENT=" + _myDs.getString("URGENT") + ",SWH='" + _myDs.getString("SWH") + "',WH='" + _myDs.getString("WH") + "',OBJCLASS='" + _myDs.getString("OBJCLASS") + "' where id=" + strMaxId;
                // }
                // _myDs.close();
                // _myRead.executeUpdate(strSql);
                // // 回收校验
                // if (isok) {
                // _myConn.commit();
                // } else {
                // throw (new Exception("该文件已被处理，无法回收！"));
                // }
                // if (returnType == null) {
                // errStr = "<script language=javascript>\n";
                // if (isth != null && "1".equals(isth)) {
                // txtRetualValue.setValue("OK");
                // errStr += "alert('退回成功!')\n";
                // errStr += "top.close()\n";
                // } else {
                // errStr += "alert('回收成功!')\n";
                // }
                // errStr += "top.close()\n";
                // errStr += "</script>\n";
                // } else {
                // if (isth != null && "1".equals(isth)) {
                // errStr = "1";
                // } else {
                // errStr = "2";
                // }
                // }
                // } catch (Exception es) {
                // _myConn.rollback();
                // if (returnType == null) {
                // errStr = "<script language=javascript>\n";
                // if (isth != null && "1".equals(isth)) {
                // errStr += "alert('退回失败!:" + es.getMessage() + "')\n";
                // errStr += "top.close()\n";
                // } else {
                // errStr += "alert('回收失败!" + es.getMessage() + strSql + "')\n";
                // }
                // errStr += "top.close()\n";
                // errStr += "</script>\n";
                // } else {
                // if (isth != null && "1".equals(isth)) {
                // errStr = "3";
                // } else {
                // errStr = "4";
                // }
                // }
                // return ERROR;
                // }
                // }
            }
            // -------------------------------------------------------------------------------------------------------------------------------------------
        } catch (Exception e) {
            e.printStackTrace();
            if (returnType == null) {
                errStr = "<script language=javascript>\n";
                if (isth != null && "1".equals(isth)) {
                    errStr += "alert('退回失败!:" + e.getMessage() + "')\n";
                    errStr += "top.close()\n";
                } else {
                    errStr += "alert('回收失败!" + e.getMessage() + strSql + "')\n";
                }
                errStr += "top.close()\n";
                errStr += "</script>\n";
            } else {
                if (isth != null && "1".equals(isth)) {
                    errStr = "3";
                } else {
                    errStr = "4";
                }
            }
            return ERROR;
        } finally {
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
  //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始

    private String GetSqlFromBack(String sPid, String sPnid) throws Exception {
        String _cmdStr = "";
        String returnValue = "";
        _cmdStr = "select a.wfnode_backsendsql,b.user_id,b.info_id,b.dept_id,b.wf_id,b.wfnode_id from wfnodelist a,g_pnodes b where a.wf_id=b.wf_id and a.wfnode_id=b.wfnode_id and b.id=" + sPnid + " and b.pid=" + sPid;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            returnValue = dr.getString("wfnode_backsendsql") != null ? dr.getString("wfnode_backsendsql") : "";
            // -----------------------------------------------------------
            returnValue = xsf.Value.getString(returnValue, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            returnValue = returnValue == null ? "" : returnValue;
            // -----------------------------------------------------------
            // 常用关键字
            int iKeyCount = 5;
            String[] sKey = new String[iKeyCount];
            String[] sValue = new String[iKeyCount];
            // 当前用户ID
            sKey[0] = "[USERID]";
            sValue[0] = dr.getString("user_id");
            // 当前文件ID
            sKey[1] = "[INFO_ID]";
            sValue[1] = dr.getString("info_id");
            // 当前部门ID
            sKey[2] = "[DEPT_ID]";
            sValue[2] = dr.getString("dept_id");
            // 当前PID
            sKey[3] = "[PID]";
            sValue[3] = sPid;
            // 当前PNID
            sKey[4] = "[PNID]";
            sValue[4] = sPnid;
            for (int i = 0; i < iKeyCount; i++) {
                returnValue = returnValue.replace(sKey[i], sValue[i]);
            }
        }
        return returnValue;
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
            if (_myConn != null) {
                _myConn.rollback();
            }
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
  //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
}
