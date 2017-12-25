﻿package dsoap.dsflow;

import java.io.BufferedReader;
import java.io.File;

import message.MessageParser;

import org.apache.axis.utils.StringUtils;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.opensymphony.xwork2.ActionContext;

import xsf.Config;
import xsf.ID;
import xsf.data.DBManager;
import xsf.data.DataRowCollections;
import xsf.data.ICommand;
import xsf.data.IDataSource;
import xsf.data.Parameter;
import xsf.data.Sql;
import xsf.exception.RuntimeException;
import xsf.log.Log;
import xsf.platform.Logger;
import xsf.resource.ResourceManager;

import xsf.util.StringHelper;
import dsoap.dsflow.model.DataRow;
import dsoap.dsflow.model.DataTable;
import dsoap.dsflow.staticdata.FlowStaticData;
import dsoap.log.SystemLog;
import dsoap.message.RemindMessage;
import dsoap.tools.AttendanceUtils;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.Dom4jTools;
import dsoap.tools.SysDataSource;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class DS_FLOWClass {
    public static SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
    public static Object lock = new Object();
    public DS_FLOWClass ds_ParentFlow = null;
    public long iInfoID = 0;// ****************************
    public long iPID = 0;// ****************************
    public long iPnID = 0;// ****************************
    public long iWfID = 0;// ****************************
    public long iWfNodeID = 0;
    public long iSendType = 0;
  //新加的静态变量，用于存储供arrUserList需要的HASHMAP
    public static HashMap<String, xsf.data.DataTable> wholeOrgMap = new HashMap<String, xsf.data.DataTable>();
    public static xsf.data.DataTable dtUserRole = null;
    // 发送方式
    // 0：一般发送（选择节点）
    // 1：一般发送（选人）
    // 2：发送未完成的后继节点
    // 3：增发
    // 4，直接办结当前节点，用作非最后一个发送到汇总节点
    // 5：先选节点候选人
    // 9：办结
    // 当前流程为子流程办结，父流程发送方式
    // 10：一般发送（选择节点）
    // 11：一般发送（选人）
    // 12：发送未完成的后继节点
    // 13：增发
    // 14，直接办结当前节点，用作非作后一个发送到汇总节点
    // 5：先选节点候选人
    // 19：办结
    // 远程
    // 29：自动返回给来件人
    // public Document ContinueNodeXml;
    public Document NextNodeInfoXml;
    public Document SendUserListXml;
    public int iOutLineFlag = 0;// 节点的出口分支类型: 0(或分支-必须先选一个节点再选人发送),1(与分支)
    public int isHideYJ = 0; // 当前节点意见显示方式
    public int iErrorCode = 0;// 无错误
    public String sErrorMessage = "";
    public String strPriSend = "";
    public String strIP = "";
    public String strMAC = "";
    public String strBT = "";
	public String tip="";//办公提示
    public boolean isBatch = false;// 是否批量
    public int joinStatus = 0;// 0：最后一个人汇总，1：第一个人汇总时设置待办箱只读状态，2：最后一个人汇总时修改待办箱正常状态
    public Map<String, String> joinChilds = new HashMap<String, String>();
    public String joinToUsers = "";
    // ---------------------------------------------------
    private SAXReader saxreader = new SAXReader();
    private DocumentFactory df = saxreader.getDocumentFactory();
    private int iActType = 0;// **************************** 操作类型:0:一般发送,2:直接发送远程,3:征询
    public long iUserID = 0;// ****************************
    private long iParentPID = 0;
    private long iParentPnID = 0;
    private long iParentWfID = 0;
    private String sObjclass = "";// ****************************
    private String sCaption = "";// 当前节点名称
    //发送时短信、邮件、即时通讯提醒总开关(默认为不显示)
    public boolean msg_lock = false;
    public boolean isSendSMS = false;
    //是否发送短信
    public boolean isForword = false;
    //是否发送邮件
    public boolean isEMail = false;
    //是否发送即时通信
    public boolean isTray = false;
    //是否发送托盘提醒
    public boolean isTuoPan = false;
    
    public boolean isSendByRole = false; //dongchj 20160219
    public boolean isAutoSendAll = false; //dongchj 20160219
    public boolean isExtraSend = false; //dongchj 20170224
    
    public String redirectMode = "1";//dongchj20170328   流程特殊发送，1特送(现在改名叫"转交")(旧，传入PNID=-1，干掉所有正在处理的待办，产生一条新待办FID等于之前最大的PNID)，
    //2重定向,也可以叫特送(PNID就是当前发送节点PNID，干掉当前待办，生成一条新待办，等价于拉根线发过去)，增发(类似旧特送，但是不处理任何现有的待办，生成一条新独立待办独自处理)，暂时三种模式
    //4错发回收的增发,寻找错发源头并进行一次增发
    
    //是否是回溯
    public boolean isSendBack = false; //dongchj 2016/11/9 
    public String sendBackPnid = null; //回溯节点PNID
    public LinkedHashSet<Long> processList = null;
    // private boolean isSendEmail = false;
    // private boolean isSendTray = false;
    private boolean isGroupView = false; // 是否显示小组
    private boolean isSubFlow = false; // 子流程
    public String sSmsContent = "";
    // private boolean isSendRemind = false;
    // private Node myRoot;
    // private Node myNode;
    // ----------------------------
    // UYTPE
    // 0：一般用户
    // 1：小组
    // 2：部门
    // 3：角色
    // 4：权限
    // 5：私有小组
    // 6：
    // 7：
    // 8：服务器
    // 9：系统管理员
    // ----------------------------
    // NODETYPE
    // 0：结束节点
    // 1：开始节点
    // 2：远程节点
    // 3：一般节点
    // 4：汇总节点
    // 5：子流程节点
    // 6：跳转节点
    // ----------------------------
    private int iUserCount = 0;
    private int iGrpsCount = 0;
    private String[][] arrUserList;
    private String[][] arrGrpsList;
    private Map<String, DataTable> map = new HashMap<String, DataTable>();
    private long iRUserID = 0;// 发送到角色时的抢办人
    // 获取汇总节点范围内的为完成文件数
    private int[] iArrPnID;
    private int[] iArrFPnID;
    private int[] iArrWFNodeID;
    private int iG_PNodes_Count = 0;
    //流程中需要往页面 发送提示信息 记录的变量 杨龙修改 2012/9/11 开始
    public String wfTip="";
    //流程中需要往页面 发送提示信息 记录的变量 杨龙修改 2012/9/11 结束
    //增加批量发送 流程参数 杨龙修改 2012/9/26 开始
    public String batchFlowXML = "";
	//增加批量发送 流程参数 杨龙修改 2012/9/26 结束
    //是否抄送发送，抄送发送或分支不限制发送节点数量  杨龙修改 2012/11/3 开始
    public int isCS = 0;  	//0:非抄送发送，1：抄送发送
    public int isFirstCS = 0; //是否第一次抄送 第一次抄送需要选择主送节点，后续抄送则不需要选择主送 0：非第一次 1：第一次抄送
	//是否抄送发送，抄送发送或分支不限制发送节点数量  杨龙修改 2012/11/3 结束
    public String send_BACKREASON="";
    //如果已知后续节点ID，则初始化后续可发送节点时，使用已知ID进行初始化信息,多个ID以都好分割(1,2,3)
    public String nextNodeIDs="";
    private long iInfoMJ=0;	//当前文件密级属性 杨龙修改 2013/4/17
	public String MainUnit="";
	//重定向ACL
    public Node redirectAcl = null;
    //20170411 汇合人员和汇合部门人员
    
    //自定义参数集合,使用集合方便扩展
    public Map customParameter=new HashMap<String, String>();
	private String backReason = "";
    // 人员数据
    private enum G_USERS {
        ID, UNAME, UTYPE, DEPT_ID, DETP_NAME, DEPT_UTYPE, ISOUTER, TEL, EMAIL, LOGNAME
    }
    // 角色数据
    private enum G_GRPS {
        GRP_ID, USER_ID, USER_NAME, USER_UTYPE, DEPT_ID, DEPT_NAME
    }

    public DS_FLOWClass(String sParms) throws Exception {
        // 实例化XML文档
        this.NextNodeInfoXml = df.createDocument();
        this.SendUserListXml = df.createDocument();
        // this.ContinueNodeXml = df.createDocument();
        // 根据发送参数初始化参数信息
        Document parmsXml = DocumentHelper.parseText(sParms);
        // System.out.println(parmsXml.asXML());
        Node flow = parmsXml.selectSingleNode("/Root/Flow");
        // ----------------------------------------------
        this.iActType = Integer.parseInt(flow.selectSingleNode("Type").getText());
        this.iInfoID = Long.parseLong(flow.selectSingleNode("Key").getText());
        this.iPID = Long.parseLong(flow.selectSingleNode("Pid").getText());
        this.iPnID = Long.parseLong(flow.selectSingleNode("Pnid").getText());
        this.iWfID = Long.parseLong(flow.selectSingleNode("WfID").getText());
        this.iUserID = Long.parseLong(flow.selectSingleNode("UserID").getText());
        this.sObjclass = flow.selectSingleNode("Objclass").getText();
        
        try {
        	if(flow.selectSingleNode("AutoSend")!=null)
            {
        		this.isAutoSendAll = flow.selectSingleNode("AutoSend").getText().equals("1")?true : false; //是否自动发送所有
            }
        	if(flow.selectSingleNode("ExtraSend")!=null)
            {
        		this.isExtraSend = flow.selectSingleNode("ExtraSend").getText().equals("1")?true : false; //是否增发(2017新)
            }
		} catch (Exception e1) {
		}
        
        if(flow.selectSingleNode("RedirectMode")!=null)
        {
    		this.redirectMode = flow.selectSingleNode("RedirectMode").getText(); //1转交 2特送 3增发
        }
        
        //如果根据已知后续节点进行初始化，则xml参数中必须包含后续节点的ID参数nextNodeIDs
        if(flow.selectSingleNode("nextNodeIDs")!=null)
        {
            this.nextNodeIDs=flow.selectSingleNode("nextNodeIDs").getText();
        }
        if(flow.selectSingleNode("SendBack")!=null)
        {
        	this.isSendBack = flow.selectSingleNode("SendBack").getText().equals("1")?true : false; //是否发送回溯
        }
        if(flow.selectSingleNode("BackReason")!=null)
        {
        	this.backReason  = flow.selectSingleNode("BackReason").getText(); //退回理由
        }
        try {
            this.iRUserID = Long.parseLong(flow.selectSingleNode("RUserID").getText());
        } catch (Exception e) {
        }
        // ----------------------------------------------
        if (this.iPID == -1 || this.redirectMode.equals("2") || this.redirectMode.equals("3") ) {
            this.iSendType = 3;
            if(this.redirectMode.equals("1") || this.redirectMode.equals("3") ){
            	this.iPID = getPid(this.iInfoID);
                this.iPnID = 1;
            }
        }
        // 是否显示小组
        String groupView = ConfigurationSettings.AppSettings("GroupView");
        if (groupView != null) {
            this.isGroupView = Integer.valueOf(groupView) == 1;
        }
        if (!isExist()) {
            throw new Exception("该文件已被办理完毕，请确认！");
        }
        // ----------------------------------------------获取短信内容（依赖于infoid,userid,pid,pnid,wfid）
        this.sSmsContent = getSmsContent();
        // ----------------------------------------------是否为增发，条件PNID=0 and PID！=0 ;如果为增发，默认当前当前节点为拟稿
       /* if (this.iPnID == 0 && this.iPID != 0) {
            this.iSendType = 3;
            if(this.redirectMode.equals("1")|| this.redirectMode.equals("3")){
            	this.iPnID = 1;
            }
        }*/
        // ----------------------------------------------设置父pid,父pnid,父wfid,isSubFlow,返回wfnodeid（依赖于pid,pnid,wfid）
        this.iWfNodeID = this.getCurNodeID();
        // ----------------------------------------------获取当前节点意见显示模式（依赖于wfid,wfnodeid）
        this.isHideYJ = Integer.valueOf(getIsHideYJ());
        // ----------------------------------------------如果为增发，则不需要取得当前节点
        if (this.iWfNodeID == -1 && !(this.iPnID == 0 && this.iPID != 0)) {
            throw new Exception("流程信息有误，请联系管理员");
        }
		this.MainUnit=DBManager.getFieldStringValue("select mainunit from g_infos where id="+this.iInfoID);
        // ----------------------------------------------设置iOutLineFlag,sCaption,isSendRemind,（依赖于wfid,wfnodeid）
        setCurNodInfo();
        // ----------------------------------------------设置部门，人员，小组信息iUserCount,arrUserList,map,（依赖于userid）
        getUserDeptGroupInfo(this.MainUnit);
        //初始化人员角色的全局变量
        initUserRole();
        //获取文件的密级属性 	 杨龙修改 2013/4/17 开始
        this.iInfoMJ=this.getInfoMJ(this.iInfoID);
        //获取文件的密级属性	 杨龙修改 2013/4/17 结束
        // 根据发送参数，初始化发送信息，判断发送方式
        if (this.iSendType != 3) {// 正常发送
            // 有后继节点：发送未完成的后继节点
            if (hasContinueNode()) {// （依赖于pid,pnid）
                this.iSendType = 2;
            } else if(this.isSendBack){
            	this.iSendType = getBackNodeInfo();
            }else{
                // ----------------------------------------------设置isSendSMS,iOutLineFlag,isSendEmail,isSendTray,NextNodeInfoXml,ds_ParentFlow,iSendType,（依赖于iActType,iWfID,iWfNodeID,isSubFlow）
                // System.out.println(NextNodeInfoXml.asXML());
                this.iSendType = getNextNodeInfo();
            }
        }
//         System.out.println(NextNodeInfoXml.asXML());
    }

    // 指定当前节点用
    public DS_FLOWClass(String sParms, long pWfNodeID) throws Exception {
        // 实例化XML文档
        this.NextNodeInfoXml = df.createDocument();
        this.SendUserListXml = df.createDocument();
        // this.ContinueNodeXml = df.createDocument();
        // 根据发送参数初始化参数信息
        Document parmsXml = DocumentHelper.parseText(sParms);
        Node flow = parmsXml.selectSingleNode("/Root/Flow");
        // ----------------------------------------------
        this.iActType = Integer.parseInt(flow.selectSingleNode("Type").getText());
        this.iInfoID = Long.parseLong(flow.selectSingleNode("Key").getText());
        this.iPID = Long.parseLong(flow.selectSingleNode("Pid").getText());
        this.iPnID = Long.parseLong(flow.selectSingleNode("Pnid").getText());
        this.iWfID = Long.parseLong(flow.selectSingleNode("WfID").getText());
        this.iUserID = Long.parseLong(flow.selectSingleNode("UserID").getText());
        this.sObjclass = flow.selectSingleNode("Objclass").getText();
        // ----------------------------------------------
        // 是否显示小组
        String groupView = ConfigurationSettings.AppSettings("GroupView");
        if (groupView != null) {
            this.isGroupView = Integer.valueOf(groupView) == 1;
        }
        if (!isExist()) {
            throw new Exception("该文件已被办理完毕，请确认！");
        }
        this.sSmsContent = getSmsContent();
        // 是否为增发，条件PNID=0 and PID！=0
        if (this.iPnID == 0 && this.iPID != 0) {// 如果为增发，默认当前当前节点为拟稿
            this.iSendType = 3;
            this.iPnID = 1;
        }
        this.iWfNodeID = this.getCurNodeID();
        this.iWfNodeID = pWfNodeID;
        this.isHideYJ = Integer.parseInt(getIsHideYJ());
        if (this.iWfNodeID == -1 && !(this.iPnID == 0 && this.iPID != 0)) {// 如果为增发，则不需要取得当前节点
            throw new Exception("流程信息有误，请联系管理员");
        }
		this.MainUnit=DBManager.getFieldStringValue("select mainunit from g_infos where id="+this.iInfoID);
        setCurNodInfo();
        // 获取部门，人员，小组信息,存储于全局变量myDs中
        // myDs=new DataSet();
        getUserDeptGroupInfo(this.MainUnit);
        // 根据发送参数，初始化发送信息，判断发送方式
        if (this.iSendType != 3) {// 正常发送
            // 有后继节点：发送未完成的后继节点
            if (hasContinueNode()) {
                this.iSendType = 2;
            } else if(this.isSendBack){
            	this.iSendType = getBackNodeInfo();
            }else{
                this.iSendType = getNextNodeInfo();
            }
        }
    }

    // 发送
    public void send() throws Exception {
        try {
            if (this.iActType == 2) {
                // 直接发远程,征询反馈
                // send_2();
            } else {
                // 一般发送，发流程远程，征询
                send_0();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.iErrorCode = 60;
            throw e;
        }
    }

    public boolean sendToEnd() throws Exception {
        Date now = new Date();
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        List<String> httpTask = new ArrayList<String>();
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myConn.setAutoCommit(false);
            _myRead = _myConn.createStatement();
            String DBMS = ConfigurationSettings.AppSettings("DBMS");
            //------------------------办结时判断 如果是拟稿节点则插入一条拟稿的g_pnodes数据 杨龙修改 2012/08/14   开始
            if (this.iPID == 0) {
                // 插入G_PNODES
                this.iPID = this.getMaxValue("G_PID", 1);
                this.iPnID = 1;
                //isDraft = true;
                // _cmdStr = "INSERT INTO G_PNODES ";
                // _cmdStr += "(PID,ID,FID,INFO_ID,DEPT_ID,USER_ID,UTYPE,TIMESPAN,TIMETYPE,DAYS,";
                // _cmdStr += "RDATE,PDATE,EDATE,HANDLEWAY,STATUS,FSTATUS,WHOHANDLE,SIGNED,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,WAITCOUNT,MUSER_ID,UNAME) ";
                // _cmdStr += "VALUES (" + this.iPID + ",1,0," + this.iInfoID + "," + this.getDetpIDFromUserID(this.iUserID) + "," + this.iUserID + ",0,0,0,0,";
                // _cmdStr += processDate("NOW,NOW,NULL,", DBMS, now);
                // _cmdStr += "0,-1,-1," + this.iUserID + ",1,'" + this.sCaption + "'," + this.iWfID + "," + this.iWfNodeID + ",'" + this.sCaption + "',0,0,0," + this.iUserID + ",'" + this.getUserName(this.iUserID) + "')";
                // _myRead.executeUpdate(_cmdStr);
                Map<String, String> data = new HashMap<String, String>();
                data.put("PID", String.valueOf(this.iPID));
                data.put("ID", "1");
                data.put("FID", "0");
                data.put("INFO_ID", String.valueOf(this.iInfoID));
                data.put("DEPT_ID", this.getDraftDept(this.iInfoID));
                data.put("USER_ID", String.valueOf(this.iUserID));
                data.put("UTYPE", "0");
                data.put("UNAME", this.getUserName(this.iUserID));
                data.put("MDEPT_ID", "NULL");// *************************************************
                data.put("MUSER_ID", "NULL");
                data.put("STATUS", "-1");
                data.put("ACTNAME", this.sCaption);
                data.put("WF_ID", String.valueOf(this.iWfID));
                data.put("WFNODE_ID", String.valueOf(this.iWfNodeID));
                data.put("WFNODE_CAPTION", this.sCaption);
                data.put("WFNODE_WAIT", "0");
                data.put("SENDTYPE", "0");
                data.put("RDATE", processDate("NOW", DBMS, now));
                data.put("PDATE", processDate("NOW", DBMS, now));
                data.put("SDATE", "NULL");
                data.put("EDATE", "NULL");
                data.put("TIMESPAN", "0");
                data.put("TIMETYPE", "0");
                data.put("DAYS", "0");
                data.put("WHOHANDLE", String.valueOf(this.iUserID));
                data.put("HANDLEWAY", "0");
                data.put("WAITCOUNT", "0");
                data.put("ISZNG", "NULL");
                data.put("FSTATUS", "-1");
                data.put("PARENTFLOWPID", "0");
                data.put("PARENTFLOWPNID", "0");
                data.put("SIGNED", "1");
                data.put("ATYPE", "NULL");
                //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9------------开始
                data.put("WFLINE_TYPE", "0");
                data.put("ISCS", String.valueOf(this.isCS));
				data.put("mainUnit", this.MainUnit);
                //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------结束
                insertGPnodes(_myConn, data);
                // 插入G_INBOX
                long Start_G_INBOX_ID = this.getMaxValue("G_INBOX", 1);
                // _cmdStr = "INSERT INTO G_INBOX ";
                // _cmdStr += "(ID,INFO_ID,DEPT_ID,USER_ID,FUSER_ID,UTYPE,HANDLEWAY,PID,PNID,";
                // _cmdStr += "STATUS,PRIORY,RDATE,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,";
                // _cmdStr += "TIMETYPE,TIMESPAN,DAYS,SENDTYPE,WAITCOUNT,FNODE,EDATE,ISZNG";
                // if (this.strPriSend.indexOf("," + this.iUserID + ",") == -1) {
                // _cmdStr += ") ";
                // } else {
                // _cmdStr += ",NEEDPRI) ";
                // }
                // _cmdStr += "VALUES ";
                // _cmdStr += "(" + Start_G_INBOX_ID + "," + this.iInfoID + ",0," + this.iUserID + "," + this.iUserID + ",0,0," + this.iPID + ",1,1,0,";
                // _cmdStr += processDate("NOW,", DBMS, now);
                // _cmdStr += "'" + this.sCaption + "'," + this.iWfID + "," + this.iWfNodeID + ",'" + this.sCaption + "',0,0,0,0,0,0,'',null,0";
                //                
                if (this.strPriSend.indexOf("," + this.iUserID + ",") == -1) {
                    // _cmdStr += ")";
                } else {
                    // _cmdStr += ",1)";
                    SystemLog.SaveLog("文件操作", "越权发送", "1", this.iUserID, data.get("UNAME"), this.strIP, this.strMAC, this.strBT, "1");
                }
                // _myRead.executeUpdate(_cmdStr);
                data.put("ID", String.valueOf(Start_G_INBOX_ID));
                // data.put("PID", null);
                data.put("PNID", "1");
                // data.put("INFO_ID", String.valueOf(this.iInfoID));
                // data.put("DEPT_ID", "0");
                // data.put("USER_ID", String.valueOf(this.iUserID));
                data.put("FUSER_ID", String.valueOf(this.iUserID));// ??????????????????????
                // data.put("UTYPE", "0");
                data.put("STATUS", "1");
                // data.put("ACTNAME", null);
                // data.put("WF_ID", null);
                // data.put("WFNODE_ID", null);
                // data.put("WFNODE_CAPTION", null);
                // data.put("WFNODE_WAIT", null);
                // data.put("SENDTYPE", null);
                // data.put("RDATE", null);
                // data.put("EDATE", null);
                // data.put("TIMESPAN", null);
                // data.put("TIMETYPE", null);
                // data.put("DAYS", null);
                // data.put("HANDLEWAY", null);
                // data.put("WAITCOUNT", null);
                // data.put("ISZNG", null);
                data.put("FNODE", "NULL");
                data.put("PRIORY", "0");
				data.put("TIP", this.tip);
                // data.put("NEEDPRI", this.strPriSend.indexOf("," + this.iUserID + ",") == -1 ? "NULL" : "1");// ??????????????????????
                insertGInbox(_myConn, data);
                // 吴红亮 添加 开始
                if (dsoap.tools.ConfigurationSettings.isDraftOpinion) {
                    _cmdStr = "UPDATE G_OPINION SET PID=" + this.iPID + ",STATUS=1 WHERE PID=0 AND PNID=1 AND ID=" + this.iInfoID;
                    _myRead.executeUpdate(_cmdStr);
                    //宋俊修改 此处代码需要G_OPINION_ATTACH有INFO_ID支持 并且业务逻辑 需要ezweb前端支持 2012/12/28开始
                    /******* 意见附件 add by john.he 20121207 ***********/
                    try{
                    _cmdStr = "UPDATE G_OPINION_ATTACH SET PID=" + this.iPID + " WHERE PID=0 AND PNID=1 AND INFO_ID=" + this.iInfoID;
                    _myRead.executeUpdate(_cmdStr);
                    }
                    catch (Exception e) {
						e.printStackTrace();
					}
                    //宋俊修改 此处代码需要G_OPINION_ATTACH有INFO_ID支持 并且业务逻辑 需要ezweb前端支持 2012/12/28结束
                }
                doCommand(getSqlFromAfterSendSql(getFirstAfter(_myRead)), _myRead, httpTask);
                // 吴红亮 添加 结束
            }
          //------------------------办结时判断 如果是拟稿节点则插入一条拟稿的g_pnodes数据 杨龙修改 2012/08/14   结束
            // ------------------------------------------------------------------------------------处理上一节点的数据G_INFOS，G_OPINION，G_PNODES，G_PROUTE，G_INBOX
            // 判别当前流程尚未办结的节点数目
            // 若为1则表示就当前节点尚未办结，此时不仅要更新当前节点的状态，还需更新G_Infos.Status
            // 反之则表示尚有其他节点没有办结，此时仅更新当前节点的状态即可
            // ATYPE=3时，为征询节点
            int iCount = 0;
            //_cmdStr = "SELECT COUNT(ID) NCOUNT FROM G_PNODES WHERE PID=" + this.iPID + " AND STATUS<>-1 AND ATYPE<>3 AND ACTNAME NOT LIKE '%抄送%'";
            //修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
            _cmdStr = "SELECT COUNT(ID) NCOUNT FROM G_PNODES WHERE PID=" + this.iPID + " AND STATUS<>-1 AND ATYPE<>3 AND ISCS<>1";
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                if (_myDs.getString("NCOUNT") != null) {
                    iCount = _myDs.getInt("NCOUNT");
                }
            }
            _myDs.close();
            // ---------------------------吴红亮 添加 开始 抄送
            boolean isCC = false;
            _cmdStr = "SELECT WFNODE_CAPTION as ACTNAME FROM WFNODELIST WHERE WF_ID=" + this.iWfID + " AND WFNODE_ID=" + this.iWfNodeID;
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                String actName = _myDs.getString("ACTNAME");
                // -----------------------------------------------------------
                actName = xsf.Value.getString(actName, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
//                if (actName != null && actName.startsWith("抄送")) {
//                    isCC = true;
//                }
                if(1==this.isCS)
                {
                	isCC = true;
                }
            }
            _myDs.close();
            // ---------------------------吴红亮 添加 结束 抄送
            String SendType = "0";
            if (this.iSendType == 39) {// 增发办结
                this.iSendType = 9;
                this.iPnID = this.getMaxPNID1(_myRead, this.iPID);
                SendType = "3";
                _cmdStr = "UPDATE G_OPINION SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID<>1 AND STATUS<>1";
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_PNODES SET STATUS=-1,MUSER_ID=USER_ID,MDEPT_ID=DEPT_ID,ISHIDEYJ=" + this.isHideYJ + " WHERE PID=" + this.iPID + " AND STATUS<>-1";
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_PROUTE SET STATUS=-1 WHERE PID=" + this.iPID + " AND STATUS<>-1";
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "DELETE FROM G_INBOX WHERE PID=" + this.iPID;
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_OPINION SET PID=" + this.iPID + ",PNID=" + this.iPnID + ",ID=1 WHERE PID=0 AND PNID=0 AND ID=" + +this.iInfoID;
                _myRead.executeUpdate(_cmdStr);
            } else {// 普通办结
                _cmdStr = "UPDATE G_OPINION SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID + " AND STATUS<>1";
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "DELETE FROM G_INBOX WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_PNODES SET STATUS=-1,MUSER_ID=USER_ID,USER_ID=" + this.iUserID + "," + processDate("SDATE=NOW,PDATE=NOW ", DBMS, now) + "WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_PROUTE SET STATUS=-1 WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
                _myRead.executeUpdate(_cmdStr);
            }
            // ---------------------------------------------------------------------------------------------------------------------------- 发送新数据
            // System.out.println(this.NextNodeInfoXml.asXML());
            for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                Node nextNode = (Node) obj;
                if ("1".equals(nextNode.valueOf("@Enabled"))) {
                    // System.out.println(nextNode.asXML());
                    // if (this.iSendType == 9) {// 汇总不执行，办结要执行
                    // 查询最大的Pnid
                    long G_PNID = this.getMaxPNID(_myRead, this.iPID) + 1;
                    // 获取插入节点的用户名
                    String strNextUname = ((Node) nextNode.selectNodes("@Enabled").get(0)).valueOf("@UName");
                    String wfNodeId = nextNode.valueOf("@NodeID");
                    String nodeCaption = nextNode.valueOf("@NodeCaption");
                    String nodeType = nextNode.valueOf("@NodeType");
                  //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9------------开始
                    String wfLineType = nextNode.valueOf("@LineType");
                  //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9------------结束
                    // --------------------------------------------------------
                    String uid = "0";
                    String mid = "0";
                    String eDate = "NOW";
                    String deptId = "";
                    if (this.iSendType == 4) {
                        Node n = (Node) nextNode.selectNodes("Node").get(0);
                        while (!n.valueOf("@UType").equals("0")) {
                            n = (Node) n.selectNodes("Node").get(0);
                        }
                        deptId = n.valueOf("@fId");
                        strNextUname = n.valueOf("@UName");
                        uid = n.valueOf("@Id");
                        mid = uid;
                        eDate = "NULL";
                        //-------------------------杨龙修改汇总抢办开始-----------------------
                        // 抢办
                        StringBuffer strSql=new StringBuffer();
                        strSql.append("select C.ID from");
                        strSql.append(" (select PID,FID,WF_ID,WFNODE_ID,DEPT_ID from G_PNODES where PID=" + this.iPID + " and ID=" + this.iPnID + ")A ");
                        strSql.append(" inner join");
                        strSql.append(" (select WF_ID,WFNODE_ID from WFNODELIST where WFNODE_NODETYPE=8)B on A.WF_ID=B.WF_ID and A.WFNODE_ID=B.WFNODE_ID ");
                        strSql.append(" left join");
                        //抢办节点 所有的部门抢办，不单独每个部门抢办
                        //strSql.append(" (select PID,ID,FID,DEPT_ID from G_PNODES where PID=" + this.iPID + ")C on C.PID=A.PID and C.FID=A.FID and C.DEPT_ID=A.DEPT_ID");
                        strSql.append(" (select PID,ID,FID,DEPT_ID from G_PNODES where PID=" + this.iPID + ")C on C.PID=A.PID and C.FID=A.FID ");
                        _myRead.execute(strSql.toString());
                        _myDs = _myRead.getResultSet();
                        String ids = "";
                        while (_myDs.next()) {
                            ids += _myDs.getString("ID") + ",";
                        }
                        _myDs.close();
//                        if (!"".equals(ids)) {
                        if (false) {
                            ids = ids.substring(0, ids.length() - 1);
                            strSql.setLength(0);
                            strSql.append("update G_PNODES set STATUS=-1,SDATE=" + processDate("NOW", DBMS, now) + " where PID=" + this.iPID + " and ID in(" + ids + ")");
                            _myRead.executeUpdate(strSql.toString());
                            strSql.setLength(0);
                            strSql.append("delete from G_INBOX where PID=" + this.iPID + " and PNID in(" + ids + ")");
                            _myRead.executeUpdate(strSql.toString());
                        }
                      //-------------------------杨龙修改汇总抢办结束-----------------------
                    } else {
                        deptId = String.valueOf(getDetpIDFromUserID(this.iUserID));
                        uid = String.valueOf(this.iUserID);
                        mid = uid;
                    }
                    // --------------------------------------------------------
                    // _cmdStr = "INSERT INTO G_PNODES ";
                    // _cmdStr += "(PID,ID,FID,INFO_ID,DEPT_ID,USER_ID,UTYPE,";
                    // _cmdStr += "TIMESPAN,TIMETYPE,DAYS,RDATE,HANDLEWAY,STATUS,FSTATUS,SIGNED,";
                    // _cmdStr += "ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,";
                    // _cmdStr += "WFNODE_WAIT,SENDTYPE,WAITCOUNT,EDATE,MUSER_ID,ISZNG,PARENTFLOWPID,PARENTFLOWPNID,PDATE,SDATE,UNAME) ";
                    // _cmdStr += "VALUES ";
                    // _cmdStr += "(" + this.iPID + "," + G_PNID + "," + this.iPnID + "," + this.iInfoID + "," + deptId + "," + uid + ",0,";
                    // _cmdStr += "0,0,0," + processDate("NOW", DBMS, now) + ",0,-1,-1,1,";
                    // _cmdStr += "'" + nodeCaption + "'," + this.iWfID + "," + wfNodeId + ",'" + nodeCaption + "',";
                    // _cmdStr += "0,0,0," + processDate(("NOW".equals(eDate) ? "NOW" : "NULL"), DBMS, now) + "," + mid + ",0," + this.iParentPID + "," + this.iParentPnID + "," + processDate("NOW,NOW", DBMS, now) + ",'" + strNextUname + "')";
                    // _myRead.executeUpdate(_cmdStr);
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("PID", String.valueOf(this.iPID));
                    data.put("ID", String.valueOf(G_PNID));
                    data.put("FID", String.valueOf(this.iPnID));
                    data.put("INFO_ID", String.valueOf(this.iInfoID));
                    data.put("DEPT_ID", deptId);
                    data.put("USER_ID", uid);
                    data.put("UTYPE", "0");
                    data.put("UNAME", strNextUname);
                    data.put("MDEPT_ID", "NULL");// *************************************************
                    data.put("MUSER_ID", String.valueOf(mid));
                    data.put("STATUS", "-1");
                    data.put("ACTNAME", nodeCaption);
                    data.put("WF_ID", String.valueOf(this.iWfID));
                    data.put("WFNODE_ID", wfNodeId);
                    data.put("WFNODE_CAPTION", nodeCaption);
                    data.put("WFNODE_WAIT", "0");
                    data.put("SENDTYPE", SendType);
                    data.put("RDATE", processDate("NOW", DBMS, now));
                    data.put("PDATE", processDate("NOW", DBMS, now));
                    data.put("SDATE", processDate("NOW", DBMS, now));
                    data.put("EDATE", processDate(("NOW".equals(eDate) ? "NOW" : "NULL"), DBMS, now));
                    data.put("TIMESPAN", "0");
                    data.put("TIMETYPE", "0");
                    data.put("DAYS", "0");
                    data.put("WHOHANDLE", String.valueOf(this.iUserID));
                    data.put("HANDLEWAY", "0");
                    data.put("WAITCOUNT", "0");
                    data.put("ISZNG", "0");
                    data.put("FSTATUS", "-1");
                    data.put("PARENTFLOWPID", String.valueOf(this.iParentPID));
                    data.put("PARENTFLOWPNID", String.valueOf(this.iParentPnID));
                    data.put("SIGNED", "1");
                    data.put("ATYPE", "NULL");
                  //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9------------开始
                    data.put("WFLINE_TYPE", wfLineType);
                    if(!"9".equals(nodeType)&&1==this.isFirstCS)
                    {
                    	data.put("ISCS", "0");
                    }
                    else
                    {
                    data.put("ISCS", String.valueOf(this.isCS));
                    }
					data.put("mainUnit", this.MainUnit);
                  //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9------------开始
                    insertGPnodes(_myConn, data);
                    if (this.isBatch) {
                        insertBatchOpinion(_myConn, _myRead);
                    }
                    // 插入G_PROUTE
                    //_cmdStr = "INSERT INTO G_PROUTE (PID,ID,FPID,FID,STATUS) VALUES (" + this.iPID + "," + G_PNID + "," + this.iPID + "," + this.iPnID + ",-1)";
                    //_myRead.executeUpdate(_cmdStr);
                    // }
                  //发送前的处理语句 杨龙修改  2013/4/23  开始
                    String strSql_WFNODE_BEFORESENDSQL="select WFNODE_BEFORESENDSQL From wfnodelist where wf_id="+this.iWfID+" and wfnode_id="+this.iWfNodeID+"";
                    ResultSet rs_WFNODE_BEFORESENDSQL=_myRead.executeQuery(strSql_WFNODE_BEFORESENDSQL);
                    if(rs_WFNODE_BEFORESENDSQL.next())
                    {
                    	String strBEFORESENDSQL=rs_WFNODE_BEFORESENDSQL.getString(1)==null?"":rs_WFNODE_BEFORESENDSQL.getString(1);
                    	if(!"".equals(strBEFORESENDSQL))
                    	{
                    		   doCommand(getSqlFromAfterSendSql(strBEFORESENDSQL), _myRead, httpTask);
                    	}
                    }
                  //发送前的处理语句 杨龙修改  2013/4/23  结束
                    // 发送结束后处理语句
                    //增加发送结束后线上SQL处理，如果线上没有SQL，则执行节点上的SQL 杨龙修改 2012/10/16 开始
                    //doCommand(getSqlFromAfterSendSql(nextNode.valueOf("@AfterSendSql")), _myRead, httpTask);
                    	if(!"".equals(nextNode.valueOf("@AfterSql")))
                        {
                    		doCommand(getSqlFromAfterSendSql(nextNode.valueOf("@AfterSql")), _myRead, httpTask);
                        }
                    	else
                    	{
                    		doCommand(getSqlFromAfterSendSql(nextNode.valueOf("@AfterSendSql")), _myRead, httpTask);
                    	}
                    //增加发送结束后线上SQL处理，如果线上没有SQL，则执行节点上的SQL 杨龙修改 2012/10/16 结束
                    if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                        if (this.iSendType == 4) {
                            // this.joinToUsers = getReceivers(_myRead, wfNodeId);
                            this.joinToUsers = strNextUname;
                            if (this.joinStatus == 2) {
                                joinFinish(_myRead, wfNodeId);
                            }
                        }
                    }
                }
            }
            // 更新G_Infos.Status设置办结状态
            // 吴红亮 更新 开始
            // if (iCount < 2 && this.iSendType == 9 && this.iSendType < 10) {
            if (iCount < 2 && this.iSendType == 9 && this.iSendType < 10 && !isCC) {
                // 吴红亮 更新 结束
                _cmdStr = "UPDATE G_INFOS SET STATUS=2,";
                _cmdStr += processDate("BJRQ=NOW,", DBMS, now);
                _cmdStr += "BJR=" + this.iUserID + " WHERE ID=" + this.iInfoID;
                _myRead.executeUpdate(_cmdStr);
            }
            // if (true) {
            // throw new Exception("测试");
            // }
            //修改线上ation执行顺序，先执行线上action后提交事务 杨龙修改 2013/09/23 开始
//            _myConn.commit();
//            doHttp(httpTask);
            doHttp(httpTask);
            _myConn.commit();
          //修改线上ation执行顺序，先执行线上action后提交事务 杨龙修改 2013/09/23 结束
            // ---------------------------------------------------------
            String BT = null;
            long MODULE_ID = 0;
            xsf.data.DataTable dt = DBManager.getDataTable("select BT,MODULE_ID from G_INFOS where ID=" + this.iInfoID);
            if (dt.getRows().size() > 0) {
                xsf.data.DataRow dr = dt.getRows().get(0);
                BT = dr.getString("BT");
                MODULE_ID = dr.getLong("MODULE_ID");
            }
            String formName = "办结了文件： " + BT + " ";
            Log log = new Log(formName);// 操作内容
            log.setAction(100001005);// 操作类型
            log.setBody(formName);// 操作内容
            log.setUserID(this.iUserID);// 操作用户ID
            log.setDate(new java.util.Date());// 操作时间
            log.setClasz(MODULE_ID);// 表单模块ID
            Logger.info(log); // 保存进g_log表
            // ---------------------------------------------------------
        } catch (Exception e) {
            e.printStackTrace();
            this.iErrorCode = 100;
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
            if (_myConn != null) {
                try {
                    _myConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    // 返回待选节点列表 DataTable
    @SuppressWarnings("unchecked")
    public DataTable getSelectNodesViewTable() {
        // 吴红亮 添加 开始
        List<DataRow> generalRows = new ArrayList<DataRow>();
        List<DataRow> specialRows = new ArrayList<DataRow>();
        // 吴红亮 添加 结束
        DataTable NodeTable = new DataTable("NODES");
        for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
            Node nextNode = (Node) obj;
            // 吴红亮 添加 开始
            String nodeCaption = nextNode.valueOf("@NodeCaption");
            String nodeType = nextNode.valueOf("@NodeType");
//            if (nodeCaption.indexOf("抄送") > -1) {
//                continue;
//            }
            if(1==this.isFirstCS&&"9".equals(nodeType))
            {
            	continue;
            }
            String lineType = nextNode.valueOf("@LineType");
            String lineCaption = nextNode.valueOf("@LineCaption");
            // 吴红亮 添加 结束
            DataRow newRow = NodeTable.NewRow();
            newRow.put("ID", nextNode.valueOf("@ID"));
            newRow.put("NAME", lineCaption == null || "".equals(lineCaption) ? nodeCaption : lineCaption);
            newRow.put("SENDVALUE", nextNode.valueOf("@ID"));
            newRow.put("LINETYPE", lineType);
            // NodeTable.Add(newRow);
            // 吴红亮 添加 开始
            if ("3".equals(lineType)) {
                specialRows.add(newRow);
            } else {
                generalRows.add(newRow);
            }
            // 吴红亮 添加 结束
        }
        // 吴红亮 添加 开始
        // Collections.sort(generalRows, new Comparator() {
        // public int compare(Object o1, Object o2) {
        // DataRow d1 = (DataRow) o1;
        // DataRow d2 = (DataRow) o2;
        // return d1.get("NAME").compareTo(d2.get("NAME"));
        // }
        // });
        // Collections.sort(specialRows, new Comparator() {
        // public int compare(Object o1, Object o2) {
        // DataRow d1 = (DataRow) o1;
        // DataRow d2 = (DataRow) o2;
        // return d1.get("NAME").compareTo(d2.get("NAME"));
        // }
        // });
        for (DataRow dr : generalRows) {
            NodeTable.Add(dr);
        }
        for (DataRow dr : specialRows) {
            NodeTable.Add(dr);
        }
        // 吴红亮 添加 结束
        return NodeTable;
    }
    
    /**
     *@author 清除后续可选节点中已选中节点，将选中节点的状态重置为未选中 杨龙修改 2013-7-30
     *@param	Type	1 清除所有抄送节点 2 清除所有节点 
     *@param	nextNodeInfoXml 后续节点的xml
     */
    private void clearNodeEndAble(Document nextNodeInfoXml,int Type) throws Exception {
        for (Object obj : nextNodeInfoXml.selectNodes("Nodes/Node")) {
            Node nextWorkFlowNode = (Node) obj;
            if ("1".equals(nextWorkFlowNode.valueOf("@Enabled"))) {
            	String nodeType = nextWorkFlowNode.valueOf("@NodeType");
                switch (Type) {
    			case 1:
    				if ("9".equals(nodeType)) {
    	                nextWorkFlowNode.selectSingleNode("@Enabled").setText("0");
    	            } 
    				break;
    			case 2:
    				nextWorkFlowNode.selectSingleNode("@Enabled").setText("0");
    				break;
    			}
            }
        }
    }
    
    // 设置所选择的节点信息
    public void setSelectNodeID(long sID) throws Exception {
      //   System.out.println(this.NextNodeInfoXml.asXML());
        boolean hasCC = false;// 是否存在抄送节点
        boolean isSelectUser=true;//抄送节点是否需要选人
        for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
            Node nextNode = (Node) obj;
            String id = nextNode.valueOf("@ID");
            //String pnid = nextNode.valueOf("@PNID"); //回溯时会用到回溯到的节点原来的PNID这个字段
            String nodeType = nextNode.valueOf("@NodeType");
            String nodeCaption = nextNode.valueOf("@NodeCaption");
            String isOnlyOneUser = nextNode.valueOf("@IsOnlyOneUser");
            String activeCount = nextNode.valueOf("@ActiveCount");
            String isSMS = nextNode.valueOf("@IsSMS");
            String wfNodeId = nextNode.valueOf("@NodeID");
            
            String wf_id = nextNode.valueOf("@WfId");
            
            // 吴红亮 添加 开始
            String LineType = nextNode.valueOf("@LineType");
//            if (nodeCaption.indexOf("抄送") > -1) {
//                hasCC = true;
//            }
            if(1==this.isCS)
            {
            	hasCC = true;
            }
            // 吴红亮 添加 结束
            // ------------------------------------------------------------------------------------处理选中节点
            //如果是按照已知的后续节点初始化的流程，则发送时不按照流程节点索引比较，而是按照真实的节点ID比较，此处需要修改indexAction中接收的选人参数sustr传的节点ID为真实节点ID
            if(!"".equals(this.nextNodeIDs))
            {
            	id=wfNodeId;
            }
            if (sID == Long.parseLong(id)) {
                nextNode.selectSingleNode("@Enabled").setText("1");
                // 当节点为远程发送时，IsOnlyOneUser=1表示自动返回给远程发送来的节点
                if ("2".equals(nodeType) && "1".equals(isOnlyOneUser)) { // ------------------远程节点
                    String sExDataID = isRemoteAotoBack();
                    if (!"0".equals(sExDataID)) {
                        remoteAutoBack(sExDataID, nextNode);
                        this.iSendType = 29;
                        break;
                    }
                }
                getNextNodeUList_ByNode(nextNode);// *********************************************************************处理发送用户列表
                if ("0".equals(nodeType)) { // ------------------办结节点
                	isSelectUser=false;//办结节点抄送不需要选人
                    if (this.isSubFlow) {// 子流程办结
                        this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                        if (this.ds_ParentFlow.iSendType == -1) {
                            this.iErrorCode = 50;
                            throw new Exception("未找到子流程的返回节点信息！");
                        }
                        this.iSendType = 10 + this.ds_ParentFlow.iSendType;
                    } else {
                        this.iSendType = 9;
                    }
                    this.clearNodeEndAble(this.NextNodeInfoXml, 1);
                } else if ("4".equals(nodeType) && "2".equals(LineType)) { // ------------------汇总节点
                	isSelectUser=false;//汇总节点抄送不需要选人
                    if (!"0".equals(activeCount)) {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                this.joinStatus = 1;
                                this.iSendType = 5;
                            } else {
                                this.iSendType = 4;
                            }
                        } else {
                            this.iSendType = 4;
                        }
                    } else {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                this.iSendType = 5;
                            } else {
                                this.joinStatus = 2;
                                this.iSendType = 4;
                            }
                        } else {
                            this.iSendType = 5;
                        }
                    }
                } else if ("6".equals(nodeType)) { // ------------------跳转节点
                    String sTempParms = "<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iPID + "</Pid><Pnid>" + this.iPnID + "</Pnid><WfID>" + this.iWfID + "</WfID></Flow></Root>";
                    DS_FLOWClass tempDS = new DS_FLOWClass(sTempParms, Long.parseLong(wfNodeId));
                    this.NextNodeInfoXml = DocumentHelper.parseText(tempDS.NextNodeInfoXml.asXML());
                    this.iSendType = tempDS.iSendType;
                } else {// ------------------默认进入选人
                	this.joinStatus = 0; //非常重要，由于DSFLOW是点击发送后全局的，所以如果点击过汇总节点产生了JOINSTATUS，需要在这里清零 董辰杰修改20150820
                    this.iSendType = 5;
                }
                this.isSendSMS = "1".equals(isSMS);// 短信
                setRemind(wf_id,wfNodeId);
            }
            // ------------------------------------------------------------------------------------处理未选中节点
            else {
//                if (nodeCaption.indexOf("抄送") < 0) {
//                    nextNode.selectSingleNode("@Enabled").setText("0");
//                }
            	//如果是第一次抄送，重新选择节点时，非抄送节点选中状态重置；非第一次抄送时，重新选择节点所有节点状态重置为未选中  杨龙修改 2013.3.20
            	if(1==this.isFirstCS)
            	{
            		if(!"9".equals(nodeType))
            		{
            			nextNode.selectSingleNode("@Enabled").setText("0");
            		}
            		if(1!=this.isCS)
                    {
                		nextNode.selectSingleNode("@Enabled").setText("0");
                    }
            	}
            	else if("".equals(this.nextNodeIDs))
            	{
            		nextNode.selectSingleNode("@Enabled").setText("0");
            	}
            }
        }
        // 吴红亮 添加 开始
        if (hasCC&&isSelectUser) {
            this.iSendType = 5;// 包含抄送时必须进入选人
        }
        // 吴红亮 添加 结束
        // System.out.println(this.NextNodeInfoXml.asXML());
    }
//------------------------------------设置所选择的节点信息，将以前的按照序列号改为按照节点ID设置  批量特送专用  杨龙修改 2012/8/23  开始
    // 设置所选择的节点信息
    public void setSelectNodeIDIsTS(long sID) throws Exception {
        // System.out.println(this.NextNodeInfoXml.asXML());
        boolean hasCC = false;// 是否存在抄送节点
        for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
            Node nextNode = (Node) obj;
            String id = nextNode.valueOf("@ID");
            String nodeType = nextNode.valueOf("@NodeType");
            String nodeCaption = nextNode.valueOf("@NodeCaption");
            String isOnlyOneUser = nextNode.valueOf("@IsOnlyOneUser");
            String activeCount = nextNode.valueOf("@ActiveCount");
            String wfNodeId = nextNode.valueOf("@NodeID");
            String isSMS = nextNode.valueOf("@IsSMS");
            // 吴红亮 添加 开始
            String LineType = nextNode.valueOf("@LineType");
//            if (nodeCaption.indexOf("抄送") > -1) {
//                hasCC = true;
//            }
            if(1==this.isCS)
            {
            	hasCC = true;
            }
            // 吴红亮 添加 结束
            // ------------------------------------------------------------------------------------处理选中节点
            if (sID == Long.parseLong(wfNodeId)) {
                nextNode.selectSingleNode("@Enabled").setText("1");
                getNextNodeUList_ByNode(nextNode);// *********************************************************************处理发送用户列表          
            }
            // ------------------------------------------------------------------------------------处理未选中节点
            else {
//                if (nodeCaption.indexOf("抄送") < 0) {
//                    nextNode.selectSingleNode("@Enabled").setText("0");
//                }
            	if(1!=this.isCS)
                {
            		nextNode.selectSingleNode("@Enabled").setText("0");
                }
            }
        }
    }
 // 设置发送列表
    public void setSendInfoIsTS(String sUser, String sSendMethod, String sNodeDate) throws Exception {
        // System.out.println(sUser);
        // System.out.println(sSendMethod);
        // System.out.println(sNodeDate);
        this.SendUserListXml = DocumentHelper.parseText(this.NextNodeInfoXml.asXML());
        Node nodes = this.SendUserListXml.selectSingleNode("Nodes");
        // System.out.println(NextNodeInfoXml.asXML());
        // System.out.println(myRoot.asXML());
        ((Element) nodes).addAttribute("Count", String.valueOf(sUser.split(";").length - 1));
        int iCount = 0;
        // System.out.println(myRoot.selectNodes("Node").size());
        for (Object obj : nodes.selectNodes("Node")) {
            Node nextNode = (Node) obj;
            //System.out.println(nextNode.asXML());
            if ("0".equals(nextNode.valueOf("@Enabled")) || "0".equals(nextNode.valueOf("@showNode"))) {
                ((Element) nodes).remove(nextNode);
                continue;
            }
            // -----------------------设置节点发送类型
            // 0：并行，1：串行
            String sNodeID = nextNode.valueOf("@ID");
            String stemp = ("," + sNodeID + ":");
            int b = sSendMethod.indexOf(stemp) + stemp.length();
            // 吴红亮 更新 开始
            // int e = stemp.length() + 1;
            int e = b + 1;
            // 吴红亮 更新 结束
            // System.out.println(sSendMethod + "-----" + stemp + "-----" + b + "-----" + e);
            String s = "0";
            // System.out.println(s);
            nextNode.selectSingleNode("@SendMethod").setText(s);
            // -----------------------设置办结日期
            nextNode.selectSingleNode("@NodeDate").setText(sNodeDate);
            // -----------------------设置发送用户列表
            setNodeSelectUserListIsTS(nextNode, sUser);
            ((Element) nextNode).addAttribute("Count", String.valueOf(nextNode.selectNodes("Node").size()));
            iCount += nextNode.selectNodes("Node").size();
        }
        nodes.selectSingleNode("@Count").setText(String.valueOf(iCount));
        if (iCount == 0) {
            this.iErrorCode = 25;
            throw new Exception("当前选择的人员数为零，请选择人员");
        }
    }
 // 根据已选用户生产用户列表(;:1:2282:节点2:投资处:王燚;:0:3081:检查大队:信访办:李四;:0:3074:检查大队:检查大队:张三)
    private void setNodeSelectUserListIsTS(Node td, String sUser) {
        String sHasUserList = "";
        Node temptd = (Node) td.clone();
        ((Element) td).clearContent();
        String sNodeID = td.valueOf("@NodeID");
        String[] sUserList = sUser.split(";");
        for (String s : sUserList) {
            if ("".equals(s)) {
                continue;
            }
            if (s.indexOf(":" + sNodeID + ":") == -1) {
                continue;
            }
            String sUserID = s.split(":")[2];
            String sDeptID = s.split(":")[6];
            Node usertd = getSelectUserbyUserID(temptd, sUserID, sDeptID);
            if (usertd != null) {
                if (((Element) usertd).elements().size() > 0) {
                    // 私有小组
                    for (Object obj : usertd.selectNodes("Node")) {
                        Node subtd = (Node) obj;
                        if (sHasUserList.indexOf("," + subtd.valueOf("@Id") + ":" + subtd.valueOf("@fId")) == -1) {
                            ((Element) td).add(subtd);
                            sHasUserList += "," + subtd.valueOf("@Id") + ":" + subtd.valueOf("@fId");
                        }
                    }
                } else {
                    if (sHasUserList.indexOf("," + usertd.valueOf("@Id") + ":" + usertd.valueOf("@fId")) == -1) {
                        ((Element) td).add(usertd);
                        sHasUserList += "," + usertd.valueOf("@Id") + ":" + usertd.valueOf("@fId");
                    }
                }
            }
        }
    }

    /**
	 * 
	 * 设置所选择的节点信息 重载setSelectNodeID()方法
	 * 
	 * @param sID流程节点ID
	 * @author yl
	 * */
	public void setSelectWfNodeID(long sID) throws Exception {
		// System.out.println(this.NextNodeInfoXml.asXML());
		boolean hasCC = false;// 是否存在抄送节点
		boolean isSelectUser = true;// 抄送节点是否需要选人
		for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
			Node nextNode = (Node) obj;
			String id = nextNode.valueOf("@ID");
			String nodeType = nextNode.valueOf("@NodeType");
			String nodeCaption = nextNode.valueOf("@NodeCaption");
			String isOnlyOneUser = nextNode.valueOf("@IsOnlyOneUser");
			String activeCount = nextNode.valueOf("@ActiveCount");
			String wfNodeId = nextNode.valueOf("@NodeID");
			String isSMS = nextNode.valueOf("@IsSMS");
			// 吴红亮 添加 开始
			String LineType = nextNode.valueOf("@LineType");
			// if (nodeCaption.indexOf("抄送") > -1) {
			// hasCC = true;
			// }
			if (1 == this.isCS) {
				hasCC = true;
			}
			// 吴红亮 添加 结束
			// ------------------------------------------------------------------------------------处理选中节点
			// 如果是按照已知的后续节点初始化的流程，则发送时不按照流程节点索引比较，而是按照真实的节点ID比较，此处需要修改indexAction中接收的选人参数sustr传的节点ID为真实节点ID
			// if(!"".equals(this.nextNodeIDs))
			// {
			// id=wfNodeId;
			// }
			if (sID == Long.parseLong(wfNodeId)) {
				nextNode.selectSingleNode("@Enabled").setText("1");
				// 当节点为远程发送时，IsOnlyOneUser=1表示自动返回给远程发送来的节点
				if ("2".equals(nodeType) && "1".equals(isOnlyOneUser)) { // ------------------远程节点
					String sExDataID = isRemoteAotoBack();
					if (!"0".equals(sExDataID)) {
						remoteAutoBack(sExDataID, nextNode);
						this.iSendType = 29;
						break;
					}
				}
				getNextNodeUList_ByNode(nextNode);// *********************************************************************处理发送用户列表
				if ("0".equals(nodeType)) { // ------------------办结节点
					isSelectUser = false;// 办结节点抄送不需要选人
					if (this.isSubFlow) {// 子流程办结
						this.ds_ParentFlow = new DS_FLOWClass(
								"<Root><Flow><Type>0</Type><Key>"
										+ this.iInfoID + "</Key><Objclass>"
										+ this.sObjclass
										+ "</Objclass><UserID>" + this.iUserID
										+ "</UserID><Pid>" + this.iParentPID
										+ "</Pid><Pnid>" + this.iParentPnID
										+ "</Pnid><WfID>" + this.iParentWfID
										+ "</WfID></Flow></Root>");
						if (this.ds_ParentFlow.iSendType == -1) {
							this.iErrorCode = 50;
							throw new Exception("未找到子流程的返回节点信息！");
						}
						this.iSendType = 10 + this.ds_ParentFlow.iSendType;
					} else {
						this.iSendType = 9;
					}
					this.clearNodeEndAble(this.NextNodeInfoXml, 1);
				} else if ("4".equals(nodeType) && "2".equals(LineType)) { // ------------------汇总节点
					isSelectUser = false;// 汇总节点抄送不需要选人
					if (!"0".equals(activeCount)) {
						if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
							if (isFirstJoin(wfNodeId)) {
								this.joinStatus = 1;
								this.iSendType = 5;
							} else {
								this.iSendType = 4;
							}
						} else {
							this.iSendType = 4;
						}
					} else {
						if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
							if (isFirstJoin(wfNodeId)) {
								this.iSendType = 5;
							} else {
								this.joinStatus = 2;
								this.iSendType = 4;
							}
						} else {
							this.iSendType = 5;
						}
					}
				} else if ("6".equals(nodeType)) { // ------------------跳转节点
					if (true) {
						break;
					}
					String sTempParms = "<Root><Flow><Type>0</Type><Key>"
							+ this.iInfoID + "</Key><Objclass>"
							+ this.sObjclass + "</Objclass><UserID>"
							+ this.iUserID + "</UserID><Pid>" + this.iPID
							+ "</Pid><Pnid>" + this.iPnID + "</Pnid><WfID>"
							+ this.iWfID + "</WfID></Flow></Root>";
					DS_FLOWClass tempDS = new DS_FLOWClass(sTempParms,
							Long.parseLong(wfNodeId));
					this.NextNodeInfoXml = DocumentHelper
							.parseText(tempDS.NextNodeInfoXml.asXML());
					this.iSendType = tempDS.iSendType;
				} else {// ------------------默认进入选人
					this.iSendType = 5;
				}
				this.isSendSMS = "1".equals(isSMS);// 短信
			}
			// ------------------------------------------------------------------------------------处理未选中节点
		}
		// 吴红亮 添加 开始
		if (hasCC && isSelectUser) {
			this.iSendType = 5;// 包含抄送时必须进入选人
		}
		// 吴红亮 添加 结束
		// System.out.println(this.NextNodeInfoXml.asXML());
	}
    
//------------------------------------设置所选择的节点信息，将以前的按照序列号改为按照节点ID设置  批量特送专用  杨龙修改 2012/8/23  结束
    // 判别当前流程尚未办结的节点数目
    public int getActiveNodeCount() throws Exception {
        String _cmdStr = "";
        // 判别当前流程尚未办结的节点数目
        // 若为1则表示就当前节点尚未办结，此时不仅要更新当前节点的状态，还需更新G_Infos.Status
        // 反之则表示尚有其他节点没有办结，此时仅更新当前节点的状态即可
        // ATYPE=3时，为征询节点
        int iCount = 0;
        _cmdStr = "SELECT COUNT(ID) NCOUNT FROM G_PNODES WHERE PID=" + this.iPID + " AND STATUS<>-1 AND ATYPE<>3";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            if (dr.getString("NCOUNT") != null) {
                iCount = dr.getInt("NCOUNT");
            }
        }
        return iCount;
    }

    // 设置发送列表
    public void setSendInfo(String sUser, String sSendMethod, String sNodeDate) throws Exception {
        // System.out.println(sUser);
        // System.out.println(sSendMethod);
        // System.out.println(sNodeDate);
        this.SendUserListXml = DocumentHelper.parseText(this.NextNodeInfoXml.asXML());
        Node nodes = this.SendUserListXml.selectSingleNode("Nodes");
        // System.out.println(NextNodeInfoXml.asXML());
        // System.out.println(myRoot.asXML());
        ((Element) nodes).addAttribute("Count", String.valueOf(sUser.split(";").length - 1));
        int iCount = 0;
        // System.out.println(myRoot.selectNodes("Node").size());
        for (Object obj : nodes.selectNodes("Node")) {
            Node nextNode = (Node) obj;
           // System.out.println(nextNode.asXML());
            if ("0".equals(nextNode.valueOf("@Enabled")) || "0".equals(nextNode.valueOf("@showNode"))) {
                ((Element) nodes).remove(nextNode);
                continue;
            }
            
            // -----------------------设置节点发送类型
            // 0：并行，1：串行
            String sNodeID = nextNode.valueOf("@ID");
            String stemp = ("," + sNodeID + ":");
            int b = sSendMethod.indexOf(stemp) + stemp.length();
            // 吴红亮 更新 开始
            // int e = stemp.length() + 1;
            int e = b + 1;
            // 吴红亮 更新 结束
            // System.out.println(sSendMethod + "-----" + stemp + "-----" + b + "-----" + e);
            String s = sSendMethod.substring(b, e);
            // System.out.println(s);
            nextNode.selectSingleNode("@SendMethod").setText(s);
            // -----------------------设置办结日期
            nextNode.selectSingleNode("@NodeDate").setText(sNodeDate);
            // -----------------------设置发送用户列表
            setNodeSelectUserList(nextNode, sUser);
            ((Element) nextNode).addAttribute("Count", String.valueOf(nextNode.selectNodes("Node").size()));
            iCount += nextNode.selectNodes("Node").size();
        }
        nodes.selectSingleNode("@Count").setText(String.valueOf(iCount));
        if (iCount == 0) {
            this.iErrorCode = 25;
            throw new Exception("当前选择的人员数为零，请选择人员");
        }
    }

    // 设置所选择的节点信息
    public void setSelectNodeID_P(long sID) throws Exception {
        this.ds_ParentFlow.setSelectNodeID(sID);
        this.iSendType = 10 + this.ds_ParentFlow.iSendType;
    }

    // 文件是否可发送
    public boolean isExist() throws Exception {
    	 String _cmdStr = "";
         int iCount = -1;
         if (ActionContext.getContext().getSession().get("isAddSend") != null)
         {
        	 if("true".equals(ActionContext.getContext().getSession().get("isAddSend").toString()))
        	 {
        		 ActionContext.getContext().getSession().remove("isAddSend");
        		 return true;
        	 }
         }
        if (this.iSendType == 3 || this.isExtraSend || this.redirectMode.equals("4")) {
            return true;
        }
//        if (this.iPnID == 0) {// 拟稿
//            return true;
//        }
        //拟稿发送，验证info_id是否在g_pnodes表中已存在,已存在说明重复拟稿 杨龙修改 20131011 开始
        if (this.iPnID == 0) {// 拟稿
        	_cmdStr="SELECT COUNT(PID) CNT FROM G_PNODES WHERE INFO_ID="+this.iInfoID;
        	 xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
             if (dt.getRows().size() > 0) {
                 iCount = dt.getRows().get(0).getInt("CNT");
             }
             return iCount == 0?true:false;
        }
      //拟稿发送，验证info_id是否在g_pnodes表中已存在,已存在说明重复拟稿 杨龙修改 20131011 结束
       
        _cmdStr = "SELECT COUNT(PID) CNT FROM G_INBOX WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            iCount = dt.getRows().get(0).getInt("CNT");
        }
        return iCount > 0;
    }

    public boolean isAuthority(String WFNODE_ID, String userId, String deptId, Statement statement) throws Exception {
        Node nextNode = null;
        for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
            nextNode = (Node) obj;
            if (WFNODE_ID.equals(nextNode.valueOf("@NodeID"))) {
                break;
            } else {
                nextNode = null;
            }
        }
        if (nextNode == null) {
            return false;
        }
        if (!"1".equals(nextNode.valueOf("@IsAuthList"))) {
            String xml = null;
            String _cmdStr = "SELECT WFNODE_XML FROM WFNODELIST where WF_ID=" + this.iWfID + " and WFNODE_ID=" + WFNODE_ID;
            ResultSet _myDs = statement.executeQuery(_cmdStr);
            if (_myDs.next()) {
                xml = _myDs.getString("WFNODE_XML");
                // -----------------------------------------------------------
                xml = xsf.Value.getString(xml, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
            } else {
                return false;
            }
            _myDs.close();
            Document NDXml = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>" + xml + "</Root>");
            String sAcl = Dom4jTools.getSingleNodeInnerXml(NDXml, "Root/ACL");
            ((Element) nextNode).clearContent();// 清除<Node>子节点
            if (!"".equals(sAcl)) {
                ((Element) nextNode).appendContent((DocumentHelper.parseText("<root><ACL>" + sAcl + "</ACL></root>").getRootElement()));// 添加<ACL>子节点
            }
            nextNode.selectSingleNode("@IsHaveUList").setText("0");
            Attribute isAuthList = (Attribute) nextNode.selectSingleNode("@IsAuthList");
            if (isAuthList == null) {
                ((org.dom4j.Element) nextNode).addAttribute("IsAuthList", "1");
            } else {
                isAuthList.setValue("1");
            }
            getNextNodeUList_ByNode(nextNode);// 解析<ACL>子节点
        }
        // System.out.println(nextNode.asXML());
        return isAuthority(nextNode, userId, deptId);
    }

    // 文件是否办结
    public boolean isEnd() throws Exception {
        String _cmdStr = "";
        boolean isEnd = false;
        _cmdStr = "select * from G_INFOS where ID=" + this.iInfoID + " and STATUS=2";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            isEnd = true;
        }
        return isEnd;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unused")
    private String parseErrorCode() {
        switch (this.iErrorCode) {
        case 0:
            return "";
        case 9:
            return "";
        case 21:
            return "获取人员列表出错！！";
        case 55:
            return "isExist()";
        case 60:
            return "send()";
        case 100:
            return "sendToEnd()";
        case 101:
            return "拟稿人不存在或已经注销！";
        default:
            return "";
        }
    }

    private boolean isAuthority(Node workFlowNode, String userId, String deptId) {
        boolean result = false;
        for (Object obj : workFlowNode.selectNodes("Node")) {
            Node userNode = (Node) obj;
            String uType = userNode.valueOf("@UType");
            // --------------------------------------------------------
            if ("8".equals(uType) || "2".equals(uType) || "5".equals(uType)) {
                result = isAuthority(userNode, userId, deptId);
                if (result) {
                    break;
                }
            } else {
                if (userId.equals(userNode.valueOf("@Id")) && deptId.equals(userNode.valueOf("@fId"))) {
                    return true;
                }
            }
        }
        return false;
    }

    // 获取当前WfNodeID
    private long getCurNodeID() throws Exception {
        String _cmdStr = "";
        long WfNodeId = -1;
        // -------------------------------------------------------------------------------------------------------
        if (this.iPID == 0) {// 起始节点
            _cmdStr = "SELECT WFNODE_ID FROM WFNODELIST WHERE WF_ID=" + this.iWfID + " AND WFNODE_NODETYPE=1";
        } else {
            _cmdStr = "SELECT WFNODE_ID FROM G_PNODES WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
        }
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            if (dr.getString("WFNODE_ID") != null) {
                WfNodeId = Integer.parseInt(dr.getString("WFNODE_ID"));
            }
        }
        // -------------------------------------------------------------------------------------------------------
        _cmdStr = "SELECT PARENTFLOWPID,PARENTFLOWPNID FROM G_PNODES WHERE PID=" + this.iPID + " AND ID=1";
        dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            this.iParentPID = Integer.parseInt(dr.getString("PARENTFLOWPID"));
            this.iParentPnID = Integer.parseInt(dr.getString("PARENTFLOWPNID"));
        }
        // -------------------------------------------------------------------------------------------------------
        _cmdStr = "SELECT WF_ID FROM G_PNODES WHERE PID=" + this.iParentPID + " AND ID=" + this.iParentPnID;
        dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            this.iParentWfID = Integer.parseInt(dt.getRows().get(0).getString("WF_ID"));
        }
        // -------------------------------------------------------------------------------------------------------
        if (this.iParentPID != 0) {
            this.isSubFlow = true;
        }
        return WfNodeId;
    }
    
    // 获取当前文件密级
    private long getInfoMJ(long infoID) throws Exception {
    	long mj=0;
    	if(!dsoap.tools.ConfigurationSettings.openInfoMJ)
    	{
    		return mj;
    	}
        String _cmdStr = "";
        _cmdStr = "select MJ from g_infos where id='"+infoID+"'";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            if (dr.getString("MJ") != null&&!"".equals(dr.getString("MJ"))) {
                mj = Integer.parseInt(dr.getString("MJ"));
            }
        }
        return mj;
    }

    // 获取当前节点信息
    private void setCurNodInfo() throws Exception {
        String _cmdStr = "";
        // 获取当前节点信息：出线类型 iOutLineFlag
        String msgServer = ConfigurationSettings.AppSettings("消息服务地址");
        if (msgServer != null && !"".equals(msgServer)) {
            _cmdStr = "SELECT WFNODE_OUTLINEFLAG,WFNODE_ID,WFNODE_CAPTION,SENDMESSAGE FROM WFNODELIST WHERE WF_ID=" + this.iWfID + " AND WFNODE_ID=" + this.iWfNodeID;
        } else {
            _cmdStr = "SELECT WFNODE_OUTLINEFLAG,WFNODE_ID,WFNODE_CAPTION FROM WFNODELIST WHERE WF_ID=" + this.iWfID + " AND WFNODE_ID=" + this.iWfNodeID;
        }
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            this.iOutLineFlag = dr.getInt("WFNODE_OUTLINEFLAG");
            this.sCaption = dr.getString("WFNODE_CAPTION") != null ? dr.getString("WFNODE_CAPTION") : "";
            // 是否需要发送提醒
            // if (msgServer != null && !"".equals(msgServer)) {
            // if ("1".equals(_myDs.getString("SENDMESSAGE") != null ? _myDs.getString("SENDMESSAGE") : "")) {
            // this.isSendRemind = true;
            // }
            // }
        }
    }

 // 获取用户，部门，小组信息
    private void getUserDeptGroupInfo() throws Exception {
        String _cmdStr = "";
        // // 获取所有部门和人员结构
        // // 吴红亮 修改 开始
        // // _cmdStr = "SELECT TOP (100) PERCENT A.ID,A.UNAME,A.UTYPE,D.ID AS DEPT_ID,D.UNAME DETP_NAME,D.UTYPE DEPT_UTYPE,A.ISOUTER,A.mobile_email as TEL,A.EMAIL,A.LOGNAME,E.UNAME AS MAINUNION FROM G_USERS A LEFT JOIN G_USERS E ON A.MAINCODE=E.ID,G_DEPT B,G_DEPT C,G_USERS D WHERE A.ID = B.USER_ID AND A.STATUS >= 0 AND A.UTYPE <> 3 AND B.FID=C.ID AND C.USER_ID=D.ID ORDER BY B.SHORDER";
        // // String a = "select count(*) as num from (" + _cmdStr + ") AS derivedtbl_1";
        // // // ---------------------------
        // // _cmdStr = "SELECT A.ID,A.UNAME,A.UTYPE,D.ID AS DEPT_ID,D.UNAME DETP_NAME,D.UTYPE DEPT_UTYPE,A.ISOUTER,A.mobile_email as TEL,A.EMAIL,A.LOGNAME,E.UNAME AS MAINUNION FROM G_USERS A LEFT JOIN G_USERS E ON A.MAINCODE=E.ID,G_DEPT B,G_DEPT C,G_USERS D WHERE A.ID=B.USER_ID AND A.STATUS>=0 AND A.UTYPE<>3 AND B.FID=C.ID AND C.USER_ID=D.ID";
        // _cmdStr = "select A.ID,A.UNAME,A.UTYPE,D.ID AS DEPT_ID,D.UNAME DETP_NAME,D.UTYPE DEPT_UTYPE,A.ISOUTER,A.mobile_email as TEL,A.EMAIL,A.LOGNAME,E.UNAME AS MAINUNION from G_USERS A left join G_USERS E on A.MAINCODE=E.ID left join G_DEPT B on A.ID=B.USER_ID left join G_DEPT C on B.FID=C.ID left join G_USERS D on C.USER_ID=D.ID where A.STATUS>=0 and A.UTYPE<>3";
        // String a = "select count(*) as num from (" + _cmdStr + ") derivedtbl_1";
        // _cmdStr += " order by B.SHORDER";
        // // 吴红亮 修改 结束
        // xsf.data.DataTable dt = DBManager.getDataTable(a);
        // if (dt.getRows().size() > 0) {
        // xsf.data.DataRow dr = dt.getRows().get(0);
        // this.iUserCount = dr.getInt("num");
        // }
        // this.arrUserList = new String[this.iUserCount][11];
        // dt = DBManager.getDataTable(_cmdStr);
        
       
        String mainUnit = DBManager.getFieldStringValue("select MAINUNIT from g_INFOS where ID = " + this.iInfoID);
        
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("MAINUNIT",mainUnit);
        sqlDataSource.setParameter("MAINUNIT2",mainUnit);
        sqlDataSource.setParameter("MAINUNIT3",mainUnit);
        xsf.data.DataTable dt = sqlDataSource.query("getUserDeptGroupInfo");
        
        if (dt != null) {
            this.iUserCount = dt.getRows().size();
        }
        this.arrUserList = new String[this.iUserCount][12];
        int i = 0;
        for (xsf.data.DataRow dr : dt.getRows()) {
            this.arrUserList[i][0] = dr.getString("ID") != null ? dr.getString("ID") : "";// ID
            this.arrUserList[i][1] = dr.getString("UNAME") != null ? dr.getString("UNAME") : "";// 名称
            this.arrUserList[i][2] = dr.getString("UTYPE") != null ? dr.getString("UTYPE") : "";// 类型
            this.arrUserList[i][3] = dr.getString("DEPT_ID") != null ? dr.getString("DEPT_ID") : "";// 父ID
            this.arrUserList[i][4] = dr.getString("DETP_NAME") != null ? dr.getString("DETP_NAME") : "";// 父名称
            this.arrUserList[i][5] = dr.getString("DEPT_UTYPE") != null ? dr.getString("DEPT_UTYPE") : "";// 父类型
            this.arrUserList[i][6] = dr.getString("ISOUTER") != null ? dr.getString("ISOUTER") : "";// 是否独立子机构
            this.arrUserList[i][7] = dr.getString("TEL") != null ? dr.getString("TEL") : "";// 电话
            this.arrUserList[i][8] = dr.getString("EMAIL") != null ? dr.getString("EMAIL") : "";// 邮箱
            this.arrUserList[i][9] = dr.getString("LOGNAME") != null ? dr.getString("LOGNAME") : "";// 登录名
            this.arrUserList[i][10] = dr.getString("MAINUNION") != null ? dr.getString("MAINUNION") : "";// ？？？？
            //人员增加密级属性 G_USERINFO.MJ 杨龙修改 2013/4/17
            this.arrUserList[i][11] = dr.getString("MJ") != null ? dr.getString("MJ") : "";// ？？？？
            i++;
        }
        // 私有小组，统计局用
        // 获取当前用户的私有小组人员结构 暂未用到该功能，如果去掉G_USERS表，该处应修改
        DataTable AclTable = new DataTable("G_GRPS_PRI");
        DataRow dr = null;
        _cmdStr = "select a.GRP_NAME,b.ID USER_ID,b.UNAME USER_NAME,b.UTYPE USER_UTYPE,e.ID DEPT_ID,e.UNAME DEPT_NAME from G_GRPS_PRI a,G_USERS b,G_DEPT c,G_DEPT d,G_USERS e where PRI_USER_ID=" + this.iUserID + " and a.USER_ID=b.ID and b.ID=c.USER_ID and c.FID=d.ID and d.USER_ID=e.ID order by a.GRP_NAME";
        // System.out.println(_cmdStr);
        dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow row : dt.getRows()) {
            dr = AclTable.NewRow();
            dr.put("grp_name", row.getString("GRP_NAME") != null ? row.getString("GRP_NAME") : "");
            dr.put("user_id", row.getString("USER_ID") != null ? row.getString("USER_ID") : "");
            dr.put("user_utype", row.getString("USER_UTYPE") != null ? row.getString("USER_UTYPE") : "");
            dr.put("dept_id", row.getString("DEPT_ID") != null ? row.getString("DEPT_ID") : "");
            dr.put("dept_name", row.getString("DEPT_NAME") != null ? row.getString("DEPT_NAME") : "");
            AclTable.Add(dr);
        }
        this.map.put(AclTable.getTablename(), AclTable);
        // -----------------------------------------------------------
    }
    
 // 获取用户，部门，小组信息
    private void getUserDeptGroupInfo(String mainUnit) throws Exception {
        String _cmdStr = "";
       
        //String mainUnit = DBManager.getFieldStringValue("select MAINUNIT from g_INFOS where ID = " + this.iInfoID);
        xsf.data.DataTable dt = null;
        if(wholeOrgMap.containsKey(mainUnit)){
        	dt = wholeOrgMap.get(mainUnit);
        }else{
        	IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            sqlDataSource.setParameter("MAINUNIT",mainUnit);
            sqlDataSource.setParameter("MAINUNIT2",mainUnit);
            sqlDataSource.setParameter("MAINUNIT3",mainUnit);
            dt = sqlDataSource.query("getUserDeptGroupInfo");
        }
        
        if (dt != null) {
            this.iUserCount = dt.getRows().size();
        }
        this.arrUserList = new String[this.iUserCount][12];
        int i = 0;
        for (xsf.data.DataRow dr : dt.getRows()) {
            this.arrUserList[i][0] = dr.getString("ID") != null ? dr.getString("ID") : "";// ID
            this.arrUserList[i][1] = dr.getString("UNAME") != null ? dr.getString("UNAME") : "";// 名称
            this.arrUserList[i][2] = dr.getString("UTYPE") != null ? dr.getString("UTYPE") : "";// 类型
            this.arrUserList[i][3] = dr.getString("DEPT_ID") != null ? dr.getString("DEPT_ID") : "";// 父ID
            this.arrUserList[i][4] = dr.getString("DETP_NAME") != null ? dr.getString("DETP_NAME") : "";// 父名称
            this.arrUserList[i][5] = dr.getString("DEPT_UTYPE") != null ? dr.getString("DEPT_UTYPE") : "";// 父类型
            this.arrUserList[i][6] = dr.getString("ISOUTER") != null ? dr.getString("ISOUTER") : "";// 是否独立子机构
            this.arrUserList[i][7] = dr.getString("TEL") != null ? dr.getString("TEL") : "";// 电话
            this.arrUserList[i][8] = dr.getString("EMAIL") != null ? dr.getString("EMAIL") : "";// 邮箱
            this.arrUserList[i][9] = dr.getString("LOGNAME") != null ? dr.getString("LOGNAME") : "";// 登录名
            this.arrUserList[i][10] = dr.getString("MAINUNION") != null ? dr.getString("MAINUNION") : "";// ？？？？
            //人员增加密级属性 G_USERINFO.MJ 杨龙修改 2013/4/17
            this.arrUserList[i][11] = dr.getString("MJ") != null ? dr.getString("MJ") : "";// ？？？？
            i++;
        }
        // 私有小组，统计局用
        // 获取当前用户的私有小组人员结构 暂未用到该功能，如果去掉G_USERS表，该处应修改
        DataTable AclTable = new DataTable("G_GRPS_PRI");
        DataRow dr = null;
        _cmdStr = "select a.GRP_NAME,b.ID USER_ID,b.UNAME USER_NAME,b.UTYPE USER_UTYPE,e.ID DEPT_ID,e.UNAME DEPT_NAME from G_GRPS_PRI a,G_USERS b,G_DEPT c,G_DEPT d,G_USERS e where PRI_USER_ID=" + this.iUserID + " and a.USER_ID=b.ID and b.ID=c.USER_ID and c.FID=d.ID and d.USER_ID=e.ID order by a.GRP_NAME";
        // System.out.println(_cmdStr);
        dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow row : dt.getRows()) {
            dr = AclTable.NewRow();
            dr.put("grp_name", row.getString("GRP_NAME") != null ? row.getString("GRP_NAME") : "");
            dr.put("user_id", row.getString("USER_ID") != null ? row.getString("USER_ID") : "");
            dr.put("user_utype", row.getString("USER_UTYPE") != null ? row.getString("USER_UTYPE") : "");
            dr.put("dept_id", row.getString("DEPT_ID") != null ? row.getString("DEPT_ID") : "");
            dr.put("dept_name", row.getString("DEPT_NAME") != null ? row.getString("DEPT_NAME") : "");
            AclTable.Add(dr);
        }
        this.map.put(AclTable.getTablename(), AclTable);
        // -----------------------------------------------------------
    }

    // 判断是否有未完成的后继节点，串行用
    private boolean hasContinueNode() throws Exception {
        String _cmdStr = "";
        boolean isHas = false;
        _cmdStr = "select * from G_INBOX where PNID in (select ID from G_PROUTE where PID=" + this.iPID + " and FID=" + this.iPnID + " and STATUS=1) and PID=" + this.iPID + " and WFNODE_WAIT=1";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            isHas = true;
        }
        return isHas;
    }

    // 获取发送节点信息，返回发送方式0：一般发送（选择节点），1：一般发送（选人）
    private long getNextNodeInfo() throws Exception {
        try {
            long iReturnValue = 0;
            // 获取节点列表
            iReturnValue = getNextNodes();
            // 获取人员列表
            // getNextNodeUList();
            return iReturnValue;
        } catch (Exception e) {
            e.printStackTrace();
            this.iErrorCode = 3;
            throw e;
        }
    }
    
    // 获取发送回溯节点信息 dongchj 20161109
    private long getBackNodeInfo() throws Exception {
        try {
            long iReturnValue = 0;
            this.processList = getProcessList(this.iPID ,this.iPnID);
            // 获取节点列表
            iReturnValue = getBackNodes();
            // 获取人员列表
            // getNextNodeUList();
            return iReturnValue;
        } catch (Exception e) {
            e.printStackTrace();
            this.iErrorCode = 3;
            throw e;
        }
    }

    private LinkedHashSet<Long> getProcessList(long pid, long pnid) {
		LinkedHashSet<Long> prolist = new LinkedHashSet<Long>();
		Sql sql = new Sql("select id,fid from g_pnodes where pid="+pid);
		DataRowCollections drcollect = DBManager.getDataTable(sql).getRows();
		long currentid = pnid;
		
		createlist(prolist,currentid,drcollect);
		return prolist;
	}

    //递归生成历经的流程PNID链表
	private void createlist(LinkedHashSet<Long> prolist, long currentid,DataRowCollections drcollect) {
		if(currentid == 0){
			return;
		}
		for(xsf.data.DataRow dr : drcollect){
			Long id =dr.getLong("ID");
			if(id == currentid){
				prolist.add(dr.getLong("FID"));
				createlist(prolist, dr.getLong("FID"), drcollect);
			}else{
				continue;
			}
		}
		
	}

	// 获取节点列表
    private long getNextNodes() throws Exception {
        String _cmdStr = "";
        // 获取当前节点的后继节点
        if (this.iActType == 2) {
            // 发送远程,取远程节点,不为征询，且不要回执
            _cmdStr = "SELECT A.WF_ID,A.WFNODE_ID,A.WFNODE_NODETYPE,A.PWF_ID,'' AS WFLINE_CAPTION,A.WFNODE_CAPTION,0 AS LineType,'' AS ACONDITION,0 AS ACONDITIONFLAG,A.WFNODE_FORWARD AS ISSMS,A.WFNODE_AFTERSENDSQL";
            _cmdStr += ",A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER ";
            _cmdStr += " FROM WFNODELIST A";
            _cmdStr += " WHERE A.WF_ID=" + this.iWfID + " AND A.WFNODE_NODETYPE=2 AND WFNODE_MEMO NOT LIKE '%征询%' AND WFNODE_ISSELECTED<>1";
        } else if (this.iActType == 3) {
            // 征询，判断WFNODE_MEMO是否有“征询”
            _cmdStr = "SELECT A.WF_ID,A.WFNODE_ID,A.WFNODE_NODETYPE,A.PWF_ID,'' AS WFLINE_CAPTION,A.WFNODE_CAPTION,0 AS LineType,'' AS ACONDITION,0 AS ACONDITIONFLAG,A.WFNODE_FORWARD AS ISSMS,A.WFNODE_AFTERSENDSQL";
            _cmdStr += ",A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER ";
            _cmdStr += " FROM WFNODELIST A";
            _cmdStr += " WHERE A.WF_ID=" + this.iWfID + " AND WFNODE_MEMO LIKE '%征询%'";
            // 如果为征询，出线为与分支
            this.iOutLineFlag = 1;
        } else {
            _cmdStr = "SELECT A.WF_ID,A.WFNODE_ID,A.WFNODE_NODETYPE,A.PWF_ID,B.WFLINE_CAPTION,A.WFNODE_CAPTION,A.WFNODE_MEMO,B.WFLINE_TYPE AS LineType,B.ACONDITION,B.ACONDITIONFLAG,A.WFNODE_FORWARD AS ISSMS,A.WFNODE_AFTERSENDSQL,";
            //查询连线的xml 获取线上条件提示信息 <ConditionMessage> 查询增加字段WFLINE_XML 杨龙修改 开始
            //_cmdStr += "A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER ";
            _cmdStr += "A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER,B.WFLINE_XML ";
          //查询连线的xml 获取线上条件提示信息 <ConditionMessage> 查询增加字段WFLINE_XML 杨龙修改 结束
            _cmdStr += "FROM WFNODELIST A,WFLINELIST B,WFDEFINITION C ";
            _cmdStr += "WHERE A.WF_ID=B.WF_ID AND A.WFNODE_ID=B.WFTAILVERTER ";
            _cmdStr += "AND A.PWF_ID=C.WF_ID ";// AND C.WF_TYPEID = '"+ this.sObjclass + "'";
            _cmdStr += "AND B.WF_ID=" + this.iWfID + " AND B.WFHEADVERTER=" + this.iWfNodeID + " ORDER BY A.WFNODE_NODEORDER ASC";
            // System.out.println("查找后续结点: " + _cmdStr);
        }
        //如果后续节点ID已知，则此处只已知ID的节点信息
        if(!"".equals(nextNodeIDs))
        {
        	 _cmdStr = "SELECT A.WF_ID,A.WFNODE_ID,A.WFNODE_NODETYPE,A.PWF_ID,B.WFLINE_CAPTION,A.WFNODE_CAPTION,A.WFNODE_MEMO,B.WFLINE_TYPE AS LineType,B.ACONDITION,B.ACONDITIONFLAG,A.WFNODE_FORWARD AS ISSMS,A.WFNODE_AFTERSENDSQL,";
             //查询连线的xml 获取线上条件提示信息 <ConditionMessage> 查询增加字段WFLINE_XML 杨龙修改 开始
             //_cmdStr += "A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER ";
             _cmdStr += "A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER,B.WFLINE_XML ";
           //查询连线的xml 获取线上条件提示信息 <ConditionMessage> 查询增加字段WFLINE_XML 杨龙修改 结束
             _cmdStr += "FROM WFNODELIST A,WFLINELIST B,WFDEFINITION C ";
             _cmdStr += "WHERE A.WF_ID=B.WF_ID AND A.WFNODE_ID=B.WFTAILVERTER ";
             _cmdStr += "AND A.PWF_ID=C.WF_ID ";// AND C.WF_TYPEID = '"+ this.sObjclass + "'";
             _cmdStr += "AND B.WF_ID=" + this.iWfID + " AND B.WFHEADVERTER=" + this.iWfNodeID + " AND A.WFNODE_ID in ("+nextNodeIDs+")  ORDER BY A.WFNODE_NODEORDER ASC";
        }
        int Index = 0;
        StringBuffer sNextNodes=new StringBuffer();
        sNextNodes.append("<Nodes OutFlag=\"" + this.iOutLineFlag + "\">");
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        //查询当前节点 抄送标识 杨龙修改 2011/11/3 开始
        String strISCS="0";
		try {
			String strSql = "select t.ISCS from g_pnodes t where t.PID=" + this.iPID+ " and t.ID=" + this.iPnID + " ";
			xsf.data.DataTable dtISCS = DBManager.getDataTable(strSql);
			if (dtISCS.getRows().size() > 0) {
				if(!"".equals(dtISCS.getRows().get(0).getString("ISCS"))&&null!=dtISCS.getRows().get(0).getString("ISCS"))
				{
					strISCS = dtISCS.getRows().get(0).getString("ISCS");
					//修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
					//修改当前节点抄送标识
					this.isCS=Integer.parseInt(strISCS);
				}
			}
		} catch (Exception ex) {
		}
      //查询当前节点 抄送标识 杨龙修改 2011/11/3 结束
        for (xsf.data.DataRow dr : dt.getRows()) {
            // 节点类型（0：结束节点 1：开始节点 2：远程 3：一般节点 4：汇总节点）
            long PWfId = 0;
            if (dr.getString("PWF_ID") != null && !"".equals(dr.getString("PWF_ID"))) {
                PWfId = dr.getLong("PWF_ID");
            }
            String sSelected = dr.getString("WFNODE_ISSELECTED") != null ? dr.getString("WFNODE_ISSELECTED") : "";
            String sMultiUser = dr.getString("WFNODE_ISMULTIUSER") != null ? dr.getString("WFNODE_ISMULTIUSER") : "";
            String sLineCaption = dr.getString("WFLINE_CAPTION") != null ? dr.getString("WFLINE_CAPTION") : "";
            String sNodeCaption = dr.getString("WFNODE_CAPTION") != null ? dr.getString("WFNODE_CAPTION") : "";
            String sNodeMemo = dr.getString("WFNODE_MEMO") != null ? dr.getString("WFNODE_MEMO") : "";
            String sWfNodeId = dr.getString("WFNODE_ID") != null ? dr.getString("WFNODE_ID") : "";
            String sWait = dr.getString("WFNODE_WAIT") != null ? dr.getString("WFNODE_WAIT") : "";
            String sTimeType = dr.getString("WFNODE_TIMETYPE") != null ? dr.getString("WFNODE_TIMETYPE") : "";
            String sTimeSpan = dr.getString("WFNODE_TIMESPAN") != null ? dr.getString("WFNODE_TIMESPAN") : "";
            String sSendMethod = dr.getString("WFNODE_SENDMETHOD") != null ? dr.getString("WFNODE_SENDMETHOD") : "";
            String sNodeType = dr.getString("WFNODE_NODETYPE") != null ? dr.getString("WFNODE_NODETYPE") : "";
            String sAcondition = dr.getString("ACONDITION") != null ? dr.getString("ACONDITION").trim() : "";
            String sIsOnlyOneUser = dr.getString("WFNODE_ISONLYONEUSER") != null ? dr.getString("WFNODE_ISONLYONEUSER").trim() : "";
            String sLineType = dr.getString("LINETYPE") != null ? dr.getString("LINETYPE").trim() : "";
            String sIsSMS = dr.getString("ISSMS") != null ? dr.getString("ISSMS").trim() : "";
            String sAcl = "";
            String sSubFlowID = "";
            String sAconditionFlag = dr.getString("ACONDITIONFLAG") != null ? dr.getString("ACONDITIONFLAG").trim() : "";
            String sAfterSendSql = dr.getString("WFNODE_AFTERSENDSQL") != null ? dr.getString("WFNODE_AFTERSENDSQL").trim() : "";
            if (this.isSendSMS == false) {
                this.isSendSMS = ("1".equals(dr.getString("ISSMS") != null ? dr.getString("ISSMS").trim() : ""));
                setRemind(dr.getString("WF_ID"),dr.getString("WFNODE_ID"));
            }
            //如果是抢办节点且线的类型为汇总线，则将节点类型改为汇总节点 杨龙修改：汇总抢办 2012/8/27 开始
            if("8".equals(sNodeType)&&"2".equals(sLineType))
            {
            	sNodeType="4";
            }
            //如果是抢办节点且线的类型为汇总线，则将节点类型改为汇总节点 杨龙修改：汇总抢办 2012/8/27 结束
            // 取WFNODE_XML
            // 吴红亮 更新 开始
            // String wf_id = dr.getString("WF_ID") != null ? dr.getString("WF_ID") : "";
            // String wfnode_id = dr.getString("WFNODE_ID") != null ? dr.getString("WFNODE_ID") : "";
            // String sxml = getWFNodeXML(wf_id, wfnode_id);
            String sxml = dr.getString("WFNODE_XML") != null ? dr.getString("WFNODE_XML") : "";
            String IsAutoExpand = "";
            // 吴红亮 更新 结束
            // ----------------------------------------------------处理 WFNODE_XML
            String sFileTranServiceXML = "";
            if (sxml.indexOf("FileTranServiceXML") != -1) {
                sFileTranServiceXML = sxml.substring(sxml.indexOf("FileTranServiceXML") + 20);
                sFileTranServiceXML = sFileTranServiceXML.substring(0, sFileTranServiceXML.indexOf("\""));
            }
            Document NDXml = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>" + sxml + "</Root>");
            // 判断汇总节点的文完成数
            int iActiveCount = 0;
            if ("4".equals(sNodeType) && "2".equals(sLineType)) {// 下一结点为“汇总节点” 并且 到下一结点的连线为“汇总线”
                iActiveCount = getActiveCount(sWfNodeId, NDXml.selectSingleNode("Root/ADSet/HZNode"));
            }
            // 获取节点展开类型
            try {
                IsAutoExpand = NDXml.selectSingleNode("Root/ACL/IsAutoExpand").getText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String desc = "";
            if(NDXml.selectSingleNode("Root/BaseInfo/Desc")!=null){
            	desc = NDXml.selectSingleNode("Root/BaseInfo/Desc").getText();
            }
           
            String gatherNode = "0";
            if(desc.contains("汇合人员")){
            	gatherNode = "1";
            }else if(desc.contains("汇合部门人员")){
            	gatherNode = "2";
            }
            
            sAcl = Dom4jTools.getSingleNodeInnerXml(NDXml, "Root/ACL");
            if ("5".equals(sNodeType)) {
                try {
                    sSubFlowID = Dom4jTools.getSingleNodeInnerXml(NDXml, "Root/BaseInfo/SubFlowID");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception("子流程设置错误！");
                }
            }
            // try {
            // if ("1".equals(NDXml.selectSingleNode("Root/BaseInfo/IsEMail").getText())) {
            // this.isSendEmail = true;
            // }
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            // try {
            // if ("1".equals(NDXml.selectSingleNode("Root/BaseInfo/IsTray").getText())) {
            // this.isSendTray = true;
            // }
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            //查询连线的xml 获取线上条件提示信息 <ConditionMessage> 2012/9/18 杨龙修改 开始
			String conditionMessage = "";
			String lineAfterSql = "";
			try {
				String lineXML = dr.getString("WFLINE_XML") != null ? dr
						.getString("WFLINE_XML") : "";
				if (!"".equals(lineXML)) {
					Document lineXMLDom = DocumentHelper
							.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>"
									+ lineXML + "</Root>");
					conditionMessage = lineXMLDom.selectSingleNode(
							"Root/ConditionMessage").getText();
					// 查询连线的xml 获取线上条件提示信息 <ConditionMessage> 2012/10/16 杨龙修改 开始
					lineAfterSql = lineXMLDom.selectSingleNode("Root/AfterSql")
							.getText();
					// 查询连线的xml 获取线上条件提示信息 <ConditionMessage> 2012/10/16 杨龙修改 结束
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
            //查询连线的xml 获取线上条件提示信息 <ConditionMessage> 2012/9/18 杨龙修改 结束
            // ----------------------------------------------------
            // 判断连线的有效性
            //重载isAconditionOK线上条件带提示 杨龙修改 2012/9/11 开始
            //if (isAconditionOK(sAcondition, sAconditionFlag)) {
            if (isAconditionOK(sAcondition, sAconditionFlag,conditionMessage)) {	
          //重载isAconditionOK线上条件带提示 杨龙修改 2012/9/11 结束
            	
            	sNextNodes.append( "<Node Enabled=\"0\" ");
                sNextNodes.append( "PWfID=\"" + PWfId + "\" ");
                sNextNodes.append( "ID=\"" + Index + "\" ");
                sNextNodes.append( "NodeType=\"" + sNodeType + "\" ");
                sNextNodes.append( "SubFlowID=\"" + sSubFlowID + "\" ");
                sNextNodes.append( "ActiveCount=\"" + iActiveCount + "\" ");
                sNextNodes.append( "WfId=\"" + this.iWfID + "\" ");
                sNextNodes.append( "Selected=\"" + sSelected + "\" ");
                sNextNodes.append( "MultiUser=\"" + sMultiUser + "\" ");
                sNextNodes.append( "LineCaption=\"" + sLineCaption + "\" ");
                sNextNodes.append( "NodeCaption=\"" + sNodeCaption + "\" ");
                sNextNodes.append( "NodeMemo=\"" + sNodeMemo + "\" ");
                sNextNodes.append( "NodeID=\"" + sWfNodeId + "\" ");
                sNextNodes.append( "Wait=\"" + sWait + "\" ");
                sNextNodes.append( "TimeType=\"" + sTimeType + "\" ");
                sNextNodes.append( "TimeSpan=\"" + sTimeSpan + "\" ");
                sNextNodes.append( "SendMethod=\"" + sSendMethod + "\" ");
                sNextNodes.append( "NodeDate=\"\" ");
                sNextNodes.append( "FileTranServiceXML=\"" + sFileTranServiceXML + "\" ");
                sNextNodes.append( "IsOnlyOneUser=\"" + sIsOnlyOneUser + "\" ");
                sNextNodes.append( "LineType=\"" + sLineType + "\" ");
                sNextNodes.append( "IsZNG=\"" + getIsZNG(sWfNodeId) + "\" ");
                sNextNodes.append( "IsSMS=\"" + sIsSMS + "\" ");
                sNextNodes.append( "IsHaveUList=\"0\" ");
                sNextNodes.append( "GatherNode=\""+gatherNode+"\" ");
                sNextNodes.append( "AfterSendSql=\"" + sAfterSendSql + "\" ");
                sNextNodes.append( "AfterSql=\"" + lineAfterSql + "\" ");
                sNextNodes.append( "IsAutoExpand=\"" + IsAutoExpand + "\" ");
                sNextNodes.append( "IsCS=\"" + strISCS + "\" ");//抄送标识 0非抄送 1抄送
                sNextNodes.append( "Acondition=\"" + sAcondition + "\">");
                sNextNodes.append( "<ACL>" + sAcl + "</ACL>");
                sNextNodes.append( "</Node>");
                Index++;
            }
        }
        sNextNodes.append("</Nodes>");
        this.NextNodeInfoXml = DocumentHelper.parseText(sNextNodes.toString());
        // 判断并返回发送类型
        // int nextNodeCount = this.NextNodeInfoXml.selectNodes("Nodes/Node").size();
        // 0:或分支（必须先选一个节点再选人发送）
        
        if (this.iOutLineFlag == 0) {
            int count = 0;
            Node onlyNode = null;
            for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                Node nextNode = (Node) obj;
                String nodeType = nextNode.valueOf("@NodeType");
                String nodeCaption = nextNode.valueOf("@NodeCaption");
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = nextNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
              //抄送节点永远都是可选节点 杨龙修改 2012/10/25 开始
                if(1==this.isFirstCS&&!"9".equals(nodeType))
                {
                	count++;
                    if (count == 1) {
                        onlyNode = nextNode;
                    }
                }
                else if (1==this.isFirstCS&& !"0".equals(nodeType)) {//抄送节点
                    nextNode.selectSingleNode("@Enabled").setText("1");
                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
					if (null == onlyNode) {
						onlyNode = nextNode;
					}
                    continue;
//                if (nodeCaption.indexOf("抄送") > -1 && !"0".equals(nodeType)) {// 普通抄送节点(不包括抄送办结)
//                    nextNode.selectSingleNode("@Enabled").setText("1");
//                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
//                    continue;
                  //抄送节点永远都是可选节点 杨龙修改 2012/10/25 结束   
                } 
                else {
                    count++;
                    if (count == 1) {
                        onlyNode = nextNode;
                    }
                }
            }
            if(0==count&&null!=onlyNode)
            {
            	count++;
            }
          //如果设置选人模式为1，则不选出选节点页面，直接选人 杨龙修改 2012/9/13 开始
        	if(1==dsoap.tools.ConfigurationSettings.SelectUserMode)
        	{
                int count1 = 0;
                Node onlyNode1 = null;
                for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                    Node nextNode = (Node) obj;
                    String nodeCaption = nextNode.valueOf("@NodeCaption");
                    String nodeType = nextNode.valueOf("@NodeType");
                    String activeCount = nextNode.valueOf("@ActiveCount");
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                    //String strISCS = onlyNode1.valueOf("@IsCS");
                    if(!"".equals(strISCS)&&"1".equals(strISCS))
                    {
                    	this.isCS=1;
                    }
                    if ("9".equals(nodeType))
                    {
                    	if(1!=this.isCS)
                    	{
                    		this.isFirstCS=1;
                    	}
                    	this.isCS=1;
                    }
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                  //抄送节点永远都是可选节点 杨龙修改 2012/10/25 开始
                    if (1==this.isCS&& !"0".equals(nodeType)) {//抄送节点
                        nextNode.selectSingleNode("@Enabled").setText("1");
                        getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                        continue;
//                    if (nodeCaption.indexOf("抄送") > -1 && !"0".equals(nodeType)) {// 普通抄送节点(不包括抄送办结)
//                        nextNode.selectSingleNode("@Enabled").setText("1");
//                        getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
//                        continue;
                      //抄送节点永远都是可选节点 杨龙修改 2012/10/25 结束   
                    } else {
                        if ("4".equals(nodeType) && !"0".equals(activeCount)) {
                            nextNode.selectSingleNode("@Enabled").setText("0");
                        } else {
                        	//与分支，不允许出现办结节点 杨龙修改 2012/10/23 
                        	if(!"0".equals(nodeType))
                        	{
                            nextNode.selectSingleNode("@Enabled").setText("1");
                            getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                        	}
                        }
                        count1++;
                        if (count1 == 1) {
                            onlyNode1 = nextNode;
                        }
                    }
                }
               
                // System.out.println(this.NextNodeInfoXml.asXML());
                if (count1 == 1) {// 一个节点
                    String nodeType = onlyNode1.valueOf("@NodeType");
                    String activeCount = onlyNode1.valueOf("@ActiveCount");
                    String LineType = onlyNode1.valueOf("@LineType");
                    String wfNodeId = onlyNode1.valueOf("@NodeID");
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                    //String strISCS = onlyNode1.valueOf("@IsCS");
                    if(!"".equals(strISCS)&&"1".equals(strISCS))
                    {
                    	this.isCS=1;
                    }
                    if ("9".equals(nodeType))
                    {
                    	if(1!=this.isCS)
                    	{
                    		this.isFirstCS=1;
                    	}
                    	this.isCS=1;
                    }
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                    if ("0".equals(nodeType)) {
                        if (this.isSubFlow) {
                            this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                            return 10 + this.ds_ParentFlow.iSendType; // 子流程办结#######################################################################################
                        }
                        return 9; // 办结节点#######################################################################################
                    } else if ("4".equals(nodeType) && "2".equals(LineType)) {
                        if (!"0".equals(activeCount)) {
                            if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                                if (isFirstJoin(wfNodeId)) {
                                    this.joinStatus = 1;
                                    return 1;
                                }
                            }
                            return 4;
                        } else {
                            if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                                if (isFirstJoin(wfNodeId)) {
                                    return 1;
                                } else {
                                    this.joinStatus = 2;
                                    return 4;
                                }
                            } else {
                                return 1;
                            }
                        }
                    }
                }
                return 1; // 选人#######################################################################################
        	}
        	//如果设置选人模式为1，则不选出选节点页面，直接选人 杨龙修改 2012/9/13 结束
            if (count == 1) {
                String nodeType = onlyNode.valueOf("@NodeType");
                String isOnlyOneUser = onlyNode.valueOf("@IsOnlyOneUser");
                String activeCount = onlyNode.valueOf("@ActiveCount");
                String LineType = onlyNode.valueOf("@LineType");
                String wfNodeId = onlyNode.valueOf("@NodeID");
                //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = onlyNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                // 远程自动返回 当节点为远程发送时，IsOnlyOneUser=1表示自动返回给远程发送来的节点
                if ("2".equals(nodeType) && "1".equals(isOnlyOneUser)) {
                    String sExDataID = isRemoteAotoBack();
                    if (!"0".equals(sExDataID)) {
                        remoteAutoBack(sExDataID, onlyNode);
                        return 29;// #######################################################################################
                    }
                }
                onlyNode.selectSingleNode("@Enabled").setText("1");
                getNextNodeUList_ByNode(onlyNode);// 加载用户列表； 用用户列表替换<ACL>标签
                if ("0".equals(nodeType)) {
                    if (this.isSubFlow) {
                        this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                        return 10 + this.ds_ParentFlow.iSendType; // 子流程办结#######################################################################################
                    }
                    return 9; // 办结节点#######################################################################################
                } else if ("4".equals(nodeType) && "2".equals(LineType)) {
                    if (!"0".equals(activeCount)) {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                this.joinStatus = 1;
                                return 1;
                            }
                        }
                        return 4;
                    } else {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                return 1;
                            } else {
                                this.joinStatus = 2;
                                return 4;
                            }
                        } else {
                            return 1;
                        }
                    }
                } else {
                    return 1; // 选人#######################################################################################
                }
            } else if (count > 1) {
                return 0; // 多个节点，选节点#######################################################################################
            } else {
                return -1;// #######################################################################################
                // throw new Exception("没有找到后继节点，请联系管理员");
            }
        }
        // 1：与分支（在选人时随意选节点和人）
        else {
            int count = 0;
            Node onlyNode = null;
            for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                Node nextNode = (Node) obj;
                String nodeCaption = nextNode.valueOf("@NodeCaption");
                String nodeType = nextNode.valueOf("@NodeType");
                String activeCount = nextNode.valueOf("@ActiveCount");
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = nextNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
              //抄送节点永远都是可选节点 杨龙修改 2012/10/25 开始
                //if (1==this.isCS&&!"0".equals(nodeType)) {//抄送节点  杨龙修改 2013-7-29
                if (1==this.isCS&&!"0".equals(nodeType)) {//抄送节点
                    nextNode.selectSingleNode("@Enabled").setText("1");
                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                    if (null == onlyNode) {
						onlyNode = nextNode;
					}
                    continue;
//                if (nodeCaption.indexOf("抄送") > -1 && !"0".equals(nodeType)) {// 普通抄送节点(不包括抄送办结)
//                    nextNode.selectSingleNode("@Enabled").setText("1");
//                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
//                    continue;
                  //抄送节点永远都是可选节点 杨龙修改 2012/10/25 结束   
                } else {
                    if ("4".equals(nodeType) && !"0".equals(activeCount)) {
                        nextNode.selectSingleNode("@Enabled").setText("0");
                    } else {
                        nextNode.selectSingleNode("@Enabled").setText("1");    
                        getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                    }
                    count++;
                    if (count == 1) {
                        onlyNode = nextNode;
                    }
                }
            }
			if (0 == count && null != onlyNode) {
				count++;
			}
            // System.out.println(this.NextNodeInfoXml.asXML());
            if (count == 1) {// 一个节点
                String nodeType = onlyNode.valueOf("@NodeType");
                String activeCount = onlyNode.valueOf("@ActiveCount");
                String LineType = onlyNode.valueOf("@LineType");
                String wfNodeId = onlyNode.valueOf("@NodeID");
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = onlyNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                if ("0".equals(nodeType)) {
                    if (this.isSubFlow) {
                        this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                        return 10 + this.ds_ParentFlow.iSendType; // 子流程办结#######################################################################################
                    }
                    return 9; // 办结节点#######################################################################################
                } else if ("4".equals(nodeType) && "2".equals(LineType)) {
                    if (!"0".equals(activeCount)) {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                this.joinStatus = 1;
                                return 1;
                            }
                        }
                        return 4;
                    } else {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                return 1;
                            } else {
                                this.joinStatus = 2;
                                return 4;
                            }
                        } else {
                            return 1;
                        }
                    }
                }
            }
            return 1; // 选人#######################################################################################
        }
    }

 // 获取回溯节点列表
    private long getBackNodes() throws Exception {
        StringBuilder _cmdStr = new StringBuilder();
        String proids = "";
        if(this.processList == null){
        	return 0;
        }
        int proindex = 0;
        for(Long propnid : this.processList){
        	if(proindex == 0){
        		proids += propnid;
        	}else{
        		proids += ","+propnid;
        	}
        	proindex++;
        }

        _cmdStr.append("SELECT A.WF_ID,A.WFNODE_ID,A.WFNODE_NODETYPE,A.PWF_ID,null as WFLINE_CAPTION,A.WFNODE_CAPTION,A.WFNODE_MEMO,")
        .append(" 0 AS LineType, null as ACONDITION,A.WFNODE_FORWARD AS ISSMS,A.WFNODE_AFTERSENDSQL,A.WFNODE_ISSELECTED,")
        .append(" A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER,null as WFLINE_XML,pnd.RDATE,pnd.PDATE,pnd.UNAME,pnd.ID as PNID ")
        .append(" FROM WFNODELIST A inner join G_PNODES pnd on(pnd.wfnode_id=A.WFNODE_ID and A.Wf_Id=pnd.wf_id) ")
        .append(" WHERE pnd.pid=").append(this.iPID).append(" and pnd.ISRETURN !=1").append(" and pnd.WF_ID = ").append(this.iWfID).append(" AND pnd.id in(").append(proids).append(") ORDER BY pnd.ID desc	,A.WFNODE_NODEORDER ASC");
     
        int Index = 0;
        StringBuffer sNextNodes=new StringBuffer();
        sNextNodes.append("<Nodes OutFlag=\"" + this.iOutLineFlag + "\">");
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr.toString());
        //查询当前节点 抄送标识 杨龙修改 2011/11/3 开始
        String strISCS="0";
		try {
			String strSql = "select t.ISCS from g_pnodes t where t.PID=" + this.iPID+ " and t.ID=" + this.iPnID + " ";
			xsf.data.DataTable dtISCS = DBManager.getDataTable(strSql);
			if (dtISCS.getRows().size() > 0) {
				if(!"".equals(dtISCS.getRows().get(0).getString("ISCS"))&&null!=dtISCS.getRows().get(0).getString("ISCS"))
				{
					strISCS = dtISCS.getRows().get(0).getString("ISCS");
					//修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
					//修改当前节点抄送标识
					this.isCS=Integer.parseInt(strISCS);
				}
			}
		} catch (Exception ex) {
		}
		//HashSet<String> nodeset = new HashSet<String>();
      //查询当前节点 抄送标识 杨龙修改 2011/11/3 结束
        for (xsf.data.DataRow dr : dt.getRows()) {
            // 节点类型（0：结束节点 1：开始节点 2：远程 3：一般节点 4：汇总节点）
            long PWfId = 0;
            if (dr.getString("PWF_ID") != null && !"".equals(dr.getString("PWF_ID"))) {
                PWfId = dr.getLong("PWF_ID");
            }
            String sSelected = dr.getString("WFNODE_ISSELECTED") != null ? dr.getString("WFNODE_ISSELECTED") : "";
            String sMultiUser = dr.getString("WFNODE_ISMULTIUSER") != null ? dr.getString("WFNODE_ISMULTIUSER") : "";
            String sLineCaption = dr.getString("WFLINE_CAPTION") != null ? dr.getString("WFLINE_CAPTION") : "";
            String sNodeCaption = dr.getString("WFNODE_CAPTION") != null ? dr.getString("WFNODE_CAPTION") : "";
            String sNodeMemo = dr.getString("WFNODE_MEMO") != null ? dr.getString("WFNODE_MEMO") : "";
            String sWfNodeId = dr.getString("WFNODE_ID") != null ? dr.getString("WFNODE_ID") : "";
            /*Date rdate = dr.getDate("RDATE");
            SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");*/
            String uname = dr.getString("UNAME")!=null?dr.getString("UNAME"):"";
            sLineCaption = this.isSendBack?sNodeCaption+"("+uname+")":sLineCaption;
            String pnid = dr.getString("PNID")!=null?dr.getString("PNID"):"";
            /*if(nodeset.contains(sWfNodeId)){
            	continue;
            }else{
            	nodeset.add(sWfNodeId);
            }*/
            String sWait = dr.getString("WFNODE_WAIT") != null ? dr.getString("WFNODE_WAIT") : "";
            String sTimeType = dr.getString("WFNODE_TIMETYPE") != null ? dr.getString("WFNODE_TIMETYPE") : "";
            String sTimeSpan = dr.getString("WFNODE_TIMESPAN") != null ? dr.getString("WFNODE_TIMESPAN") : "";
            String sSendMethod = dr.getString("WFNODE_SENDMETHOD") != null ? dr.getString("WFNODE_SENDMETHOD") : "";
            String sNodeType = dr.getString("WFNODE_NODETYPE") != null ? dr.getString("WFNODE_NODETYPE") : "";
            String sAcondition = dr.getString("ACONDITION") != null ? dr.getString("ACONDITION").trim() : "";
            String sIsOnlyOneUser = dr.getString("WFNODE_ISONLYONEUSER") != null ? dr.getString("WFNODE_ISONLYONEUSER").trim() : "";
            String sLineType = dr.getString("LINETYPE") != null ? dr.getString("LINETYPE").trim() : "";
            String sIsSMS = dr.getString("ISSMS") != null ? dr.getString("ISSMS").trim() : "";
            String sAcl = "";
            String sSubFlowID = "";
            String sAconditionFlag = dr.getString("ACONDITIONFLAG") != null ? dr.getString("ACONDITIONFLAG").trim() : "";
            String sAfterSendSql = dr.getString("WFNODE_AFTERSENDSQL") != null ? dr.getString("WFNODE_AFTERSENDSQL").trim() : "";
            if (this.isSendSMS == false) {
                this.isSendSMS = ("1".equals(dr.getString("ISSMS") != null ? dr.getString("ISSMS").trim() : ""));
                setRemind(dr.getString("WF_ID"),dr.getString("WFNODE_ID"));
            }

            String sxml = dr.getString("WFNODE_XML") != null ? dr.getString("WFNODE_XML") : "";
            String IsAutoExpand = "";
            // 吴红亮 更新 结束
            // ----------------------------------------------------处理 WFNODE_XML
            String sFileTranServiceXML = "";
            if (sxml.indexOf("FileTranServiceXML") != -1) {
                sFileTranServiceXML = sxml.substring(sxml.indexOf("FileTranServiceXML") + 20);
                sFileTranServiceXML = sFileTranServiceXML.substring(0, sFileTranServiceXML.indexOf("\""));
            }
            Document NDXml = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>" + sxml + "</Root>");
            // 判断汇总节点的文完成数
            int iActiveCount = 0;
            if ("4".equals(sNodeType) && "2".equals(sLineType)) {// 下一结点为“汇总节点” 并且 到下一结点的连线为“汇总线”
                iActiveCount = getActiveCount(sWfNodeId, NDXml.selectSingleNode("Root/ADSet/HZNode"));
            }
            // 获取节点展开类型
            try {
                IsAutoExpand = NDXml.selectSingleNode("Root/ACL/IsAutoExpand").getText();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sAcl = Dom4jTools.getSingleNodeInnerXml(NDXml, "Root/ACL");
            if ("5".equals(sNodeType)) {
                try {
                    sSubFlowID = Dom4jTools.getSingleNodeInnerXml(NDXml, "Root/BaseInfo/SubFlowID");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception("子流程设置错误！");
                }
            }
			String conditionMessage = "";
			String lineAfterSql = "";
			try {
				String lineXML = dr.getString("WFLINE_XML") != null ? dr
						.getString("WFLINE_XML") : "";
				if (!"".equals(lineXML)) {
					Document lineXMLDom = DocumentHelper
							.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>"
									+ lineXML + "</Root>");
					conditionMessage = lineXMLDom.selectSingleNode(
							"Root/ConditionMessage").getText();
					// 查询连线的xml 获取线上条件提示信息 <ConditionMessage> 2012/10/16 杨龙修改 开始
					lineAfterSql = lineXMLDom.selectSingleNode("Root/AfterSql")
							.getText();
					// 查询连线的xml 获取线上条件提示信息 <ConditionMessage> 2012/10/16 杨龙修改 结束
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
            	
            	sNextNodes.append( "<Node Enabled=\"0\" ");
                sNextNodes.append( "PWfID=\"" + PWfId + "\" ");
                sNextNodes.append( "ID=\"" + Index + "\" ");
                sNextNodes.append( "PNID=\"" + pnid + "\" ");
                sNextNodes.append( "NodeType=\"" + sNodeType + "\" ");
                sNextNodes.append( "SubFlowID=\"" + sSubFlowID + "\" ");
                sNextNodes.append( "ActiveCount=\"" + iActiveCount + "\" ");
                sNextNodes.append( "WfId=\"" + this.iWfID + "\" ");
                sNextNodes.append( "Selected=\"" + sSelected + "\" ");
                sNextNodes.append( "MultiUser=\"" + sMultiUser + "\" ");
                sNextNodes.append( "LineCaption=\"" + sLineCaption + "\" ");
                sNextNodes.append( "NodeCaption=\"" + sNodeCaption + "\" ");
                sNextNodes.append( "NodeMemo=\"" + sNodeMemo + "\" ");
                sNextNodes.append( "NodeID=\"" + sWfNodeId + "\" ");
                sNextNodes.append( "Wait=\"" + sWait + "\" ");
                sNextNodes.append( "TimeType=\"" + sTimeType + "\" ");
                sNextNodes.append( "TimeSpan=\"" + sTimeSpan + "\" ");
                sNextNodes.append( "SendMethod=\"" + sSendMethod + "\" ");
                sNextNodes.append( "NodeDate=\"\" ");
                sNextNodes.append( "FileTranServiceXML=\"" + sFileTranServiceXML + "\" ");
                sNextNodes.append( "IsOnlyOneUser=\"" + sIsOnlyOneUser + "\" ");
                sNextNodes.append( "LineType=\"" + sLineType + "\" ");
                sNextNodes.append( "IsZNG=\"" + getIsZNG(sWfNodeId) + "\" ");
                sNextNodes.append( "IsSMS=\"" + sIsSMS + "\" ");
                sNextNodes.append( "IsHaveUList=\"0\" ");
                sNextNodes.append( "AfterSendSql=\"" + sAfterSendSql + "\" ");
                sNextNodes.append( "AfterSql=\"" + lineAfterSql + "\" ");
                sNextNodes.append( "IsAutoExpand=\"" + IsAutoExpand + "\" ");
                sNextNodes.append( "IsCS=\"" + strISCS + "\" ");//抄送标识 0非抄送 1抄送
                sNextNodes.append( "Acondition=\"" + sAcondition + "\">");
                sNextNodes.append( "<ACL>" + sAcl + "</ACL>");
                sNextNodes.append( "</Node>");
                Index++;
            
        }
        sNextNodes.append("</Nodes>");
        this.NextNodeInfoXml = DocumentHelper.parseText(sNextNodes.toString());
        // 判断并返回发送类型
        // int nextNodeCount = this.NextNodeInfoXml.selectNodes("Nodes/Node").size();
        // 0:或分支（必须先选一个节点再选人发送）
        
        if (this.iOutLineFlag == 0) {
            int count = 0;
            Node onlyNode = null;
            for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                Node nextNode = (Node) obj;
                String nodeType = nextNode.valueOf("@NodeType");
                String nodeCaption = nextNode.valueOf("@NodeCaption");
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = nextNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
              //抄送节点永远都是可选节点 杨龙修改 2012/10/25 开始
                if(1==this.isFirstCS&&!"9".equals(nodeType))
                {
                	count++;
                    if (count == 1) {
                        onlyNode = nextNode;
                    }
                }
                else if (1==this.isFirstCS&& !"0".equals(nodeType)) {//抄送节点
                    nextNode.selectSingleNode("@Enabled").setText("1");
                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
					if (null == onlyNode) {
						onlyNode = nextNode;
					}
                    continue;
//                if (nodeCaption.indexOf("抄送") > -1 && !"0".equals(nodeType)) {// 普通抄送节点(不包括抄送办结)
//                    nextNode.selectSingleNode("@Enabled").setText("1");
//                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
//                    continue;
                  //抄送节点永远都是可选节点 杨龙修改 2012/10/25 结束   
                } 
                else {
                    count++;
                    if (count == 1) {
                        onlyNode = nextNode;
                    }
                }
            }
            if(0==count&&null!=onlyNode)
            {
            	count++;
            }
          //如果设置选人模式为1，则不选出选节点页面，直接选人 杨龙修改 2012/9/13 开始
        	if(1==dsoap.tools.ConfigurationSettings.SelectUserMode)
        	{
                int count1 = 0;
                Node onlyNode1 = null;
                for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                    Node nextNode = (Node) obj;
                    String nodeCaption = nextNode.valueOf("@NodeCaption");
                    String nodeType = nextNode.valueOf("@NodeType");
                    String activeCount = nextNode.valueOf("@ActiveCount");
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                    //String strISCS = onlyNode1.valueOf("@IsCS");
                    if(!"".equals(strISCS)&&"1".equals(strISCS))
                    {
                    	this.isCS=1;
                    }
                    if ("9".equals(nodeType))
                    {
                    	if(1!=this.isCS)
                    	{
                    		this.isFirstCS=1;
                    	}
                    	this.isCS=1;
                    }
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                  //抄送节点永远都是可选节点 杨龙修改 2012/10/25 开始
                    if (1==this.isCS&& !"0".equals(nodeType)) {//抄送节点
                        nextNode.selectSingleNode("@Enabled").setText("1");
                        getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                        continue;
//                    if (nodeCaption.indexOf("抄送") > -1 && !"0".equals(nodeType)) {// 普通抄送节点(不包括抄送办结)
//                        nextNode.selectSingleNode("@Enabled").setText("1");
//                        getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
//                        continue;
                      //抄送节点永远都是可选节点 杨龙修改 2012/10/25 结束   
                    } else {
                        if ("4".equals(nodeType) && !"0".equals(activeCount)) {
                            nextNode.selectSingleNode("@Enabled").setText("0");
                        } else {
                        	//与分支，不允许出现办结节点 杨龙修改 2012/10/23 
                        	if(!"0".equals(nodeType))
                        	{
                            nextNode.selectSingleNode("@Enabled").setText("1");
                            getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                        	}
                        }
                        count1++;
                        if (count1 == 1) {
                            onlyNode1 = nextNode;
                        }
                    }
                }
               
                // System.out.println(this.NextNodeInfoXml.asXML());
                if (count1 == 1) {// 一个节点
                    String nodeType = onlyNode1.valueOf("@NodeType");
                    String activeCount = onlyNode1.valueOf("@ActiveCount");
                    String LineType = onlyNode1.valueOf("@LineType");
                    String wfNodeId = onlyNode1.valueOf("@NodeID");
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                    //String strISCS = onlyNode1.valueOf("@IsCS");
                    if(!"".equals(strISCS)&&"1".equals(strISCS))
                    {
                    	this.isCS=1;
                    }
                    if ("9".equals(nodeType))
                    {
                    	if(1!=this.isCS)
                    	{
                    		this.isFirstCS=1;
                    	}
                    	this.isCS=1;
                    }
                  //获取抄送节点标识  杨龙修改 2012/11/3 开始
                    if ("0".equals(nodeType)) {
                        if (this.isSubFlow) {
                            this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                            return 10 + this.ds_ParentFlow.iSendType; // 子流程办结#######################################################################################
                        }
                        return 9; // 办结节点#######################################################################################
                    } else if ("4".equals(nodeType) && "2".equals(LineType)) {
                        if (!"0".equals(activeCount)) {
                            if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                                if (isFirstJoin(wfNodeId)) {
                                    this.joinStatus = 1;
                                    return 1;
                                }
                            }
                            return 4;
                        } else {
                            if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                                if (isFirstJoin(wfNodeId)) {
                                    return 1;
                                } else {
                                    this.joinStatus = 2;
                                    return 4;
                                }
                            } else {
                                return 1;
                            }
                        }
                    }
                }
                return 1; // 选人#######################################################################################
        	}
        	//如果设置选人模式为1，则不选出选节点页面，直接选人 杨龙修改 2012/9/13 结束
            if (count == 1) {
                String nodeType = onlyNode.valueOf("@NodeType");
                String isOnlyOneUser = onlyNode.valueOf("@IsOnlyOneUser");
                String activeCount = onlyNode.valueOf("@ActiveCount");
                String LineType = onlyNode.valueOf("@LineType");
                String wfNodeId = onlyNode.valueOf("@NodeID");
                //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = onlyNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                // 远程自动返回 当节点为远程发送时，IsOnlyOneUser=1表示自动返回给远程发送来的节点
                if ("2".equals(nodeType) && "1".equals(isOnlyOneUser)) {
                    String sExDataID = isRemoteAotoBack();
                    if (!"0".equals(sExDataID)) {
                        remoteAutoBack(sExDataID, onlyNode);
                        return 29;// #######################################################################################
                    }
                }
                onlyNode.selectSingleNode("@Enabled").setText("1");
                getNextNodeUList_ByNode(onlyNode);// 加载用户列表； 用用户列表替换<ACL>标签
                if ("0".equals(nodeType)) {
                    if (this.isSubFlow) {
                        this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                        return 10 + this.ds_ParentFlow.iSendType; // 子流程办结#######################################################################################
                    }
                    return 9; // 办结节点#######################################################################################
                } else if ("4".equals(nodeType) && "2".equals(LineType)) {
                    if (!"0".equals(activeCount)) {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                this.joinStatus = 1;
                                return 1;
                            }
                        }
                        return 4;
                    } else {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                return 1;
                            } else {
                                this.joinStatus = 2;
                                return 4;
                            }
                        } else {
                            return 1;
                        }
                    }
                } else {
                    return 1; // 选人#######################################################################################
                }
            } else if (count > 1) {
                return 0; // 多个节点，选节点#######################################################################################
            } else {
                return -1;// #######################################################################################
                // throw new Exception("没有找到后继节点，请联系管理员");
            }
        }
        // 1：与分支（在选人时随意选节点和人）
        else {
            int count = 0;
            Node onlyNode = null;
            for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                Node nextNode = (Node) obj;
                String nodeCaption = nextNode.valueOf("@NodeCaption");
                String nodeType = nextNode.valueOf("@NodeType");
                String activeCount = nextNode.valueOf("@ActiveCount");
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = nextNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
              //抄送节点永远都是可选节点 杨龙修改 2012/10/25 开始
                //if (1==this.isCS&&!"0".equals(nodeType)) {//抄送节点  杨龙修改 2013-7-29
                if (1==this.isCS&&!"0".equals(nodeType)) {//抄送节点
                    nextNode.selectSingleNode("@Enabled").setText("1");
                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                    if (null == onlyNode) {
						onlyNode = nextNode;
					}
                    continue;
//                if (nodeCaption.indexOf("抄送") > -1 && !"0".equals(nodeType)) {// 普通抄送节点(不包括抄送办结)
//                    nextNode.selectSingleNode("@Enabled").setText("1");
//                    getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
//                    continue;
                  //抄送节点永远都是可选节点 杨龙修改 2012/10/25 结束   
                } else {
                    if ("4".equals(nodeType) && !"0".equals(activeCount)) {
                        nextNode.selectSingleNode("@Enabled").setText("0");
                    } else {
                        nextNode.selectSingleNode("@Enabled").setText("1");    
                        getNextNodeUList_ByNode(nextNode);// 加载用户列表； 用用户列表替换<ACL>标签
                    }
                    count++;
                    if (count == 1) {
                        onlyNode = nextNode;
                    }
                }
            }
			if (0 == count && null != onlyNode) {
				count++;
			}
            // System.out.println(this.NextNodeInfoXml.asXML());
            if (count == 1) {// 一个节点
                String nodeType = onlyNode.valueOf("@NodeType");
                String activeCount = onlyNode.valueOf("@ActiveCount");
                String LineType = onlyNode.valueOf("@LineType");
                String wfNodeId = onlyNode.valueOf("@NodeID");
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                //String strISCS = onlyNode.valueOf("@IsCS");
                if(!"".equals(strISCS)&&"1".equals(strISCS))
                {
                	this.isCS=1;
                }
                if ("9".equals(nodeType))
                {
                	if(1!=this.isCS)
                	{
                		this.isFirstCS=1;
                	}
                	this.isCS=1;
                }
              //获取抄送节点标识  杨龙修改 2012/11/3 开始
                if ("0".equals(nodeType)) {
                    if (this.isSubFlow) {
                        this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                        return 10 + this.ds_ParentFlow.iSendType; // 子流程办结#######################################################################################
                    }
                    return 9; // 办结节点#######################################################################################
                } else if ("4".equals(nodeType) && "2".equals(LineType)) {
                    if (!"0".equals(activeCount)) {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                this.joinStatus = 1;
                                return 1;
                            }
                        }
                        return 4;
                    } else {
                        if (dsoap.tools.ConfigurationSettings.JoinOrder == 1) {
                            if (isFirstJoin(wfNodeId)) {
                                return 1;
                            } else {
                                this.joinStatus = 2;
                                return 4;
                            }
                        } else {
                            return 1;
                        }
                    }
                }
            }
            return 1; // 选人#######################################################################################
        }
    }

    // 查找汇总节点g_pnodes记录条数
    private int getActiveCount(String wfnodeId, Node HZNode) throws Exception {
        int iCount = 0;
        // System.out.println("-----" + HZNode.asXML());
        // Iterator<Element> it = HZNode.getDocument().getRootElement().elementIterator();
        // while (it.hasNext()) {
        // Element e = it.next();
        // String name = e.getName();
        // String value = e.getText();
        // System.out.println("节点:" + name + "值:" + value);
        // }
        // if (!HZNode.getDocument().getRootElement().elementIterator().hasNext()) {
        if (!HZNode.hasContent()) {
            this.iErrorCode = 26;
            throw new Exception("流程发送出错，请联系管理员！(DS_ERRORCODE：" + this.iErrorCode + ",请检查起始汇总节点设置！)");
        }
        String _cmdStr = "";
        // --------------------------------------------------------查询G_PNODES构建 算法所需的 数据结构（所有节点的数组）
        // 吴红亮 修改 开始
        // _cmdStr = "select TOP (100) PERCENT id as PNID,fid as FPNID,wfnode_id from g_pnodes where pid=" + iPID + " order by PNID";
        // String s = "select count(*) as num from (" + _cmdStr + ") AS derivedtbl_1";
      //修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
        if(1==this.isCS)
        {
        	 _cmdStr = "select ID as PNID,FID as FPNID,WFNODE_ID,DEPT_ID from G_PNODES where  PID=" + this.iPID;
        }
        else
        {
        	 _cmdStr = "select ID as PNID,FID as FPNID,WFNODE_ID,DEPT_ID from G_PNODES where (iscs<>1 or ISCS is null) and PID=" + this.iPID;
        }
       // _cmdStr = "select ID as PNID,FID as FPNID,WFNODE_ID,DEPT_ID from G_PNODES where PID=" + this.iPID;
        String s = "select count(*) as num from (" + _cmdStr + ") derivedtbl_1";
        _cmdStr += " order by PNID";
        // 吴红亮 修改 结束
        xsf.data.DataTable dt = DBManager.getDataTable(s);
        if (dt.getRows().size() > 0) {
            this.iG_PNodes_Count = dt.getRows().get(0).getInt("num");
        }
        dt = DBManager.getDataTable(_cmdStr);
        this.iArrPnID = new int[this.iG_PNodes_Count];
        this.iArrFPnID = new int[this.iG_PNodes_Count];
        this.iArrWFNodeID = new int[this.iG_PNodes_Count];
        int i = 0;
        for (xsf.data.DataRow dr : dt.getRows()) {
            this.iArrPnID[i] = dr.getInt("PNID");
            this.iArrFPnID[i] = dr.getInt("FPNID");
            this.iArrWFNodeID[i] = dr.getInt("WFNODE_ID");
            i++;
        }
        
        // --------------------------------------------------------计算“以起始汇总节点为头的子孙节点的pnid列表”
        String sPnidList = "";// 以起始汇总节点为头的子孙节点的pnid列表
        for (Object obj : HZNode.selectNodes("NodeIndex")) {
            Node td = (Node) obj;
            sPnidList += getHZPnIDS(td.getText());
        }
        if ("".equals(sPnidList)) {
            return 0;
        }
        sPnidList = sPnidList.replace("," + this.iPnID, "");
        if (sPnidList.length() < 2) {
            return 0;
        }
        sPnidList = sPnidList.substring(1);
        //如果是抄送节点，将汇总节点中的非抄送节点过滤掉 杨龙修改 2013/4/10 开始
        if(1==this.isCS)
        {
        	_cmdStr = "select ID as PNID from G_PNODES where iscs=1 and id in("+sPnidList+") and  PID=" + this.iPID ;
        	dt = DBManager.getDataTable(_cmdStr);
        	sPnidList="";
        	 for (xsf.data.DataRow dr : dt.getRows()) {
        		 sPnidList += dr.getString("PNID")+",";
             }
        	 if(!"".equals(sPnidList))
        	 {
        		 sPnidList = sPnidList.substring(0,sPnidList.length()-1);
        	 }
        }
        //如果是抄送节点，将汇总节点中的非抄送节点过滤掉 杨龙修改 2013/4/10 结束
        this.joinChilds.put(wfnodeId, sPnidList);
        // --------------------------------------------------------查询“以起始汇总节点为头的子孙节点”中未办理文件（关联g_inbox即可）
        // _cmdStr = "select count(*) ACTIVECOUNT from g_inbox where (pid=" + this.iPID + " and pnid in (" + sPnidList + ")) OR ((PID,PNID) IN (SELECT PID,PNID FROM G_PNODES WHERE PARENTFLOWPID=" + this.iPID + " AND PARENTFLOWPNID IN (" + sPnidList + ")))";
        // 吴红亮 更改 开始
        // _cmdStr = "SELECT COUNT(A.ID) ACTIVECOUNT FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID =" + this.iPID + " AND B.ID IN (" + sPnidList + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + sPnidList + ")))";
      //修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
        if(this.isCS==1)
        {
        	_cmdStr = "SELECT COUNT(A.ID) ACTIVECOUNT FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID=" + this.iPID + " AND B.ID IN (" + sPnidList + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + sPnidList + "))) AND A.STATUS<>3";
        }
        else
        {
        	_cmdStr = "SELECT COUNT(A.ID) ACTIVECOUNT FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID=" + this.iPID + " AND B.ID IN (" + sPnidList + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + sPnidList + "))) AND B.ISCS<>1 AND A.STATUS<>3";
        }
       // _cmdStr = "SELECT COUNT(A.ID) ACTIVECOUNT FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID=" + this.iPID + " AND B.ID IN (" + sPnidList + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + sPnidList + "))) AND A.ACTNAME NOT LIKE '%抄送%' AND A.STATUS<>3";
        // System.out.println(_cmdStr);
        // 吴红亮 更改 结束
        dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            iCount = dt.getRows().get(0).getInt("ACTIVECOUNT");
        }
        //----------------------------------------------抢办汇总修改：最后一个人结束汇总修改为最后一个部门结束汇总 杨龙修改:抢办 2012/8/2
        StringBuffer strSql=new StringBuffer();
        //查询当前节点的节点类型
        String nodeType="";
        strSql.append("select t.wfnode_nodetype From wfnodelist t where wf_id="+this.iWfID+" and wfnode_id="+this.iWfNodeID+"");
        xsf.data.DataTable dtNodeType = DBManager.getDataTable(strSql.toString());
        if (dtNodeType.getRows().size() > 0) {
        	nodeType = dtNodeType.getRows().get(0).getString("wfnode_nodetype");
        }
        //如果是抢办节点，则进入下面处理逻辑
        if("8".equals(nodeType))
        {
        	strSql.setLength(0);
        	//查询出与当前处理人g_pnodes相同FID和部门的g_pnodes的ID
        	String id="";
        	strSql.append(" select id From g_pnodes t");
        	strSql.append(" where id <> "+this.iPnID+"");
        	strSql.append(" and t.FID");
        	strSql.append(" in (select fid from g_pnodes where id = "+this.iPnID+" and pid = "+this.iPID+")");
        	/*
        	//修改抢办逻辑，多个部门一起抢办，不在做每个部门独立抢办
        	strSql.append(" and t.DEPT_ID");
        	strSql.append(" in (select dept_id from g_pnodes where id = "+this.iPnID+" and pid = "+this.iPID+")");
        	*/
        	strSql.append(" and t.pid = "+this.iPID+"");
        	//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>查出当前处理人同部门的g_pnodes的ID SQL: "+strSql.toString());
        	dt = DBManager.getDataTable(strSql.toString());
            if (dt.getRows().size() > 0) {
            	for (xsf.data.DataRow dr : dt.getRows()) {
                    id+=dr.getInt("id")+",";
                }
            	if(id!="")
            	{
            		id=id.substring(0,id.length()-1);
            	}
            }
            //按照Fid分组统计出 相同的一个部门算一个可处理的人，且过滤掉与当前处理人相同部门和FID的人
        	strSql.setLength(0);
        	strSql.append(" SELECT sum(t.DEPTNUM) as ACTIVECOUNT");
        	strSql.append(" FROM (SELECT count(distinct(B.DEPT_ID)) as DEPTNUM");
        	strSql.append(" FROM G_PNODES B, G_INBOX A");
        	strSql.append(" WHERE A.PID = B.PID");
        	strSql.append(" AND A.PNID = B.ID");
        	if(!"".equals(id))
        	{
        		strSql.append(" AND B.ID NOT IN ("+id+")");	
        	}
        	strSql.append(" AND ((B.PID = " + this.iPID + " AND B.ID IN (" + sPnidList + ")) OR");
        	strSql.append(" (B.PARENTFLOWPID = " + this.iPID + " AND");
        	strSql.append(" B.PARENTFLOWPNID IN (" + sPnidList + ")))");
        	//修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
        	if(this.isCS!=1)
        	{
        		strSql.append(" AND B.ISCS<>1 ");
        	}
        	//strSql.append(" AND A.ACTNAME NOT LIKE '%抄送%'");
        	strSql.append(" AND A.STATUS <> 3");
        	strSql.append(" GROUP BY B.Fid) t");
        	//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>统计剩余可汇总的部门 过滤掉处理人同部门的人 SQL: "+strSql.toString());
        	dt = DBManager.getDataTable(strSql.toString());
            if (dt.getRows().size() > 0) {
                iCount = dt.getRows().get(0).getInt("ACTIVECOUNT");
            }
        }
        
        //----------------------------------------------杨龙修改:抢办 2012/8/2 结束
        return iCount;
    }

 // 查找汇总节点_WFNODE_ID相关的祖先pnid的后续子,孙pnid
    private String getHZPnIDS(String _WFNODE_ID) {
        int iHeadPnid = -1;
        long curFid = this.iPnID;
        boolean isHZSTOP=false;
        for (int i = this.iG_PNodes_Count - 1; i >= 0; i--) {
            if (curFid == this.iArrPnID[i]) {
            	//查找汇总起始节点中，包含HZSTOP=1则，停止查询汇总起始节点，杨龙修改 2013/9/21 开始
            	try{
                String strSql = "select HZSTOP  from G_PNODES where PID="+this.iPID+" AND ID="+curFid+"";
                xsf.data.DataTable dt = DBManager.getDataTable(strSql);
                if (dt.getRows().size() > 0) {
                    String hzstop = dt.getRows().get(0).getString("HZSTOP");
                    if(hzstop!=null&&"1".equals(hzstop))
                    {
                    	isHZSTOP=true;
                    }
                }
            	}
            	catch (Exception e) {
					e.printStackTrace();
				}
                //查找汇总起始节点中，包含HZSTOP=1则，停止查询汇总起始节点，杨龙修改 2013/9/21 结束
                if (_WFNODE_ID.equals(String.valueOf(this.iArrWFNodeID[i]))) {
                    iHeadPnid = this.iArrPnID[i];
                    break;
                } else {
                	if(isHZSTOP)
                	{
                		break;
                	}
                    curFid = this.iArrFPnID[i];// 向上找头
                }
            }
        }
        // if (iHeadPnid == -1) {
        // throw(new System.Exception("查找汇总头节点出错！！"));
        // }
        return getNextPnid(iHeadPnid, 0);
    }
    // 查找某pnid的后续子,孙pnid
    private String getNextPnid(int iHeadPnid, int i) {
        String retStr = "";
        for (; i < this.iG_PNodes_Count; i++) {
            if (this.iArrFPnID[i] == iHeadPnid) {
                retStr += "," + this.iArrPnID[i];// 向下找子
                retStr += getNextPnid(this.iArrPnID[i], i);// 向下找孙
            }
        }
        return retStr;
    }
    
//判断线上条件增加 提示 重载isAconditionOK(String sAcondition, String sAconditionFlag)方法 杨龙修改 2012/9/11 开始
 // 判断节点连线条件是否有效(完成有效后续节点判断)
    private boolean isAconditionOK(String sAcondition, String sAconditionFlag,String wfTip) throws Exception {
        // System.out.println("判断连线的有效性0: " + sAcondition);
        sAcondition = sAcondition.replace("大于", ">");
        sAcondition = sAcondition.replace("小于", "<");
        sAcondition = sAcondition.replace("加", "+");
        boolean returnValue = false;
        if ("".equals(sAcondition.trim())) {
            return true;
        }
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 5;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = String.valueOf(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = String.valueOf(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = String.valueOf(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = String.valueOf(this.iPnID);
        String sSql = sAcondition;
        for (int i = 0; i < iKeyCount; i++) {
            sSql = sSql.replace(sKey[i], sValue[i]);
        }
        xsf.data.DataTable dt = DBManager.getDataTable(sSql);
        if (dt == null) {
            throw new Exception("条件配置有误！");
        }
        // System.out.println("判断连线的有效性2: " + sSql);
        if (dt.getRows().size() > 0) {
            if (dt.getRows().get(0).getInt(0) > 0) {
                returnValue = true;
            } else {
            	//线上条件执行成功 写入提示信息 杨龙修改 2012/9/11 开始
            	this.wfTip+=wfTip+",";
            	//线上条件执行成功 写入提示信息 杨龙修改 2012/9/11 结束
                returnValue = false;
            }
        }
        if ("1".equals(sAconditionFlag)) {
            returnValue = !returnValue;
        }
        return returnValue;
    }
  //判断线上条件增加 提示 重载isAconditionOK(String sAcondition, String sAconditionFlag)方法 杨龙修改 2012/9/11 开始
    
    // 判断节点连线条件是否有效(完成有效后续节点判断)
    private boolean isAconditionOK(String sAcondition, String sAconditionFlag) throws Exception {
        // System.out.println("判断连线的有效性0: " + sAcondition);
        sAcondition = sAcondition.replace("大于", ">");
        sAcondition = sAcondition.replace("小于", "<");
        sAcondition = sAcondition.replace("加", "+");
        boolean returnValue = false;
        if ("".equals(sAcondition.trim())) {
            return true;
        }
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 5;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = String.valueOf(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = String.valueOf(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = String.valueOf(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = String.valueOf(this.iPnID);
        String sSql = sAcondition;
        for (int i = 0; i < iKeyCount; i++) {
            sSql = sSql.replace(sKey[i], sValue[i]);
        }
        
        //20150827修改 支持多条SQL语句取布尔值的与(&&)
        String[] sqllist = sSql.split(";");
        returnValue = true; //这里置为TRUE先，如果有一句FALSE了 那么&&的结果必然是FALSE，即有一条SQL不通过整体就不通过
        for(String sql:sqllist){
        	xsf.data.DataTable dt = DBManager.getDataTable(sql);
            if (dt == null) {
                throw new Exception("条件配置有误！");
            }
            // System.out.println("判断连线的有效性2: " + sSql);
            if (dt.getRows().size() > 0) {
                if (dt.getRows().get(0).getInt(0) > 0) {
                	System.out.println("线上SQL语句通过");
                } else {
                	//线上条件执行成功 写入提示信息 杨龙修改 2012/9/11 开始
                	this.wfTip+=wfTip+",";
                	//线上条件执行成功 写入提示信息 杨龙修改 2012/9/11 结束
                	returnValue = false;
                	break;
                }
            }
            
        }
        if ("1".equals(sAconditionFlag)) {
            returnValue = !returnValue;
        }
        return returnValue;
    }

 // 可多人办理时，出小组(私有小组，统计局用)
    private String getPrivateGroup() throws Exception {
        String sAcl = "";
        try {
            String sGrpName = "";
            sAcl += "<Node Id=\"0\" UType=\"2\" UName=\"我的小组\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">";
            // 吴红亮 添加 开始
            List<String> myUserGroups = new ArrayList<String>();
            // 吴红亮 添加 结束
            for (DataRow dr : map.get("G_GRPS_PRI").rows) {
                String grp_name = dr.get("grp_name");
                if (grp_name.equals(sGrpName)) {
                    continue;
                } else {
                    sGrpName = grp_name;
                    if (dsoap.tools.ConfigurationSettings.isPrivateGroup) {
                        myUserGroups.add(grp_name);
                    } else {
                        sAcl += "<Node Id=\"" + sGrpName + "\" UType=\"5\" UName=\"" + sGrpName + "\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">";
                        sAcl += "</Node>";
                    }
                }
            }
            if (dsoap.tools.ConfigurationSettings.isPrivateGroup) {
                sAcl += getPrivateGroup(myUserGroups);
            }
            sAcl += "</Node>";
        } catch (Exception e) {
            throw e;
        }
        return sAcl;
    }

    // 获取当前节点意见显示模式
    private String getIsHideYJ() throws Exception {
        String _cmdStr = "";
        String ret = "0";
        _cmdStr = "SELECT ISHIDEYJ FROM WFNODEYJ WHERE WF_ID=" + this.iWfID + " AND WFNODE_ID=" + this.iWfNodeID;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            ret = dr.getString("ISHIDEYJ") != null ? dr.getString("ISHIDEYJ") : "";
        }
        return ret;
    }

    // 获取当前是否需要职能能柜
    private String getIsZNG(String sNodeID) throws Exception {
        String ret = "0";
        String _cmdStr = "";
        _cmdStr = "select count(*) as cnt from G_INFOS a,WFNODEYJ b where a.ISZNG=1 and b.ISZNG=1 and a.WF_ID=b.WF_ID and a.ID=" + this.iInfoID + " and b.WFNODE_ID=" + sNodeID;
        if (DBManager.getFieldLongValue(_cmdStr) > 0) {
            ret = "1";
        }
        return ret;
    }

    // 远程
    private String isRemoteAotoBack() throws Exception {
        String _cmdStr = "";
        String returnValue = "0";
        String sLocalNode = ConfigurationSettings.AppSettings("LocalNodeName");
        _cmdStr = "SELECT ID FROM S_EXDATA WHERE NEEDBACK=1 AND TO_INFO_ID=" + this.iInfoID + " and TONODE='" + sLocalNode + "'" + " ORDER BY ID DESC";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            returnValue = dr.getString("ID") != null ? dr.getString("ID") : "";
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

    private String getSenderDeptId(Statement statement, long uid, long pid, long pnid) throws Exception {
        // 本人取G_PNODES.DEPT_ID
        // 是代办人取代办人部门ID
        // 否则取主部门
        String DEPT_ID = null;
        // -----------------------------------------------------------------------------
        String sql = SysDataSource.getSysDataSource().getSelectCommands().get("getSenderDeptID").getCommandText().trim();
        sql = sql.replace("?USER_ID", String.valueOf(uid)).replace("?PID", String.valueOf(pid)).replace("?PNID", String.valueOf(pnid));
        ResultSet _myDs = null;
        try {
            _myDs = statement.executeQuery(sql);
            if (_myDs.next()) {
                long USER_ID = _myDs.getLong("USER_ID");
                if (USER_ID == uid) {
                    DEPT_ID = _myDs.getString("DEPT_ID");
                }
                if (DEPT_ID == null || "".equals(DEPT_ID)) {
                    DEPT_ID = _myDs.getString("ADEPT_ID");
                }
                if (DEPT_ID == null || "".equals(DEPT_ID)) {
                    DEPT_ID = _myDs.getString("MAIN_DEPT_ID");
                }
            } else {
                DEPT_ID = "DEPT_ID";
            }
            return DEPT_ID;
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
        }
    }

    private String getMUserInfo(long pid, long pnid, long rUserId) throws Exception {
        // String _cmdStr = "";
        String MUSER_ID = "USER_ID";
        // _cmdStr = "select B.USER_ID,C.UNAME ";
        // _cmdStr += "from G_PNODES A,G_GRPS B,G_USERS C where A.UTYPE=3 and A.USER_ID=B.GRP_ID and B.USER_ID=C.ID and A.PID=" + pid + " and A.ID=" + pnid + " and B.USER_ID=" + rUserId;
        // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("PID", pid);
        sqlDataSource.setParameter("PNID", pnid);
        sqlDataSource.setParameter("USER_ID", rUserId);
        xsf.data.DataTable dt = sqlDataSource.query("getRoleUser");
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            MUSER_ID = dr.getLong("USER_ID") + ",UTYPE=0,UNAME='" + dr.getString("UNAME") + "'";
        }
        return MUSER_ID;
    }

    // 根据ID取得标题
    private String getBTFromInfoID(String sInfoID) throws Exception {
        String _cmdStr = "";
        String sBT = "";
        _cmdStr = "SELECT BT FROM G_INFOS WHERE ID=" + sInfoID;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            sBT = dr.getString("BT") != null ? dr.getString("BT") : "";
        }
        return sBT;
    }

    // 获取人员列表
    private void getNextNodeUList_ByNode(Node _mynode) throws Exception {
        // System.out.println(_mynode.asXML());
        String sAcl = "";
        try {  	
            if ("1".equals(_mynode.valueOf("@IsHaveUList"))) {
                return;
            }
            Node AclTD = _mynode.selectSingleNode("ACL");
            String sNodeMemo=_mynode.valueOf("@NodeMemo");
          //如果是流程回溯，需要设置回溯到的节点的PNID
            if(this.isSendBack){
            	String pnid = _mynode.valueOf("@PNID");
            	this.sendBackPnid = pnid;
            }
            // 判断是否唯一经办人
            if ((!"2".equals(_mynode.valueOf("@NodeType")) && "1".equals(_mynode.valueOf("@IsOnlyOneUser")) && !"1".equals(_mynode.valueOf("@LineType")))|| this.isSendBack) {
                sAcl = this.getOnlyOneUserID(_mynode.valueOf("@NodeID"), 0);
            }
            if ("".equals(sAcl)) {
                sAcl = getUTree(AclTD);
                //获取流程节点的名称备注,如果备注中包含小组关键字则发送时显示小组
                sNodeMemo=_mynode.valueOf("@NodeMemo");
                if(sNodeMemo.indexOf("小组")>=0)
                {
                	sAcl += getUserGroup();
                }
                // 私有小组，统计局专用
                if ("1".equals(_mynode.valueOf("@MultiUser")) && !"4".equals(_mynode.selectSingleNode("ACL").selectSingleNode("type").getText()) && this.isGroupView) {
                    // 可多人办理时，出小组(私有小组，统计局用)
                    // 吴红亮 修改 开始
                    if (hasDisplayGroupNode()) {
                        sAcl += getPublicUserGroup();
                    }
                    // 吴红亮 修改 结束
                    sAcl += getPrivateGroup();
                }
            }
            ((Element) _mynode).clearContent();// 清除<ACL>子节点
            if (!"".equals(sAcl)) {
                ((Element) _mynode).appendContent((DocumentHelper.parseText("<root>" + sAcl + "</root>").getRootElement()));// 添加<Node>子节点
            }
            ((Attribute) _mynode.selectSingleNode("@IsHaveUList")).setValue("1");
            // ----------------------------------------------吴红亮 2012.06.20流程节点添加自动发送属性
            ((org.dom4j.Element) _mynode).addAttribute("IsAutoSend", AclTD.selectSingleNode("IsAutoSend").getText());
            // ----------------------------------------------
        } catch (Exception e) {
            e.printStackTrace();
            _mynode.setText(sAcl);
            this.iErrorCode = 40;
            throw e;
        }
        // System.out.println("xml:-----" + _mynode.asXML());
    }
    //节点经办人 按照顺序查找上级节点 改为 循环查找经办节点 增加递归查询方法 getOnlyOneUser 杨龙修改 2013-7-16 开始
    // 选取唯一经办人(iType=0:继承本节点经办人)
    private String getOnlyOneUserID(String wfNodeId, int iType) throws Exception {
        String reStr = "";
        // String _cmdStr = "";
        if (iType != 0 && (this.iPID == 0 || this.iPnID == 0)) {
            for (int i = 0; i < this.iUserCount; i++) {
                if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(String.valueOf(this.iUserID))) {
                    reStr = "<Node Id=\"" + this.arrUserList[i][G_USERS.ID.ordinal()] + "\" UType=\"" + this.arrUserList[i][G_USERS.UTYPE.ordinal()] + "\" UName=\"" + this.arrUserList[i][G_USERS.UNAME.ordinal()] + "\" fId=\"" + this.arrUserList[i][G_USERS.DEPT_ID.ordinal()] + "\" fName=\"" + this.arrUserList[i][G_USERS.DETP_NAME.ordinal()] + " \" gId=\"\" phone=\"\"  topname=\"" + this.arrUserList[i][10] + "\"  Blqx=\"\"></Node>";
                    break;
                }
            }
        } else {
            // // _cmdStr = "select A.id,A.fid,A.wfnode_id,A.user_id,A.dept_id,B.UTYPE,B.UNAME as USERNAME,C.UNAME as DEPTNAME from g_pnodes A,G_USERS B,G_USERS C where pid="+iPID.ToString()+" AND A.USER_ID=B.ID AND A.DEPT_ID=C.ID order by id desc";
            // // _cmdStr = "select A.ID,A.FID,A.WFNODE_ID,A.USER_ID,A.DEPT_ID,0 AS UTYPE,A.UNAME AS USERNAME,'' as DEPTNAME from G_PNODES A where PID=" + this.iPID + " order by ID desc";
            // _cmdStr = "select A.ID,A.FID,A.WFNODE_ID,A.USER_ID,A.MUSER_ID,B.UTYPE,B.UNAME as USERNAME,A.UNAME as MUSERNAME,DEPT_ID,MDEPT_ID from G_PNODES A,G_USERS B where A.PID=" + this.iPID + " AND A.USER_ID=B.ID order by ID desc";
            // String curFid = String.valueOf(this.iPnID);
            // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
            String curFid = String.valueOf(this.iPnID);
            IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            sqlDataSource.setParameter("PID", this.iPID);
            xsf.data.DataTable dt = sqlDataSource.query("getOnlyOneUserID");
            reStr=this.getOnlyOneUser(dt, curFid,wfNodeId);
            
        }
        return reStr;
    }
    
    //递归循环经办节点，找到需要继承的节点数据
    private String getOnlyOneUser(xsf.data.DataTable dt, String curFid,String wfNodeId,String Selected) throws Exception {
    	String msg="";
    	//董辰杰增加属性
    	String UpToDept = "";
    	String UpToDeptname = "";
    	for (xsf.data.DataRow dr : dt.getRows()) {
            String ID = dr.getString("ID");
            if (curFid.equals(ID)) {
                String WFNODE_ID = dr.getString("WFNODE_ID");
                String FID = dr.getString("FID");
                if ((this.isSendBack && ID.equals(this.sendBackPnid)) || (!this.isSendBack && wfNodeId.equals(WFNODE_ID))) {
                    String USER_ID = dr.getString("USER_ID");
                    String UTYPE = dr.getString("UTYPE");
                    String USERNAME = dr.getString("USERNAME");
                    String DEPT_ID = dr.getString("DEPT_ID");
                    //董辰杰给属性赋值
                    UpToDept = DEPT_ID;
                    String MUSER_ID = dr.getString("MUSER_ID");
                    String MUSERNAME = dr.getString("MUSERNAME");
                    String MDEPT_ID = dr.getString("MDEPT_ID");
                    // ----------------------------------------------------
                    USER_ID = USER_ID != null ? USER_ID : "";
                    UTYPE = UTYPE != null ? UTYPE : "";
                    USERNAME = USERNAME != null ? USERNAME : "";
                    DEPT_ID = DEPT_ID != null ? DEPT_ID : "";
                    MUSER_ID = MUSER_ID != null ? MUSER_ID : "";
                    MUSERNAME = MUSERNAME != null ? MUSERNAME : "";
                    MDEPT_ID = MDEPT_ID != null ? MDEPT_ID : "";
                    if (("0".equals(UTYPE) || "9".equals(UTYPE)) && !USER_ID.equals(MUSER_ID)) {
                        USER_ID = MUSER_ID;
                        USERNAME = MUSERNAME;
                        DEPT_ID = MDEPT_ID;
                    }
                    if (USER_ID == null || "".equals(USER_ID)) {
                        curFid = FID != null ? FID : "";
                        continue;
                    }
                    // ----------------------------------------------------
                    // 查找当前用户所在部门
                    String sFDeptID = "";
                    String sFDeptName = "";
                    String sTopName = "";
                    for (int i = 0; i < this.iUserCount; i++) {
                        // if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(_userId)) {
                        if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(USER_ID) && this.arrUserList[i][G_USERS.DEPT_ID.ordinal()].equals(DEPT_ID)) {
                            sFDeptID = this.arrUserList[i][G_USERS.DEPT_ID.ordinal()];
                            sFDeptName = this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
                            sTopName = this.arrUserList[i][10];
                            msg = "<Node Id=\"" + USER_ID + "\" UType=\"" + UTYPE + "\" UName=\"" + USERNAME + "\" fId=\"" + sFDeptID + "\" fName=\"" + Selected+sFDeptName + " \" gId=\"\" phone=\"\" topname=\"" + sTopName + "\" Blqx=\"\"></Node>";
                            break;
                        }
                    }
                    //System.out.println("***" + msg);
                    break;
                } else {
                    curFid = FID != null ? FID : "";
                    if(!"0".equals(curFid)&&"".equals(msg))
                	{
                    	msg=this.getOnlyOneUser(dt, curFid, wfNodeId,Selected);
                	}
                    break;
                }
            }
        }
    	//董辰杰修改 如果没有继承到任何人，那么把继承人所在原来部门的人都列举出来
    	if(StringHelper.isNullOrEmpty(msg)){
    		return getUList(UpToDept,UpToDeptname, "", "全部人员", "");
    	}
    	//董辰杰修改结束
    	return msg;
    }
    
    private String getOnlyOneUser(xsf.data.DataTable dt, String curFid,String wfNodeId) throws Exception {
    	String msg="";
    	for (xsf.data.DataRow dr : dt.getRows()) {
            String ID = dr.getString("ID");
            if (curFid.equals(ID)) {
                String WFNODE_ID = dr.getString("WFNODE_ID");
                String FID = dr.getString("FID");
                if ((this.isSendBack && ID.equals(this.sendBackPnid)) || (!this.isSendBack && wfNodeId.equals(WFNODE_ID))) {
                    String USER_ID = dr.getString("USER_ID");
                    String UTYPE = dr.getString("UTYPE");
                    String USERNAME = dr.getString("USERNAME");
                    String DEPT_ID = dr.getString("DEPT_ID");
                    String MUSER_ID = dr.getString("MUSER_ID");
                    String MUSERNAME = dr.getString("MUSERNAME");
                    String MDEPT_ID = dr.getString("MDEPT_ID");
                    // ----------------------------------------------------
                    USER_ID = USER_ID != null ? USER_ID : "";
                    UTYPE = UTYPE != null ? UTYPE : "";
                    USERNAME = USERNAME != null ? USERNAME : "";
                    DEPT_ID = DEPT_ID != null ? DEPT_ID : "";
                    MUSER_ID = MUSER_ID != null ? MUSER_ID : "";
                    MUSERNAME = MUSERNAME != null ? MUSERNAME : "";
                    MDEPT_ID = MDEPT_ID != null ? MDEPT_ID : "";
                    if (("0".equals(UTYPE) || "9".equals(UTYPE)) && !USER_ID.equals(MUSER_ID)) {
                        USER_ID = MUSER_ID;
                        USERNAME = MUSERNAME;
                        DEPT_ID = MDEPT_ID;
                    }
                    if (USER_ID == null || "".equals(USER_ID)) {
                        curFid = FID != null ? FID : "";
                        continue;
                    }
                    // ----------------------------------------------------
                    // 查找当前用户所在部门
                    String sFDeptID = "";
                    String sFDeptName = "";
                    String sTopName = "";
                    for (int i = 0; i < this.iUserCount; i++) {
                        // if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(_userId)) {
                        if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(USER_ID) && this.arrUserList[i][G_USERS.DEPT_ID.ordinal()].equals(DEPT_ID)) {
                            sFDeptID = this.arrUserList[i][G_USERS.DEPT_ID.ordinal()];
                            sFDeptName = this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
                            sTopName = this.arrUserList[i][10];
                            msg = "<Node Id=\"" + USER_ID + "\" UType=\"" + UTYPE + "\" UName=\"" + USERNAME + "\" fId=\"" + sFDeptID + "\" fName=\"" + sFDeptName + " \" gId=\"\" phone=\"\" topname=\"" + sTopName + "\"  Blqx=\"\"></Node>";
                            break;
                        }
                    }
                    //System.out.println("***" + msg);
                    break;
                } else {
                    curFid = FID != null ? FID : "";
                    if(!"0".equals(curFid)&&"".equals(msg))
                	{
                    	msg=this.getOnlyOneUser(dt, curFid, wfNodeId);
                	}
                    break;
                }
            }
        }
    	return msg;
    }
    //节点经办人 按照顺序查找上级节点 改为 循环查找经办节点 增加递归查询方法 getOnlyOneUser 杨龙修改 2013-7-16 结束
    
    //重载getOnlyOneUserID方法 增加默认选中和必选的过滤 杨龙修改 2013-6-18 选取唯一经办人(iType=0:继承本节点经办人)
    private String getOnlyOneUserID(String wfNodeId, int iType,String Selected) throws Exception {
        String reStr = "";
        // String _cmdStr = "";
        if (iType != 0 && (this.iPID == 0 || this.iPnID == 0)) {
            for (int i = 0; i < this.iUserCount; i++) {
                if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(String.valueOf(this.iUserID))) {
                    reStr = "<Node Id=\"" + this.arrUserList[i][G_USERS.ID.ordinal()] + "\" UType=\"" + this.arrUserList[i][G_USERS.UTYPE.ordinal()] + "\" UName=\"" + this.arrUserList[i][G_USERS.UNAME.ordinal()] + "\" fId=\"" + this.arrUserList[i][G_USERS.DEPT_ID.ordinal()] + "\" fName=\"" + Selected+this.arrUserList[i][G_USERS.DETP_NAME.ordinal()] + " \" gId=\"\" phone=\"\"  topname=\"" + this.arrUserList[i][10] + "\"></Node>";
                    break;
                }
            }
        } else {
            // // _cmdStr = "select A.id,A.fid,A.wfnode_id,A.user_id,A.dept_id,B.UTYPE,B.UNAME as USERNAME,C.UNAME as DEPTNAME from g_pnodes A,G_USERS B,G_USERS C where pid="+iPID.ToString()+" AND A.USER_ID=B.ID AND A.DEPT_ID=C.ID order by id desc";
            // // _cmdStr = "select A.ID,A.FID,A.WFNODE_ID,A.USER_ID,A.DEPT_ID,0 AS UTYPE,A.UNAME AS USERNAME,'' as DEPTNAME from G_PNODES A where PID=" + this.iPID + " order by ID desc";
            // _cmdStr = "select A.ID,A.FID,A.WFNODE_ID,A.USER_ID,A.MUSER_ID,B.UTYPE,B.UNAME as USERNAME,A.UNAME as MUSERNAME,DEPT_ID,MDEPT_ID from G_PNODES A,G_USERS B where A.PID=" + this.iPID + " AND A.USER_ID=B.ID order by ID desc";
            // String curFid = String.valueOf(this.iPnID);
            // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
            String curFid = String.valueOf(this.iPnID);
            IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            sqlDataSource.setParameter("PID", this.iPID);
            xsf.data.DataTable dt = sqlDataSource.query("getOnlyOneUserID");
            reStr=this.getOnlyOneUser(dt, curFid,wfNodeId,Selected);
        }
        return reStr;
    }
    
    // 通过用户Id获取部门ID
    private long getDetpIDFromUserID(long userid) throws Exception {
        long DetpID = -1;
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("USER_ID", userid);
        xsf.data.DataTable dt = sqlDataSource.query("getDeptID");
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            String ID = dr.getString("ID");
            if (ID != null && !"".equals(ID)) {
                DetpID = Long.parseLong(ID);
            }
        }
        if (DetpID == -1) {
            throw new Exception("获取用户部门ID出错！");
        }
        return DetpID;
    }

    private String getDraftDept(long infoId) throws Exception {
        String draftDeptId = "";
        String _cmdStr = "";
        _cmdStr = "SELECT MAINDEPT FROM G_INFOS WHERE ID=" + infoId;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            draftDeptId = dr.getString("MAINDEPT");
        }
        if ("".equals(draftDeptId)) {
            throw new Exception("获取用户部门ID出错！");
        }
        return draftDeptId;
    }

    private void remoteAutoBack(String sExID, Node td) throws Exception {
        Date now = new Date();
        if (true) {
            return;
        }
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        List<String> httpTask = new ArrayList<String>();
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myConn.setAutoCommit(false);
            _myRead = _myConn.createStatement();
            String isNeedBack = td.valueOf("@Selected");
            String FileTranServiceXML = td.valueOf("@FileTranServiceXML"); // 处理当前节点
            _cmdStr = "UPDATE G_OPINION SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID + " AND STATUS<>1";
            _myRead.executeUpdate(_cmdStr);
            String DBMS = ConfigurationSettings.AppSettings("DBMS");
            _cmdStr = "UPDATE G_PNODES SET STATUS=-1,";
            _cmdStr += processDate("SDATE=NOW,PDATE=NOW ", DBMS, now);
            _cmdStr += "WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
            _myRead.executeUpdate(_cmdStr);
            _cmdStr = "UPDATE G_PROUTE SET STATUS=-1 WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
            _myRead.executeUpdate(_cmdStr);
            _cmdStr = "DELETE G_INBOX WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
            _myRead.executeUpdate(_cmdStr); // 远程 生成OUTXML
            Document myRemoteXml = DocumentHelper.parseText("<SendInfo><NextNodeInfo></NextNodeInfo><DataInfo></DataInfo></SendInfo>");
            Node myRemoteNode;
            String sID = "(SELECT DECODE(MAX(ID),'',1,MAX(ID)+1) FROM S_EXDATA)";
            String sFromNode = ConfigurationSettings.AppSettings("LocalNodeName");
            String sGiid = sFromNode + "_" + this.iInfoID;
            String sFromUserName = getUserName(this.iUserID);
            String sFormInfoID = String.valueOf(this.iInfoID);
            String sToNode = "";
            String sToUserName = "";
            String sBT = getBTFromInfoID(String.valueOf(this.iInfoID));
            String sObjClass = this.sObjclass;
            _cmdStr = "SELECT GIID,FROMNODE,FROMUSERNAME FROM S_EXDATA WHERE ID=" + sExID;
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                sToNode = _myDs.getString("FROMNODE");
                sToUserName = _myDs.getString("FROMUSERNAME");
                sGiid = _myDs.getString("GIID");
            }
            _myDs.close();
            String xmlpath = ConfigurationSettings.AppSettings("MailPath_INFO") + FileTranServiceXML + ConfigurationSettings.AppSettings("MailPath_INFO") + "Data.xml";
            Document document = saxreader.read(new File(xmlpath));
            // ActionXMLClass pAX = new ActionXMLClass();
            // pAX.SetXMLDoc(ConfigurationSettings.AppSettings("MailPath_INFO") + FileTranServiceXML, ConfigurationSettings .AppSettings("MailPath_INFO") + "Data.xml");
            // pAX.SetData("INFO_ID", iInfoID, 0);
            // String sDataInfo = pAX.PraseActionXml();
            // pAX = null;
            // 配备XML
            myRemoteNode = myRemoteXml.selectSingleNode("SendInfo");
            ((Element) myRemoteNode).addAttribute("GIID", sGiid);
            ((Element) myRemoteNode).addAttribute("FROMNODE", sFromNode);
            ((Element) myRemoteNode).addAttribute("FROMUSERNAME", sFromUserName);
            ((Element) myRemoteNode).addAttribute("FormInfoID", sFormInfoID);
            ((Element) myRemoteNode).addAttribute("TONODE", sToNode);
            ((Element) myRemoteNode).addAttribute("TOUSERNAME", sToUserName);
            ((Element) myRemoteNode).addAttribute("OBJCLASS", sObjClass);
            ((Element) myRemoteNode).addAttribute("BT", sBT);
            ((Element) myRemoteNode).addAttribute("NEEDBACK", isNeedBack);
            ((Element) myRemoteNode).addAttribute("PID", String.valueOf(this.iPID));
            ((Element) myRemoteNode).addAttribute("PNID", "0");
            ((Element) myRemoteNode).addAttribute("NODEID", td.valueOf("@NodeID"));
            ((Element) myRemoteNode).addAttribute("NODECAPTION", td.valueOf("@NodeCaption"));
            ((Element) myRemoteNode).addAttribute("FPNID", String.valueOf(this.iPnID));
            String sBackUserInfo = ";:0:" + this.iUserID + ":" + this.sCaption + "::" + sFromUserName + ":" + this.getDetpIDFromUserID(this.iUserID);
            ((Element) myRemoteNode).addAttribute("BACKUSERINFO", sBackUserInfo);
            // ((Element) myRemoteNode.selectSingleNode("DataInfo")).add(DocumentHelper.parseText(sDataInfo).getRootElement());
            // 插入S_EXDATA
            _cmdStr = "INSERT INTO S_EXDATA(ID,GIID,FROMNODE,FROMUSERNAME,FROM_INFO_ID,TONODE,TOUSERNAME,TO_INFO_ID,OBJCLASS,BT,CREATEDATE,TOTALBYTES,STATUS,NEEDBACK,XML,TYPE) ";
            _cmdStr += "VALUES (" + sID + ",'" + sGiid + "','" + sFromNode + "','" + sFromUserName + "'," + sFormInfoID + ",'" + sToNode + "','" + sToUserName + "',0,'" + sObjClass + "','" + sBT + "',";
            _cmdStr += processDate("NOW,", DBMS, now);
            _cmdStr += "0,0," + isNeedBack + ",'" + sBackUserInfo + "',1)";
            _myRead.executeUpdate(_cmdStr);
            if (!"1".equals(isNeedBack)) { // 不需要回执，将当前节点置为办结
                _cmdStr = "UPDATE G_OPINION SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID + " AND STATUS<>1";
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_PNODES SET STATUS=-1,";
                _cmdStr += processDate("SDATE=NOW,PDATE=NOW ", DBMS, now);
                _cmdStr += "WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_PROUTE SET STATUS=-1 WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "DELETE G_INBOX WHERE INFO_ID=" + this.iInfoID;
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "UPDATE G_INFOS SET STATUS=2 WHERE ID=" + this.iInfoID;
                _myRead.executeUpdate(_cmdStr);
            }
            // 保存文件发送文件包
            // myRemoteXml.Save(ConfigurationSettings.AppSettings("MailPath_OUT") + sGiid + "_" + sToNode + ".XML");
            // myRemoteXml = null;
            // 发送结束后处理语句
            doCommand(getSqlFromAfterSendSql(td.valueOf("@AfterSendSql")), _myRead, httpTask);
            _myConn.commit();
            doHttp(httpTask);
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
            if (_myConn != null) {
                try {
                    _myConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getSqlFromAfterSendSql(String sSql) throws Exception {
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 5;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = String.valueOf(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = String.valueOf(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = String.valueOf(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = String.valueOf(this.iPnID);
        for (int i = 0; i < iKeyCount; i++) {
            sSql = sSql.replace(sKey[i], sValue[i]);
        }
        return sSql;
    }

    //.net向上找子机构的逻辑
    private String getDeptFromLevelDotNet(String sDeptID, String strLevel) {
    	String strDepts = "";
    	for (int i = 0; i < this.iUserCount; i++) {
    		 if (sDeptID.equals(this.arrUserList[i][G_USERS.ID.ordinal()])) {
    			 if (strLevel.equals(this.arrUserList[i][G_USERS.ISOUTER.ordinal()])) {
                     strDepts += this.arrUserList[i][G_USERS.ID.ordinal()] + "^" + this.arrUserList[i][G_USERS.UNAME.ordinal()] + ",";
                 } else{
                	 // 向上
                     for (int j = 0; j < this.iUserCount; j++) {
                         if (sDeptID.equals(this.arrUserList[j][G_USERS.ID.ordinal()])) {
                             if ("2".equals(this.arrUserList[j][G_USERS.UTYPE.ordinal()])) {
                                 strDepts += getDeptFromLevelDotNet(this.arrUserList[j][G_USERS.DEPT_ID.ordinal()], strLevel);
                             }
                         }
                     }
                 }
    		 }
    	}
    	return strDepts;
    }
    
    private String getDeptFromLevel(String sDeptID, String strLevel) {
        String strDepts = "";
        for (int i = 0; i < this.iUserCount; i++) {
            if (sDeptID.equals(this.arrUserList[i][G_USERS.ID.ordinal()])) {
                if (strLevel.equals(this.arrUserList[i][G_USERS.ISOUTER.ordinal()])) {
                    strDepts += this.arrUserList[i][G_USERS.ID.ordinal()] + "^" + this.arrUserList[i][G_USERS.UNAME.ordinal()] + ",";
                } else if (this.arrUserList[i][G_USERS.ISOUTER.ordinal()] != "" && Integer.parseInt((this.arrUserList[i][G_USERS.ISOUTER.ordinal()])) < Integer.parseInt(strLevel)) {
                    // 向下
                    for (int j = 0; j < this.iUserCount; j++) {
                        if (sDeptID.equals(this.arrUserList[j][G_USERS.DEPT_ID.ordinal()])) {
                            if ("2".equals(this.arrUserList[j][G_USERS.UTYPE.ordinal()])) {
                                strDepts += getDeptFromLevel_DN(this.arrUserList[j][G_USERS.ID.ordinal()], strLevel);
                            }
                        }
                    }
                } else {
                    // 向上
                    for (int j = 0; j < this.iUserCount; j++) {
                        if (sDeptID.equals(this.arrUserList[j][G_USERS.ID.ordinal()])) {
                            if ("2".equals(this.arrUserList[j][G_USERS.UTYPE.ordinal()])) {
                                strDepts += getDeptFromLevel_UP(this.arrUserList[j][G_USERS.DEPT_ID.ordinal()], strLevel);
                            }
                        }
                    }
                }
            }
        }
        return strDepts;
    }

    private String getDeptFromLevel_DN(String sDeptID, String strLevel) {
        String strDepts = "";
        for (int i = 0; i < this.iUserCount; i++) {
            if (sDeptID.equals(this.arrUserList[i][G_USERS.ID.ordinal()])) {
                if (strLevel.equals(this.arrUserList[i][G_USERS.ISOUTER.ordinal()])) {
                    strDepts += this.arrUserList[i][G_USERS.ID.ordinal()] + "^" + this.arrUserList[i][G_USERS.UNAME.ordinal()] + ",";
                } else if (!StringUtils.isEmpty(this.arrUserList[i][G_USERS.ISOUTER.ordinal()]) && Integer.parseInt((this.arrUserList[i][G_USERS.ISOUTER.ordinal()])) < Integer.parseInt(strLevel)) {
                    // 向下
                    for (int j = 0; j < this.iUserCount; j++) {
                        if (sDeptID.equals(this.arrUserList[j][G_USERS.DEPT_ID.ordinal()])) {
                            if ("2".equals(this.arrUserList[j][G_USERS.UTYPE.ordinal()])) {
                                strDepts += getDeptFromLevel_DN(this.arrUserList[j][G_USERS.ID.ordinal()], strLevel);
                            }
                        }
                    }
                }
            }
        }
        return strDepts;
    }

    private String getDeptFromLevel_UP(String sDeptID, String strLevel) {
        String strDepts = "";
        for (int i = 0; i < this.iUserCount; i++) {
            if (sDeptID.equals(this.arrUserList[i][G_USERS.ID.ordinal()])) {
                if (strLevel.equals(this.arrUserList[i][G_USERS.ISOUTER.ordinal()])) {
                    strDepts += this.arrUserList[i][G_USERS.ID.ordinal()] + "^" + this.arrUserList[i][G_USERS.UNAME.ordinal()] + ",";
                } else if (!StringUtils.isEmpty(this.arrUserList[i][G_USERS.ISOUTER.ordinal()]) && Integer.parseInt((this.arrUserList[i][G_USERS.ISOUTER.ordinal()])) > Integer.parseInt(strLevel)) {
                    // 向上
                    for (int j = 0; j < this.iUserCount; j++) {
                        if (sDeptID.equals(this.arrUserList[j][G_USERS.ID.ordinal()])) {
                            if ("2".equals(this.arrUserList[j][G_USERS.UTYPE.ordinal()])) {
                                strDepts += getDeptFromLevel_UP(this.arrUserList[j][G_USERS.DEPT_ID.ordinal()], strLevel);
                            }
                        }
                    }
                }
            }
        }
        return strDepts;
    }


    //将原有的按照PID排序顺序查找父节点修改为递归所有数据查找父节点 杨龙修改 2013-7-18 开始
    // 选取节点部门
    private TempDept getOnlyOneDeptInfo(String _WFNODEID) throws Exception {
        String _cmdStr = "";
        String curFid = String.valueOf(this.iPnID);
        _cmdStr = "select A.ID,A.FID,A.WFNODE_ID,A.USER_ID,A.DEPT_ID,0 AS UTYPE,A.UNAME AS USERNAME,'' as DEPTNAME from G_PNODES A where PID=" + this.iPID + " order by ID desc";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        TempDept td = this.getOnlyOneDept(dt, curFid, _WFNODEID);
        return td;
    }

    //递归循环经办节点部门，找到需要继承的节点数据
    private TempDept getOnlyOneDept(xsf.data.DataTable dt, String curFid,String wfNodeId) throws Exception {
    	TempDept td=null;
    	for (xsf.data.DataRow dr : dt.getRows()) {
            if (curFid.equals(dr.getString("ID"))) {
                if (wfNodeId.equals(dr.getString("WFNODE_ID"))) {
                	td = new TempDept();
                	String sDeptRetID = "";
                    String sDeptRetName = "";
                    // 查找当前用户所在部门
                    for (int i = 0; i < iUserCount; i++) {
                        if (arrUserList[i][G_USERS.ID.ordinal()].equals(dr.getString("USER_ID")) && arrUserList[i][G_USERS.DEPT_ID.ordinal()].equals(dr.getString("DEPT_ID"))) {
                        	sDeptRetID += "," + arrUserList[i][G_USERS.DEPT_ID.ordinal()];
                            sDeptRetName += "," + arrUserList[i][G_USERS.DETP_NAME.ordinal()];
                            // break;
                        }
                    }
                    td.setSDept_ID(sDeptRetID);
                    td.setSDept_Name(sDeptRetName);
                    break;
                } else {
                    curFid = dr.getString("FID") != null ? dr.getString("FID") : "";
                    if(td==null&&!"0".equals(curFid))
                    {
                    	td=getOnlyOneDept(dt,curFid,wfNodeId);
                    }
                    break;
                }
            }
        }
    	return td;
    }
  //将原有的按照PID排序顺序查找父节点修改为递归所有数据查找父节点 杨龙修改 2013-7-18 结束
    
    private String getUserBySql(String sSql) throws Exception {
        String ret = "";
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 5;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = String.valueOf(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = String.valueOf(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = String.valueOf(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = String.valueOf(this.iPnID);
        for (int i = 0; i < iKeyCount; i++) {
            sSql = sSql.replace(sKey[i], sValue[i]);
        }
        // ---------------------------------------------------------------------
        xsf.data.DataTable dt = DBManager.getDataTable(sSql);
        String sUserList = "";
        for (xsf.data.DataRow dr : dt.getRows()) {
            sUserList += dr.getString(0) + ",";
        }
        sUserList = sUserList.substring(0, sUserList.length() - 1);
        // ---------------------------------------------------------------------
        // 吴红亮 添加 开始
        Map<String, String> u = new HashMap<String, String>();
        Map<String, String> ud = new HashMap<String, String>();
        List<String> d = new ArrayList<String>();
        String sDeptList = "";
        if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
            // dt = DBManager.getDataTable("select A.USER_ID USER_ID,B.USER_ID DEPT_ID from G_DEPT A,G_DEPT B where A.FID=B.ID and A.ISMAIN=1 and A.USER_ID in(" + sUserList + ")");
            IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            sqlDataSource.setParameter("IDs", sUserList);
            dt = sqlDataSource.query("getUsersMainDeptID");
            for (xsf.data.DataRow dr : dt.getRows()) {
                ud.put(dr.getString("USER_ID"), dr.getString("DEPT_ID"));
                sDeptList += dr.getString("DEPT_ID") + ",";
            }
            sDeptList = "," + sDeptList;
        }
        // 吴红亮 添加 结束
        // ---------------------------------------------------------------------
        sUserList = "," + sUserList + ",";
        for (int i = 0; i < this.iUserCount; i++) {
            String sDept_Name = this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
            String sDept_ID = this.arrUserList[i][G_USERS.DEPT_ID.ordinal()];
            String sUser_ID = this.arrUserList[i][G_USERS.ID.ordinal()];
            String sUser_NAME = this.arrUserList[i][G_USERS.UNAME.ordinal()];
            String sUser_UTYPE = this.arrUserList[i][G_USERS.UTYPE.ordinal()];
            String sPhone = this.arrUserList[i][G_USERS.TEL.ordinal()];
            String sEmail = this.arrUserList[i][G_USERS.EMAIL.ordinal()];
            String sLogname = this.arrUserList[i][G_USERS.LOGNAME.ordinal()];
            String sTopName = this.arrUserList[i][10];
            if ("0".equals(sUser_UTYPE) || "9".equals(sUser_UTYPE)) {
                // 普通用户 || 系统管理员
                if (sUserList.indexOf("," + sUser_ID + ",") != -1) {
                    if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
                        if (sDept_ID.equals(ud.get(sUser_ID))) {
                            String s = "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\"></Node>";
                            String t = u.get(sDept_ID);
                            if (t == null) {
                                u.put(sDept_ID, s);
                            } else {
                                u.put(sDept_ID, t + s);
                            }
                            sUserList = sUserList.replace("," + sUser_ID + ",", ",");
                        }
                    } else {
                        ret += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\">";
                        ret += "</Node>";
                        sUserList = sUserList.replace("," + sUser_ID + ",", ",");
                    }
                }
            } else {
                // ret += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\">";
                // ret += "</Node>";
                // 吴红亮 添加 开始
                if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
                    if (sDeptList.indexOf("," + sUser_ID + ",") != -1) {
                        String s = "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\">";
                        d.add(sUser_ID + s);
                    }
                }
                // 吴红亮 添加 结束
            }
        }
        // 吴红亮 添加 开始
        if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
            for (String s : d) {
                int i = s.indexOf("<");
                ret += s.substring(i, s.length()) + u.get(s.substring(0, i)) + "</Node>";
            }
        }
        // 吴红亮 添加 结束
        return ret;
    }

//重载getDeptBySql方法 增加默认选中和必选的过滤 杨龙修改 2013-6-18
    private String getUserBySql(String sSql,String strSelected) throws Exception {
        String ret = "";
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 5;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = String.valueOf(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = String.valueOf(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = String.valueOf(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = String.valueOf(this.iPnID);
        for (int i = 0; i < iKeyCount; i++) {
            sSql = sSql.replace(sKey[i], sValue[i]);
        }
        // ---------------------------------------------------------------------
        xsf.data.DataTable dt = DBManager.getDataTable(sSql);
        String sUserList = "";
      	String nodes="";//当用户使用SQL选人 跨机构发送流程时直接获取sql查询出的人员  2014-07-01 huml add
      	String sqlMainUnit="";
      	
      	Map<String, String> u = new HashMap<String, String>();
        Map<String, String> ud = new HashMap<String, String>();
        List<String> d = new ArrayList<String>();
        String sDeptList = "";
      	
        // ---------------------------------------------------------------------
        // 吴红亮 添加 开始
        
        if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
            // dt = DBManager.getDataTable("select A.USER_ID USER_ID,B.USER_ID DEPT_ID from G_DEPT A,G_DEPT B where A.FID=B.ID and A.ISMAIN=1 and A.USER_ID in(" + sUserList + ")");
            IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            sqlDataSource.setParameter("IDs", sUserList);
            dt = sqlDataSource.query("getUsersMainDeptID");
            for (xsf.data.DataRow dr : dt.getRows()) {
                ud.put(dr.getString("USER_ID"), dr.getString("DEPT_ID"));
                sDeptList += dr.getString("DEPT_ID") + ",";
            }
            sDeptList = "," + sDeptList;
        }
        // 吴红亮 添加 结束
        // ---------------------------------------------------------------------
        
        for (xsf.data.DataRow dr : dt.getRows()) {
            sUserList += dr.getString(0) + ",";
            String tempdept = dr.getString("DEPT_ID");
            for (int i = 0; i < this.iUserCount; i++) {
                String sDept_Name = this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
                String sDept_ID =StringHelper.isNullOrEmpty(tempdept)? this.arrUserList[i][G_USERS.DEPT_ID.ordinal()]:tempdept;
                String sUser_ID = this.arrUserList[i][G_USERS.ID.ordinal()];
                String sUser_NAME = this.arrUserList[i][G_USERS.UNAME.ordinal()];
                String sUser_UTYPE = this.arrUserList[i][G_USERS.UTYPE.ordinal()];
                String sPhone = this.arrUserList[i][G_USERS.TEL.ordinal()];
                String sEmail = this.arrUserList[i][G_USERS.EMAIL.ordinal()];
                String sLogname = this.arrUserList[i][G_USERS.LOGNAME.ordinal()];
                String sTopName = this.arrUserList[i][10];
                if ("0".equals(sUser_UTYPE) || "9".equals(sUser_UTYPE)) {
                    // 普通用户 || 系统管理员
                    if (sUser_ID.equals(dr.getString(0))) {
                        if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
                            if (sDept_ID.equals(ud.get(sUser_ID))) {
                                String s = "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + strSelected+sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\"></Node>";
                                String t = u.get(sDept_ID);
                                if (t == null) {
                                    u.put(sDept_ID, s);
                                } else {
                                    u.put(sDept_ID, t + s);
                                }
                                break;
                                //sUserList = sUserList.replace("," + sUser_ID + ",", ",");
                            }
                        } else {
                            ret += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + strSelected+sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\">";
                            ret += "</Node>";
                            break;
                            //sUserList = sUserList.replace("," + sUser_ID + ",", ",");
                        }
                    }
                } else {
                    // ret += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\">";
                    // ret += "</Node>";
                    // 吴红亮 添加 开始
                    if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
                        if (sUser_ID.equals(dr.getString("ID"))) {
                            String s = "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + strSelected+sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\">";
                            d.add(sUser_ID + s);
                        }
                    }
                    // 吴红亮 添加 结束
                }
            }
            
            String sUser_ID=dr.getString("ID");
            String sUser_UTYPE=dr.getString("UTYPE");
            String sUser_NAME=dr.getString("UNAME");
            String sDept_ID=dr.getString("DEPT_ID");
            String sDept_Name=dr.getString("DETP_NAME");
            String sPhone=dr.getString("TEL");
            String sEmail=dr.getString("EMAIL");
            String sLogname=dr.getString("LOGNAME");
            String sTopName=dr.getString("MAINUNION");
            sqlMainUnit=dr.getString("MAINUNIT");
            nodes+= "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + strSelected+sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\">";
            nodes += "</Node>";
            
        }
        sUserList = sUserList.substring(0, sUserList.length() - 1);
       
        sUserList = "," + sUserList + ",";
        /*for (int i = 0; i < this.iUserCount; i++) {
            String sDept_Name = this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
            String sDept_ID = this.arrUserList[i][G_USERS.DEPT_ID.ordinal()];
            String sUser_ID = this.arrUserList[i][G_USERS.ID.ordinal()];
            String sUser_NAME = this.arrUserList[i][G_USERS.UNAME.ordinal()];
            String sUser_UTYPE = this.arrUserList[i][G_USERS.UTYPE.ordinal()];
            String sPhone = this.arrUserList[i][G_USERS.TEL.ordinal()];
            String sEmail = this.arrUserList[i][G_USERS.EMAIL.ordinal()];
            String sLogname = this.arrUserList[i][G_USERS.LOGNAME.ordinal()];
            String sTopName = this.arrUserList[i][10];
            if ("0".equals(sUser_UTYPE) || "9".equals(sUser_UTYPE)) {
                // 普通用户 || 系统管理员
                if (sUserList.indexOf("," + sUser_ID + ",") != -1) {
                    if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
                        if (sDept_ID.equals(ud.get(sUser_ID))) {
                            String s = "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + strSelected+sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\"></Node>";
                            String t = u.get(sDept_ID);
                            if (t == null) {
                                u.put(sDept_ID, s);
                            } else {
                                u.put(sDept_ID, t + s);
                            }
                            sUserList = sUserList.replace("," + sUser_ID + ",", ",");
                        }
                    } else {
                        ret += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + strSelected+sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\">";
                        ret += "</Node>";
                        sUserList = sUserList.replace("," + sUser_ID + ",", ",");
                    }
                }
            } else {
                // ret += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\">";
                // ret += "</Node>";
                // 吴红亮 添加 开始
                if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
                    if (sDeptList.indexOf("," + sUser_ID + ",") != -1) {
                        String s = "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + strSelected+sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\">";
                        d.add(sUser_ID + s);
                    }
                }
                // 吴红亮 添加 结束
            }
        }*/
        // 吴红亮 添加 开始
        if (dsoap.tools.ConfigurationSettings.isSqlUserShowDept) {
            for (String s : d) {
                int i = s.indexOf("<");
                ret += s.substring(i, s.length()) + u.get(s.substring(0, i)) + "</Node>";
            }
        }
        // 吴红亮 添加 结束
        
        if(StringHelper.isNullOrEmpty(ret) || this.isSendBack){//当用户使用SQL选人 跨机构发送流程时直接获取sql查询出的人员  2014-07-01 huml add
        	ret=nodes;
        	if(StringHelper.isNotNullAndEmpty(sqlMainUnit)){
        		this.MainUnit=sqlMainUnit;
        	}
        }
        return ret;
    }

    private String getDeptBySql(String sSql, String sRole_ID, String sRole_Name, DataTable AclTable) throws Exception {
        String ret = "";
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 5;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = String.valueOf(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = String.valueOf(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = String.valueOf(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = String.valueOf(this.iPnID);
        for (int i = 0; i < iKeyCount; i++) {
            sSql = sSql.replace(sKey[i], sValue[i]);
        }
        xsf.data.DataTable dt = DBManager.getDataTable(sSql);
        String sDeptList = "";
        for (xsf.data.DataRow dr : dt.getRows()) {
            sDeptList += "," + dr.getString(1);
        }
        sDeptList = sDeptList.substring(1);
        // sSql = "select ID DEPT_ID,UNAME DEPT_NAME from G_USERS where ID in (" + sDeptList + ")";
        // dt = DBManager.getDataTable(sSql);
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("IDs", sDeptList);
        dt = sqlDataSource.query("getDeptBySql");
        for (xsf.data.DataRow dr : dt.getRows()) {
            String sDept_ID = dr.getString("DEPT_ID") != null ? dr.getString("DEPT_ID") : "";
            String sDept_Name = dr.getString("DEPT_NAME") != null ? dr.getString("DEPT_NAME") : "";
            boolean isHas = false;
            for (DataRow tr : AclTable.rows) {
                if (sDept_ID.equals(tr.get("DEPT_ID"))) {
                    tr.put("ROLE_ID", tr.get("ROLE_ID") + "," + sRole_ID);
                    tr.put("ROLE_NAME", tr.get("ROLE_NAME") + "," + sRole_Name);
                    isHas = true;
                    break;
                }
            }
            if (!isHas) {
                DataRow tr = AclTable.NewRow();
                tr.put("DEPT_ID", sDept_ID);
                tr.put("DEPT_NAME", sDept_Name);
                //-------------------根据SQL选部门 显示部门名称  杨龙修改 2012/8/23 开始
              //tr.put("DEPT_NAME", sDept_ID); 将原来的ID改为NAME
                tr.put("DEPT_NAME", sDept_Name);
                //-------------------根据SQL选部门 显示部门名称  杨龙修改 2012/8/23 结束
                tr.put("ROLE_ID", sRole_ID);
                tr.put("ROLE_NAME", sRole_Name);
                AclTable.rows.add(tr);
            }
        }
        return ret;
    }

//重载getDeptBySql方法 增加默认选中和必选的过滤 杨龙修改 2013-6-20
    private String getDeptBySql(String sSql, String sRole_ID, String sRole_Name, DataTable AclTable,String strSelected) throws Exception {
        String ret = "";
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 5;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = String.valueOf(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = String.valueOf(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = String.valueOf(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = String.valueOf(this.iPnID);
        for (int i = 0; i < iKeyCount; i++) {
            sSql = sSql.replace(sKey[i], sValue[i]);
        }
        xsf.data.DataTable dt = DBManager.getDataTable(sSql);
        String sDeptList = "";
        for (xsf.data.DataRow dr : dt.getRows()) {
            sDeptList += "," + dr.getString(1);
        }
        sDeptList = sDeptList.substring(1);
        // sSql = "select ID DEPT_ID,UNAME DEPT_NAME from G_USERS where ID in (" + sDeptList + ")";
        // dt = DBManager.getDataTable(sSql);
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("IDs", sDeptList);
        dt = sqlDataSource.query("getDeptBySql");
        for (xsf.data.DataRow dr : dt.getRows()) {
            String sDept_ID = dr.getString("DEPT_ID") != null ? dr.getString("DEPT_ID") : "";
            String sDept_Name = dr.getString("DEPT_NAME") != null ? dr.getString("DEPT_NAME") : "";
            boolean isHas = false;
            for (DataRow tr : AclTable.rows) {
                if (sDept_ID.equals(tr.get("DEPT_ID"))) {
                    tr.put("ROLE_ID", tr.get("ROLE_ID") + "," + sRole_ID);
                    tr.put("ROLE_NAME", tr.get("ROLE_NAME") + "," + sRole_Name);
                    isHas = true;
                    break;
                }
            }
            if (!isHas) {
                DataRow tr = AclTable.NewRow();
                tr.put("DEPT_ID", sDept_ID);
                //tr.put("DEPT_NAME", sDept_Name);
                //-------------------根据SQL选部门 显示部门名称  杨龙修改 2012/8/23 开始
              //tr.put("DEPT_NAME", sDept_ID); 将原来的ID改为NAME
                tr.put("DEPT_NAME", strSelected+sDept_Name);
                //-------------------根据SQL选部门 显示部门名称  杨龙修改 2012/8/23 结束
                tr.put("ROLE_ID", sRole_ID);
                tr.put("ROLE_NAME", sRole_Name);
                AclTable.rows.add(tr);
            }
        }
        return ret;
    }

    private String getUTree(Node AclTD) throws Exception {
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        StringBuffer reStr = new StringBuffer();
        String sRole_IDList = "0";
        String _cmdStr = "";
        // 整理ACL
        int iIsRoleSelect = Integer.parseInt(AclTD.selectSingleNode("IsRoleSelect").getText());
        // int iIsSelected = Integer.parseInt(AclTD.selectSingleNode("IsSelected").getText());
        // int iIsOnlyOneUser = Integer.parseInt(AclTD.selectSingleNode("IsOnlyOneUser").getText());
        // int iIsMultiUser = Integer.parseInt(AclTD.selectSingleNode("IsMultiUser").getText());
        int iType = Integer.parseInt(AclTD.selectSingleNode("type").getText());
        DataTable AclTable = new DataTable("ACL");
        AclTable.columns.put("DEPT_ID", String.class);
        AclTable.columns.put("DEPT_NAME", String.class);
        AclTable.columns.put("ROLE_ID", String.class);
        AclTable.columns.put("ROLE_NAME", String.class);
        AclTable.columns.put("MAINUNION", String.class);
        switch (iType) {
        case 0:
        case 9:
            // 通过部门选角色+远程部门
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                String sDept_ID = td.selectSingleNode("dept").getText();
                String sDept_Name = td.selectSingleNode("dept").valueOf("@Name").trim();
                String sRole_ID = td.selectSingleNode("role").getText();
                String sRole_Name = td.selectSingleNode("role").valueOf("@Name").trim();
                sRole_IDList += "," + sRole_ID;
                boolean isHas = false;
                for (DataRow tr : AclTable.rows) {
                    if (sDept_ID.equals(tr.get("DEPT_ID"))) {
                        tr.put("ROLE_ID", tr.get("ROLE_ID") + "," + sRole_ID);
                        tr.put("ROLE_NAME", tr.get("ROLE_NAME") + "," + sRole_Name);
                        isHas = true;
                        break;
                    }
                }
                if (!isHas) {
                    DataRow tr = AclTable.NewRow();
                    tr.put("DEPT_ID", sDept_ID);
                    tr.put("DEPT_NAME", sDept_Name);
                    tr.put("ROLE_ID", sRole_ID);
                    tr.put("ROLE_NAME", sRole_Name);
                    AclTable.rows.add(tr);
                }
            }
            break;
        case 1:
            // 继承节点部门
        	String strSelected="";
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                String sDept_ID = td.selectSingleNode("dept").getText();
                String sDept_Name = td.selectSingleNode("dept").valueOf("@Name").trim();
                //增加默认选中，必选标识 杨龙修改 2013/6/20
                if(sDept_Name.indexOf("默认")>-1)
                {
                	strSelected="[默认选中]";
                }
                else if(sDept_Name.indexOf("必选")>-1)
                {
                	strSelected="[必选]";
                }
                String sRole_ID = td.selectSingleNode("role").getText();
                String sRole_Name = td.selectSingleNode("role").valueOf("@Name").trim();
                String sLevel = td.selectSingleNode("dept").valueOf("@Level").trim();
                sRole_IDList += "," + sRole_ID;
                if (this.iPID == 0 || this.iPnID == 0) {
                    String sMainUserID = null;
                    // 吴红亮 添加 开始 过滤跨部门
                    String sMainDeptID = null;
                    // 吴红亮 添加 结束
                    // // _cmdStr = "select ID,UNAME from G_USERS where ID=(select USER_ID from G_INFOS where ID=" + this.iInfoID + ")";
                    // _cmdStr = "select USER_ID,MAINDEPT from G_INFOS A,G_USERS B where A.USER_ID=B.ID and B.STATUS>-1 and A.ID=" + this.iInfoID;
                    // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
                    sqlDataSource.setParameter("INFO_ID", this.iInfoID);
                    xsf.data.DataTable dt = sqlDataSource.query("getMainDept");
                    for (xsf.data.DataRow dr : dt.getRows()) {
                        sMainUserID = dr.getString("USER_ID") != null ? dr.getString("USER_ID") : "";
                        sMainDeptID = dr.getString("MAINDEPT") != null ? dr.getString("MAINDEPT") : "";
                        break;
                    }
                    if (null == sMainUserID) {
                        this.iErrorCode = 101;
                        throw new Exception();
                    }
                    for (int i = 0; i < this.iUserCount; i++) {
                        // if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(sMainUserID)) {
                        if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(sMainUserID) && (!dsoap.tools.ConfigurationSettings.isCrossDept || (dsoap.tools.ConfigurationSettings.isCrossDept && this.arrUserList[i][G_USERS.DEPT_ID.ordinal()].equals(sMainDeptID)))) {
                            sDept_ID += "," + this.arrUserList[i][G_USERS.DEPT_ID.ordinal()];
                            sDept_Name += "," + this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
                            // break;
                        }
                    }
                } else {// sDept_ID为要继承的nodeid
                    if ("-1".equals(sDept_ID)) {// 发送节点
                        sDept_ID = String.valueOf(this.iWfNodeID);
                    }
                    TempDept tempd = getOnlyOneDeptInfo(sDept_ID);
                    sDept_ID = tempd.getSDept_ID();
                    sDept_Name = tempd.getSDept_Name();
                    if ("".equals(sDept_ID != null ? sDept_ID.trim() : "")) {
                        continue;
                    }
                }
                String[] sDeptIDList = sDept_ID.split(",");
                String[] sDeptNameList = sDept_Name.split(",");
                String strDepts = "";
                // 根据部门级别查找当前部门
                if (!"0".equals(sLevel)) {
                    for (int i = 1; i < sDeptIDList.length; i++) {
                        strDepts += getDeptFromLevel(sDeptIDList[i], sLevel);
                    }
                } else {
                    for (int i = 1; i < sDeptIDList.length; i++) {
                        strDepts += sDeptIDList[i] + "^" + sDeptNameList[i] + ",";
                    }
                }
                if (!"".equals(strDepts.trim())) {
                    strDepts = strDepts.substring(0, strDepts.length() - 1);
                    String[] strDeptList = strDepts.split(",");
                    for (String s : strDeptList) {
                        sDept_ID = s.split("\\^")[0];
                        sDept_Name = s.split("\\^")[1];
                        boolean isHas = false;
                        for (DataRow tr : AclTable.rows) {
                            if (sDept_ID.equals(tr.get("DEPT_ID"))) {
                                tr.put("ROLE_ID", tr.get("ROLE_ID") + "," + sRole_ID);
                                tr.put("ROLE_NAME", tr.get("ROLE_NAME") + "," + sRole_Name);
                                isHas = true;
                                break;
                            }
                        }
                        if (!isHas) {
                            DataRow tr = AclTable.NewRow();
                            tr.put("DEPT_ID", sDept_ID);
                            tr.put("DEPT_NAME", strSelected+sDept_Name);
                            tr.put("ROLE_ID", sRole_ID);
                            tr.put("ROLE_NAME", sRole_Name);
                            AclTable.rows.add(tr);
                        }
                    }
                }
                strSelected="";
            }
            break;
        case 2:
            // SQL选部门+人
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
              //增加 默认选中和必选的判断 杨龙修改 2013/6/18
                String roleText=td.selectSingleNode("text").getText();
                if(roleText.indexOf("默认")>-1)
                {
                	roleText="[默认选中]";
                }
                else if(roleText.indexOf("必选")>-1)
                {
                	roleText="[必选]";
                }
                else
                {
                	roleText="";
                }
                String sSql = td.selectSingleNode("dept").getText();
                String sRole_ID = td.selectSingleNode("role").getText();
                String sRole_Name = td.selectSingleNode("role").valueOf("@Name").trim();
                sRole_IDList += "," + sRole_ID;
                getDeptBySql(sSql, sRole_ID, sRole_Name, AclTable,roleText);          
            }
            break;
        case 3:
            // 继承节点经办人
            reStr.setLength(0);
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                //增加 默认选中和必选的判断 杨龙修改 2013/6/18
                String roleText=td.selectSingleNode("text").getText();
                if(roleText.indexOf("默认")>-1)
                {
                	roleText="[默认选中]";
                }
                else if(roleText.indexOf("必选")>-1)
                {
                	roleText="[必选]";
                }
                else
                {
                	roleText="";
                }
                reStr.append(getOnlyOneUserID(td.selectSingleNode("role").getText(), 1,roleText));
            }
            if ("".equals(reStr)) {
                throw new Exception("经办人不存在！");
            }
            break;
        case 4:// SQL选人
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
              //增加 默认选中和必选的判断 杨龙修改 2013/6/18
                String roleText=td.selectSingleNode("text").getText();
                if(roleText.indexOf("默认")>-1)
                {
                	roleText="[默认选中]";
                }
                else if(roleText.indexOf("必选")>-1)
                {
                	roleText="[必选]";
                }
                else
                {
                	roleText="";
                }
                String sSelectUserSql = td.selectSingleNode("role").getText();
                reStr.append(getUserBySql(sSelectUserSql,roleText));
            }
            break;
        }
        // 0，1，9三种方式继续
        // // 取角色和部门信息;获取所有的小组人员结构
        // // 吴红亮 修改 开始
        // // _cmdStr = "SELECT DISTINCT TOP (100) PERCENT A.GRP_ID,A.USER_ID,B.UNAME AS USER_NAME,B.UTYPE AS USER_UTYPE,D.USER_ID AS DEPT_ID,E.UNAME AS DEPT_NAME,G.UNAME AS MAINUNION " + "FROM G_GRPS A,G_USERS B LEFT JOIN G_USERS G ON B.MAINCODE=G.ID,G_DEPT C,G_DEPT D,G_USERS E,G_USERS F " + "WHERE A.USER_ID=B.ID AND B.ID=C.USER_ID AND C.FID=D.ID AND D.USER_ID=E.ID AND A.GRP_ID=F.ID " + " AND (F.UTYPE=1 OR ( A.GRP_ID IN (" + sRole_IDList + ") ))" + "ORDER BY GRP_ID,USER_ID";
        // // String str = "select count(*) as num from (" + _cmdStr + ") AS derivedtbl_1";
        // if (!dsoap.tools.ConfigurationSettings.isRoleDept) {
        // _cmdStr = "SELECT DISTINCT A.GRP_ID,A.USER_ID,B.UNAME AS USER_NAME,B.UTYPE AS USER_UTYPE,D.USER_ID AS DEPT_ID,E.UNAME AS DEPT_NAME,G.UNAME AS MAINUNION FROM G_GRPS A,G_USERS B LEFT JOIN G_USERS G ON B.MAINCODE=G.ID,G_DEPT C,G_DEPT D,G_USERS E,G_USERS F WHERE A.USER_ID=B.ID AND B.ID=C.USER_ID AND C.FID=D.ID AND D.USER_ID=E.ID AND A.GRP_ID=F.ID AND (F.UTYPE=1 OR (A.GRP_ID IN (" + sRole_IDList + ")))";
        // } else {
        // _cmdStr = "SELECT DISTINCT A.GRP_ID,A.USER_ID,B.UNAME AS USER_NAME,B.UTYPE AS USER_UTYPE,D.USER_ID AS DEPT_ID,E.UNAME AS DEPT_NAME,G.UNAME AS MAINUNION FROM G_GRPS A,G_USERS B LEFT JOIN G_USERS G ON B.MAINCODE=G.ID,G_DEPT C,G_DEPT D,G_USERS E,G_USERS F WHERE A.USER_ID=B.ID AND B.ID=C.USER_ID AND C.FID=D.ID AND D.USER_ID=E.ID AND A.GRP_ID=F.ID AND A.DEPTID=E.ID AND (F.UTYPE=1 OR (A.GRP_ID IN (" + sRole_IDList + ")))";
        // }
        // String str = "select count(*) as num from (" + _cmdStr + ") derivedtbl_1";
        // _cmdStr += " ORDER BY GRP_ID,USER_ID";
        // // 吴红亮 修改 结束
        // xsf.data.DataTable dt = DBManager.getDataTable(str);
        // if (dt.getRows().size() > 0) {
        // xsf.data.DataRow dr = dt.getRows().get(0);
        // this.iGrpsCount = dr.getInt("num");
        // }
        // dt = DBManager.getDataTable(_cmdStr);
        for (DataRow tr : AclTable.rows) {
            reStr.append("<Node Id=\"" + tr.get("DEPT_ID") + "\" UType=\"2\" UName=\"" + tr.get("DEPT_NAME") + "\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">");
            if (iIsRoleSelect == 1) {
                // 通过角色选人
                reStr.append(getUList(tr.get("DEPT_ID"), tr.get("DEPT_NAME"), tr.get("ROLE_ID"), tr.get("ROLE_NAME"), tr.get("MAINUNION")));
                //dongchj如果继承节点部门 该角色下没有任何人，那么列举该部门所有人
                if(StringHelper.isNullOrEmpty(reStr)){
                	reStr.append(getUList(tr.get("DEPT_ID"),tr.get("DEPT_NAME"), tr.get("ROLE_ID"), "全部人员", tr.get("MAINUNION")));
                }
                
            } else {
                // 发送到角色
            	//dongchj 0217  增加当前发送是否发送角色
            	isSendByRole = true;
                String[] sRoleIDList = tr.get("ROLE_ID").split(",");
                String[] sRoleNameList = tr.get("ROLE_NAME").split(",");
                reStr.append(getRoleList(tr.get("DEPT_ID"), tr.get("DEPT_NAME"), sRoleIDList, sRoleNameList, tr.get("MAINUNION")));
                
            }
            reStr.append("</Node>");
        }
        return reStr.toString();
    }
    
    private void initUserRole()
    {
    	
    	xsf.data.DataTable dt = null;
    	
    	if(dtUserRole !=null){
    		dt = dtUserRole;
    	}else{
    		 IDataSource sqlDataSource = SysDataSource.getSysDataSource();
             String sRole_IDList = "0";
             String _cmdStr = "";
        	 if (!dsoap.tools.ConfigurationSettings.isRoleDept) {
                 _cmdStr = "getRoleUsers";
             } else {
                 _cmdStr = "getRoleDeptUsers";
             }
             sqlDataSource = SysDataSource.getSysDataSource();
             sqlDataSource.setParameter("ROLE_IDS", sRole_IDList);
             dt = sqlDataSource.query(_cmdStr);
    	}
    	
         if (dt != null) {
             this.iGrpsCount = dt.getRows().size();
         }
         this.arrGrpsList = new String[this.iGrpsCount][7];
         DataTable G_GRPS = new DataTable("G_GRPS");
         int i = 0;
         // 吴红亮 修改 结束
         for (xsf.data.DataRow row : dt.getRows()) {
             DataRow dr = G_GRPS.NewRow();
             this.arrGrpsList[i][0] = row.getString("GRP_ID") != null ? row.getString("GRP_ID") : "";
             this.arrGrpsList[i][1] = row.getString("USER_ID") != null ? row.getString("USER_ID") : "";
             this.arrGrpsList[i][2] = row.getString("USER_NAME") != null ? row.getString("USER_NAME") : "";
             this.arrGrpsList[i][3] = row.getString("USER_UTYPE") != null ? row.getString("USER_UTYPE") : "";
             this.arrGrpsList[i][4] = row.getString("DEPT_ID") != null ? row.getString("DEPT_ID") : "";
             this.arrGrpsList[i][5] = row.getString("DEPT_NAME") != null ? row.getString("DEPT_NAME") : "";
             this.arrGrpsList[i][6] = row.getString("MAINUNION") != null ? row.getString("MAINUNION") : "";
             // 构造datarow
             dr.put("GRP_ID", this.arrGrpsList[i][0]);
             dr.put("USER_ID", this.arrGrpsList[i][1]);
             dr.put("USER_NAME", this.arrGrpsList[i][2]);
             dr.put("USER_UTYPE", this.arrGrpsList[i][3]);
             dr.put("DEPT_ID", this.arrGrpsList[i][4]);
             dr.put("DEPT_NAME", this.arrGrpsList[i][5]);
             dr.put("MAINUNION", this.arrGrpsList[i][6]);
             G_GRPS.Add(dr);
             i++;
         }
         // 给map中添加相关datatable
         this.map.put("G_GRPS", G_GRPS);	
    }
    
    private String getUList(String sDept_ID, String sDept_Name, String sRole_ID, String sRole_Name, String sTopName) {
        String reStr = "";
        for (int i = 0; i < this.iUserCount; i++) {
            if (!sDept_ID.equals(this.arrUserList[i][G_USERS.DEPT_ID.ordinal()])) {
                continue;
            }
            String sUser_ID = this.arrUserList[i][G_USERS.ID.ordinal()];
            String sUser_NAME = this.arrUserList[i][G_USERS.UNAME.ordinal()];
            String sUser_UTYPE = this.arrUserList[i][G_USERS.UTYPE.ordinal()].substring(0, 1).toString();
            String sPhone = this.arrUserList[i][G_USERS.TEL.ordinal()];
            String sEmail = this.arrUserList[i][G_USERS.EMAIL.ordinal()];
            String sLogname = this.arrUserList[i][G_USERS.LOGNAME.ordinal()];
            sTopName = this.arrUserList[i][10];
            // sUser_UTYPE = String.valueOf(Integer.parseInt(sUser_UTYPE));
            if ("0".equals(sUser_UTYPE) || "9".equals(sUser_UTYPE)) {
            	//AclTable数据集增加MJ字段，表示人的密级强度 杨龙修改 2013/4/17 开始
            	if("1".equals(ConfigurationSettings.AppSettings("是否使用人员密级"))){
            		if(this.iInfoMJ>0)
                    {
                    	long userMJ=Long.parseLong(this.arrUserList[i][11]); 
                    	if(this.iInfoMJ>userMJ)
                    	{
                    		continue;
                    	}
                    }
            	}
                
                //AclTable数据集增加MJ字段，表示人的密级强度 杨龙修改 2013/4/17 结束
                // 普通用户 || 系统管理员
                if (isRoleUser(sUser_ID, sRole_ID, dsoap.tools.ConfigurationSettings.isRoleDept ? sDept_ID : null) || "全部人员".equals(sRole_Name)) {
                    reStr += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\"  Blqx=\"\" >";
                    reStr += "</Node>";
                }
                // } else if ("2".equals(sUser_UTYPE) || "8".equals(sUser_UTYPE)) {
            } else if (("2".equals(sUser_UTYPE) || "8".equals(sUser_UTYPE) || "5".equals(sUser_UTYPE)) && (!dsoap.tools.ConfigurationSettings.isStandaloneDept || (dsoap.tools.ConfigurationSettings.isStandaloneDept && sTopName.equals(sDept_Name.replace("[默认选中]", "").replace("[必选]", ""))))) {
                // 部门 || 服务器
            	/******如果部门是必选或者默认选中那么下级部门也继承该属性   杨龙修改 2013-6-18*********************************************************************/
            	if(sDept_Name.indexOf("默认")>-1)
            	{
            		sUser_NAME="[默认选中]"+sUser_NAME;
            	}else if(sDept_Name.indexOf("必选")>-1)
            	{
            		sUser_NAME="[必选]"+sUser_NAME;
            	}
                String sUlistStr = getUList(sUser_ID, sUser_NAME, sRole_ID, sRole_Name, sTopName);
                if (!"".equals(sUlistStr)) {
                    reStr += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\">";
                    reStr += sUlistStr;
                    reStr += "</Node>";
                }
            } else if ("1".equals(sUser_UTYPE) && this.isGroupView) {
                // 小组
                reStr += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"" + sPhone + "\" email=\"" + sEmail + "\" logname=\"" + sLogname + "\" topname=\"" + sTopName + "\">";
                reStr += "</Node>";
            }
        }
        
        return reStr;
    }

    private String getRoleList(String sDept_ID, String sDept_Name, String[] sRole_ID, String[] sRole_Name, String sTopName) {
        String reStr = "";
        for (int i = 0; i < sRole_ID.length; i++) {
            if (isHaveRoleUser(sDept_ID, sRole_ID[i])) {
                reStr += "<Node Id=\"" + sRole_ID[i] + "\" UType=\"3\" UName=\"" + sRole_Name[i] + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"\"  topname=\"" + sTopName + "\"></Node>";
            }
        }
        for (int i = 0; i < this.iUserCount; i++) {
            if (!sDept_ID.equals(this.arrUserList[i][G_USERS.DEPT_ID.ordinal()])) {
                continue;
            }
            String sUser_ID = this.arrUserList[i][G_USERS.ID.ordinal()];
            String sUser_NAME = this.arrUserList[i][G_USERS.UNAME.ordinal()];
            String sUser_UTYPE = this.arrUserList[i][G_USERS.UTYPE.ordinal()];
            sTopName = this.arrUserList[i][10];
            // if ("2".equals(sUser_UTYPE) || "8".equals(sUser_UTYPE)) {
            if ("2".equals(sUser_UTYPE) || "8".equals(sUser_UTYPE) || "5".equals(sUser_UTYPE)) {
                // 部门 || 服务器
            	/******如果部门是必选或者默认选中那么下级部门也继承该属性   杨龙修改 2013-6-18*********************************************************************/
            	if(sDept_Name.indexOf("默认")>-1)
            	{
            		sUser_NAME="[默认选中]"+sUser_NAME;
            	}else if(sDept_Name.indexOf("必选")>-1)
            	{
            		sUser_NAME="[必选]"+sUser_NAME;
            	}
            	boolean isshow = false;
            	for (int j = 0; j < sRole_ID.length; j++) {
            		if(isHaveRoleUser(sUser_ID, sRole_ID[j])){
            			isshow = true;
            			break;
            		}
            	}
            	if(true){
            		reStr += "<Node Id=\"" + sUser_ID + "\" UType=\"" + sUser_UTYPE + "\" UName=\"" + sUser_NAME + "\" fId=\"" + sDept_ID + "\" fName=\"" + sDept_Name + "\" gId=\"\" phone=\"\" topname=\"" + sTopName + "\">";
                    reStr += getRoleList(sUser_ID, sUser_NAME, sRole_ID, sRole_Name, sTopName);
                    reStr += "</Node>";
            	}
                
            }
        }
        return reStr;
    }

    // 判断人员是否具备改角色
    private boolean isRoleUser(String sUserID, String sRole_ID, String sDept_ID) {
        boolean returnValue = false;
        sRole_ID = "," + sRole_ID;
        for (int i = 0; i < this.iGrpsCount; i++) {
            if (sUserID.equals(this.arrGrpsList[i][G_GRPS.USER_ID.ordinal()]) && sRole_ID.indexOf("," + this.arrGrpsList[i][G_GRPS.GRP_ID.ordinal()]) != -1 && (sDept_ID == null || (null != sDept_ID && sDept_ID.equals(this.arrGrpsList[i][G_GRPS.DEPT_ID.ordinal()])))) {
                returnValue = true;
                break;
            }
        }
        return returnValue;
    }

    // 判断当前部门是否存在该角色人员
    private boolean isHaveRoleUser(String sDept_ID, String sRole_ID) {
        boolean returnValue = false;
        for (int i = 0; i < this.iGrpsCount; i++) {
            if (sDept_ID.equals(this.arrGrpsList[i][G_GRPS.DEPT_ID.ordinal()]) && sRole_ID.equals(this.arrGrpsList[i][G_GRPS.GRP_ID.ordinal()])) {
                returnValue = true;
                break;
            }
        }
        return returnValue;
    }

    private String getFlowDate() throws Exception {
        String _cmdStr = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
        _cmdStr = "select WF_TIMETYPE,WF_TIMESPAN from WFDEFINITION WHERE WF_ID=" + this.iWfID;
        String timetype = "0";
        String timespan = "0";
        //增加异常判断
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
            try{
        		if(dr.getString("WF_TIMETYPE")!=null&&!"".equals(dr.getString("WF_TIMETYPE")))
        		{
        			timetype = dr.getString("WF_TIMETYPE");
        		}
        		if(dr.getString("WF_TIMESPAN")!=null&&!"".equals(dr.getString("WF_TIMESPAN")))
        		{
        			timespan = dr.getString("WF_TIMESPAN");
        		}
        	}
        	catch (Exception e) {
				e.printStackTrace();
			}
            break;
        }
        return getEndTime(sdf.format(new Date()), Integer.parseInt(timespan), Integer.parseInt(timetype));
    }
    //获取文件的预警期限 杨龙修改 2013/6/24
    private String getYJQX() throws Exception {
        String _cmdStr = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
        _cmdStr = "select WF_YJTIMETYPE,WF_YJTIMESPAN from WFDEFINITION WHERE WF_ID=" + this.iWfID;
        String timetype = "0";
        String timespan = "0";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
        	try{
        		if(dr.getString("WF_YJTIMETYPE")!=null&&!"".equals(dr.getString("WF_YJTIMETYPE")))
        		{
        			timetype = dr.getString("WF_YJTIMETYPE");
        		}
        		if(dr.getString("WF_YJTIMESPAN")!=null&&!"".equals(dr.getString("WF_YJTIMESPAN")))
        		{
        			timespan = dr.getString("WF_YJTIMESPAN");
        		}
        	}
        	catch (Exception e) {
				e.printStackTrace();
			}
            break;
        }
        return getEndTime(sdf.format(new Date()), Integer.parseInt(timespan), Integer.parseInt(timetype));
    }

    // 根据已选用户生产用户列表(;:1:2282:节点2:投资处:王燚;:0:3081:检查大队:信访办:李四;:0:3074:检查大队:检查大队:张三)
    private void setNodeSelectUserList(Node td, String sUser) {
        String sHasUserList = "";
        Node temptd = (Node) td.clone();
        ((Element) td).clearContent();
        String sNodeID = td.valueOf("@ID");
      //如果是按照已知的后续节点初始化的流程，则发送时不按照流程节点索引比较，而是按照真实的节点ID比较，此处需要修改indexAction中接收的选人参数sustr传的节点ID为真实节点ID
        if(!"".equals(this.nextNodeIDs))
        {
        	sNodeID = td.valueOf("@NodeID");
        }
        String[] sUserList = sUser.split(";");
        String reltag = ID.get16bID().toString();  ///把reltag标示同一个角色发送的人员的字段独立出来，这样能解决不同部门同一个角色一起发送时判定是同一拨人的逻辑问题
        for (String s : sUserList) {
        	
        	if(ResourceManager.getAppKey("SeparateDeptRole", "0").equals("1")){
        		reltag = ID.get16bID().toString(); //如果设置了每个部门各自发送角色 那么这里RELTAG取各自的 umaydie 2017/3/1
        	}
        	
            if ("".equals(s)) {
                continue;
            }
            if (s.indexOf(":" + sNodeID + ":") == -1) {
                continue;
            }
            String sUserID = s.split(":")[2];
            String sDeptID = s.split(":")[6];
            String sBlqx = "";
            if(s.split(":").length>7){
            	sBlqx = s.split(":")[7];
            }

            Node usertd = getSelectUserbyUserID(temptd, sUserID, sDeptID , reltag,sBlqx);
            if (usertd != null) {
                if (((Element) usertd).elements().size() > 0) {  //getSelectUserbyUserID
                    // 私有小组
                    for (Object obj : usertd.selectNodes("Node")) {
                        Node subtd = (Node) obj;
                        if (sHasUserList.indexOf("," + subtd.valueOf("@Id") + ":" + subtd.valueOf("@fId")) == -1) {
                            ((Element) td).add((Element)subtd.clone());
                            sHasUserList += "," + subtd.valueOf("@Id") + ":" + subtd.valueOf("@fId");
                        }
                    }
                } else {
                    if (sHasUserList.indexOf("," + usertd.valueOf("@Id") + ":" + usertd.valueOf("@fId")) == -1) {
                        ((Element) td).add(usertd);
                        sHasUserList += "," + usertd.valueOf("@Id") + ":" + usertd.valueOf("@fId");
                    }
                }
            }
        }
    }
    
    

 // 通过USERID选取人员信息
    private Node getSelectUserbyUserID(Node td, String sUserID, String sDeptID) {
        Node returntd = null;
        for (Object obj : td.selectNodes("Node")) {
            Node subtd = (Node) obj;
            if ("0".equals(subtd.valueOf("@UType")) || "9".equals(subtd.valueOf("@UType"))) {
                if (sUserID.equals(subtd.valueOf("@Id")) && sDeptID.equals(subtd.valueOf("@fId"))) {
                    returntd = (Node) subtd.clone();
                    break;
                }
            }else if("3".equals(subtd.valueOf("@UType"))){  //处理角色的情况 2014.3.25 taolb
            	//sUserID 是
            	if (sUserID.equals(subtd.valueOf("@Id"))) { //根据角色查询
            		IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            		sqlDataSource = SysDataSource.getSysDataSource();
            		sqlDataSource.setParameter("ROLE_ID", sUserID);
            		sqlDataSource.setParameter("ORG_ID", sDeptID);
            		xsf.data.DataTable dt = sqlDataSource.query("getUsersByRole");
            		 if(dt != null){
            			 DataRowCollections roles = dt.getRows();
            			 returntd = (Node) subtd.clone();
            			 
            			 List<String> list = new ArrayList<String>();
            			String uuid = subtd.valueOf("@reltag")!=null&&!subtd.valueOf("@reltag").equals("")?subtd.valueOf("@reltag"):String.valueOf(ID.get16bID()); //
            			 for( xsf.data.DataRow role : roles){//角色转换人员
            				 Element newtd = (Element) subtd.clone();
            				 String user_id = role.getString("USER_ID") ;
            				 if(!list.contains(user_id)){
            					 newtd.selectSingleNode("@Id").setText(role.getString("USER_ID"));
                				 newtd.selectSingleNode("@UType").setText("0");
                				 newtd.selectSingleNode("@UName").setText(role.getString("USER_NAME"));
                				 newtd.selectSingleNode("@fId").setText(role.getString("DEPT_ID"));
                				 newtd.selectSingleNode("@fName").setText(role.getString("DEPT_NAME"));
                				 
                				 newtd.addAttribute("reltag", uuid);//标识同一角色
                				 
                				 ((Element) returntd).add(newtd);
                				 
                				 list.add(user_id) ;
            				 }
            			 }
            		 }
            	}
            	 break;
            }else if ("5".equals(subtd.valueOf("@UType"))) {  // 私有小组,统计局用
                if (sUserID.equals(subtd.valueOf("@Id"))) {
                    DataTable table = map.get("G_GRPS_PRI");
                    List<DataRow> myRows = table.select("grp_name", sUserID);
                    if (myRows.size() > 0) {
                        returntd = (Node) subtd.clone();
                        for (DataRow myRow : myRows) {
                            Node newtd = (Node) subtd.clone();
                            newtd.selectSingleNode("@Id").setText(myRow.get("user_id"));
                            newtd.selectSingleNode("@UType").setText(myRow.get("user_utype"));
                            newtd.selectSingleNode("@UName").setText(myRow.get("user_name"));
                            newtd.selectSingleNode("@fId").setText(myRow.get("dept_id"));
                            newtd.selectSingleNode("@fName").setText(myRow.get("dept_name"));
                            ((Element) returntd).add(newtd);
                        }
                    }
                    break;
                }
            }
            // 共有小组
            else if ("1".equals(subtd.valueOf("@UType")) && isGroupView) {
                if (sUserID.equals(subtd.valueOf("@Id"))) {
                    DataTable table = map.get("G_GRPS");
                    List<DataRow> myRows = table.select("GRP_ID", sUserID);
                    if (myRows.size() > 0) {
                        returntd = (Node) subtd.clone();
                        for (DataRow myRow : myRows) {
                            Node newtd = (Node) subtd.clone();
                            newtd.selectSingleNode("@Id").setText(myRow.get("user_id"));
                            newtd.selectSingleNode("@UType").setText(myRow.get("user_utype"));
                            newtd.selectSingleNode("@UName").setText(myRow.get("user_name"));
                            newtd.selectSingleNode("@fId").setText(myRow.get("dept_id"));
                            newtd.selectSingleNode("@fName").setText(myRow.get("dept_name"));
                            ((Element) returntd).add(newtd);
                        }
                    }
                    break;
                }
            } else {
                returntd = getSelectUserbyUserID(subtd, sUserID, sDeptID);
                if (returntd != null) {
                    break;
                }
            }
        }
        return returntd;
    }
    
    // 通过USERID选取人员信息
    private Node getSelectUserbyUserID(Node td, String sUserID, String sDeptID,String reltag) {
        Node returntd = null;
        for (Object obj : td.selectNodes("Node")) {
            Node subtd = (Node) obj;
            if ("0".equals(subtd.valueOf("@UType")) || "9".equals(subtd.valueOf("@UType"))) {
                if (sUserID.equals(subtd.valueOf("@Id")) && sDeptID.equals(subtd.valueOf("@fId"))) {
                    returntd = (Node) subtd.clone();
                    break;
                }
            }else if("3".equals(subtd.valueOf("@UType"))){  //处理角色的情况 2014.3.25 taolb
            	//sUserID 是
            	if (sUserID.equals(subtd.valueOf("@Id"))) { //根据角色查询
            		IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            		sqlDataSource = SysDataSource.getSysDataSource();
            		sqlDataSource.setParameter("ROLE_ID", sUserID);
            		sqlDataSource.setParameter("ORG_ID", sDeptID);
            		xsf.data.DataTable dt = sqlDataSource.query("getUsersByRole");
            		 if(dt != null){
            			 DataRowCollections roles = dt.getRows();
            			 returntd = (Node) subtd.clone();
            			 
            			 List<String> list = new ArrayList<String>();
            			String uuid = subtd.valueOf("@reltag")!=null&&!subtd.valueOf("@reltag").equals("")?subtd.valueOf("@reltag"):reltag; //
            			 for( xsf.data.DataRow role : roles){//角色转换人员
            				 Element newtd = (Element) subtd.clone();
            				 String user_id = role.getString("USER_ID") ;
            				 if(!list.contains(user_id)){
            					 newtd.selectSingleNode("@Id").setText(role.getString("USER_ID"));
                				 newtd.selectSingleNode("@UType").setText("0");
                				 newtd.selectSingleNode("@UName").setText(role.getString("USER_NAME"));
                				 newtd.selectSingleNode("@fId").setText(role.getString("DEPT_ID"));
                				 newtd.selectSingleNode("@fName").setText(role.getString("DEPT_NAME"));
                				 
                				 newtd.addAttribute("reltag", uuid);//标识同一角色
                				 
                				 ((Element) returntd).add(newtd);
                				 
                				 list.add(user_id) ;
            				 }
            			 }
            		 }
            	}
            	 break;
            }else if ("5".equals(subtd.valueOf("@UType"))) {  // 私有小组,统计局用
                if (sUserID.equals(subtd.valueOf("@Id"))) {
                    DataTable table = map.get("G_GRPS_PRI");
                    List<DataRow> myRows = table.select("grp_name", sUserID);
                    if (myRows.size() > 0) {
                        returntd = (Node) subtd.clone();
                        for (DataRow myRow : myRows) {
                            Node newtd = (Node) subtd.clone();
                            newtd.selectSingleNode("@Id").setText(myRow.get("user_id"));
                            newtd.selectSingleNode("@UType").setText(myRow.get("user_utype"));
                            newtd.selectSingleNode("@UName").setText(myRow.get("user_name"));
                            newtd.selectSingleNode("@fId").setText(myRow.get("dept_id"));
                            newtd.selectSingleNode("@fName").setText(myRow.get("dept_name"));
                            ((Element) returntd).add(newtd);
                        }
                    }
                    break;
                }
            }
            // 共有小组
            else if ("1".equals(subtd.valueOf("@UType")) && isGroupView) {
                if (sUserID.equals(subtd.valueOf("@Id"))) {
                    DataTable table = map.get("G_GRPS");
                    List<DataRow> myRows = table.select("GRP_ID", sUserID);
                    if (myRows.size() > 0) {
                        returntd = (Node) subtd.clone();
                        for (DataRow myRow : myRows) {
                            Node newtd = (Node) subtd.clone();
                            newtd.selectSingleNode("@Id").setText(myRow.get("user_id"));
                            newtd.selectSingleNode("@UType").setText(myRow.get("user_utype"));
                            newtd.selectSingleNode("@UName").setText(myRow.get("user_name"));
                            newtd.selectSingleNode("@fId").setText(myRow.get("dept_id"));
                            newtd.selectSingleNode("@fName").setText(myRow.get("dept_name"));
                            ((Element) returntd).add(newtd);
                        }
                    }
                    break;
                }
            } else {
                returntd = getSelectUserbyUserID(subtd, sUserID, sDeptID,reltag);
                if (returntd != null) {
                    break;
                }
            }
        }
        return returntd;
    }
    
    // 通过USERID选取人员信息,加办理期限的重构
    private Node getSelectUserbyUserID(Node td, String sUserID, String sDeptID,String reltag,String blqx) {
        Node returntd = null;
        for (Object obj : td.selectNodes("Node")) {
            Node subtd = (Node) obj;
            if ("0".equals(subtd.valueOf("@UType")) || "9".equals(subtd.valueOf("@UType"))) {
                if (sUserID.equals(subtd.valueOf("@Id")) && sDeptID.equals(subtd.valueOf("@fId"))) {
                	try{
                		subtd.selectSingleNode("@Blqx").setText(blqx);
                	}catch(Exception e){
                		System.out.println("no blqx support!");
                	}
                    returntd = (Node) subtd.clone();
                    break;
                }
            }else if("3".equals(subtd.valueOf("@UType"))){  //处理角色的情况 2014.3.25 taolb
            	//sUserID 是
            	if (sUserID.equals(subtd.valueOf("@Id"))) { //根据角色查询
            		IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            		sqlDataSource = SysDataSource.getSysDataSource();
            		sqlDataSource.setParameter("ROLE_ID", sUserID);
            		sqlDataSource.setParameter("ORG_ID", sDeptID);
            		xsf.data.DataTable dt = sqlDataSource.query("getUsersByRole");
            		 if(dt != null){
            			 DataRowCollections roles = dt.getRows();
            			 returntd = (Node) subtd.clone();
            			 
            			 List<String> list = new ArrayList<String>();
            			String uuid = subtd.valueOf("@reltag")!=null&&!subtd.valueOf("@reltag").equals("")?subtd.valueOf("@reltag"):reltag; //
            			 for( xsf.data.DataRow role : roles){//角色转换人员
            				 Element newtd = (Element) subtd.clone();
            				 String user_id = role.getString("USER_ID") ;
            				 if(!list.contains(user_id)){
            					 newtd.selectSingleNode("@Id").setText(role.getString("USER_ID"));
                				 newtd.selectSingleNode("@UType").setText("0");
                				 newtd.selectSingleNode("@UName").setText(role.getString("USER_NAME"));
                				 newtd.selectSingleNode("@fId").setText(role.getString("DEPT_ID"));
                				 newtd.selectSingleNode("@fName").setText(role.getString("DEPT_NAME"));
                				 
                				 newtd.addAttribute("reltag", uuid);//标识同一角色
                				 
                				 ((Element) returntd).add(newtd);
                				 
                				 list.add(user_id) ;
            				 }
            			 }
            		 }
            	}
            	 break;
            }else if ("5".equals(subtd.valueOf("@UType"))) {  // 私有小组,统计局用
                if (sUserID.equals(subtd.valueOf("@Id"))) {
                    DataTable table = map.get("G_GRPS_PRI");
                    List<DataRow> myRows = table.select("grp_name", sUserID);
                    if (myRows.size() > 0) {
                        returntd = (Node) subtd.clone();
                        for (DataRow myRow : myRows) {
                            Node newtd = (Node) subtd.clone();
                            newtd.selectSingleNode("@Id").setText(myRow.get("user_id"));
                            newtd.selectSingleNode("@UType").setText(myRow.get("user_utype"));
                            newtd.selectSingleNode("@UName").setText(myRow.get("user_name"));
                            newtd.selectSingleNode("@fId").setText(myRow.get("dept_id"));
                            newtd.selectSingleNode("@fName").setText(myRow.get("dept_name"));
                            ((Element) returntd).add(newtd);
                        }
                    }
                    break;
                }
            }
            // 共有小组
            else if ("1".equals(subtd.valueOf("@UType")) && isGroupView) {
                if (sUserID.equals(subtd.valueOf("@Id"))) {
                    DataTable table = map.get("G_GRPS");
                    List<DataRow> myRows = table.select("GRP_ID", sUserID);
                    if (myRows.size() > 0) {
                        returntd = (Node) subtd.clone();
                        for (DataRow myRow : myRows) {
                            Node newtd = (Node) subtd.clone();
                            newtd.selectSingleNode("@Id").setText(myRow.get("user_id"));
                            newtd.selectSingleNode("@UType").setText(myRow.get("user_utype"));
                            newtd.selectSingleNode("@UName").setText(myRow.get("user_name"));
                            newtd.selectSingleNode("@fId").setText(myRow.get("dept_id"));
                            newtd.selectSingleNode("@fName").setText(myRow.get("dept_name"));
                            ((Element) returntd).add(newtd);
                        }
                    }
                    break;
                }
            } else {
                returntd = getSelectUserbyUserID(subtd, sUserID, sDeptID,reltag,blqx);
                if (returntd != null) {
                    break;
                }
            }
        }
        return returntd;
    }

    // 一般发送
    private void send_0() throws Exception {
        Date now = new Date();
        Map<String, String> mails = new HashMap<String, String>();
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        List<String> httpTask = new ArrayList<String>();
        String receiveUserIds = "";
		String receiveUserNames = "";
        boolean isDraft = false;
        try {
        	
            
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
           // System.out.println("send_0 事务隔离级别:" + _myConn.getTransactionIsolation() + "    自动提交：" + _myConn.getAutoCommit());
            _myConn.setAutoCommit(false);
            _myRead = _myConn.createStatement();
            // // 查找所有需要提醒的人员
            // _cmdStr = "SELECT DISTINCT C.ID,C.UNAME FROM G_PNODES
            // A,WFNODELIST B,G_USERS C WHERE A.WF_ID=B.WF_ID AND
            // A.WFNODE_ID=B.WFNODE_ID AND A.USER_ID=C.ID AND A.INFO_ID="
            // + iInfoID + " AND B.RECIEVEMESSAGE=1";
            // // M_Dapt = new OleDbDataAdapter(_cmdStr, _myConn);
            // // M_Dapt.Fill(M_Dset, "UID");
            // DataTable AclTable = new DataTable("UID");
            // DataRow tr = null;
            // _myDs = _myRead.executeQuery(_cmdStr);
            // while (_myDs.next()) {
            // tr = AclTable.NewRow();
            // tr.put("ID", _myDs.getString("ID"));
            // tr.put("UNAME", _myDs.getString("UNAME"));
            // AclTable.rows.add(tr);
            // }
            // map.put(AclTable.getTablename(), AclTable);
            String DBMS = ConfigurationSettings.AppSettings("DBMS");
            // String msgServer = ConfigurationSettings.AppSettings("消息服务地址");
            // ---------------------------------------------------------------------------------------------------------------------------- 起始节点初始化
            if (this.iPID == 0) {
                // 插入G_PNODES
                this.iPID = this.getMaxValue("G_PID", 1);
                this.iPnID = 1;
                isDraft = true;
                // _cmdStr = "INSERT INTO G_PNODES ";
                // _cmdStr += "(PID,ID,FID,INFO_ID,DEPT_ID,USER_ID,UTYPE,TIMESPAN,TIMETYPE,DAYS,";
                // _cmdStr += "RDATE,PDATE,EDATE,HANDLEWAY,STATUS,FSTATUS,WHOHANDLE,SIGNED,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,WAITCOUNT,MUSER_ID,UNAME) ";
                // _cmdStr += "VALUES (" + this.iPID + ",1,0," + this.iInfoID + "," + this.getDetpIDFromUserID(this.iUserID) + "," + this.iUserID + ",0,0,0,0,";
                // _cmdStr += processDate("NOW,NOW,NULL,", DBMS, now);
                // _cmdStr += "0,-1,-1," + this.iUserID + ",1,'" + this.sCaption + "'," + this.iWfID + "," + this.iWfNodeID + ",'" + this.sCaption + "',0,0,0," + this.iUserID + ",'" + this.getUserName(this.iUserID) + "')";
                // _myRead.executeUpdate(_cmdStr);
                Map<String, String> data = new HashMap<String, String>();
                data.put("PID", String.valueOf(this.iPID));
                data.put("ID", "1");
                data.put("FID", "0");
                data.put("INFO_ID", String.valueOf(this.iInfoID));
                data.put("DEPT_ID", this.getDraftDept(this.iInfoID));
                data.put("USER_ID", String.valueOf(this.iUserID));
                data.put("UTYPE", "0");
                data.put("UNAME", this.getUserName(this.iUserID));
                //data.put("MDEPT_ID", "NULL");// *************************************************
                //data.put("MUSER_ID", "NULL");
                //环保部  吕晓丹  2013-02-26-----start----
                data.put("MDEPT_ID", this.getDraftDept(this.iInfoID));// *************************************************
                data.put("MUSER_ID", String.valueOf(this.iUserID));
                //环保部  吕晓丹  2013-02-26-----end----
                data.put("STATUS", "-1");
                data.put("ACTNAME", this.sCaption);
                data.put("WF_ID", String.valueOf(this.iWfID));
                data.put("WFNODE_ID", String.valueOf(this.iWfNodeID));
                data.put("WFNODE_CAPTION", this.sCaption);
                data.put("WFNODE_WAIT", "0");
                data.put("SENDTYPE", "0");
                data.put("RDATE", processDate("NOW", DBMS, now));
                data.put("PDATE", processDate("NOW", DBMS, now));
                data.put("SDATE", "NULL");
                data.put("EDATE", "NULL");
                data.put("TIMESPAN", "0");
                data.put("TIMETYPE", "0");
                data.put("DAYS", "0");
                data.put("WHOHANDLE", String.valueOf(this.iUserID));
                data.put("HANDLEWAY", "0");
                data.put("WAITCOUNT", "0");
                data.put("ISZNG", "NULL");
                data.put("FSTATUS", "-1");
                data.put("PARENTFLOWPID", "0");
                data.put("PARENTFLOWPNID", "0");
                data.put("SIGNED", "1");
                data.put("ATYPE", "NULL");
                //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9------------开始
                data.put("WFLINE_TYPE", "0");
                data.put("ISCS", "0");
				data.put("mainUnit", this.MainUnit);
                //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------结束
                insertGPnodes(_myConn, data);
                // 插入G_INBOX
                long Start_G_INBOX_ID = this.getMaxValue("G_INBOX", 1);
                // _cmdStr = "INSERT INTO G_INBOX ";
                // _cmdStr += "(ID,INFO_ID,DEPT_ID,USER_ID,FUSER_ID,UTYPE,HANDLEWAY,PID,PNID,";
                // _cmdStr += "STATUS,PRIORY,RDATE,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,";
                // _cmdStr += "TIMETYPE,TIMESPAN,DAYS,SENDTYPE,WAITCOUNT,FNODE,EDATE,ISZNG";
                // if (this.strPriSend.indexOf("," + this.iUserID + ",") == -1) {
                // _cmdStr += ") ";
                // } else {
                // _cmdStr += ",NEEDPRI) ";
                // }
                // _cmdStr += "VALUES ";
                // _cmdStr += "(" + Start_G_INBOX_ID + "," + this.iInfoID + ",0," + this.iUserID + "," + this.iUserID + ",0,0," + this.iPID + ",1,1,0,";
                // _cmdStr += processDate("NOW,", DBMS, now);
                // _cmdStr += "'" + this.sCaption + "'," + this.iWfID + "," + this.iWfNodeID + ",'" + this.sCaption + "',0,0,0,0,0,0,'',null,0";
                //                
                if (this.strPriSend.indexOf("," + this.iUserID + ",") == -1) {
                    // _cmdStr += ")";
                } else {
                    // _cmdStr += ",1)";
                    SystemLog.SaveLog("文件操作", "越权发送", "1", this.iUserID, data.get("UNAME"), this.strIP, this.strMAC, this.strBT, "1");
                }
                // _myRead.executeUpdate(_cmdStr);
                data.put("ID", String.valueOf(Start_G_INBOX_ID));
                // data.put("PID", null);
                data.put("PNID", "1");
                // data.put("INFO_ID", String.valueOf(this.iInfoID));
                // data.put("DEPT_ID", "0");
                // data.put("USER_ID", String.valueOf(this.iUserID));
                data.put("FUSER_ID", String.valueOf(this.iUserID));// ??????????????????????
                // data.put("UTYPE", "0");
                data.put("STATUS", "1");
                // data.put("ACTNAME", null);
                // data.put("WF_ID", null);
                // data.put("WFNODE_ID", null);
                // data.put("WFNODE_CAPTION", null);
                // data.put("WFNODE_WAIT", null);
                // data.put("SENDTYPE", null);
                // data.put("RDATE", null);
                // data.put("EDATE", null);
                // data.put("TIMESPAN", null);
                // data.put("TIMETYPE", null);
                // data.put("DAYS", null);
                // data.put("HANDLEWAY", null);
                // data.put("WAITCOUNT", null);
                // data.put("ISZNG", null);
                data.put("FNODE", "NULL");
                data.put("PRIORY", "0");
                // data.put("NEEDPRI", this.strPriSend.indexOf("," + this.iUserID + ",") == -1 ? "NULL" : "1");// ??????????????????????
              //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                data.put("RECEIVE_USERNAME", this.getUserName(this.iUserID));
                data.put("SEND_USERNAME", this.getUserName(this.iUserID));
              //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                data.put("TIP", this.tip);
				insertGInbox(_myConn, data);
                // 吴红亮 添加 开始
                if (dsoap.tools.ConfigurationSettings.isDraftOpinion) {
                    _cmdStr = "UPDATE G_OPINION SET PID=" + this.iPID + ",STATUS=1 WHERE PID=0 AND PNID=1 AND ID=" + this.iInfoID;
                    _myRead.executeUpdate(_cmdStr);
                    //宋俊修改 此处代码需要G_OPINION_ATTACH有INFO_ID支持 并且业务逻辑 需要ezweb前端支持 2012/12/28开始
                    try{
                    /******* 意见附件 add by john.he 20121207 ***********/
                    _cmdStr = "UPDATE G_OPINION_ATTACH SET PID=" + this.iPID + " WHERE PID=0 AND PNID=1 AND INFO_ID=" + this.iInfoID;
                    _myRead.executeUpdate(_cmdStr);
                    }
                    catch (Exception e) {
						e.printStackTrace();
					}
                  //宋俊修改 此处代码需要G_OPINION_ATTACH有INFO_ID支持 并且业务逻辑 需要ezweb前端支持 2012/12/28结束
                }
                doCommand(getSqlFromAfterSendSql(getFirstAfter(_myRead)), _myRead, httpTask);
                // 吴红亮 添加 结束
            }
            // ------------------------------------------------------------------------------------存在性校验
            if (this.iSendType != 3 && !this.isExtraSend && !this.redirectMode.equals("4")) {// 不是增发时要做校验
                _cmdStr = "SELECT ID FROM G_INBOX WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
                _myRead.execute(_cmdStr);
                _myDs = _myRead.getResultSet();
                if (!_myDs.next()) {
                    throw new Exception("该文件已被处理!");
                }
            }
            // ------------------------------------------------------------------------------------处理上一节点的数据G_INFOS，G_OPINION，G_PNODES，G_PROUTE，G_INBOX
            //增加预警期限的判断 杨龙修改 2013/6/24
            String strYJQX=this.getYJQX();
            if(!"".equals(strYJQX))
            {
            	try {
            		if ("0".equals(strYJQX)) {
            			strYJQX = "YJQX";
                    } else {
                        if ("ORACLE".equals(DBMS)) {
                        	strYJQX = "TO_DATE('" + strYJQX + "','YYYY-MM-DD HH24:MI')";
                        } else {
                        	strYJQX = "'" + strYJQX + "'";
                        }
                        _cmdStr = "UPDATE G_INFOS SET YJQX=" + strYJQX + " WHERE ID=" + this.iInfoID + " AND YJQX IS NULL";
                        _myRead.executeUpdate(_cmdStr);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
            String s = this.getFlowDate();
            if ("0".equals(s)) {
                s = "BLQX";
            } else {
                if ("ORACLE".equals(DBMS)) {
                    s = "TO_DATE('" + s + "','YYYY-MM-DD HH24:MI')";
                } else {
                    s = "'" + s + "'";
                }
                _cmdStr = "UPDATE G_INFOS SET BLQX=" + s + " WHERE ID=" + this.iInfoID + " AND BLQX IS NULL";
                _myRead.executeUpdate(_cmdStr);
            }
            // 设文件为流转状态，for tjj，统计局程批件，起始发送节点没有设置该状态
            _cmdStr = "UPDATE G_INFOS SET STATUS=1 WHERE ID=" + this.iInfoID;
            _myRead.executeUpdate(_cmdStr);
            // 吴红亮 添加 开始
            // String owner = "";
            int count = 0;
            // 吴红亮 添加 结束
            if (this.iActType == 3) {// 征询，不处理当前节点
                ;
            } else {// 处理当前节点
                // G_PNODES表只存委托人的名字UNAME字段
            	String muserid=getMUserInfo(this.iPID, this.iPnID, this.iRUserID);
            	String PROXYSTATUS="0";
            	if(muserid.equals(this.iUserID)){
            		PROXYSTATUS="1";
            	}
                if (this.iSendType == 3 || this.redirectMode.equals("4")) {// 增发时
                	if(this.redirectMode.equals("1")){ //转交模式上个节点取最新的待办
                		 this.iPnID = this.getMaxPNID1(_myRead, this.iPID);
                	}
                    _cmdStr = "UPDATE G_OPINION SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID<>1 AND STATUS<>1";
                    _myRead.executeUpdate(_cmdStr);
                    //处理流转信息
                    if(this.redirectMode.equals("1")){ //转交模式改变所有流程信息，置为已处理
                    	_cmdStr = "UPDATE G_PNODES SET STATUS=-1,MUSER_ID=USER_ID,MDEPT_ID=DEPT_ID," + processDate("SDATE=NOW,PDATE=NOW", DBMS, now) + ",ISHIDEYJ=" + this.isHideYJ + " WHERE PID=" + this.iPID + " AND STATUS<>-1";
                        _myRead.executeUpdate(_cmdStr);
                    }else if(this.redirectMode.equals("2")){//重定向特送模式类似正常发送操作G_PNODES表
                    	_cmdStr = "UPDATE G_PNODES SET STATUS=-1,MUSER_ID=" + getMUserInfo(this.iPID, this.iPnID, this.iRUserID) + ",USER_ID=" + this.iUserID + ",DEPT_ID=" + getSenderDeptId(_myRead, this.iUserID, this.iPID, this.iPnID) + ",MDEPT_ID=DEPT_ID," + processDate("SDATE=NOW,PDATE=NOW", DBMS, now) + ",ISHIDEYJ=" + this.isHideYJ+",PROXYSTATUS="+PROXYSTATUS+" WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
                        _myRead.executeUpdate(_cmdStr);
                    }else if(this.redirectMode.equals("3")){//增发模式不处理当前G_PNODES
                    	
                    }
                    /*_cmdStr = "UPDATE G_PROUTE SET STATUS=-1 WHERE PID=" + this.iPID + " AND STATUS<>-1";
                    _myRead.executeUpdate(_cmdStr);*/
                    //处理待办信息
                    if(this.redirectMode.equals("1")){ //转交模式 删除所有待办
                    	 _cmdStr = "DELETE FROM G_INBOX WHERE PID=" + this.iPID;
                         _myRead.executeUpdate(_cmdStr);
                    }else if(this.redirectMode.equals("2")){//重定向特送模式 删除当前待办
                    	_cmdStr = "DELETE FROM G_INBOX WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
                    	_myRead.executeUpdate(_cmdStr);
                    }else if(this.redirectMode.equals("3")){//增发模式不处理待办
                    	
                    }
                   
                    _cmdStr = "UPDATE G_OPINION SET PID=" + this.iPID + ",PNID=" + this.iPnID + ",ID=1 WHERE PID=0 AND PNID=0 AND ID=" + +this.iInfoID;
                    _myRead.executeUpdate(_cmdStr);
                } else {
					if(this.isSendBack){
						if(StringHelper.isNotNullAndEmpty(this.sendBackPnid)){
							_cmdStr = "UPDATE G_PNODES set ISRETURN=1 where id>="+this.sendBackPnid+" and PID=" + this.iPID;
		                    _myRead.executeUpdate(_cmdStr);
						}
					}
                    _cmdStr = "UPDATE G_PNODES SET STATUS=-1,MUSER_ID=" + getMUserInfo(this.iPID, this.iPnID, this.iRUserID) + ",USER_ID=" + this.iUserID + ",DEPT_ID=" + getSenderDeptId(_myRead, this.iUserID, this.iPID, this.iPnID) + ",MDEPT_ID=DEPT_ID," + processDate("SDATE=NOW,PDATE=NOW", DBMS, now) + ",ISHIDEYJ=" + this.isHideYJ+",PROXYSTATUS="+PROXYSTATUS+" WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
                    _myRead.executeUpdate(_cmdStr);
                    _cmdStr = "UPDATE G_PROUTE SET STATUS=-1 WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
                    _myRead.executeUpdate(_cmdStr);
                    // _cmdStr = "UPDATE G_OPINION SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
                    // _myRead.executeUpdate(_cmdStr);
                    // 吴红亮 添加 开始
                    _cmdStr = "UPDATE G_OPINION SET STATUS=1,ORGNAME=(select NAME from G_ORGANIZE where ID=(select DEPT_ID from G_PNODES where PID=" + this.iPID + " AND ID=" + this.iPnID + ")) WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
                    _myRead.executeUpdate(_cmdStr);
//                    _cmdStr = "DELETE FROM G_OPINION WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID + " AND USERID<>" + this.iUserID;
//                    _myRead.executeUpdate(_cmdStr);
                    // owner = getGInboxUser(_myRead);
                    // 吴红亮 添加 结束
                    
                    
                    /**
                     * 特殊处理 2014.3.26 taolb 
                     */
                  //查询RELTAG 标识  ----------------------开始
                   StringBuilder sql = new StringBuilder();
                   sql.append("select RELTAG from G_INBOX where PID = ").append(this.iPID).append(" and PNID = ").append(this.iPnID) ;
                   String relTag = DBManager.getFieldStringValue(sql.toString()) ;
                   if(!StringUtils.isEmpty(relTag)){
                	     sql.setLength(0);
	               		 sql.append("insert into G_LOSTINBOX(PID,USER_ID,PNID,INFO_ID,WFNODE_CAPTION,ACTNAME,MUSER_ID) ");
	               		 sql.append("select PID,USER_ID,ID,INFO_ID,WFNODE_CAPTION,ACTNAME,MUSER_ID from g_inbox where reltag= ").append(relTag).append(" and PID = ").append(this.iPID).append(" and pnid <> ").append(this.iPnID);
	               		 DBManager.execute(new Sql(sql.toString()));
                	   
	               		 sql.setLength(0) ;
	               		 sql.append("delete from G_INBOX where RELTAG = ").append(relTag) ;
	               		 _myRead.executeUpdate(sql.toString());
	               		 
	               		 sql.setLength(0) ;
	               		 sql.append("delete from G_PNODES where RELTAG = ").append(relTag).append(" and PID = ").append(this.iPID).append(" and ID <> ").append(this.iPnID) ;
	               		 _myRead.executeUpdate(sql.toString());
               	   }else{
               		   _cmdStr = "DELETE FROM G_INBOX WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
               		   _myRead.executeUpdate(_cmdStr);
               	   }
               	   //查询RELTAG 标识 ---------------------- 结束
                }
            }
            // 增发
            if (this.iSendType == 3) {
                _cmdStr = "UPDATE G_INFOS SET STATUS=1 WHERE ID=" + this.iInfoID;
                _myRead.executeUpdate(_cmdStr);
            }
          //抢办增加部门过滤  杨龙修改 2012/8/9--------------开始
            // 抢办
            _cmdStr = "select C.ID from";
            _cmdStr += "(select PID,FID,WF_ID,WFNODE_ID,DEPT_ID from G_PNODES where PID=" + this.iPID + " and ID=" + this.iPnID + ")A ";
            _cmdStr += "inner join";
            _cmdStr += "(select WF_ID,WFNODE_ID from WFNODELIST where WFNODE_NODETYPE=8)B on A.WF_ID=B.WF_ID and A.WFNODE_ID=B.WFNODE_ID ";
            _cmdStr += "left join";
          //抢办节点 所有的部门抢办，不单独每个部门抢办
            //_cmdStr += "(select PID,ID,FID,DEPT_ID from G_PNODES where PID=" + this.iPID + ")C on C.PID=A.PID and C.FID=A.FID and C.DEPT_ID=A.DEPT_ID";
            _cmdStr += "(select PID,ID,FID,DEPT_ID from G_PNODES where PID=" + this.iPID + ")C on C.PID=A.PID and C.FID=A.FID ";
            _myRead.execute(_cmdStr);
            _myDs = _myRead.getResultSet();
            String ids = "";
            while (_myDs.next()) {
                ids += _myDs.getString("ID") + ",";
            }
            _myDs.close();
           /* if (false) {
                ids = ids.substring(0, ids.length() - 1);
                _cmdStr = "update G_PNODES set STATUS=-1,SDATE=" + processDate("NOW", DBMS, now) + " where PID=" + this.iPID + " and ID in(" + ids + ")";
                _myRead.executeUpdate(_cmdStr);
                _cmdStr = "delete from G_INBOX where PID=" + this.iPID + " and PNID in(" + ids + ")";
                _myRead.executeUpdate(_cmdStr);
            }*/
          //抢办增加部门过滤  杨龙修改 2012/8/9--------------结束
            // ---------------------------------------------------------------------------------------------------------------------------- 发送新数据
            // 获取发送人数：
            Node nodes = this.SendUserListXml.selectSingleNode("Nodes");
            int iUserCount = Integer.parseInt(nodes.valueOf("@Count"));
            // 获取文件流转状态
            // int G_INFOS_STATUS = 1;
            // _cmdStr = "SELECT STATUS,BT FROM G_INFOS WHERE ID = " + this.iInfoID;
            // _myDs = _myRead.executeQuery(_cmdStr);
            // if (_myDs.next()) {
            // G_INFOS_STATUS = _myDs.getInt("STATUS");
            // strBt = _myDs.getString("BT");
            // }
            // _myDs.close();
            // 获取收件箱的Id的最大值
            long G_INBOX_ID = this.getMaxValue("G_INBOX", iUserCount);
            // 查询最大的Pnid
            long G_PNID = this.getMaxPNID(_myRead, this.iPID) + 1;
            // 获取发送人员信息
            // String strUname = "";
            // String strMaindept = "0";
            // _cmdStr = "SELECT UNAME,MAINCODE FROM G_USERS WHERE ID = " + this.iUserID;
            // _myDs = _myRead.executeQuery(_cmdStr);
            // if (_myDs.next()) {
            // strUname = _myDs.getString("UNAME");
            // strMaindept = _myDs.getString("MAINCODE");
            // }
            // _myDs.close();
            // String strSmsContent = "";
            //判断发送的节点是否全是抄送节点 杨龙修改 2012/9/11 开始
            //非抄送节点的个数 如果为0则说明全部为抄送节点
            int notCS=0;
          //判断发送的节点是否全是抄送节点 杨龙修改 2012/9/11 结束
            
          /*******************根据流程ID获取流程消息内容 消息提交开始 liuyang 2014/8/7*****************************/
            //根据流程ID获取流程定义表（wfdefinition）里wf_xml字段中节点的消息标题和内容
            
            String content = "";
            
            //标题和内容赋值的异常校验
            try {
            	
            	String wf_ID = String.valueOf(this.iWfID);
            	
                //从流程定义表wfdefinition的wf_xml字段中取出配置的消息
                String xml = MessageParser.getXmlFromWfdefinitionTable(wf_ID);
                
                //格式化xml
                String formatXml = MessageParser.formatXML(xml);
                
                String sql = "";
                
                //获取sql语句
                try {
                	
                	sql = MessageParser.getBodyFromXml(formatXml, "/Root/SMS/Sql");
                	
				} catch (DocumentException e) {
					
					throw new RuntimeException(e);
					
				}
                
                String sql_ = "";
                
                //sql非空校验
                if(sql!=null && !"".equals(sql)){
                	
                	sql_ = getSqlFromAfterSendSql(sql);//将sql中的占位符，如[INFO_ID],替换成现有的参数
                	 
                }
               
                try {
                	
                	content = MessageParser.getMessage(formatXml,sql_);
                	
				} catch (Exception e) {
					
					throw new RuntimeException(e);
					
				}
                 
			} catch (Exception e) {
				
				e.printStackTrace();
				
			} 
            
          //记录已经发送过的人
            Set<String> listReceiveUsers = new HashSet<String>();
         /************************消息提交结束 2014/8/7************************/
            for (Object obj : nodes.selectNodes("Node")) {// 遍历目标流程节点
                Node nextNode = (Node) obj;
                //System.out.println("-----------------" + nextNode.asXML());
                String sNodeDate = nextNode.valueOf("@NodeDate");
                String sIsZNG = nextNode.valueOf("@IsZNG");
                String nodeCaption = nextNode.valueOf("@NodeCaption");
                String timeSpan = nextNode.valueOf("@TimeSpan");
                String timeType = nextNode.valueOf("@TimeType");
                String wfNodeId = nextNode.valueOf("@NodeID");
                String nodeType = nextNode.valueOf("@NodeType");
                String subFlowID = nextNode.valueOf("@SubFlowID");
                String lineCaption = nextNode.valueOf("@LineCaption");
                String isNeedBack = nextNode.valueOf("@Selected");
              //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------开始
                String WFLINE_TYPE = nextNode.valueOf("@LineType");
                //是否是汇合节点
                String gatherNode = nextNode.valueOf("@GatherNode");
              //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------结束
                //批量处理时有问题---开始
                //int iSendMethod = Integer.parseInt(nextNode.valueOf("@SendMethod"));
                //批量处理时有问题---结束
                // String FileTranServiceXML = nextNode.valueOf("@FileTranServiceXML");
                lineCaption = lineCaption == null || "".equals(lineCaption) ? nodeCaption : lineCaption;
                boolean isStartCX = true;
                String isSMS = nextNode.valueOf("@IsSMS");
               //发送前的处理语句 杨龙修改  2012/9/4  开始
                String strSql_WFNODE_BEFORESENDSQL="select WFNODE_BEFORESENDSQL From wfnodelist where wf_id="+this.iWfID+" and wfnode_id="+this.iWfNodeID+"";
                ResultSet rs_WFNODE_BEFORESENDSQL=_myRead.executeQuery(strSql_WFNODE_BEFORESENDSQL);
                if(rs_WFNODE_BEFORESENDSQL.next())
                {
                	String strBEFORESENDSQL=rs_WFNODE_BEFORESENDSQL.getString(1)==null?"":rs_WFNODE_BEFORESENDSQL.getString(1);
                	if(!"".equals(strBEFORESENDSQL))
                	{
                		   doCommand(getSqlFromAfterSendSql(strBEFORESENDSQL), _myRead, httpTask);
                	}
                }
              //发送前的处理语句 杨龙修改  2012/9/4  结束
                
                /*******************此处获取：接收节点的ID：wfNodeId，根据节点ID可以查询到是否需要消息提醒 消息提交开始  liuyang 2014/8/7*****************************/
                //接收节点的ID
//                String toNodeID=wfNodeId;
                
//                String xml_ = "";
//                try {
//                	 //根据接收节点ID，查出在该节点配置的提醒方式，如短信通知、邮件通知、托盘提醒
//                     xml_ = MessageParser.getXmlFromWfnodelist(String.valueOf(this.iWfID), toNodeID);
//                     
//				} catch (Exception e) {
//					
//					throw new RuntimeException(e);
//					
//				}
//               
//                String typeXml = MessageParser.formatXML(xml_);
//                
//                String type = "";
//                
//                try {
//                	
//                	type = MessageParser.getTypeFromXml(typeXml);
//                	
//				} catch (DocumentException e) {
//					
//					throw new RuntimeException(e); 
//				} 
//                
//                String textType = ""; 
//                
//                String emailType = "";
//                                
//                String trayType = "";
//                
//                //type非空校验,types长度校验
//                if(type!=null && !"".equals(type)){
//                	
//                	String[] types = type.split(",");
//                	
//                	textType = types[0];
//                    
//                    emailType = types[1];
//                    
//                    trayType = types[2];
//                } 
//                
//                boolean isForword = false;
//                
//                boolean isMail = false;
//                
//                boolean isTray = false;
//                
//                if("1".equals(textType)){
//  
//                	isForword = true;
//                	
//                }
//                
//                if("1".equals(emailType)){
//                	  
//                	isMail = true;
//                	
//                }
//                
//                if("1".equals(trayType)){
//              	  
//                	isTray = true;
//                	
//                } 
                /************************消息提交结束 2014/8/7************************/
                String supreltag="";
                if(nodeType.equals("8")){
                	supreltag = ID.get16bID().toString();
                }
                for (Object obj1 : nextNode.selectNodes("Node")) {// 遍历目标流程节点中的所选接收人
                    Node userNode = (Node) obj1;
                    String uType = userNode.valueOf("@UType");
                    String fId = userNode.valueOf("@fId");
                    String id = userNode.valueOf("@Id");
                    String uName = userNode.valueOf("@UName");
                    String blqx = userNode.valueOf("@Blqx");
                    //发角色时多条记录之间的关联ID,如果有存，没有存空  2014.3.26 taolb 开始----------
                    String reltag = userNode.valueOf("@reltag") ;
                    if(StringHelper.isNotNullAndEmpty(supreltag)){
                    	reltag = supreltag;
                    }
                    if(reltag == null){
                    	reltag = "" ;
                    }
                   //发角色时多条记录之间的关联ID,如果有存，没有存空  2014.3.26 taolb 结束----------
                    
                    // ----------------------------------------------------
                    // 吴红亮 添加 江苏财政厅 不发送给自己和正在办理的人
                    if (dsoap.tools.ConfigurationSettings.isFilterPerson) {
                        // if ((id).equals(owner) || checkReceiver(_myRead, id)) {
                        if (checkReceiver(_myRead, id)) {
                            ((org.dom4j.Element) userNode).addAttribute("filter", "1");
                            continue;
                        }
                        count++;
                    }
                    // boolean pass = isAuthority(wfNodeId, id, fId, _myRead);
                    // if (!pass) {
                    // throw new Exception("-100");
                    // }
                    // 吴红亮 添加 江苏财政厅 不发送给自己和正在办理的人
                    int curWait = 0;
                    // 吴红亮 修改 开始
                    // int SendType = 0;
                    int SendType = Integer.parseInt(String.valueOf(this.iSendType));
                    // 吴红亮 修改 结束
                    int curWaitCount = 0;
                    if (!"NULL".equals(sNodeDate)) {
                        sNodeDate = "'" + sNodeDate + "'";
                    }
                    String curHandleway = "0";
                    if ("2".equals(uType) || "1".equals(uType)) {
                        curHandleway = "1";
                    } else {
                        curHandleway = "0";
                    }
                    Map<String, String> data = null;
                    // ----------------------------------------------------
                    // 发送远程处理
                    if (nodeType == "2") {
                        Document myRemoteXml = DocumentHelper.parseText("<SendInfo><NextNodeInfo></NextNodeInfo><DataInfo></DataInfo></SendInfo>");
                        Node myRemoteNode;
                        // myRemoteXml.LoadXml("<SendInfo><NextNodeInfo></NextNodeInfo><DataInfo></DataInfo></SendInfo>");
                        String sID = "(SELECT DECODE(MAX(ID),'',1,MAX(ID)+1) FROM S_EXDATA)";
                        String sFromNode = ConfigurationSettings.AppSettings("LocalNodeName");
                        String sGiid = sFromNode + "_" + this.iInfoID; // 如果该文件为远程文件，则取已有的GIID
                        _cmdStr = "SELECT GIID FROM S_EXDATA WHERE TO_INFO_ID=" + this.iInfoID + " and TONODE='" + sFromNode + "'";
                        _myDs = _myRead.executeQuery(_cmdStr);
                        if (_myDs.next()) {
                            sGiid = _myDs.getString("GIID");
                        }
                        _myDs.close();
                        String sFromUserName = getUserName(this.iUserID);
                        String sFormInfoID = String.valueOf(this.iInfoID);
                        String sToNode = "";// GetNodeName(Long.parseLong((subtd .valueOf("@Id"))));
                        String sToUserName = uName;
                        String sBT = getBTFromInfoID(String.valueOf(this.iInfoID));
                        String sObjClass = this.sObjclass;
                        // 远程生成OUTXML
                        // ActionXMLClass pAX = new ActionXMLClass();
                        // pAX.SetXMLDoc(ConfigurationSettings .AppSettings("MailPath_INFO") + FileTranServiceXML, ConfigurationSettings .AppSettings("MailPath_INFO") + "Data.xml");
                        // pAX.SetData("INFO_ID", iInfoID, 0);
                        // String sDataInfo = pAX.PraseActionXml();
                        // pAX = null;
                        // 配备XML
                        myRemoteNode = myRemoteXml.selectSingleNode("SendInfo");
                        ((Element) myRemoteNode).addAttribute("GIID", sGiid); // XmlAttribute myAttr = // myRemoteXml.CreateAttribute("GIID"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["GIID"].Value = sGiid;
                        ((Element) myRemoteNode).addAttribute("FROMNODE", sFromNode); // myAttr = myRemoteXml.CreateAttribute("FROMNODE"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["FROMNODE"].Value = // sFromNode;
                        ((Element) myRemoteNode).addAttribute("FROMUSERNAME", sFromUserName); // myAttr = myRemoteXml.CreateAttribute("FROMUSERNAME"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["FROMUSERNAME"].Value = // sFromUserName;
                        ((Element) myRemoteNode).addAttribute("FormInfoID", sFormInfoID); // myAttr = myRemoteXml.CreateAttribute("FormInfoID"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["FormInfoID"].Value = // sFormInfoID;
                        ((Element) myRemoteNode).addAttribute("TONODE", sToNode); // myAttr = myRemoteXml.CreateAttribute("TONODE"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["TONODE"].Value = sToNode;
                        ((Element) myRemoteNode).addAttribute("TOUSERNAME", sToUserName); // myAttr = myRemoteXml.CreateAttribute("TOUSERNAME"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["TOUSERNAME"].Value = // sToUserName;
                        ((Element) myRemoteNode).addAttribute("OBJCLASS", sObjClass); // myAttr = myRemoteXml.CreateAttribute("OBJCLASS"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["OBJCLASS"].Value = // sObjClass;
                        ((Element) myRemoteNode).addAttribute("BT", sBT); // myAttr = myRemoteXml.CreateAttribute("BT"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["BT"].Value = sBT;
                        ((Element) myRemoteNode).addAttribute("NEEDBACK", isNeedBack); // myAttr = myRemoteXml.CreateAttribute("NEEDBACK"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["NEEDBACK"].Value = // isNeedBack;
                        ((Element) myRemoteNode).addAttribute("PID", String.valueOf(this.iPID)); // myAttr = myRemoteXml.CreateAttribute("PID"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["PID"].Value = iPID;
                        ((Element) myRemoteNode).addAttribute("PNID", String.valueOf(G_PNID)); // myAttr = myRemoteXml.CreateAttribute("PNID"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["PNID"].Value = G_PNID;
                        ((Element) myRemoteNode).addAttribute("NODEID", wfNodeId); // myAttr = myRemoteXml.CreateAttribute("NODEID"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["NODEID"].Value = // td.Attributes["NodeID"].Value;
                        ((Element) myRemoteNode).addAttribute("NODECAPTION", nodeCaption); // myAttr = myRemoteXml.CreateAttribute("NODECAPTION"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["NODECAPTION"].Value = // td.Attributes["NodeCaption"].Value;
                        ((Element) myRemoteNode).addAttribute("FPNID", String.valueOf(this.iPnID)); // myAttr = myRemoteXml.CreateAttribute("FPNID"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["FPNID"].Value = iPnID;
                        String sBackUserInfo = ";:0:" + this.iUserID + ":" + this.sCaption + "::" + sFromUserName + ":" + this.getDetpIDFromUserID(this.iUserID);
                        ((Element) myRemoteNode).addAttribute("BACKUSERINFO", sBackUserInfo); // myAttr = myRemoteXml.CreateAttribute("BACKUSERINFO"); // myRemoteNode.Attributes.Append(myAttr); // myRemoteNode.Attributes["BACKUSERINFO"].Value = // sBackUserInfo;
                        // ((Element) myRemoteNode.selectSingleNode("DataInfo")) .add(DocumentHelper.parseText(sDataInfo) .getRootElement());
                        // 插入S_EXDATA
                        _cmdStr = "INSERT INTO S_EXDATA(ID,GIID,FROMNODE,FROMUSERNAME,FROM_INFO_ID,TONODE,TOUSERNAME,TO_INFO_ID,OBJCLASS,BT,CREATEDATE,TOTALBYTES,STATUS,NEEDBACK,XML,PID,PNID) ";
                        _cmdStr += "VALUES";
                        _cmdStr += "(" + sID + ",'" + sGiid + "','" + sFromNode + "','" + sFromUserName + "'," + sFormInfoID + ",'" + sToNode + "','" + sToUserName + "',0,'" + sObjClass + "','" + sBT + "'," + processDate("NOW", DBMS, now) + ",0,0," + isNeedBack + ",'" + sBackUserInfo + "'," + this.iPID + "," + G_PNID + ")";
                        _myRead.executeUpdate(_cmdStr);
                        _cmdStr = "DELETE G_INBOX WHERE PID=" + this.iPID + " AND PNID=" + G_PNID;
                        _myRead.executeUpdate(_cmdStr);
                        if (isNeedBack != "1") { // 不需要回执，将当前节点置为办结
                            _cmdStr = "UPDATE G_OPINION SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID=" + G_PNID + " AND STATUS<>1";
                            _myRead.executeUpdate(_cmdStr);
                            _cmdStr = "UPDATE G_PNODES SET STATUS=-1," + processDate("SDATE=NOW,PDATE=NOW", DBMS, now) + " WHERE PID=" + iPID + " AND ID=" + G_PNID;
                            _myRead.executeUpdate(_cmdStr);
                            _cmdStr = "UPDATE G_PROUTE SET STATUS=-1 WHERE PID=" + this.iPID + " AND ID=" + G_PNID;
                            _myRead.executeUpdate(_cmdStr);
                        }
                        // 保存文件发送文件包
                        // myRemoteXml.Save(ConfigurationSettings .AppSettings("MailPath_OUT") + sGiid + "_" + sToNode + ".XML");
                        // myRemoteXml = null;
                    } else if ("5".equals(nodeType)) { // 发送到子流程 **************************
                        // 子流程ID
                        int iSubFlowID = Integer.parseInt(subFlowID);
                        // 子流程起始节点
                        // 插入G_PNODES
                        long iSubPID = this.getMaxValue("G_PID", 1);
                        // _cmdStr = "INSERT INTO G_PNODES ";
                        // _cmdStr += "(PID,ID,FID,INFO_ID,DEPT_ID,USER_ID,UTYPE,TIMESPAN,TIMETYPE,DAYS,";
                        // _cmdStr += "RDATE,HANDLEWAY,STATUS,FSTATUS,SIGNED,";
                        // _cmdStr += "ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,WAITCOUNT,EDATE,MUSER_ID,ISZNG,PARENTFLOWPID,PARENTFLOWPNID,UNAME) ";
                        // _cmdStr += "VALUES ";
                        // _cmdStr += "(" + iSubPID + ",1,0," + this.iInfoID + ",";
                        // _cmdStr += fId + "," + id + "," + uType + "," + timeSpan + "," + timeType + ",0,";
                        // _cmdStr += processDate("NOW,", DBMS, now);
                        // _cmdStr += curHandleway + ",1,-1,1,'" + nodeCaption + "'," + iSubFlowID + ",(SELECT WFNODE_ID FROM WFNODELIST WHERE WF_ID=" + iSubFlowID + " AND WFNODE_NODETYPE=1),'" + nodeCaption + "',";
                        // _cmdStr += curWait + "," + SendType + "," + curWaitCount + "," + sNodeDate + "," + id + "," + sIsZNG + "," + this.iPID + "," + G_PNID + ",'" + uName + "')";
                        // _myRead.executeUpdate(_cmdStr);
                        data = new HashMap<String, String>();
                        data.put("PID", String.valueOf(iSubPID));
                        data.put("ID", "1");
                        data.put("FID", "0");
                        data.put("INFO_ID", String.valueOf(this.iInfoID));
                        data.put("DEPT_ID", fId);
                        data.put("USER_ID", id);
                        data.put("UTYPE", uType);
                        data.put("UNAME", uName);
                        data.put("MDEPT_ID", "NULL");
                        data.put("MUSER_ID", id);
                        data.put("STATUS", "1");
                        data.put("ACTNAME", lineCaption);
                        data.put("WF_ID", String.valueOf(iSubFlowID));
                        data.put("WFNODE_ID", "(SELECT WFNODE_ID FROM WFNODELIST WHERE WF_ID=" + iSubFlowID + " AND WFNODE_NODETYPE=1)");
                        data.put("WFNODE_CAPTION", nodeCaption);
                        data.put("WFNODE_WAIT", String.valueOf(curWait));
                        data.put("SENDTYPE", String.valueOf(SendType));
                        data.put("RDATE", processDate("NOW", DBMS, now));
                        data.put("PDATE", "NULL");
                        data.put("SDATE", "NULL");
                        data.put("EDATE", sNodeDate);
                        data.put("TIMESPAN", timeSpan);
                        data.put("TIMETYPE", timeType);
                        data.put("DAYS", "0");
                        data.put("WHOHANDLE", String.valueOf(this.iUserID));
                        data.put("HANDLEWAY", curHandleway);
                        data.put("WAITCOUNT", String.valueOf(curWaitCount));
                        data.put("ISZNG", sIsZNG);
                        data.put("FSTATUS", "-1");
                        data.put("PARENTFLOWPID", String.valueOf(this.iPID));
                        data.put("PARENTFLOWPNID", String.valueOf(G_PNID));
                        data.put("SIGNED", "1");
                        data.put("ATYPE", String.valueOf(this.iActType));
                        
                        //往g_pnodes中增加 角色关联标识  taolb 2014.3.26 --------------开始
                        data.put("RELTAG", reltag);
                        //往g_pnodes中增加 角色关联标识  taolb 2014.3.26 --------------结束
                        
                      //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------开始
                        data.put("WFLINE_TYPE", WFLINE_TYPE);
                        if(!"9".equals(nodeType)&&1==this.isFirstCS)
                        {
                        	data.put("ISCS", "0");
                        }
                        else
                        {
                        data.put("ISCS", String.valueOf(this.isCS));
                        }
						data.put("mainUnit", this.MainUnit);
                      //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------结束
                        insertGPnodes(_myConn, data);
                        // 当前g_inbox设为子流程状态
                        _cmdStr = "UPDATE G_INBOX SET PID=" + iSubPID + ",PNID=1,WF_ID=" + iSubFlowID + ",WFNODE_ID=(SELECT WFNODE_ID FROM WFNODELIST WHERE WF_ID=" + iSubFlowID + " AND WFNODE_NODETYPE=1) WHERE PID=" + this.iPID + " AND PNID=" + G_PNID;
                        _myRead.executeUpdate(_cmdStr);
                    } else if ("7".equals(nodeType)) {// 文件合并
                        String joinFileId = getJoinFileId(_myRead, String.valueOf(this.iInfoID));
                        boolean isStop = isStop(_myRead, String.valueOf(this.iWfID), wfNodeId, joinFileId);
                        boolean isPass = isPass(_myRead, String.valueOf(this.iWfID), wfNodeId, joinFileId);
                        // ##############################################################################################################
                        // 插入G_PNODES **************************
                        data = new HashMap<String, String>();
                        data.put("PID", String.valueOf(this.iPID));
                        data.put("ID", String.valueOf(G_PNID));
                        data.put("FID", String.valueOf(this.iPnID));
                        data.put("INFO_ID", joinFileId);
                        data.put("DEPT_ID", fId);
                        data.put("USER_ID", id);
                        data.put("UTYPE", uType);
                        data.put("UNAME", uName);
                        data.put("MDEPT_ID", "NULL");
                        data.put("MUSER_ID", "NULL");
                        data.put("STATUS", "1");
                        data.put("ACTNAME", lineCaption);
                        data.put("WF_ID", String.valueOf(this.iWfID));
                        data.put("WFNODE_ID", wfNodeId);
                        data.put("WFNODE_CAPTION", nodeCaption);
                        data.put("WFNODE_WAIT", String.valueOf(curWait));
                        data.put("SENDTYPE", nodeType);
                        data.put("RDATE", processDate("NOW", DBMS, now));
                        data.put("PDATE", "NULL");
                        data.put("SDATE", "NULL");
                        data.put("EDATE", sNodeDate);
                        data.put("TIMESPAN", timeSpan);
                        data.put("TIMETYPE", timeType);
                        data.put("DAYS", "0");
                        data.put("WHOHANDLE", String.valueOf(this.iUserID));
                        data.put("HANDLEWAY", curHandleway);
                        data.put("WAITCOUNT", String.valueOf(curWaitCount));
                        data.put("ISZNG", sIsZNG);
                        data.put("FSTATUS", "-1");
                        data.put("PARENTFLOWPID", String.valueOf(this.iParentPID));
                        data.put("PARENTFLOWPNID", String.valueOf(this.iParentPnID));
                        data.put("SIGNED", "1");
                        data.put("ATYPE", String.valueOf(this.iActType));
                      //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------开始
                        data.put("WFLINE_TYPE", WFLINE_TYPE);
                        
                        //往g_pnodes中增加 角色关联标识  taolb 2014.3.26 --------------开始
                        data.put("RELTAG", reltag);
                        //往g_pnodes中增加 角色关联标识  taolb 2014.3.26 --------------结束
                        
                        
                        if(!"9".equals(nodeType)&&1==this.isFirstCS)
                        {
                        	data.put("ISCS", "0");
                        }
                        else
                        {
                        data.put("ISCS", String.valueOf(this.isCS));
                        }
						data.put("mainUnit", this.MainUnit);
                      //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------结束
                        insertGPnodes(_myConn, data);
                        // 插入G_UFILES **************************
                        _cmdStr = "SELECT COUNT(*) NUM FROM G_UFILES WHERE INFO_ID=" + this.iInfoID + " and ISBW=1 AND USER_ID=" + id;
                        _myDs = _myRead.executeQuery(_cmdStr);
                        int isHave = 0;
                        if (_myDs.next()) {
                            isHave = _myDs.getInt("NUM");
                        }
                        _myDs.close();
                        if (isHave == 0) {
                            _cmdStr = "INSERT INTO G_UFILES (INFO_ID,USER_ID,PRIVILEGE,ISBW) VALUES (" + this.iInfoID + "," + id + ",7,1)";
                            _myRead.executeUpdate(_cmdStr);
                        }
                        // 插入G_PROUTE **************************
//                        _cmdStr = "INSERT INTO G_PROUTE (PID,ID,FPID,FID,STATUS) VALUES (" + this.iPID + "," + G_PNID + "," + this.iPID + ",";
//                        if (iSendMethod == 0) {// 并行
//                            _cmdStr += this.iPnID + ",1)";
//                        } else {// 串行
//                            if (isStartCX) {
//                                _cmdStr += this.iPnID + ",1)";
//                                isStartCX = false;
//                            } else {
//                                _cmdStr += (G_PNID - 1) + ",1)";
//                                curWait = 1;
//                            }
//                        }
//                        _myRead.executeUpdate(_cmdStr);
                        // 插入G_OPINION 批量处理默认意见 **************************
                        // 吴红亮 更新 开始
                        if (this.isBatch) {
                            insertBatchOpinion(_myConn, _myRead);
                        }
                        // ##############################################################################################################
                        if (!isStop && !isPass) {
                            // 吴红亮 更新 结束
                            // 插入G_INBOX **************************
                            if (this.strPriSend.indexOf("," + id + ",") == -1) {
                            } else {
                                SystemLog.SaveLog("文件操作", "越权发送", "1", this.iUserID, this.getUserName(this.iUserID), this.strIP, this.strMAC, this.strBT, "1");
                            }
                            data.put("ID", String.valueOf(G_INBOX_ID));
                            data.put("PNID", String.valueOf(G_PNID));
                            data.put("FUSER_ID", id);
                            data.put("STATUS", (this.joinStatus == 1 ? "3" : "1"));
                            data.put("WFNODE_WAIT", String.valueOf(curWait));
                            data.put("FNODE", "NULL");
                            data.put("PRIORY", "0");
                            data.put("NEEDPRI", this.strPriSend.indexOf("," + this.iUserID + ",") == -1 ? "NULL" : "1");
                            data.put("TIP", this.tip);
							insertGInbox(_myConn, data);
                            // 吴红亮 更新 开始 邮件发送
                            mails.put(id, String.valueOf(this.iInfoID));
                            // 吴红亮 更新 结束
                            if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                                _cmdStr = "update g_inbox set BT=(select BT from g_infos where id=" + this.iInfoID + "),DELETED=(select DELETED from g_infos where id=" + this.iInfoID + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + this.iInfoID + "),URGENT=(select URGENT from g_infos where id=" + this.iInfoID + "),SWH=(select SWH from g_infos where id=" + this.iInfoID + "),WH=(select WH from g_infos where id=" + this.iInfoID + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                            } else if ("ORACLE".equals(DBMS)) {
                                _cmdStr = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                            } else if ("SQLSERVER".equals(DBMS)) {
                                _cmdStr = "update g_inbox set BT=(select BT from g_infos where id=" + this.iInfoID + "),DELETED=(select DELETED from g_infos where id=" + this.iInfoID + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + this.iInfoID + "),URGENT=(select URGENT from g_infos where id=" + this.iInfoID + "),SWH=(select SWH from g_infos where id=" + this.iInfoID + "),WH=(select WH from g_infos where id=" + this.iInfoID + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                            } else if ("MYSQL".equals(DBMS)) {
                                _cmdStr = "update g_inbox set BT=(select BT from g_infos where id=" + this.iInfoID + "),DELETED=(select DELETED from g_infos where id=" + this.iInfoID + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + this.iInfoID + "),URGENT=(select URGENT from g_infos where id=" + this.iInfoID + "),SWH=(select SWH from g_infos where id=" + this.iInfoID + "),WH=(select WH from g_infos where id=" + this.iInfoID + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                            } else {
                                _cmdStr = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                            }
                            _myRead.executeUpdate(_cmdStr);
                            _cmdStr = "update G_INFOS set USER_ID=" + id + ",MAINDEPT=" + fId + " where ID=" + joinFileId;
                            _myRead.executeUpdate(_cmdStr);
                        } else {
                            if (isPass) {
                                throw new Exception("文件汇总已经停止！");
                            }
                        }
                    } else {
                        // 插入G_PNODES **************************
                        // _cmdStr = "INSERT INTO G_PNODES ";
                        // _cmdStr += "(PID,ID,FID,INFO_ID,DEPT_ID,USER_ID,UTYPE,TIMESPAN,TIMETYPE,DAYS,RDATE,HANDLEWAY,STATUS,FSTATUS,SIGNED,";
                        // _cmdStr += "ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,WAITCOUNT,EDATE,MUSER_ID,ISZNG,PARENTFLOWPID,PARENTFLOWPNID,UNAME,ATYPE) ";
                        // _cmdStr += "VALUES ";
                        // _cmdStr += "(" + this.iPID + "," + G_PNID + "," + this.iPnID + "," + this.iInfoID + "," + fId + "," + id + ",";
                        // _cmdStr += uType + "," + timeSpan + "," + timeType + ",0,";
                        // _cmdStr += processDate("NOW,", DBMS, now);
                        // _cmdStr += curHandleway + ",1,-1,1,'" + nodeCaption + "'," + this.iWfID + "," + wfNodeId + ",'" + nodeCaption + "',";
                        // _cmdStr += curWait + "," + SendType + "," + curWaitCount + "," + sNodeDate + "," + id + "," + sIsZNG + "," + this.iParentPID + "," + this.iParentPnID + ",'" + uName + "'," + this.iActType + ")";
                        // _myRead.executeUpdate(_cmdStr);
                        data = new HashMap<String, String>();
                        data.put("PID", String.valueOf(this.iPID));
                        data.put("ID", String.valueOf(G_PNID));
                        data.put("FID", String.valueOf(this.iPnID));
                        data.put("INFO_ID", String.valueOf(this.iInfoID));
                        data.put("DEPT_ID", fId);
                        data.put("USER_ID", id);
                        data.put("UTYPE", uType);
                        data.put("UNAME", uName);
                        //data.put("MDEPT_ID", "NULL");
                        //data.put("MUSER_ID", "NULL");
                      //环保部  吕晓丹  2013-02-26-----start----
                        data.put("MDEPT_ID", fId);
                        data.put("MUSER_ID", id);
                      //环保部  吕晓丹  2013-02-26-----end----
                        data.put("STATUS", "1");
                        data.put("ACTNAME", lineCaption);
                        data.put("WF_ID", String.valueOf(this.iWfID));
                        data.put("WFNODE_ID", wfNodeId);
                        data.put("WFNODE_CAPTION", nodeCaption);
                        data.put("WFNODE_WAIT", String.valueOf(curWait));
                        data.put("SENDTYPE", String.valueOf(SendType));
                        data.put("RDATE", processDate("NOW", DBMS, now));
                        data.put("PDATE", "NULL");
                        data.put("SDATE", "NULL");
                        data.put("EDATE", sNodeDate);
                        data.put("TIMESPAN", timeSpan);
                        data.put("TIMETYPE", timeType);
                        data.put("DAYS", "0");
                        data.put("WHOHANDLE", String.valueOf(this.iUserID));
                        data.put("HANDLEWAY", curHandleway);
                        data.put("WAITCOUNT", String.valueOf(curWaitCount));
                        data.put("ISZNG", sIsZNG);
                        data.put("FSTATUS", "-1");
                        data.put("PARENTFLOWPID", String.valueOf(this.iParentPID));
                        data.put("PARENTFLOWPNID", String.valueOf(this.iParentPnID));
                        data.put("SIGNED", "1");
                        data.put("ATYPE", String.valueOf(this.iActType));
                      //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------开始
                        
                      //往g_pnodes中增加 角色关联标识  taolb 2014.3.26 --------------开始
                        data.put("RELTAG", reltag);
                        //往g_pnodes中增加 角色关联标识  taolb 2014.3.26 --------------结束
                        
                        
                        data.put("WFLINE_TYPE", WFLINE_TYPE);
                        if(!"9".equals(nodeType)&&1==this.isFirstCS)
                        {
                        	data.put("ISCS", "0");
                        }
                        else
                        {
                        data.put("ISCS", String.valueOf(this.isCS));
                        }
						data.put("mainUnit", this.MainUnit);
                      //g_pnodes 增加 连线类型WFLINE_TYPE 杨龙修改 2012/8/9--------------结束
                        insertGPnodes(_myConn, data);
                        
                        //如果发送时有退回意见，将退回意见写入G_PNODES表，特送退回任意节点使用 杨龙修改 2013-1-13
                        if(!"".equals(this.send_BACKREASON))
                        {
                        	_cmdStr = "update g_pnodes t set t.BACKREASON='"+this.send_BACKREASON+"' where t.PID="+this.iPID+" and t.ID= "+G_PNID+"";
                        	 _myRead.executeUpdate(_cmdStr);
                        }
                        // 插入G_UFILES **************************
                        _cmdStr = "SELECT COUNT(*) NUM FROM G_UFILES WHERE INFO_ID=" + this.iInfoID + " AND USER_ID=" + id;
                        _myDs = _myRead.executeQuery(_cmdStr);
                        int isHave = 0;
                        if (_myDs.next()) {
                            isHave = _myDs.getInt("NUM");
                        }
                        _myDs.close();
                        if (isHave == 0) {
                        	//G_UFILES表增加UTYPE字段 幸世诠提供更新 2012/9/12 开始
                        	//_cmdStr = "INSERT INTO G_UFILES (INFO_ID,USER_ID,PRIVILEGE) VALUES (" + this.iInfoID + "," + id + ",7)";
                        	////增加ISBW，标识为是否办文
                            _cmdStr = "INSERT INTO G_UFILES (INFO_ID,USER_ID,PRIVILEGE,UTYPE,ISBW,MAINUNIT) VALUES (" + this.iInfoID + "," + id + ",7,3,1,"+this.MainUnit+")";
                            //G_UFILES表增加UTYPE字段 幸世诠提供更新 2012/9/12 结束
                            _myRead.executeUpdate(_cmdStr);
                        }else{
                        	_cmdStr = "update G_UFILES set ISBW=1 where INFO_ID=" + this.iInfoID + " and USER_ID=" + id ;
                        	_myRead.executeUpdate(_cmdStr);
                        }
                        // 插入G_PROUTE **************************
//                        _cmdStr = "INSERT INTO G_PROUTE (PID,ID,FPID,FID,STATUS) VALUES (" + this.iPID + "," + G_PNID + "," + this.iPID + ",";
//                        if (iSendMethod == 0) {// 并行
//                            _cmdStr += this.iPnID + ",1)";
//                        } else {// 串行
//                            if (isStartCX) {
//                                _cmdStr += this.iPnID + ",1)";
//                                isStartCX = false;
//                            } else {
//                                _cmdStr += (G_PNID - 1) + ",1)";
//                                curWait = 1;
//                            }
//                        }
//                        // System.out.println(_cmdStr);// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//                        _myRead.executeUpdate(_cmdStr);
                        if ("1".equals(isSMS)) {
                            //receiveUserIds += id + ",";
							receiveUserIds +=  ","+id ;
                            receiveUserNames=receiveUserNames+","+uName;
                        }
                        // if (msgServer != null && !"".equals(msgServer)) {// 短信，邮件，托盘
                        // String strXPhone = "";
                        // String strXEmail = "";
                        // String strXTray = "";
                        // RemindInfo ri = getRemindInfo(id);
                        // strXPhone = ri.getStrPhone();
                        // strXEmail = ri.getStrEmail();
                        // strXTray = ri.getStrTray();
                        // String strSms = this.isSendSMS && !"".equals(strXPhone.trim()) ? "1" : "0";
                        // String strEmail = this.isSendEmail && !"".equals(strXEmail.trim()) ? "1" : "0";
                        // String strTray = this.isSendTray && !"".equals(strXTray.trim()) ? "1" : "0";
                        // if ("1".equals(strSms) || "1".equals(strEmail) || "1".equals(strTray)) {
                        // // strSmsContent = ToBase64FromString(sSmsContent);
                        // RemindMessage RMSms = new RemindMessage();
                        // RMSms.Url = msgServer;
                        // // String strReturn = RMSms.PutMessage(strSmsContent, "", strXPhone.trim(), strXEmail.trim(), strXTray.trim(), iUserID, strUname, id, uName, "", iInfoID, sObjclass, strMaindept, strSms, strEmail, strTray, "0", "", "");
                        // }
                        // }
                        // 插入G_OPINION 批量处理默认意见 **************************
                        // 吴红亮 更新 开始
                        if (this.isBatch) {
                            insertBatchOpinion(_myConn, _myRead);
                        }
                        // 吴红亮 更新 结束
                        // 插入G_INBOX **************************
                        // _cmdStr = "INSERT INTO G_INBOX ";
                        // _cmdStr += "(ID,INFO_ID,DEPT_ID,USER_ID,FUSER_ID,UTYPE,HANDLEWAY,PID,PNID,STATUS,PRIORY,RDATE,ACTNAME,";
                        // _cmdStr += "WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,TIMETYPE,TIMESPAN,DAYS,SENDTYPE,WAITCOUNT,FNODE,EDATE,ISZNG";
                        // if (this.strPriSend.indexOf("," + id + ",") == -1) {
                        // _cmdStr += ") ";
                        // } else {
                        // _cmdStr += ",NEEDPRI) ";
                        // }
                        // _cmdStr += "VALUES ";
                        // _cmdStr += "(" + G_INBOX_ID + "," + this.iInfoID + "," + fId + "," + id + "," + this.iUserID + "," + uType + "," + curHandleway + "," + this.iPID + "," + G_PNID + "," + (this.joinStatus == 1 ? "3" : "1") + ",0,";
                        // _cmdStr += processDate("NOW,", DBMS, now);
                        // _cmdStr += "'" + nodeCaption + "'," + this.iWfID + "," + wfNodeId + ",'" + nodeCaption + "'," + curWait + "," + timeType + "," + timeSpan + ",0," + SendType + "," + curWaitCount + ",''," + sNodeDate + "," + sIsZNG;
                        if (this.strPriSend.indexOf("," + id + ",") == -1) {
                            // _cmdStr += ")";
                        } else {
                            // _cmdStr += ",1)";
                            SystemLog.SaveLog("文件操作", "越权发送", "1", this.iUserID, this.getUserName(this.iUserID), this.strIP, this.strMAC, this.strBT, "1");
                        }
                        // // 插入G_OPINION 批量处理默认意见
                        // // 吴红亮 更新 开始
                        // if (this.isBatch) {
                        // insertBatchOpinion(_myRead);
                        // }
                        // // 吴红亮 更新 结束
                        // _myRead.executeUpdate(_cmdStr);
                        data.put("ID", String.valueOf(G_INBOX_ID));
                        data.put("PNID", String.valueOf(G_PNID));
                        data.put("FUSER_ID", String.valueOf(this.iUserID));
                        data.put("STATUS", (this.joinStatus == 1 ? "3" : "1"));
                        data.put("WFNODE_WAIT", String.valueOf(curWait));
                        data.put("FNODE", "NULL");
                        data.put("PRIORY", "0");
                        data.put("NEEDPRI", this.strPriSend.indexOf("," + this.iUserID + ",") == -1 ? "NULL" : "1");
                      //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
                        data.put("RECEIVE_USERNAME", uName);
                        data.put("SEND_USERNAME", this.getUserName(this.iUserID));
                      //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
                        if ("SYBASE".equals(DBMS)) {
                        } else if ("ORACLE".equals(DBMS)) {
                            blqx = "TO_DATE('" + blqx + "','YYYY-MM-DD')";
                        } else if ("SQLSERVER".equals(DBMS)) {
                        	blqx ="CAST('" + blqx + "' AS DATE)";
                        } else if ("MYSQL".equals(DBMS)) {
                        	blqx = "'" + DB_DATE_FORMAT.format(blqx) + "'";
                        } else {
                            blqx = "TO_DATE('" + DB_DATE_FORMAT.format(blqx) + "','YYYY-MM-DD HH24:MI:SS')";
                        }
                        //dongchenjie添加EDATE为催办日期,20160725
                        data.put("EDATE", blqx);
                        //往g_inboxs中增加 角色关联标识  taolb 2014.3.26 --------------开始
                        data.put("RELTAG", reltag);
                        //往g_inboxs中增加 角色关联标识  taolb 2014.3.26 --------------结束
                        data.put("TIP", this.tip);
                        if(StringHelper.isNotNullAndEmpty(this.backReason)){
                        	data.put("BACKREASON", this.backReason);
                        }
                        data.put("GATHERNODE", String.valueOf(gatherNode));
                        insertGInbox(_myConn, data);
                        // 吴红亮 更新 开始 邮件发送
                        mails.put(id, String.valueOf(this.iInfoID));
                        // 吴红亮 更新 结束
                        if ("SYBASE".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
                            _cmdStr = "update g_inbox set BT=(select BT from g_infos where id=" + this.iInfoID + "),DELETED=(select DELETED from g_infos where id=" + this.iInfoID + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + this.iInfoID + "),URGENT=(select URGENT from g_infos where id=" + this.iInfoID + "),SWH=(select SWH from g_infos where id=" + this.iInfoID + "),WH=(select WH from g_infos where id=" + this.iInfoID + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                        } else if ("ORACLE".equals(DBMS)) {
                            _cmdStr = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
//                        	_cmdStr = "update g_inbox b set b.BT=a.BT,b.DELETED=a.DELETED,b.HASCONTENT=0,b.URGENT=a.URGENT,b.SWH=a.SWH,b.WH=a.WH,b.OBJCLASS=a.OBJCLASS FROM G_INFOS a  where a.id=" + this.iInfoID + "and b.id=" + G_INBOX_ID;
                        } else if ("SQLSERVER".equals(DBMS)) {
                            _cmdStr = "update g_inbox set BT=(select BT from g_infos where id=" + this.iInfoID + "),DELETED=(select DELETED from g_infos where id=" + this.iInfoID + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + this.iInfoID + "),URGENT=(select URGENT from g_infos where id=" + this.iInfoID + "),SWH=(select SWH from g_infos where id=" + this.iInfoID + "),WH=(select WH from g_infos where id=" + this.iInfoID + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                        } else if ("MYSQL".equals(DBMS)) {
                            _cmdStr = "update g_inbox set BT=(select BT from g_infos where id=" + this.iInfoID + "),DELETED=(select DELETED from g_infos where id=" + this.iInfoID + "),HASCONTENT=(select HASCONTENT from g_infos where id=" + this.iInfoID + "),URGENT=(select URGENT from g_infos where id=" + this.iInfoID + "),SWH=(select SWH from g_infos where id=" + this.iInfoID + "),WH=(select WH from g_infos where id=" + this.iInfoID + "),OBJCLASS=(select OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                        } else {
                         //   _cmdStr = "update g_inbox set (BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS)=(select BT,DELETED,HASCONTENT,URGENT,SWH,WH,OBJCLASS from g_infos where id=" + this.iInfoID + ") where id=" + G_INBOX_ID;
                       	 _cmdStr = "update g_inbox b set b.BT=a.BT,b.DELETED=a.DELETED,b.HASCONTENT=0,b.URGENT=a.URGENT,b.SWH=a.SWH,b.WH=a.WH,b.OBJCLASS=a.OBJCLASS FROM G_INFOS a  where a.id=" + this.iInfoID + "and b.id=" + G_INBOX_ID;
                        }
                        _myRead.executeUpdate(_cmdStr);
                    }
                    G_INBOX_ID++;
                    G_PNID++;
                    //判断发送的节点是否全是抄送节点 杨龙修改 2012/9/11 开始
                    //非抄送节点 notCS+1
                    if(!"9".equals(nodeType))
                    {
                    	notCS++;
                    }
                  //判断发送的节点是否全是抄送节点 杨龙修改 2012/9/11 结束
                    
                    /**************插入目标节点的消息开始  liuyang 2014/8/7*******************/
                    
                    Map<String, String> params = new HashMap<String, String>();
                    
                    params.put("infoId", String.valueOf(this.iInfoID));
                    
                    params.put("moduleId", String.valueOf(this.sObjclass));//模块ID this.sObjclass
                    
                    params.put("sUserId", String.valueOf(this.iUserID));
                    
                    params.put("rUserId", id); 
                    
                    params.put("body", "");
                    
                    params.put("bt", content);
                    
                    params.put("pid", String.valueOf(this.iPID));
                    
                    params.put("pnid", String.valueOf(this.iPnID)); 
                   
                   String sendType = "";
                    if(sSmsContent!=null&&!"".equals(sSmsContent)){
                    	  if(isForword)
                          {
                          	//发送时间：当前
                          	//发送人 this.iUserID
                          	//接收人 id
                          	//添加sendType 传入接收人类型
                          	sendType = "isForword";
                          	MessageParser.sendMsg_user(params,new Date(),sendType,sSmsContent); 
                          }
                          
                          if(isEMail)
                          {
                          	//发送时间：当前
                          	//发送人 this.iUserID
                          	//接收人 id
                          	//添加sendType 传入接收人类型
                          	sendType = "isMail";
                          	MessageParser.sendMsg(params,new Date(),sendType,sSmsContent); 
                          	
                          }
                          
                          if(isTray)
                          {
                          	//发送时间：当前
                          	//发送人 this.iUserID
                          	//接收人 id
                          	//添加sendType 传入接收人类型
                          	sendType = "isTray";
                          	MessageParser.sendMsg(params,new Date(),sendType,sSmsContent); 
                          	
                          }
                          
//                          if(isTuoPan)
//                          {
//                          	//发送时间：当前
//                          	//发送人 this.iUserID
//                          	//接收人 id
//                          	//添加sendType 传入接收人类型
//                          	sendType = "isTuoPan";
//                          	MessageParser.sendMsg(params,new Date(),sendType,sSmsContent); 
//                          	
//                          }
                          
                          
                    }
                  
                    /**************插入目标节点的消息结束 2014/8/7*******************/
                }
				RemindInfo ri = new RemindInfo();
                if ("1".equals(isSMS)) {
                	ri.setRuserid(receiveUserIds.substring(1));
                	ri.setRusername(receiveUserNames.substring(1));
                	ri.setContent(sSmsContent);
                	ri.setInfoID(iInfoID);
                	ri.setUserID(iUserID);
                	ri.setMainUnit(MainUnit);
                	RemindMessage RMSms = new RemindMessage();
                	String strReturn=RMSms.PutMessage(ri);
                }
                // 发送结束后处理语句
              //增加发送结束后线上SQL处理，如果线上没有SQL，则执行节点上的SQL 杨龙修改 2012/10/16 开始
                //doCommand(getSqlFromAfterSendSql(nextNode.valueOf("@AfterSendSql")), _myRead, httpTask);
                if(nextNode.selectNodes("Node").size()>0)
                {
	                	try {
							if(!"".equals(nextNode.valueOf("@AfterSql")))
							{
								doCommand(getSqlFromAfterSendSql(nextNode.valueOf("@AfterSql")), _myRead, httpTask);
							}
							else
							{
								doCommand(getSqlFromAfterSendSql(nextNode.valueOf("@AfterSendSql")), _myRead, httpTask);
							}
						} catch (Exception e) {
							e.printStackTrace();
							this.sErrorMessage = "执行线上/节点语句出错";
			                throw new Exception(sErrorMessage);
						}
                }
                //增加发送结束后线上SQL处理，如果线上没有SQL，则执行节点上的SQL 杨龙修改 2012/10/16 结束
            }
          //判断发送的节点是否全是抄送节点 杨龙修改 2012/9/11 开始
            //如果notCS为0，说明全部为抄送节点，强制不能发送
            if(1>notCS&&1==this.isFirstCS)
            {
            	sErrorMessage = "86";
            	//标识错误CODE：86，增sending Action中根据86提示用户需要增加主送节点
                throw new Exception(sErrorMessage);
            }
          //判断发送的节点是否全是抄送节点 杨龙修改 2012/9/11 结束
            // if (msgServer != null && !"".equals(msgServer)) {
            // if (isSendRemind) {
            // strSmsContent = ToBase64FromString(strBt);
            // RemindMessage RMSms = new RemindMessage();
            // RMSms.Url = msgServer;
            // for (DataRow mRow : map.get("UID").rows) {
            // if (Long.parseLong(mRow.get("ID")) != iUserID)
            // RMSms.PutMessage(strSmsContent, "", "", "", "",
            // iUserID, strUname, mRow.get("ID"), mRow
            // .get("UNAME"), "", iInfoID,
            // sObjclass, strMaindept, "0", "0", "0", "1",
            // "", "");
            // }
            // }
            // }
            // _myConn.rollback();
            // 吴红亮 添加 开始
            if (dsoap.tools.ConfigurationSettings.isFilterPerson && count == 0) {
                // sErrorMessage = "不能发送给自己或正在处理的用户！";
                sErrorMessage = "不能发送给正在处理的用户！";
                throw new Exception(sErrorMessage);
            }
            // if (true) {
            // throw new Exception("测试");
            // }
            // 吴红亮 添加 结束
            //执行线上action
            doHttp(httpTask);
            _myConn.commit();
            // 吴红亮 更新 开始 邮件发送
            //MailUtil.sendMails(mails);
            //doSMS(String.valueOf(this.iUserID), receiveUserIds, String.valueOf(this.iInfoID), this.sSmsContent);
            // 吴红亮 更新 结束 邮件发送
            // ---------------------------------------------------------
            
            // 
            Sql mnrSql = new Sql("SELECT MNR,NRBT,PATH FROM G_NR WHERE INFO_ID=?ID");
            mnrSql.getParameters().add(new Parameter("ID", this.iInfoID));
            xsf.data.DataTable mnrDt = DBManager.getDataTable(mnrSql);
            if(mnrDt!=null){
            	for(xsf.data.DataRow mnrDr :mnrDt.getRows()){
                	String mnr = mnrDr.getString("MNR");
                	String nrbt =  mnrDr.getString("NRBT");
                	String path = mnrDr.getString("PATH");
                	if(StringHelper.isNotNullAndEmpty(path)){
                		String file1=getExtName(nrbt);
                    	String file2 = getExtName(path);
                    	if(!file1.equals(file2)){
                    		mnrSql = new Sql("UPDATE G_NR SET NRBT=?NRBT WHERE MNR = ?MNR");
                    		mnrSql.getParameters().add(new Parameter("NRBT", getName(nrbt)+"."+file2));
                    		mnrSql.getParameters().add(new Parameter("MNR", mnr));
                    		DBManager.execute(mnrSql);
                    	}
                	}
                	
                }
            }
            
            
            String BT = null;
            long MODULE_ID = 0;
            xsf.data.DataTable dt = DBManager.getDataTable("select BT,MODULE_ID from G_INFOS where ID=" + this.iInfoID);
            if (dt.getRows().size() > 0) {
                xsf.data.DataRow dr = dt.getRows().get(0);
                BT = dr.getString("BT");
                MODULE_ID = dr.getLong("MODULE_ID");
            }
            String formName = null;
            int action = 0;
            if (isDraft) {
                formName = "发送了文件： " + BT + " ";
                action = 100001005;
                Log log = new Log(formName);// 操作内容
                log.setAction(action);// 操作类型
                log.setBody(formName);// 操作内容
                log.setUserID(this.iUserID);// 操作用户ID
                log.setDate(new java.util.Date());// 操作时间
                log.setClasz(MODULE_ID);// 表单模块ID
                Logger.info(log); // 保存进g_log表
            }
            if (this.iSendType == 3) {
                formName = "增发了文件： " + BT + " ";
                action = 100001009;
            } else {
                formName = "处理了文件： " + BT + " ";
                action = 100001005;
            }
            Log log = new Log(formName);// 操作内容
            log.setAction(action);// 操作类型
            log.setBody(formName);// 操作内容
            log.setUserID(this.iUserID);// 操作用户ID
            log.setDate(new java.util.Date());// 操作时间
            log.setClasz(MODULE_ID);// 表单模块ID
            Logger.info(log); // 保存进g_log表
            // ---------------------------------------------------------
        } catch (Exception e) {
            e.printStackTrace();
            this.iErrorCode = 16;
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
            if (_myConn != null) {
                try {
                    _myConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
	/**
	 * 获取文件名，不包含后缀
	 * @param fileName
	 * @return
	 */
	private  String getName(String fileName) {
	    String extName = fileName;
	    if (fileName != null) {
	        int index = fileName.lastIndexOf(".");
	        if (index != -1) {
	            extName = fileName.substring(0,index);
	        }
	    }
	    return extName;
	}
	
    /**
	 * 获取文件后缀
	 * @param fileName
	 * @return
	 */
	private  String getExtName(String fileName) {
	    String extName = "";
	    if (fileName != null) {
	        int index = fileName.lastIndexOf(".");
	        if (index != -1) {
	            extName = fileName.substring(index+1);
	        }
	    }
	    return extName;
	}
	
    // 获取MAXVALUE(查询，更新必须在一个事务中完成，所以沒用 DBManager)
    private long getMaxValue(String sTag, int iCount) throws Exception {
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        long returnValue = -1;
        try {
        	//增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27
        	String DBMS = ConfigurationSettings.AppSettings("DBMS");
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myConn.setAutoCommit(false);
            _myRead = _myConn.createStatement();
            if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
            	_cmdStr = "SELECT TAG,MAXID FROM `MAXVALUE` WHERE TAG='" + sTag + "'";
            } else {
            	_cmdStr = "SELECT TAG,MAXID FROM MAXVALUE WHERE TAG='" + sTag + "'";
            }
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                returnValue = _myDs.getInt("MAXID");
            }
            if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
            	_cmdStr = "UPDATE `MAXVALUE` SET MAXID=MAXID + " + iCount + " WHERE TAG='" + sTag + "'";
            } else {
            	_cmdStr = "UPDATE MAXVALUE SET MAXID=MAXID + " + iCount + " WHERE TAG='" + sTag + "'";
            }
            _myRead.executeUpdate(_cmdStr);
            _myConn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            this.iErrorCode = 13;
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
            if (_myConn != null) {
                try {
                    _myConn.close();
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

    // 获取G_PNODES中最大PNID(共享事务)
    private long getMaxPNID(Statement statement, long pid) throws Exception {
        ResultSet _myDs = null;
        long maxId = 0;
        try {
            String _cmdStr = "SELECT MAX(ID) MAXID FROM G_PNODES WHERE PID=" + pid;
            _myDs = statement.executeQuery(_cmdStr);
            if (_myDs.next()) {
                if (_myDs.getString("MAXID") != null) {
                    maxId = _myDs.getInt("MAXID");
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (_myDs != null) {
                try {
                    _myDs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return maxId;
    }

    // 获取G_INBOX中最大PNID(共享事务)
    private long getMaxPNID1(Statement statement, long pid) throws Exception {
        ResultSet _myDs = null;
        long maxId = 0;
        try {
            String _cmdStr = "SELECT MAX(PNID) MAXID FROM G_INBOX WHERE PID=" + pid;
            _myDs = statement.executeQuery(_cmdStr);
            if (_myDs.next()) {
                if (_myDs.getString("MAXID") != null) {
                    maxId = _myDs.getInt("MAXID");
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (_myDs != null) {
                try {
                    _myDs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return maxId;
    }

    private RemindInfo getRemindInfo(String uid) {
    String strPhone = "";
    String strEmail = "";
    String strTray = "";
    Connection _myConn = null;
    Statement _myRead = null;
    ResultSet _myDs = null;
    String _cmdStr = "";
    RemindInfo ri = new RemindInfo();
    try {
    _cmdStr = "SELECT LOGNAME,MOBILE_EMAIL,EMAIL FROM G_USERS WHERE ID=" + uid;
    _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
    _myRead = _myConn.createStatement();
    _myDs = _myRead.executeQuery(_cmdStr);
    if (_myDs.next()) {
      strPhone = _myDs.getString("MOBILE_EMAIL") != null ? _myDs.getString("MOBILE_EMAIL") : "";
      strEmail = _myDs.getString("EMAIL") != null ? _myDs.getString("EMAIL") : "";
      strTray = _myDs.getString("LOGNAME") != null ? _myDs.getString("LOGNAME") : "";
     }
    ri.setStrEmail(strEmail);
    ri.setStrTray(strTray);
    ri.setStrPhone(strPhone);
    } catch (Exception e) {
      e.printStackTrace();
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
    if (_myConn != null) {
     try {
     _myConn.close();
      } catch (SQLException e) {
      e.printStackTrace();
      }
      }
      }
      return ri;
    }

    private void insertBatchOpinion(Connection _myConn, Statement _myRead) throws SQLException {
        ResultSet _myDs = null;
        try {
            Date now = new Date();
            String _cmdStr = "SELECT * FROM G_OPINION WHERE PID=" + this.iPID + " AND PNID=" + this.iPnID;
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                return;
            }
            Long userID = 0L;
            Long mUserID = 0L;
            String orgName = "";
            // String actName = "";
            // String userName = "";
            // String mUserName = "";
            // int aType = 0;
            _cmdStr = "SELECT USER_ID,MUSER_ID,ACTNAME FROM G_PNODES WHERE PID=" + this.iPID + " AND ID=" + this.iPnID;
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                userID = _myDs.getLong("USER_ID");
                mUserID = _myDs.getLong("MUSER_ID");
                // actName = _myDs.getString("ACTNAME");
                // aType = userID == mUserID ? 0 : 1;
            }
            _myDs.close();
            IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            ICommand command = (ICommand) sqlDataSource.getSelectCommands().get("getUnameDname");
            _cmdStr = command.getCommandText().trim();
            _cmdStr = _cmdStr.replace("?USER_ID", String.valueOf(userID));
            _myDs = _myRead.executeQuery(_cmdStr);
            if (_myDs.next()) {
                // userName = _myDs.getString("Name");
                orgName = _myDs.getString("DeptName");
                // -----------------------------------------------------------
                orgName = xsf.Value.getString(orgName, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
            }
            _myDs.close();
            // _cmdStr = "SELECT UNAME FROM G_USERS WHERE ID=" + mUserID;
            // _myDs = _myRead.executeQuery(_cmdStr);
            // if (_myDs.next()) {
            // mUserName = _myDs.getString("UNAME");
            // }
            // _myDs.close();
            Date time = new Date();
            String id = String.valueOf(time.getTime());
            id = id.substring(4, id.length());
            String DBMS = ConfigurationSettings.DBMS;
            _cmdStr = "INSERT INTO G_OPINION(ID,PID,PNID,STATUS,ORGNAME,ATYPE,CONTENT,USERID,USERNAME,MUSERID,MUSERNAME,YJTYPE,NODENAME,MAINUNIT,LASTUPDATEDATE,LASTUPDATEDATE_DATE,PDATE)VALUES(";
            _cmdStr += "1,";// _cmdStr += id + ",";
            _cmdStr += this.iPID + ",";
            _cmdStr += this.iPnID + ",";
            _cmdStr += "1,";
            _cmdStr += "?,";
            _cmdStr += "NULL,";// _cmdStr += aType + ",";
            _cmdStr += "'',";
            _cmdStr += mUserID + ",";
            _cmdStr += "NULL,";// _cmdStr += "'" + mUserName + "',";
            _cmdStr += "NULL,";// _cmdStr += userID + ",";
            _cmdStr += "NULL,";// _cmdStr += "'" + userName + "',";
            _cmdStr += "NULL,";// _cmdStr += "0,";
            _cmdStr += "NULL,";// _cmdStr += "'" + actName + "',";
            _cmdStr += this.MainUnit+",";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String date=format.format(new Date());
            _cmdStr += processDate("NOW,'"+date+"',NULL)", DBMS, now);// _cmdStr += processDate("NOW,NOW)", DBMS, now);
            PreparedStatement ps = _myConn.prepareStatement(_cmdStr);
            // -----------------------------------------------------------
            orgName = xsf.Value.getString(orgName, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(1, orgName);
            ps.executeUpdate();
        } catch (SQLException e) {
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
        }
    }

    private String getFirstAfter(Statement statement) throws Exception {
        ResultSet _myDs = null;
        try {
            String sql = "SELECT WFNODE_AFTERSENDSQL FROM WFNODELIST WHERE WF_ID=" + this.iWfID + " AND WFNODE_ID=" + this.iWfNodeID;
            statement.executeQuery(sql);
            String command = null;
            _myDs = statement.executeQuery(sql);
            if (_myDs.next()) {
                command = _myDs.getString("WFNODE_AFTERSENDSQL");
            }
            return command == null ? "" : xsf.Value.getString(command, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
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
        }
    }

    private boolean checkReceiver(Statement statement, String receiverId) throws Exception {
        ResultSet _myDs = null;
        try {
            boolean result = false;
            String sql = "SELECT * FROM G_INBOX A WHERE A.INFO_ID=" + this.iInfoID + "and A.USER_ID=" + receiverId;
            statement.executeQuery(sql);
            _myDs = statement.executeQuery(sql);
            if (_myDs.next()) {
                result = true;
            }
            return result;
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
        }
    }

    // private String getGInboxUser(Statement statement) throws Exception {
    // String userId = "";
    // String sql = "SELECT A.USER_ID FROM G_INBOX A WHERE A.PID=" + this.iPID + " AND A.PNID=" + this.iPnID;
    // statement.executeQuery(sql);
    // ResultSet rs = statement.executeQuery(sql);
    // if (rs.next()) {
    // userId = rs.getString("USER_ID");
    // }
    // rs.close();
    // return userId;
    // }

    private String getPrivateGroup(List<String> userGroupNames) {
        StringBuffer groupInfos = new StringBuffer();
        List<DataRow> _rows = map.get("G_GRPS_PRI").rows;
        for (String userGroupName : userGroupNames) {
            groupInfos.append("<Node Id=\"").append(userGroupName).append("\" UType=\"2\" UName=\"").append(userGroupName).append("\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">");
            for (DataRow _row : _rows) {
                String groupName = _row.get("grp_name");
                if (userGroupName.equals(groupName)) {
                    groupInfos.append("<Node ").append("Id=\"").append(_row.get("user_id")).append("\" ").append("UType=\"").append(_row.get("user_utype")).append("\" ").append("UName=\"").append(_row.get("user_name")).append("\" ").append("fName=\"").append(_row.get("dept_name")).append("\" ").append("fId=\"").append(_row.get("dept_id")).append("\" />");
                }
            }
            groupInfos.append("</Node>");
        }
        return groupInfos.toString();
    }

    private boolean hasDisplayGroupNode() {
        boolean hasAddPublicGroup = false;
        if (iInfoID > 0) {
            long count = DBManager.getFieldLongValue("select count(*) from g_issue where info_id = " + iInfoID);
            if (count > 0) {
                hasAddPublicGroup = true;
            }
        }
        return hasAddPublicGroup;
    }

    // 获取公有组xml
    private String getPublicUserGroup() {
        StringBuffer publicGroupInfos = new StringBuffer();
        DataTable publicGroupTable = null;
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append("a.uname AS groupName,c.id AS userId,c.uname AS userName,f.id AS deptId,f.uname AS deptName ").append("from G_USERS a ").append("inner join G_GRPS b on a.UTYPE=1 and a.ID=b.GRP_ID ").append("inner join G_USERS c on c.UTYPE=0 and b.user_id=c.id ").append("inner join G_DEPT d on c.id =d.user_id ").append("inner join G_Dept e on d.fid=e.id ").append("inner join G_users f on f.id=e.user_id ").append("order by a.uname");
        List<String> groups = new ArrayList<String>();
        publicGroupTable = new DataTable("PUBLIC_G_GRPS");
        DataRow dataRow = null;
        xsf.data.DataTable dt = DBManager.getDataTable(sql.toString());
        for (xsf.data.DataRow dr : dt.getRows()) {
            dataRow = publicGroupTable.NewRow();
            String name = dr.getString("groupName") != null ? dr.getString("groupName") : "";
            dataRow.put("groupName", name);
            dataRow.put("userId", dr.getString("userId") != null ? dr.getString("userId") : "");
            dataRow.put("userName", dr.getString("userName") != null ? dr.getString("userName") : "");
            dataRow.put("deptId", dr.getString("deptId") != null ? dr.getString("deptId") : "");
            dataRow.put("deptName", dr.getString("deptName") != null ? dr.getString("deptName") : "");
            if (!groups.contains(name)) {
                groups.add(name);
            }
            publicGroupTable.Add(dataRow);
        }
        int count = groups.size();
        if (count > 0) {
            publicGroupInfos.append("<Node Id=\"0\" UType=\"2\" UName=\"公有小组\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">");
            if (publicGroupTable != null) {
                List<DataRow> dataRows = publicGroupTable.getRows();
                for (String _group : groups) {
                    publicGroupInfos.append("<Node Id=\"").append(_group).append("\" UType=\"2\" UName=\"").append(_group).append("\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">");
                    for (DataRow _row : dataRows) {
                        String groupName = _row.get("groupName");
                        if (_group.equals(groupName)) {
                            publicGroupInfos.append("<Node ").append("Id=\"").append(_row.get("userId")).append("\" ").append("UType=\"0\" ").append("UName=\"").append(_row.get("userName")).append("\" ").append("fName=\"").append(_row.get("deptName")).append("\" ").append("fId=\"").append(_row.get("deptId")).append("\" />");
                        }
                    }
                    publicGroupInfos.append("</Node>");
                }
            }
            publicGroupInfos.append("</Node>");
        }
        return publicGroupInfos.toString();
    }
    
    /**
     * 获取小组信息广东调查总队专用
     * */
    private String getUserGroup() {
        StringBuffer publicGroupInfos = new StringBuffer();
        DataTable publicGroupTable = null;
        StringBuffer sql = new StringBuffer();
        /*
        sql.append(" select g_usergroup.*, G_Code_State.Name AS StateName from g_usergroup ");
        sql.append(" left join G_Code G_Code_State on G_Code_State.Value = g_usergroup.State and G_Code_State.PID=200");
        sql.append(" where   g_usergroup.OrganizeID IS NULL");
        sql.append(" and g_usergroup.CreateUser = "+this.iUserID+"");
        sql.append(" Order by g_usergroup.ItemIndex");
        */
        sql.append(" select * from ( SELECT g_organize.id AS ORGID,g_organize.Name AS ORGNAME,G_UserGroup.Name AS GROUPNAME,G_UserGroup.Itemindex,G_UserGroupUser.UserGroupID,G_UserGroupUser.ID,G_UserInfo.ID AS UserID,G_user_level.name AS Position,G_UserInfo.Name,G_UserInfo.LogName,G_UserInfo.Rank,G_UserInfo.State,G_UserInfo.IsOnline,G_Code_Rank.Name as RankName,G_Code_State.Name AS StateName,G_Code_Sex.Name AS SexName,G_UserInfo.Name AS UserName FROM G_UserInfo");
        sql.append(" INNER JOIN G_UserGroupUser  ON G_UserGroupUser.UserID = G_UserInfo.ID");
        sql.append(" INNER JOIN G_UserGroup ON G_UserGroupUser.UserGroupID = G_UserGroup.ID");
        sql.append(" inner join g_orguser on g_orguser.UserInfoID = G_UserInfo.ID and g_orguser.ismaindept = 1");
        sql.append(" inner join g_organize on g_organize.ID = g_orguser.OrganizeID ");
        sql.append(" left JOIN G_Code G_Code_Rank ON G_Code_Rank.Value = G_UserInfo.Rank AND G_Code_Rank.PID=204");
        sql.append(" left JOIN G_Code G_Code_State ON G_Code_State.Value = G_UserInfo.State AND G_Code_State.PID = 201");
        sql.append(" left JOIN G_Code G_Code_Sex ON G_Code_Sex.Value = G_UserInfo.Sex AND G_Code_Sex.PID = 150");
        sql.append(" LEFT JOIN G_user_level ON g_userinfo.Position = G_user_level.user_level ");
        sql.append(" WHERE G_UserGroup.Createuser = "+this.iUserID+"  order by G_UserGroup.Itemindex ) ");
        List<String> groups = new ArrayList<String>();
        publicGroupTable = new DataTable("PUBLIC_G_GRPS");
        DataRow dataRow = null;
        xsf.data.DataTable dt = DBManager.getDataTable(sql.toString());
        for (xsf.data.DataRow dr : dt.getRows()) {
            dataRow = publicGroupTable.NewRow();
            String name = dr.getString("GROUPNAME") != null ? dr.getString("GROUPNAME") : "";
            dataRow.put("GROUPNAME", name);
            dataRow.put("USERID", dr.getString("USERID") != null ? dr.getString("USERID") : "");
            dataRow.put("NAME", dr.getString("NAME") != null ? dr.getString("NAME") : "");
            dataRow.put("ORGID", dr.getString("ORGID") != null ? dr.getString("ORGID") : "");
            dataRow.put("ORGNAME", dr.getString("ORGNAME") != null ? dr.getString("ORGNAME") : "");
            if (!groups.contains(name)) {
                groups.add(name);
            }
            publicGroupTable.Add(dataRow);
        }
        int count = groups.size();
        if (count > 0) {
            publicGroupInfos.append("<Node Id=\"0\" UType=\"2\" UName=\"我的小组\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">");
            if (publicGroupTable != null) {
                List<DataRow> dataRows = publicGroupTable.getRows();
                for (String _group : groups) {
                    publicGroupInfos.append("<Node Id=\"").append(_group).append("\" UType=\"2\" UName=\"").append(_group).append("\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">");
                    for (DataRow _row : dataRows) {
                        String groupName = _row.get("GROUPNAME");
                        if (_group.equals(groupName)) {
                            publicGroupInfos.append("<Node ").append("Id=\"").append(_row.get("USERID")).append("\" ").append("UType=\"0\" ").append("UName=\"").append(_row.get("NAME")).append("\" ").append("fName=\"").append(_row.get("ORGNAME")).append("\" ").append("fId=\"").append(_row.get("ORGID")).append("\" />");
                        }
                    }
                    publicGroupInfos.append("</Node>");
                }
            }
            publicGroupInfos.append("</Node>");
        }
        return publicGroupInfos.toString();
    }

    // 判断是否是第一个汇总处理人员
    private boolean isFirstJoin(String lineTo) throws Exception {
        Connection _myConn = null;
        Statement _myRead = null;
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myRead = _myConn.createStatement();
            return null == this.getJoinNode(_myRead, lineTo);
        } catch (Exception e) {
            iErrorCode = 17;
            throw e;
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

    // 结束汇总时更新待办箱状态
    private void joinFinish(Statement statement, String joinTo) throws Exception {
        String pnids = getJoinNode(statement, joinTo);
        if (pnids != null) {
            statement.executeUpdate("UPDATE G_INBOX SET STATUS=1 WHERE PID=" + this.iPID + " AND PNID in(" + pnids + ")");
        }
    }

    // 获取汇总接收人的pnid
    private String getJoinNode(Statement statement, String lineTo) throws Exception {
        ResultSet _myDs = null;
        try {
            String result = "";
            String _cmdStr="";
          //修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
           // String _cmdStr = "SELECT B.ID FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID=" + this.iPID + " AND B.ID IN (" + this.joinChilds.get(lineTo) + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + this.joinChilds.get(lineTo) + "))) AND A.ACTNAME NOT LIKE '%抄送%' AND A.STATUS=3 AND B.WFNODE_ID=" + lineTo;
            if(this.isCS==1)
            {
            	_cmdStr = "SELECT B.ID FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID=" + this.iPID + " AND B.ID IN (" + this.joinChilds.get(lineTo) + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + this.joinChilds.get(lineTo) + "))) AND A.STATUS=3 AND B.WFNODE_ID=" + lineTo;
            }
            else
            {
            	_cmdStr = "SELECT B.ID FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID=" + this.iPID + " AND B.ID IN (" + this.joinChilds.get(lineTo) + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + this.joinChilds.get(lineTo) + "))) AND B.ISCS<>1 AND A.STATUS=3 AND B.WFNODE_ID=" + lineTo;
            }
            _myDs = statement.executeQuery(_cmdStr);
            while (_myDs.next()) {
                result += _myDs.getString("ID") + ",";
            }
            return "".equals(result) ? null : result.substring(0, result.length() - ",".length());
        } catch (Exception e) {
            throw e;
        } finally {
            if (_myDs != null) {
                try {
                    _myDs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 返回待选节点列表 DataTable
    @SuppressWarnings("unchecked")
    public DataTable getSelectCurNodesViewTable(String ids) {
    	DataTable NodeTable = new DataTable("NODES");
        String[] idlist = ids.split(",");
        StringBuffer decodestr = new StringBuffer("decode(WFNODE_ID,");
        for(int i=0;i<idlist.length;i++){
        	decodestr.append(idlist[i]).append(",").append(i);
        	if(i!=idlist.length-1){
        		decodestr.append(",");
        	}
        }
        decodestr.append(")");
        String _cmdStr = "SELECT WFNODE_ID,WFNODE_CAPTION FROM WFNODELIST WHERE WF_ID=" + this.iWfID + " AND WFNODE_ID IN(" + ids + ") ORDER BY "+decodestr.toString();
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
            DataRow newRow = NodeTable.NewRow();
            newRow.put("ID", dr.getString("WFNODE_ID"));
            newRow.put("NAME", dr.getString("WFNODE_CAPTION"));
            NodeTable.Add(newRow);
        }
        return NodeTable;
    }

    // 设置所选择的增发节点信息
    public void setSelectCurNodeID(long toId) throws Exception {
        setTSNodeInfoXml(toId);
        // boolean hasCC = false;// 是否存在抄送节点
        Node nextNode = this.NextNodeInfoXml.selectSingleNode("Nodes/Node");
        // String id = nextNode.valueOf("@ID");
        String nodeType = nextNode.valueOf("@NodeType");
        // String nodeCaption = nextNode.valueOf("@NodeCaption");
        String isOnlyOneUser = nextNode.valueOf("@IsOnlyOneUser");
        // String activeCount = nextNode.valueOf("@ActiveCount");
        String wfNodeId = nextNode.valueOf("@NodeID");
        String isSMS = nextNode.valueOf("@IsSMS");
        String wf_id = nextNode.valueOf("@WfId");
        // String LineType = nextNode.valueOf("@LineType");
        // if (nodeCaption.indexOf("抄送") > -1) {
        // hasCC = true;
        // }
        nextNode.selectSingleNode("@Enabled").setText("1");
        // 当节点为远程发送时，IsOnlyOneUser=1表示自动返回给远程发送来的节点
        if ("2".equals(nodeType) && "1".equals(isOnlyOneUser)) { // ------------------远程节点
            String sExDataID = isRemoteAotoBack();
            if (!"0".equals(sExDataID)) {
                remoteAutoBack(sExDataID, nextNode);
                this.iSendType = 29;
                return;
            }
        }
        getTsNodeUList_ByNode(nextNode);// *********************************************************************处理发送用户列表
        if ("0".equals(nodeType)) { // ------------------办结节点
            if (this.isSubFlow) {// 子流程办结
                this.ds_ParentFlow = new DS_FLOWClass("<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iParentPID + "</Pid><Pnid>" + this.iParentPnID + "</Pnid><WfID>" + this.iParentWfID + "</WfID></Flow></Root>");
                if (this.ds_ParentFlow.iSendType == -1) {
                    this.iErrorCode = 50;
                    throw new Exception("未找到子流程的返回节点信息！");
                }
                this.iSendType = 10 + this.ds_ParentFlow.iSendType;
            } else {
                this.iSendType = 9;
            }
        } else if ("6".equals(nodeType)) { // ------------------跳转节点
            String sTempParms = "<Root><Flow><Type>0</Type><Key>" + this.iInfoID + "</Key><Objclass>" + this.sObjclass + "</Objclass><UserID>" + this.iUserID + "</UserID><Pid>" + this.iPID + "</Pid><Pnid>" + this.iPnID + "</Pnid><WfID>" + this.iWfID + "</WfID></Flow></Root>";
            DS_FLOWClass tempDS = new DS_FLOWClass(sTempParms, Long.parseLong(wfNodeId));
            this.NextNodeInfoXml = DocumentHelper.parseText(tempDS.NextNodeInfoXml.asXML());
            this.iSendType = tempDS.iSendType;
        } else {// ------------------默认进入选人
            // this.iSendType = 5;
            this.isSendSMS = "1".equals(isSMS);// 短信
            setRemind(wf_id,wfNodeId);
        }
        // if (hasCC) {
        // this.iSendType = 5;
        // }
    }

    // 获取特送节点
    public String getTSNodeList() {
        String sTSNodeList = "";
        String _cmdStr = "";
        
        if(FlowStaticData.RedirectSqls.containsKey(String.valueOf(this.iWfID))){
       	 int iKeyCount = 5;
       	 String[] sKey = new String[iKeyCount];
            String[] sValue = new String[iKeyCount];
            // 当前用户ID
            sKey[0] = "[USERID]";
            sValue[0] = Long.toString(this.iUserID);
            // 当前文件ID
            sKey[1] = "[INFO_ID]";
            sValue[1] = Long.toString(this.iInfoID);
            // 当前部门ID
            sKey[2] = "[DEPT_ID]";
            try {
				sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
			} catch (Exception e) {
				e.printStackTrace();
			}
            // 当前PID
            sKey[3] = "[PID]";
            sValue[3] = Long.toString(this.iPID);
            // 当前PNID
            sKey[4] = "[PNID]";
            sValue[4] = Long.toString(this.iPnID);
            String nodesql = FlowStaticData.RedirectSqls.get(String.valueOf(this.iWfID));
            for (int i = 0; i < iKeyCount; i++) {
           	 nodesql = nodesql.replace(sKey[i], sValue[i]);
            }
            
            sTSNodeList = DBManager.getFieldStringValue(nodesql);
            return sTSNodeList;
       }
        
        try {
            _cmdStr = "SELECT WFNODE_XML FROM WFNODELIST WHERE WF_ID=" + this.iWfID + " AND WFNODE_NODETYPE=1";// 开始节点上配置的特送节点
            String sXml = "";
            xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
            for (xsf.data.DataRow dr : dt.getRows()) {
                sXml = dr.getString("WFNODE_XML");
                Document NDXml = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Node>" + sXml + "</Node>");
                sTSNodeList = NDXml.selectSingleNode("Node/OtherControl/TSNodeList").getText();
                sTSNodeList = sTSNodeList.trim();
                sTSNodeList = ",".equals(sTSNodeList) ? "" : sTSNodeList;
                break;
            }
        } catch (Exception e) {
            sTSNodeList = "";
        }
        return sTSNodeList;
    }

    private long getPid(long infoId) {
        long pid = 0;
        if (infoId == 0) {
            return pid;
        }
        String _cmdStr = "SELECT PID FROM G_PNODES WHERE INFO_ID=" + infoId;
        pid = DBManager.getFieldLongValue(_cmdStr);
        return pid;
    }

    // 获取特送节点信息
    private void setTSNodeInfoXml(long wfNodeId) {
        String _cmdStr = "";
        _cmdStr = "SELECT A.WF_ID,A.WFNODE_ID,A.WFNODE_NODETYPE,A.PWF_ID,A.WFNODE_CAPTION,A.WFNODE_FORWARD AS ISSMS,A.WFNODE_AFTERSENDSQL,";
        _cmdStr += "A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER ";
        _cmdStr += "FROM WFNODELIST A WHERE A.WF_ID=" + this.iWfID + " AND A.WFNODE_ID=" + wfNodeId;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        StringBuffer sNextNodes =new StringBuffer();
        sNextNodes.append("<Nodes>");
        try {
            for (xsf.data.DataRow dr : dt.getRows()) {
                // 节点类型（0：结束节点 1：开始节点 2：远程 3：一般节点 4：汇总节点）
                long PWfId = 0;
                if (dr.getString("PWF_ID") != null && !"".equals(dr.getString("PWF_ID"))) {
                    PWfId = dr.getLong("PWF_ID");
                }
                String sSelected = dr.getString("WFNODE_ISSELECTED") != null ? dr.getString("WFNODE_ISSELECTED") : "";
                String sMultiUser = dr.getString("WFNODE_ISMULTIUSER") != null ? dr.getString("WFNODE_ISMULTIUSER") : "";
                // String sLineCaption = dr.getString("WFLINE_CAPTION") != null ? dr.getString("WFLINE_CAPTION") : "";
                String sNodeCaption = dr.getString("WFNODE_CAPTION") != null ? dr.getString("WFNODE_CAPTION") : "";
                String sWfNodeId = dr.getString("WFNODE_ID") != null ? dr.getString("WFNODE_ID") : "";
                String sWait = dr.getString("WFNODE_WAIT") != null ? dr.getString("WFNODE_WAIT") : "";
                String sTimeType = dr.getString("WFNODE_TIMETYPE") != null ? dr.getString("WFNODE_TIMETYPE") : "";
                String sTimeSpan = dr.getString("WFNODE_TIMESPAN") != null ? dr.getString("WFNODE_TIMESPAN") : "";
                String sSendMethod = dr.getString("WFNODE_SENDMETHOD") != null ? dr.getString("WFNODE_SENDMETHOD") : "";
                String sNodeType = dr.getString("WFNODE_NODETYPE") != null ? dr.getString("WFNODE_NODETYPE") : "";
                // String sAcondition = dr.getString("ACONDITION") != null ? dr.getString("ACONDITION").trim() : "";
                String sIsOnlyOneUser = dr.getString("WFNODE_ISONLYONEUSER") != null ? dr.getString("WFNODE_ISONLYONEUSER").trim() : "";
                // String sLineType = dr.getString("LineType") != null ? dr.getString("LineType").trim() : "";
                String sIsSMS = dr.getString("ISSMS") != null ? dr.getString("ISSMS").trim() : "";
                String sAcl = "";
                String sSubFlowID = "";
                // String sAconditionFlag = dr.getString("AconditionFlag") != null ? dr.getString("AconditionFlag").trim() : "";
                String sAfterSendSql = dr.getString("WFNODE_AFTERSENDSQL") != null ? dr.getString("WFNODE_AFTERSENDSQL").trim() : "";
                if (this.isSendSMS == false) {
                    this.isSendSMS = ("1".equals(dr.getString("ISSMS") != null ? dr.getString("ISSMS").trim() : ""));
                    setRemind(dr.getString("WF_ID"),dr.getString("WFNODE_ID"));
                }
                // 取WFNODE_XML
                // 吴红亮 更新 开始
                // String wf_id = dr.getString("WF_ID") != null ? dr.getString("WF_ID") : "";
                // String wfnode_id = dr.getString("WFNODE_ID") != null ? dr.getString("WFNODE_ID") : "";
                // String sxml = getWFNodeXML(wf_id, wfnode_id);
                String sxml = dr.getString("WFNODE_XML") != null ? dr.getString("WFNODE_XML") : "";
                String IsAutoExpand = "";
                // 吴红亮 更新 结束
                // ----------------------------------------------------处理 WFNODE_XML
                String sFileTranServiceXML = "";
                if (sxml.indexOf("FileTranServiceXML") != -1) {
                    sFileTranServiceXML = sxml.substring(sxml.indexOf("FileTranServiceXML") + 20);
                    sFileTranServiceXML = sFileTranServiceXML.substring(0, sFileTranServiceXML.indexOf("\""));
                }
                Document NDXml = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>" + sxml + "</Root>");
                // 判断汇总节点的文完成数
                int iActiveCount = 0;
                // if ("4".equals(sNodeType) && "2".equals(sLineType)) {// 下一结点为“汇总节点” 并且 到下一结点的连线为“汇总线”
                // iActiveCount = getActiveCount(sWfNodeId, NDXml.selectSingleNode("Root/ADSet/HZNode"));
                // }
                
                String desc = "";//备注中如果有重定向信息那么特送重定向到指定节点
                // 获取节点展开类型
                try {
                    IsAutoExpand = NDXml.selectSingleNode("Root/ACL/IsAutoExpand").getText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if(NDXml.selectSingleNode("Root/BaseInfo/Desc")!=null){
                	desc = NDXml.selectSingleNode("Root/BaseInfo/Desc").getText();
                }
               
                String gatherNode = "0";
                if(desc.contains("汇合人员")){
                	gatherNode = "1";
                }else if(desc.contains("汇合部门人员")){
                	gatherNode = "2";
                }
                
                if(desc.contains("重定向节点#")){
                	try{
                		long redirectid = Long.parseLong(desc.substring(desc.indexOf("重定向节点#")+6));
                		String sql = "";
                        sql = "SELECT A.WF_ID,A.WFNODE_ID,A.WFNODE_NODETYPE,A.PWF_ID,A.WFNODE_CAPTION,A.WFNODE_FORWARD AS ISSMS,A.WFNODE_AFTERSENDSQL,";
                        sql += "A.WFNODE_ISSELECTED,A.WFNODE_ISMULTIUSER,A.WFNODE_WAIT,A.WFNODE_TIMETYPE,A.WFNODE_TIMESPAN,A.WFNODE_SENDMETHOD,A.WFNODE_XML,A.WFNODE_ISONLYONEUSER ";
                        sql += "FROM WFNODELIST A WHERE A.WF_ID=" + this.iWfID + " AND A.WFNODE_ID=" + redirectid;
                        xsf.data.DataTable dtredirect = DBManager.getDataTable(sql);
                        if(dtredirect!=null && dtredirect.getRowCount()>0){
                        	xsf.data.DataRow red = dtredirect.getRow(0);
                        	String redxml = red.getString("WFNODE_XML") != null ? red.getString("WFNODE_XML") : "";
                        	Document reddoc = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>" + redxml + "</Root>");
                        	this.redirectAcl = reddoc.selectSingleNode("Root/ACL");
                        }
                	}catch(Exception e){
                		System.out.println("number:"+desc.substring(desc.indexOf("重定向节点#")+6)+" format error!");
                	}
                }
                
                sAcl = Dom4jTools.getSingleNodeInnerXml(NDXml, "Root/ACL");
                if ("5".equals(sNodeType)) {
                    try {
                        sSubFlowID = Dom4jTools.getSingleNodeInnerXml(NDXml, "Root/BaseInfo/SubFlowID");
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("子流程设置错误！");
                    }
                }
                // try {
                // if ("1".equals(NDXml.selectSingleNode("Root/BaseInfo/IsEMail").getText())) {
                // this.isSendEmail = true;
                // }
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // try {
                // if ("1".equals(NDXml.selectSingleNode("Root/BaseInfo/IsTray").getText())) {
                // this.isSendTray = true;
                // }
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
                // ----------------------------------------------------
                // 判断连线的有效性
                // if (isAconditionOK(sAcondition, sAconditionFlag)) {
                sNextNodes.append("<Node Enabled=\"1\" ");
                sNextNodes.append( "PWfID=\"" + PWfId + "\" ");
                sNextNodes.append( "ID=\"0\" ");
                sNextNodes.append( "NodeType=\"" + sNodeType + "\" ");
                sNextNodes.append( "SubFlowID=\"" + sSubFlowID + "\" ");
                sNextNodes.append( "ActiveCount=\"" + iActiveCount + "\" ");
                sNextNodes.append( "WfId=\"" + this.iWfID + "\" ");
                sNextNodes.append( "Selected=\"" + sSelected + "\" ");
                sNextNodes.append( "MultiUser=\"" + sMultiUser + "\" ");
                sNextNodes.append( "LineCaption=\"\" ");
                sNextNodes.append( "NodeCaption=\"" + sNodeCaption + "\" ");
                sNextNodes.append( "NodeID=\"" + sWfNodeId + "\" ");
                sNextNodes.append( "Wait=\"" + sWait + "\" ");
                sNextNodes.append( "TimeType=\"" + sTimeType + "\" ");
                sNextNodes.append( "TimeSpan=\"" + sTimeSpan + "\" ");
                sNextNodes.append( "SendMethod=\"" + sSendMethod + "\" ");
                sNextNodes.append( "NodeDate=\"\" ");
                sNextNodes.append( "FileTranServiceXML=\"" + sFileTranServiceXML + "\" ");
                /**
                 * 如果该节点是汇总节点，则该节点不继承节点经办人
                 * 开始cuij
                 */
                if("4".equals(sNodeType)){
                	sIsOnlyOneUser="0";
                }
                /**
                 * 结束
                 */
                sNextNodes.append( "IsOnlyOneUser=\"" + sIsOnlyOneUser + "\" ");
                sNextNodes.append( "LineType=\"\" ");
                sNextNodes.append( "IsZNG=\"" + getIsZNG(sWfNodeId) + "\" ");
                sNextNodes.append( "IsSMS=\"" + sIsSMS + "\" ");
                sNextNodes.append( "IsHaveUList=\"0\" ");
                sNextNodes.append( "GatherNode=\""+gatherNode+"\" ");
                sNextNodes.append( "AfterSendSql=\"" + sAfterSendSql + "\" ");
                sNextNodes.append( "IsAutoExpand=\"" + IsAutoExpand + "\" ");
                sNextNodes.append( "Acondition=\"\">");
                sNextNodes.append( "<ACL>" + sAcl + "</ACL>");
                sNextNodes.append( "</Node>");
                // }
            }
            sNextNodes.append( "</Nodes>");
            this.NextNodeInfoXml = DocumentHelper.parseText(sNextNodes.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取特送人员列表
    private void getTsNodeUList_ByNode(Node _mynode) throws Exception {
        // System.out.println(_mynode.asXML());
        String sAcl = "";
        try {
            if ("1".equals(_mynode.valueOf("@IsHaveUList"))) {
                return;
            }
            /*// 判断是否唯一经办人
            if (!"2".equals(_mynode.valueOf("@NodeType")) && "1".equals(_mynode.valueOf("@IsOnlyOneUser")) && !"1".equals(_mynode.valueOf("@LineType"))) {
                sAcl = this.getTSOnlyOneUser(_mynode.valueOf("@NodeID"), 0);
            }*/
            Node aclnode = redirectAcl==null?_mynode.selectSingleNode("ACL"):redirectAcl;
            if ("".equals(sAcl)) {
                sAcl = getTSUTree(aclnode);
                // 私有小组，统计局专用
                if ("1".equals(_mynode.valueOf("@MultiUser")) && !"4".equals(aclnode.selectSingleNode("type").getText()) && this.isGroupView) {
                    // 可多人办理时，出小组(私有小组，统计局用)
                    // 吴红亮 修改 开始
                    if (hasDisplayGroupNode()) {
                        sAcl += getPublicUserGroup();
                    }
                    // 吴红亮 修改 结束
                    sAcl += getPrivateGroup();
                }
            }
            ((Element) _mynode).clearContent();// 清除<ACL>子节点
            if (!"".equals(sAcl)) {
                ((Element) _mynode).appendContent((DocumentHelper.parseText("<root>" + sAcl + "</root>").getRootElement()));// 添加<Node>子节点
            }
            ((Attribute) _mynode.selectSingleNode("@IsHaveUList")).setValue("1");
        } catch (Exception e) {
            e.printStackTrace();
            _mynode.setText(sAcl);
            this.iErrorCode = 40;
            throw e;
        }
        // System.out.println("xml:-----" + _mynode.asXML());
    }

    private String getTSUTree(Node AclTD) throws Exception {
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        String reStr = "";
        String sRole_IDList = "0";
        String _cmdStr = "";
        // 整理ACL
        int iIsRoleSelect = Integer.parseInt(AclTD.selectSingleNode("IsRoleSelect").getText());
        // int iIsSelected = Integer.parseInt(AclTD.selectSingleNode("IsSelected").getText());
        // int iIsOnlyOneUser = Integer.parseInt(AclTD.selectSingleNode("IsOnlyOneUser").getText());
        // int iIsMultiUser = Integer.parseInt(AclTD.selectSingleNode("IsMultiUser").getText());
        int iType = Integer.parseInt(AclTD.selectSingleNode("type").getText());
        DataTable AclTable = new DataTable("ACL");
        AclTable.columns.put("DEPT_ID", String.class);
        AclTable.columns.put("DEPT_NAME", String.class);
        AclTable.columns.put("ROLE_ID", String.class);
        AclTable.columns.put("ROLE_NAME", String.class);
        AclTable.columns.put("MAINUNION", String.class);
        switch (iType) {
        case 0:
        case 9:
            // 通过部门选角色+远程部门
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                String sDept_ID = td.selectSingleNode("dept").getText();
                String sDept_Name = td.selectSingleNode("dept").valueOf("@Name").trim();
                String sRole_ID = td.selectSingleNode("role").getText();
                String sRole_Name = td.selectSingleNode("role").valueOf("@Name").trim();
                sRole_IDList += "," + sRole_ID;
                boolean isHas = false;
                for (DataRow tr : AclTable.rows) {
                    if (sDept_ID.equals(tr.get("DEPT_ID"))) {
                        tr.put("ROLE_ID", tr.get("ROLE_ID") + "," + sRole_ID);
                        tr.put("ROLE_NAME", tr.get("ROLE_NAME") + "," + sRole_Name);
                        isHas = true;
                        break;
                    }
                }
                if (!isHas) {
                    DataRow tr = AclTable.NewRow();
                    tr.put("DEPT_ID", sDept_ID);
                    tr.put("DEPT_NAME", sDept_Name);
                    tr.put("ROLE_ID", sRole_ID);
                    tr.put("ROLE_NAME", sRole_Name);
                    AclTable.rows.add(tr);
                }
            }
            break;
        case 1:
            // 继承节点部门
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                String sDept_ID = td.selectSingleNode("dept").getText();
                String sDept_Name = td.selectSingleNode("dept").valueOf("@Name").trim();
                String sRole_ID = td.selectSingleNode("role").getText();
                String sRole_Name = td.selectSingleNode("role").valueOf("@Name").trim();
                String sLevel = td.selectSingleNode("dept").valueOf("@Level").trim();
                sRole_IDList += "," + sRole_ID;
                if (this.iPID == 0 || this.iPnID == 0) {
                    String sMainUserID = null;
                    // 吴红亮 添加 开始 过滤跨部门
                    String sMainDeptID = null;
                    // 吴红亮 添加 结束
                    // // _cmdStr = "select ID,UNAME from G_USERS where ID=(select USER_ID from G_INFOS where ID=" + this.iInfoID + ")";
                    // _cmdStr = "select USER_ID,MAINDEPT from G_INFOS A,G_USERS B where A.USER_ID=B.ID and B.STATUS>-1 and A.ID=" + this.iInfoID;
                    // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
                    sqlDataSource.setParameter("INFO_ID", this.iInfoID);
                    xsf.data.DataTable dt = sqlDataSource.query("getMainDept");
                    if (dt.getRows().size() > 0) {
                        xsf.data.DataRow dr = dt.getRows().get(0);
                        sMainUserID = dr.getString("USER_ID") != null ? dr.getString("USER_ID") : "";
                        sMainDeptID = dr.getString("MAINDEPT") != null ? dr.getString("MAINDEPT") : "";
                    }
                    if (null == sMainUserID) {
                        this.iErrorCode = 101;
                        throw new Exception();
                    }
                    for (int i = 0; i < this.iUserCount; i++) {
                        // if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(sMainUserID)) {
                        if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(sMainUserID) && (!dsoap.tools.ConfigurationSettings.isCrossDept || (dsoap.tools.ConfigurationSettings.isCrossDept && this.arrUserList[i][G_USERS.DEPT_ID.ordinal()].equals(sMainDeptID)))) {
                            sDept_ID += "," + this.arrUserList[i][G_USERS.DEPT_ID.ordinal()];
                            sDept_Name += "," + this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
                            // break;
                        }
                    }
                } else {// sDept_ID为要继承的nodeid
                    if ("-1".equals(sDept_ID)) {// 发送节点
                        sDept_ID = String.valueOf(this.iWfNodeID);
                    }
                    TempDept tempd = getOnlyOneDeptInfo(sDept_ID);
                    sDept_ID = tempd.getSDept_ID();
                    sDept_Name = tempd.getSDept_Name();
                    if ("".equals(sDept_ID != null ? sDept_ID.trim() : "")) {
                        continue;
                    }
                }
                String[] sDeptIDList = sDept_ID.split(",");
                String[] sDeptNameList = sDept_Name.split(",");
                String strDepts = "";
                // 根据部门级别查找当前部门
                if (!"0".equals(sLevel)) {
                    for (int i = 1; i < sDeptIDList.length; i++) {
                        strDepts += getDeptFromLevel(sDeptIDList[i], sLevel);
                    }
                } else {
                    for (int i = 1; i < sDeptIDList.length; i++) {
                        strDepts += sDeptIDList[i] + "^" + sDeptNameList[i] + ",";
                    }
                }
                if (!"".equals(strDepts.trim())) {
                    strDepts = strDepts.substring(0, strDepts.length() - 1);
                    String[] strDeptList = strDepts.split(",");
                    for (String s : strDeptList) {
                        sDept_ID = s.split("\\^")[0];
                        sDept_Name = s.split("\\^")[1];
                        boolean isHas = false;
                        for (DataRow tr : AclTable.rows) {
                            if (sDept_ID.equals(tr.get("DEPT_ID"))) {
                                tr.put("ROLE_ID", tr.get("ROLE_ID") + "," + sRole_ID);
                                tr.put("ROLE_NAME", tr.get("ROLE_NAME") + "," + sRole_Name);
                                isHas = true;
                                break;
                            }
                        }
                        if (!isHas) {
                            DataRow tr = AclTable.NewRow();
                            tr.put("DEPT_ID", sDept_ID);
                            tr.put("DEPT_NAME", sDept_Name);
                            tr.put("ROLE_ID", sRole_ID);
                            tr.put("ROLE_NAME", sRole_Name);
                            AclTable.rows.add(tr);
                        }
                    }
                }
            }
            break;
        case 2:
            // SQL选部门+人
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                String sSql = td.selectSingleNode("dept").getText();
                String sRole_ID = td.selectSingleNode("role").getText();
                String sRole_Name = td.selectSingleNode("role").valueOf("@Name").trim();
                sRole_IDList += "," + sRole_ID;
                getDeptBySql(sSql, sRole_ID, sRole_Name, AclTable);
            }
            break;
        case 3:
            // 继承节点经办人
            reStr = "";
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                reStr += getTSOnlyOneUser(td.selectSingleNode("role").getText(), 1);
            }
            if ("".equals(reStr)) {
                throw new Exception("经办人不存在！");
            }
            break;
        case 4:// SQL选人
            for (Object obj : AclTD.selectNodes("deptrole")) {
                Node td = (Node) obj;
                String sSelectUserSql = td.selectSingleNode("role").getText();
                reStr += getUserBySql(sSelectUserSql);
            }
            break;
        }
        // 0，1，9三种方式继续
        // // 取角色和部门信息;获取所有的小组人员结构
        // // 吴红亮 修改 开始
        // // _cmdStr = "SELECT DISTINCT TOP (100) PERCENT A.GRP_ID,A.USER_ID,B.UNAME AS USER_NAME,B.UTYPE AS USER_UTYPE,D.USER_ID AS DEPT_ID,E.UNAME AS DEPT_NAME,G.UNAME AS MAINUNION " + "FROM G_GRPS A,G_USERS B LEFT JOIN G_USERS G ON B.MAINCODE=G.ID,G_DEPT C,G_DEPT D,G_USERS E,G_USERS F " + "WHERE A.USER_ID=B.ID AND B.ID=C.USER_ID AND C.FID=D.ID AND D.USER_ID=E.ID AND A.GRP_ID=F.ID " + " AND (F.UTYPE=1 OR ( A.GRP_ID IN (" + sRole_IDList + ") ))" + "ORDER BY GRP_ID,USER_ID";
        // // String str = "select count(*) as num from (" + _cmdStr + ") AS derivedtbl_1";
        // if (!dsoap.tools.ConfigurationSettings.isRoleDept) {
        // _cmdStr = "SELECT DISTINCT A.GRP_ID,A.USER_ID,B.UNAME AS USER_NAME,B.UTYPE AS USER_UTYPE,D.USER_ID AS DEPT_ID,E.UNAME AS DEPT_NAME,G.UNAME AS MAINUNION FROM G_GRPS A,G_USERS B LEFT JOIN G_USERS G ON B.MAINCODE=G.ID,G_DEPT C,G_DEPT D,G_USERS E,G_USERS F WHERE A.USER_ID=B.ID AND B.ID=C.USER_ID AND C.FID=D.ID AND D.USER_ID=E.ID AND A.GRP_ID=F.ID AND (F.UTYPE=1 OR (A.GRP_ID IN (" + sRole_IDList + ")))";
        // } else {
        // _cmdStr = "SELECT DISTINCT A.GRP_ID,A.USER_ID,B.UNAME AS USER_NAME,B.UTYPE AS USER_UTYPE,D.USER_ID AS DEPT_ID,E.UNAME AS DEPT_NAME,G.UNAME AS MAINUNION FROM G_GRPS A,G_USERS B LEFT JOIN G_USERS G ON B.MAINCODE=G.ID,G_DEPT C,G_DEPT D,G_USERS E,G_USERS F WHERE A.USER_ID=B.ID AND B.ID=C.USER_ID AND C.FID=D.ID AND D.USER_ID=E.ID AND A.GRP_ID=F.ID AND A.DEPTID=E.ID AND (F.UTYPE=1 OR (A.GRP_ID IN (" + sRole_IDList + ")))";
        // }
        // String str = "select count(*) as num from (" + _cmdStr + ") derivedtbl_1";
        // _cmdStr += " ORDER BY GRP_ID,USER_ID";
        // // 吴红亮 修改 结束
        // xsf.data.DataTable dt = DBManager.getDataTable(str);
        // if (dt.getRows().size() > 0) {
        // xsf.data.DataRow dr = dt.getRows().get(0);
        // this.iGrpsCount = dr.getInt("num");
        // }
        // dt = DBManager.getDataTable(_cmdStr);
        if (!dsoap.tools.ConfigurationSettings.isRoleDept) {
            _cmdStr = "getRoleUsers";
        } else {
            _cmdStr = "getRoleDeptUsers";
        }
        sqlDataSource = SysDataSource.getSysDataSource();
        xsf.data.DataTable dt = sqlDataSource.query(_cmdStr);
        if (dt != null) {
            this.iGrpsCount = dt.getRows().size();
        }
        this.arrGrpsList = new String[this.iGrpsCount][7];
        DataTable G_GRPS = new DataTable("G_GRPS");
        int i = 0;
        for (xsf.data.DataRow row : dt.getRows()) {
            DataRow dr = G_GRPS.NewRow();
            this.arrGrpsList[i][0] = row.getString("GRP_ID") != null ? row.getString("GRP_ID") : "";
            this.arrGrpsList[i][1] = row.getString("USER_ID") != null ? row.getString("USER_ID") : "";
            this.arrGrpsList[i][2] = row.getString("USER_NAME") != null ? row.getString("USER_NAME") : "";
            this.arrGrpsList[i][3] = row.getString("USER_UTYPE") != null ? row.getString("USER_UTYPE") : "";
            this.arrGrpsList[i][4] = row.getString("DEPT_ID") != null ? row.getString("DEPT_ID") : "";
            this.arrGrpsList[i][5] = row.getString("DEPT_NAME") != null ? row.getString("DEPT_NAME") : "";
            this.arrGrpsList[i][6] = row.getString("MAINUNION") != null ? row.getString("MAINUNION") : "";
            // 构造datarow
            dr.put("GRP_ID", this.arrGrpsList[i][0]);
            dr.put("USER_ID", this.arrGrpsList[i][1]);
            dr.put("USER_NAME", this.arrGrpsList[i][2]);
            dr.put("USER_UTYPE", this.arrGrpsList[i][3]);
            dr.put("DEPT_ID", this.arrGrpsList[i][4]);
            dr.put("DEPT_NAME", this.arrGrpsList[i][5]);
            dr.put("MAINUNION", this.arrGrpsList[i][6]);
            G_GRPS.Add(dr);
            i++;
        }
        // 给map中添加相关datatable
        this.map.put("G_GRPS", G_GRPS);
        for (DataRow tr : AclTable.rows) {
            reStr += "<Node Id=\"" + tr.get("DEPT_ID") + "\" UType=\"2\" UName=\"" + tr.get("DEPT_NAME") + "\" fId=\"\" fName=\"\" gId=\"\" phone=\"\" topname=\"\">";
            if (iIsRoleSelect == 1) {
                // 通过角色选人
                reStr += getUList(tr.get("DEPT_ID"), tr.get("DEPT_NAME"), tr.get("ROLE_ID"), tr.get("ROLE_NAME"), tr.get("MAINUNION"));
            } else {
                // 发送到角色
                String[] sRoleIDList = tr.get("ROLE_ID").split(",");
                String[] sRoleNameList = tr.get("ROLE_NAME").split(",");
                reStr += getRoleList(tr.get("DEPT_ID"), tr.get("DEPT_NAME"), sRoleIDList, sRoleNameList, tr.get("MAINUNION"));
            }
            reStr += "</Node>";
        }
        return reStr;
    }

    // 选取特送节点唯一经办人(iType=0:继承本节点经办人)
    private String getTSOnlyOneUser(String wfNodeId, int iType) throws Exception {
        String reStr = "";
        // String _cmdStr = "";
        if (iType != 0 && (this.iPID == 0 || this.iPnID == 0)) {
            for (int i = 0; i < this.iUserCount; i++) {
                if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(String.valueOf(this.iUserID))) {
                    reStr = "<Node Id=\"" + this.arrUserList[i][G_USERS.ID.ordinal()] + "\" UType=\"" + this.arrUserList[i][G_USERS.UTYPE.ordinal()] + "\" UName=\"" + this.arrUserList[i][G_USERS.UNAME.ordinal()] + "\" fId=\"" + this.arrUserList[i][G_USERS.DEPT_ID.ordinal()] + "\" fName=\"" + this.arrUserList[i][G_USERS.DETP_NAME.ordinal()] + " \" gId=\"\" phone=\"\"  topname=\"" + this.arrUserList[i][10] + "\"></Node>";
                    break;
                }
            }
        } else {
            // // _cmdStr = "select A.id,A.fid,A.wfnode_id,A.user_id,A.dept_id,B.UTYPE,B.UNAME as USERNAME,C.UNAME as DEPTNAME from g_pnodes A,G_USERS B,G_USERS C where pid="+iPID.ToString()+" AND A.USER_ID=B.ID AND A.DEPT_ID=C.ID order by id desc";
            // // _cmdStr = "select A.ID,A.FID,A.WFNODE_ID,A.USER_ID,A.DEPT_ID,0 AS UTYPE,A.UNAME AS USERNAME,'' as DEPTNAME from G_PNODES A where PID=" + this.iPID + " order by ID desc";
            // _cmdStr = "select A.ID,A.FID,A.WFNODE_ID,A.USER_ID,A.MUSER_ID,B.UTYPE,B.UNAME as USERNAME,A.UNAME as MUSERNAME,DEPT_ID,MDEPT_ID from G_PNODES A,G_USERS B where A.PID=" + this.iPID + " AND A.USER_ID=B.ID order by ID desc";
            // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
            IDataSource sqlDataSource = SysDataSource.getSysDataSource();
            sqlDataSource.setParameter("PID", this.iPID);
            xsf.data.DataTable dt = sqlDataSource.query("getOnlyOneUserID");
            for (xsf.data.DataRow dr : dt.getRows()) {
                // String ID = _myDs.getString("ID");
                // if (curFid.equals(ID)) {
                String WFNODE_ID = dr.getString("WFNODE_ID");
                // String FID = dr.getString("FID");
                if (wfNodeId.equals(WFNODE_ID)) {
                    String USER_ID = dr.getString("USER_ID");
                    String UTYPE = dr.getString("UTYPE");
                    String USERNAME = dr.getString("USERNAME");
                    String DEPT_ID = dr.getString("DEPT_ID");
                    String MUSER_ID = dr.getString("MUSER_ID");
                    String MUSERNAME = dr.getString("MUSERNAME");
                    String MDEPT_ID = dr.getString("MDEPT_ID");
                    // ----------------------------------------------------
                    USER_ID = USER_ID != null ? USER_ID : "";
                    UTYPE = UTYPE != null ? UTYPE : "";
                    USERNAME = USERNAME != null ? USERNAME : "";
                    DEPT_ID = DEPT_ID != null ? DEPT_ID : "";
                    MUSER_ID = MUSER_ID != null ? MUSER_ID : "";
                    MUSERNAME = MUSERNAME != null ? MUSERNAME : "";
                    MDEPT_ID = MDEPT_ID != null ? MDEPT_ID : "";
                    if (("0".equals(UTYPE) || "9".equals(UTYPE)) && !USER_ID.equals(MUSER_ID)) {
                        USER_ID = MUSER_ID;
                        USERNAME = MUSERNAME;
                        DEPT_ID = MDEPT_ID;
                    }
                    if (USER_ID == null || "".equals(USER_ID)) {
                        continue;
                    }
                    // ----------------------------------------------------
                    // 查找当前用户所在部门
                    String sFDeptID = "";
                    String sFDeptName = "";
                    String sTopName = "";
                    for (int i = 0; i < this.iUserCount; i++) {
                        // if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(USER_ID)) {
                        if (this.arrUserList[i][G_USERS.ID.ordinal()].equals(USER_ID) && this.arrUserList[i][G_USERS.DEPT_ID.ordinal()].equals(DEPT_ID)) {
                            sFDeptID = this.arrUserList[i][G_USERS.DEPT_ID.ordinal()];
                            sFDeptName = this.arrUserList[i][G_USERS.DETP_NAME.ordinal()];
                            sTopName = this.arrUserList[i][10];
                            reStr = "<Node Id=\"" + USER_ID + "\" UType=\"" + UTYPE + "\" UName=\"" + USERNAME + "\" fId=\"" + sFDeptID + "\" fName=\"" + sFDeptName + " \" gId=\"\" phone=\"\" topname=\"" + sTopName + "\"></Node>";
                            break;
                        }
                    }
                    // System.out.println("***" + reStr);
                    break;
                } else {
                    // curFid = FID != null ? FID : "";
                }
                // }
            }
        }
        return reStr;
    }

    // 获取汇总文件ID
    private String getJoinFileId(Statement statement, String INFO_ID) throws Exception {
        String _cmdStr = "SELECT MAININFO_ID FROM G_INFOS WHERE ID=" + INFO_ID;
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        String joinInfoId = null;
        if (_myDs.next()) {
            joinInfoId = _myDs.getString("MAININFO_ID");
        }
        _myDs.close();
        return joinInfoId;
    }

    // 文件是否停在某节点
    private boolean isStop(Statement statement, String WF_ID, String WFNODE_ID, String JOIN_INFO_ID) throws Exception {
        boolean result = false;
        if (JOIN_INFO_ID != null && !"".equals(JOIN_INFO_ID)) {
            String _cmdStr = "SELECT * FROM G_INBOX WHERE INFO_ID=" + JOIN_INFO_ID + " AND WF_ID=" + WF_ID + " AND WFNODE_ID=" + WFNODE_ID;
            ResultSet _myDs = statement.executeQuery(_cmdStr);
            result = _myDs.next();
            _myDs.close();
        }
        return result;
    }

    // 文件是否经过某节点
    private boolean isPass(Statement statement, String WF_ID, String WFNODE_ID, String JOIN_INFO_ID) throws Exception {
        boolean result = true;
        if (JOIN_INFO_ID != null && !"".equals(JOIN_INFO_ID)) {
            String _cmdStr = "SELECT * FROM G_PNODES WHERE STATUS=-1 AND INFO_ID=" + JOIN_INFO_ID + " AND WF_ID=" + WF_ID + " AND WFNODE_ID=" + WFNODE_ID;
            ResultSet _myDs = statement.executeQuery(_cmdStr);
            result = _myDs.next();
            _myDs.close();
        }
        return result;
    }

    // // 获取汇总接收人
    // private String getReceivers(Statement statement, String lineTo) throws Exception {
    // String result = "";
    // String _cmdStr = "SELECT B.UNAME FROM G_PNODES B,G_INBOX A WHERE A.PID=B.PID AND A.PNID=B.ID AND ((B.PID=" + this.iPID + " AND B.ID IN (" + this.joinChilds.get(lineTo) + ")) OR (B.PARENTFLOWPID=" + this.iPID + " AND B.PARENTFLOWPNID IN (" + this.joinChilds.get(lineTo) + "))) AND A.ACTNAME NOT LIKE '%抄送%' AND A.STATUS=3 AND B.WFNODE_ID=" + lineTo;
    // ResultSet _myDs = statement.executeQuery(_cmdStr);
    // while (_myDs.next()) {
    // result += _myDs.getString("UNAME") + "、";
    // }
    // _myDs.close();
    // return "".equals(result) ? "" : result.substring(0, result.length() - "、".length());
    // }

    // private static sun.misc.BASE64Encoder base64encoder = new sun.misc.BASE64Encoder();
    // private String ToBase64FromString(String strSource) throws Exception {
    // strSource = base64encoder.encode(strSource.getBytes("UTF-8"));
    // return strSource;
    // }

    // 获取节点办结期限
    // private String getNodeDate() throws Exception {
    // for (Object obj : this.NextNodeInfoXml.selectNodes("Nodes/Node")) {
    // Node td = (Node) obj;
    // if ("1".equals(td.valueOf("@Enabled"))) {
    // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
    // return getEndTime(sdf.format(new Date()), Integer.parseInt(td.valueOf("@TimeSpan")), Integer.parseInt(td.valueOf("@TimeType")));
    // } else {
    // continue;
    // }
    // }
    // return "0";
    // }

    // private String GetNodeName(long uid) throws Exception {
    // Connection _myConn = null;
    // Statement _myRead = null;
    // ResultSet _myDs = null;
    // String _cmdStr = "";
    // String sNodeName = "";
    // try {
    // _myConn = new JdbcConnection().getConnection();
    // _myRead = _myConn.createStatement();
    // _cmdStr = "SELECT NOTE FROM G_USERS WHERE ID = " + uid;
    // _myDs = _myRead.executeQuery(_cmdStr);
    // if (_myDs.next()) {
    // sNodeName = _myDs.getString("NOTE");
    // }
    // _myDs.close();
    // return sNodeName;
    // } catch (Exception e) {
    // iErrorCode = 17;
    // throw e;
    // } finally {
    // try {
    // _myRead.close();
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // if (_myConn != null) {
    // try {
    // _myConn.close();
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // }

    // private String getWFNodeXML(String wf_id, String wfnode_id) throws Exception {
    // Connection _myConn = null;
    // Statement _myRead = null;
    // ResultSet _myDs = null;
    // String _cmdStr = "";
    // try {
    // _myConn = new JdbcConnection().getConnection();
    // _myRead = _myConn.createStatement();
    // _cmdStr = "SELECT WFNODE_XML FROM WFNODELIST WHERE WF_ID=" + wf_id + " AND WFNODE_ID=" + wfnode_id;
    // _myRead.execute(_cmdStr);
    // String ret = "";
    // _myDs = _myRead.getResultSet();
    // if (_myDs.next()) {
    // ret = _myDs.getString("WFNODE_XML") != null ? _myDs.getString("WFNODE_XML") : "";
    // }
    // _myDs.close();
    // return ret;
    // } catch (Exception es) {
    // this.iErrorCode = 14;
    // throw (es);
    // } finally {
    // try {
    // _myRead.close();
    // } catch (SQLException e1) {
    // e1.printStackTrace();
    // }
    // if (_myConn != null) {
    // try {
    // _myConn.close();
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // }

    // 获取短信内容
    private String getSmsContent() throws Exception {
        String sql = "select D.ID from G_USERS U,G_DEPT U1,G_DEPT D1,G_USERS D where U.ID=U1.USER_ID and U1.FID=D1.ID and D1.USER_ID=D.ID and U.ID=" + this.iUserID;
        String reStr = "";
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 6;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = Long.toString(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = Long.toString(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = Long.toString(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = Long.toString(this.iPnID);
        // 当前动作名称
        sKey[5] = "[CAPTION]";
        sValue[5] = this.sCaption;
        // 获取短信模版
        String sXml = "";
        sql = "select WF_XML from WFDEFINITION where WF_ID=" + this.iWfID;
        String temp = DBManager.getFieldStringValue(sql);
        if (temp == null || "".equals(temp)) {
            return "";
        }
        sXml = "<root>" + temp + "</root>";
        // ---------------------------------
        Document smsXml = DocumentHelper.parseText(sXml);
        String sSmsSqls = smsXml.selectSingleNode("/root/SMS/Sql").getText();
        reStr = smsXml.selectSingleNode("root/SMS/Content").getText();
        if ("".equals(reStr)) {
            return "";
        }
        String[] sSqlList = sSmsSqls.split(";");
        for (String sSmsSql : sSqlList) {
            if (!"".equals(sSmsSql.trim())) {
                for (int i = 0; i < iKeyCount; i++) {
                    sSmsSql = sSmsSql.replace(sKey[i], sValue[i]);
                }
                xsf.data.DataTable dt = DBManager.getDataTable(sSmsSql);
                for (xsf.data.DataRow dr : dt.getRows()) {
                    for (Object obj : smsXml.selectNodes("/root/SMS/Key")) {
                        Node td = (Node) obj;
                        try {
                            if (dt.containsColumn(td.selectSingleNode("Field").getText())) {
                                String s = dr.getString(td.selectSingleNode("Field").getText()) != null ? dr.getString(td.selectSingleNode("Field").getText()) : "";
                                reStr = reStr.replace("[" + td.selectSingleNode("Name").getText() + "]", s);
                            }
                        } catch (Exception ex) {
                        }
                    }
                    break;
                }
            }
        }
        return reStr;
    }

    // 获取节点提示内容
    @SuppressWarnings("unchecked")
    public String getRemind(String tail) throws Exception {
        String sql = "select D.ID from G_USERS U,G_DEPT U1,G_DEPT D1,G_USERS D where U.ID=U1.USER_ID and U1.FID=D1.ID and D1.USER_ID=D.ID and U.ID=" + this.iUserID;
        String reStr = "";
        // String _cmdStr = "";
        // 常用关键字
        int iKeyCount = 6;
        String[] sKey = new String[iKeyCount];
        String[] sValue = new String[iKeyCount];
        // 当前用户ID
        sKey[0] = "[USERID]";
        sValue[0] = Long.toString(this.iUserID);
        // 当前文件ID
        sKey[1] = "[INFO_ID]";
        sValue[1] = Long.toString(this.iInfoID);
        // 当前部门ID
        sKey[2] = "[DEPT_ID]";
        sValue[2] = String.valueOf(getDetpIDFromUserID(this.iUserID));
        // 当前PID
        sKey[3] = "[PID]";
        sValue[3] = Long.toString(this.iPID);
        // 当前PNID
        sKey[4] = "[PNID]";
        sValue[4] = Long.toString(this.iPnID);
        // 当前动作名称
        sKey[5] = "[CAPTION]";
        sValue[5] = this.sCaption;
        // 获取短信模版
        // ---------------------------------
        String sXml = null;
        if (tail == null || "".equals(tail)) {
            sql = "select XML from G_FLOW_REMINDCONFIGINFO where WF_ID=" + this.iWfID + " and WFNODE_ID=" + this.iWfNodeID;
        } else {
            sql = "select XML from G_FLOW_REMINDCONFIGINFO where WF_ID=" + this.iWfID + " and WFHEADVERTER=" + this.iWfNodeID + " and WFTAILVERTER=" + tail;
        }
        sXml = DBManager.getFieldStringValue(sql);
        if (sXml == null || "".equals(sXml)) {
            return "";
        }
        // ---------------------------------
        Document smsXml = DocumentHelper.parseText(sXml);
        String sSmsSqls = smsXml.selectSingleNode("/root/Sql").getText();
        reStr = smsXml.selectSingleNode("root/Content").getText();
        if (reStr == null || "".equals(reStr)) {
            return "";
        }
        String[] sSqlList = sSmsSqls.split(";");
        for (String sSmsSql : sSqlList) {
            if (!"".equals(sSmsSql.trim())) {
                // -----参数替换
                for (int i = 0; i < iKeyCount; i++) {
                    sSmsSql = sSmsSql.replace(sKey[i], sValue[i]);
                }
                // -----
                xsf.data.DataTable dt = DBManager.getDataTable(sSmsSql);
                for (xsf.data.DataRow dr : dt.getRows()) {
                    Node key = smsXml.selectSingleNode("/root/Key");
                    List<Node> fields = (List<Node>) key.selectNodes("Field");
                    List<Node> names = (List<Node>) key.selectNodes("Name");
                    for (int j = 0; j < names.size(); j++) {
                        String name = ((Node) names.get(j)).getText();
                        String field = ((Node) fields.get(j)).getText();
                        try {
                            if (dt.containsColumn(field)) {
                                String s = dr.getString(field) == null ? "" : dr.getString(field);
                                // 金额带千分号 begin add by niquan
                                if ("MONEY".equals(field)) {
                                    s = dealMoney(s);
                                }
                                // end
                                reStr = reStr.replace("[" + name + "]", s);
                            }
                        } catch (Exception ex) {
                        }
                    }
                    break;
                }
            }
        }
        return reStr;
    }

    /**
     * add by niquan 金额带千分号
     * 
     * @param money
     * @return
     */
    public String dealMoney(String money) {
        // String money="1000";
        String[] arr = money.split("\\.");
        int count = 0;
        String moneys = "";
        String _money = arr[0];
        for (int i = _money.length() - 1; i >= 0; i--) {
            if (count == 3) {
                moneys = "," + moneys;
                count = 0;
            }
            moneys = _money.charAt(i) + moneys;
            count++;
        }
        if (arr.length == 2) {
            return moneys + "." + arr[1];
            // System.out.println(moneys+"."+arr[1]);
        } else {
            return moneys + ".00";
            // System.out.println(moneys);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        // ------------------------------------------------
        try {
          //  System.out.println(getEndTime("2011-10-25 12:39", 3, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ------------------------------------------------
        // String s = "<Root><Flow><Type>0</Type><Key>150011194</Key><Objclass>FW</Objclass><UserID>102000001</UserID><Pid>150011068</Pid><Pnid>1</Pnid><WfID>125</WfID></Flow></Root>";
        // try {
        // DS_FLOWClass dfc = new DS_FLOWClass(s);
        // System.out.println(dfc.iSendType);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
    }

    public static void insertGPnodes(Connection _myConn, Map<String, String> data) throws Exception {
        String _cmdStr = "INSERT INTO G_PNODES ";
        _cmdStr += "(PID,ID,FID,INFO_ID,DEPT_ID,USER_ID,UTYPE,UNAME,MDEPT_ID,MUSER_ID,STATUS,ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,";
      //g_pnodes 增加 连线类型WFLINE_TYPE,抄送标识 ISCS 杨龙修改 2012/8/9--------------开始
        _cmdStr += "SENDTYPE,RDATE,PDATE,SDATE,EDATE,TIMESPAN,TIMETYPE,DAYS,WHOHANDLE,HANDLEWAY,WAITCOUNT,ISZNG,FSTATUS,PARENTFLOWPID,PARENTFLOWPNID,SIGNED,ATYPE,WFLINE_TYPE,ISCS,RELTAG,SIGNSTATUS,MAINUNIT,PROXYSTATUS)VALUES(";
      //g_pnodes 增加 连线类型WFLINE_TYPE,抄送标识 ISCS 杨龙修改 2012/8/9--------------结束
        _cmdStr += data.get("PID") + ",";
        _cmdStr += data.get("ID") + ",";
        _cmdStr += data.get("FID") + ",";
        _cmdStr += data.get("INFO_ID") + ",";
        _cmdStr += data.get("DEPT_ID") + ",";
        _cmdStr += data.get("USER_ID") + ",";
        _cmdStr += data.get("UTYPE") + ",";
        _cmdStr += "?,";
        _cmdStr += data.get("MDEPT_ID") + ",";
        _cmdStr += data.get("MUSER_ID") + ",";
        _cmdStr += data.get("STATUS") + ",";
        _cmdStr += "?,";
        _cmdStr += data.get("WF_ID") + ",";
        _cmdStr += data.get("WFNODE_ID") + ",";
        _cmdStr += "?,";
        _cmdStr += data.get("WFNODE_WAIT") + ",";
        _cmdStr += data.get("SENDTYPE") + ",";
        _cmdStr += data.get("RDATE") + ",";
        _cmdStr += data.get("PDATE") + ",";
        _cmdStr += data.get("SDATE") + ",";
        _cmdStr += data.get("EDATE") + ",";
        _cmdStr += data.get("TIMESPAN") + ",";
        _cmdStr += data.get("TIMETYPE") + ",";
        _cmdStr += data.get("DAYS") + ",";
        _cmdStr += data.get("WHOHANDLE") + ",";
        _cmdStr += data.get("HANDLEWAY") + ",";
        _cmdStr += data.get("WAITCOUNT") + ",";
        _cmdStr += data.get("ISZNG") + ",";
        _cmdStr += data.get("FSTATUS") + ",";
        _cmdStr += data.get("PARENTFLOWPID") + ",";
        _cmdStr += data.get("PARENTFLOWPNID") + ",";
        _cmdStr += data.get("SIGNED") + ",";
      //g_pnodes 增加 连线类型WFLINE_TYPE,抄送标识 ISCS 杨龙修改 2012/8/9--------------开始
        _cmdStr += data.get("ATYPE")+ ",";
        if(data.containsKey("WFLINE_TYPE")&&!"".equals(data.get("WFLINE_TYPE"))&&null!=data.get("WFLINE_TYPE"))
        {
        	_cmdStr += data.get("WFLINE_TYPE")+ ",";
        }
        else
        {
        	_cmdStr += "0"+ ",";
        }
        if(data.containsKey("ISCS")&&!"".equals(data.get("ISCS"))&&null!=data.get("ISCS"))
        {
        	_cmdStr += data.get("ISCS");
        }
        else
        {
        	_cmdStr += "0";
        }
      //g_pnodes 增加 连线类型WFLINE_TYPE,抄送标识 ISCS 杨龙修改 2012/8/9--------------结束
        _cmdStr += ",?,0,?,?)";
      //  System.out.println("=================insertGPnodes()插入G_Pnodes数据的SQL："+_cmdStr);
        PreparedStatement ps = _myConn.prepareStatement(_cmdStr);
        // -----------------------------------------------------------
        String UNAME = xsf.Value.getString(data.get("UNAME"), xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
        String ACTNAME = xsf.Value.getString(data.get("ACTNAME"), xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
        String WFNODE_CAPTION = xsf.Value.getString(data.get("WFNODE_CAPTION"), xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
        // -----------------------------------------------------------
        
      //-----------g_pnodes 插入 reltag 字段
        String relTag = data.get("RELTAG");
        if(relTag == null){
        	relTag = "";
        }
        
        ps.setString(1, UNAME);
        ps.setString(2, ACTNAME);
        ps.setString(3, WFNODE_CAPTION);
        ps.setString(4, relTag);
		ps.setString(5, data.get("mainUnit"));
        String PROXYSTATUS=data.get("PROXYSTATUS");
        if(StringHelper.isNullOrEmpty(PROXYSTATUS)){
        	PROXYSTATUS="0";
        }
        ps.setString(6, PROXYSTATUS);
        ps.executeUpdate();
    }

    public static void insertGInbox(Connection _myConn, Map<String, String> data) throws Exception {	
    	
    	String gathernode = data.get("GATHERNODE")==null?"0":data.get("GATHERNODE");
    	if(gathernode.equals("1") || gathernode.equals("2")){
    		String wfnodeid = data.get("WFNODE_ID");
    		String pid = data.get("PID");
    		String userid = data.get("USER_ID");
    		String deptid = data.get("DEPT_ID");
    		xsf.data.DataTable dt = DBManager.getDataTable("select USER_ID,DEPT_ID from g_pnodes where pid="+pid+" and wfnode_id="+wfnodeid);
    		if(dt!=null && dt.getRowCount()>0){
    			for(xsf.data.DataRow dr:dt.getRows()){
    				String inbusr = dr.getString("USER_ID");
    				String inbdept = dr.getString("DEPT_ID");
    				//1 汇合人员 2 汇合部门和人员
    				if(  (gathernode.equals("1")&&userid.equals(inbusr)) || (gathernode.equals("2")&&userid.equals(inbusr)&&deptid.equals(inbdept))  ){
    					return;
    				}
    			}
    		}
    	}
    	
        String _cmdStr = null;
        _cmdStr = "INSERT INTO G_INBOX ";
        _cmdStr += "(ID,PID,PNID,INFO_ID,DEPT_ID,USER_ID,FUSER_ID,UTYPE,STATUS,";
      //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
       // _cmdStr += "ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,RDATE,EDATE,TIMETYPE,TIMESPAN,DAYS,HANDLEWAY,WAITCOUNT,ISZNG,FNODE,PRIORY)VALUES(";// ,NEEDPRI)VALUES(";
        _cmdStr += "ACTNAME,WF_ID,WFNODE_ID,WFNODE_CAPTION,WFNODE_WAIT,SENDTYPE,RDATE,EDATE,TIMETYPE,TIMESPAN,DAYS,HANDLEWAY,WAITCOUNT,ISZNG,FNODE,PRIORY,RECEIVE_USERNAME,SEND_USERNAME,RELTAG,SIGNSTATUS,MUSER_ID,BACKREASON,TIP)VALUES(";// ,NEEDPRI)VALUES(";
      //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
        _cmdStr += data.get("ID") + ",";
        _cmdStr += data.get("PID") + ",";
        _cmdStr += data.get("PNID") + ",";
        _cmdStr += data.get("INFO_ID") + ",";
        _cmdStr += data.get("DEPT_ID") + ",";
        _cmdStr += data.get("USER_ID") + ",";
        _cmdStr += data.get("FUSER_ID") + ",";
        _cmdStr += data.get("UTYPE") + ",";
        _cmdStr += data.get("STATUS") + ",";
        _cmdStr += "?,";
        _cmdStr += data.get("WF_ID") + ",";
        _cmdStr += data.get("WFNODE_ID") + ",";
        _cmdStr += "?,";
        _cmdStr += data.get("WFNODE_WAIT") + ",";
        _cmdStr += data.get("SENDTYPE") + ",";
        _cmdStr += data.get("RDATE") + ",";
        _cmdStr += data.get("EDATE") + ",";
        _cmdStr += data.get("TIMETYPE") + ",";
        _cmdStr += data.get("TIMESPAN") + ",";
        _cmdStr += data.get("DAYS") + ",";
        _cmdStr += data.get("HANDLEWAY") + ",";
        _cmdStr += data.get("WAITCOUNT") + ",";
        _cmdStr += data.get("ISZNG") + ",";
        _cmdStr += data.get("FNODE") + ",";
        //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 开始
        //_cmdStr += data.get("PRIORY");// + ",";
        _cmdStr += data.get("PRIORY") + ",";
        String RECEIVE_USERNAME="NULL";
        String SEND_USERNAME="NULL";
        if(data.containsKey("RECEIVE_USERNAME"))
        {
        	if(!"".equals(data.get("RECEIVE_USERNAME")))
        	{
        		RECEIVE_USERNAME="'"+data.get("RECEIVE_USERNAME")+"'";
        	}
        	
        }
        if(data.containsKey("SEND_USERNAME"))
        {
        	if(!"".equals(data.get("SEND_USERNAME")))
        	{
        		SEND_USERNAME="'"+data.get("SEND_USERNAME")+"'";
        	}
        }
        _cmdStr +=  RECEIVE_USERNAME+ ",";
        _cmdStr += SEND_USERNAME;// + ",";
        //g_inbox表增加字段 接收人姓名RECEIVE_USERNAME，发送人姓名SEND_USERNAME 杨龙修改 2012/10/15 结束
        // _cmdStr += data.get("NEEDPRI");
        _cmdStr += ",?,0,";
        _cmdStr += data.get("USER_ID") + ",?,?)";
        PreparedStatement ps = _myConn.prepareStatement(_cmdStr);
        // -----------------------------------------------------------
        String ACTNAME = xsf.Value.getString(data.get("ACTNAME"), xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
        String WFNODE_CAPTION = xsf.Value.getString(data.get("WFNODE_CAPTION"), xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
        // -----------------------------------------------------------
        
        //-----------g_inbox 插入 reltag 字段 
        String relTag = data.get("RELTAG");
        if(relTag == null){
        	relTag = "";
        }
        String reason = data.get("BACKREASON");
        ps.setString(1, ACTNAME);
        ps.setString(2, WFNODE_CAPTION);
        ps.setString(3, relTag);
        ps.setString(4, reason);
        ps.setString(5, data.get("TIP"));
        ps.executeUpdate();
    }

    public static String getMachine(String url) {
        String strReturn = url.replace("[ERP_MACHINE]", ConfigurationSettings.getServerInfo(null));
        return strReturn;
    }

    public static void doCommand(String command, Statement statement, List<String> httpTask) throws Exception {
        if (command == null || "".equals(command)) {
            return;
        }
        String[] sSqlList = command.split(";");
        for (String sSubSql : sSqlList) {
            if (sSubSql.toUpperCase().indexOf("HTTP:") == 0 || sSubSql.toUpperCase().startsWith("[")) {
                String sSubSql2 = getMachine(sSubSql.replace("%26", "&"));
                httpTask.add(sSubSql2);
            } else {
                command = sSubSql;
                statement.executeUpdate(sSubSql);
            }
        }
    }

    public static void doHttp(List<String> httpTask) throws Exception {
        for (String url : httpTask) {
            try {
                String result = httpService(url, "GET", null, "");
              //  System.out.println("doHttp:------------------------------------------" + url + "[" + result + "]");
                if (null != result && !"TRUE".equalsIgnoreCase(result) && !"".equals(result) && !"NULL".equalsIgnoreCase(result)) {
                    throw new Exception(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static void doSMS(String userId, String receiveUsers, String info_id, String content) throws Exception {
        if (content == null || "".equals(content)) {
            return;
        }
        if (receiveUsers == null || "".equals(receiveUsers)) {
            return;
        } else {
            receiveUsers = receiveUsers.substring(0, receiveUsers.length() - 1);
        }
        String url = ConfigurationSettings.getServerInfo(null) + "/action?MessageSupport=1&operate=0";
        Map<String, String> data = new HashMap<String, String>();
        data.put("Info_ID", info_id);
        data.put("sendUser", userId);
        data.put("receiveUser", receiveUsers);
        data.put("message", java.net.URLEncoder.encode(content, "UTF-8"));
        try {
            String result = httpService(url, "POST", data, null);
          //  System.out.println("doSMS:------------------------------------------" + url + "[" + result + "]");
            if (null != result && !"TRUE".equalsIgnoreCase(result) && !"".equals(result) && !"NULL".equalsIgnoreCase(result)) {
                throw new Exception(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String httpService(String urlStr, String method, Map<String, String> params, String cookie) {
        String result = null;
        try {
            // ---------------------------------------------
            StringBuffer buffer = new StringBuffer(1024);
            if (params != null) {
                int count = 0;
                for (String key : params.keySet()) {
                    buffer.append(key);
                    buffer.append("=");
                    buffer.append(params.get(key));
                    if (count < params.size() - 1) {
                        buffer.append("&");
                    }
                    count++;
                }
            }
            // ---------------------------------------------
            if ("GET".equals(method)) {
                String p = buffer.toString();
                urlStr += p.length() > 0 ? "?" + p : "";
            }
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            if ("POST".equals(method)) {
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(buffer);
                out.close();
            }
            // ---------------------------------------------
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie.toString());
            }
            // ---------------------------------------------
            Map<String, List<String>> map = conn.getHeaderFields();
            for (String key : map.keySet()) {
                // System.out.println(key + "------------------------");
                List<String> l = map.get(key);
                for (int i = 0; i < l.size(); i++) {
                    // System.out.println(" " + l.get(i));
                }
            }
            // ---------------------------------------------
            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuffer bs = new StringBuffer();
            String l = null;
            while ((l = responseBuffer.readLine()) != null) {
                bs.append(l);
            }
            result = bs.toString();
            conn.disconnect();
            // ---------------------------------------------
        } catch (Exception e) {
          //  System.out.println("服务器异常! ");
        }
        return result;
    }

    // 增加工作日的天数 (nowTime起始日期 “YYYY-MM-DD HH24:MI”;iDays天数;返回增加工作日后的日期 “YYYY-MM-DD HH24:MI”)
    public static String addWorkDays(String nowTime, int iDays) throws Exception {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
    	String ret = nowTime;
        String _cmdStr = "";
        try {
//        	String DBMS = ConfigurationSettings.AppSettings("DBMS");
//            if ("SYBASE".equals(DBMS)) {
//                _cmdStr = "SELECT RETDATE FROM(SELECT TOP " + iDays + " ID,CONVERT(char(16),VDATE,20) AS RETDATE FROM (select top 1000 ID,DATEADD(dd,ID, '" + nowTime + "') AS VDATE,DATEPART(DW,DATEADD(dd,ID, '" + nowTime + "'))-1 AS WEEKDAY " + "from CK " + "where ID<" + iDays * 3 + "+100 ORDER BY ID) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6) " + "OR CONVERT(char(16),VDATE,20) IN (SELECT CONVERT(char(16),WDATE,20) FROM G_WEEKDAY WHERE STATUS=0)) " + "AND CONVERT(char(16),VDATE,20) NOT IN (SELECT CONVERT(char(16),WDATE,20) FROM G_WEEKDAY WHERE STATUS=1) " + " ORDER BY ID) AS B ORDER BY ID DESC";
//            } else if ("ORACLE".equals(DBMS)) {
//                _cmdStr = "SELECT TO_CHAR(VDATE,'YYYY-MM-DD HH24:MI') AS RETDATE FROM (select TO_DATE('" + nowTime + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + nowTime + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum<" + iDays * 3 + "+100) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) " + "AND ROWNUM<=" + iDays + " ORDER BY ROWNUM DESC";
//            } else if ("SQLSERVER".equals(DBMS)) {
//                _cmdStr = "SELECT RETDATE FROM(SELECT TOP " + iDays + " ID,CONVERT(char(16),VDATE,20) AS RETDATE FROM (select top 1000 ID,DATEADD(dd,ID, '" + nowTime + "') AS VDATE,DATEPART(DW,DATEADD(dd,ID, '" + nowTime + "'))-1 AS WEEKDAY " + "from CK " + "where ID<" + iDays * 3 + "+100 ORDER BY ID) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6) " + "OR CONVERT(char(16),VDATE,20) IN (SELECT CONVERT(char(16),WDATE,20) FROM G_WEEKDAY WHERE STATUS=0)) " + "AND CONVERT(char(16),VDATE,20) NOT IN (SELECT CONVERT(char(16),WDATE,20) FROM G_WEEKDAY WHERE STATUS=1) " + " ORDER BY ID) AS B ORDER BY ID DESC";
//            } else if ("MYSQL".equals(DBMS)) {
//                _cmdStr = "SELECT RETDATE FROM(SELECT TOP " + iDays + " ID,CONVERT(char(16),VDATE,20) AS RETDATE FROM (select top 1000 ID,DATEADD(dd,ID, '" + nowTime + "') AS VDATE,DATEPART(DW,DATEADD(dd,ID, '" + nowTime + "'))-1 AS WEEKDAY " + "from CK " + "where ID<" + iDays * 3 + "+100 ORDER BY ID) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6) " + "OR CONVERT(char(16),VDATE,20) IN (SELECT CONVERT(char(16),WDATE,20) FROM G_WEEKDAY WHERE STATUS=0)) " + "AND CONVERT(char(16),VDATE,20) NOT IN (SELECT CONVERT(char(16),WDATE,20) FROM G_WEEKDAY WHERE STATUS=1) " + " ORDER BY ID) AS B ORDER BY ID DESC";
//            } else {
//                _cmdStr = "SELECT TO_CHAR(VDATE,'YYYY-MM-DD HH24:MI') AS RETDATE FROM (select TO_DATE('" + nowTime + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + nowTime + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum<" + iDays * 3 + "+100) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) " + "AND ROWNUM<=" + iDays + " ORDER BY ROWNUM DESC";
//            }
//            ret = DBManager.getFieldStringValue(_cmdStr);
        	Date date= AttendanceUtils.getWorkDate(sdf.parse(nowTime), iDays);
        	if(date!=null)
        	{
        		ret=sdf.format(date);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}        
        return ret;
    }

    // 获取两节点时间的工作日差值 (sNowWorkTime当前时间,sEndWorkTime预计结束时间)
    public static String getDiffTime(String sNowWorkTime, String sEndWorkTime) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
        SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-M-dd");
        Date dEndTime = sdfOut.parse(sEndWorkTime);
        Date dNow = sdfOut.parse(sNowWorkTime);
        String cmdStr = "";
        String sStartTime = ConfigurationSettings.AppSettings("WorkStartTime");
        String sEndTime = ConfigurationSettings.AppSettings("WorkEndTime");
        Calendar cal = Calendar.getInstance();
        cal.setTime(dNow);
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sStartTime.split(":")[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(sStartTime.split(":")[1]));
        cal.set(Calendar.MILLISECOND, 0);
        cal.setTime(dNow);
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sEndTime.split(":")[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(sEndTime.split(":")[1]));
        cal.set(Calendar.MILLISECOND, 0);
        Date dNowEndWorkTime = cal.getTime();
        String DBMS = ConfigurationSettings.AppSettings("DBMS");
        if (dEndTime.getTime() == dNow.getTime()) {
            // 当天，小时计
            if (dNow.compareTo(dNowEndWorkTime) > 0) {
                // 已经下班啦
                dNow = dNowEndWorkTime;
            }
            TimeSpan tDiff = TimeSpan.toCm(dNow, dEndTime);
            if (tDiff.getTotalHours() < 0) {
                // 未超时
                if (Math.abs(tDiff.getTotalMinutes()) >= 60) {
                    return "剩" + Math.abs(tDiff.getTotalHours()) + "小时";
                } else {
                    return "剩" + Math.abs(tDiff.getTotalMinutes()) + "分钟";
                }
            } else {
                // 已超时
                if (Math.abs(tDiff.getTotalMinutes()) >= 60) {
                    return "<font color=red>超" + Math.abs(tDiff.getTotalHours()) + "小时</font>";
                } else {
                    return "<font color=red>超" + Math.abs(tDiff.getTotalMinutes()) + "分钟</font>";
                }
            }
        } else if (dEndTime.getTime() > dNow.getTime()) {
            // 隔天，工作日记,未超时
            if ("SYBASE".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*) DAYS FROM (select top 1000 ID,DATEADD(dd, ID, '" + sdf.format(dNow) + "') AS VDATE, DATEPART(DW, DATEADD(dd, ID, '" + sdf.format(dNow) + "'))-1 AS WEEKDAY " + "from CK " + "where ID < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR CONVERT(char(16), VDATE, 20) IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE <= '" + sEndWorkTime + "' " + "AND CONVERT(char(16), VDATE, 20) NOT IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=1) ";
            } else if ("ORACLE".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*) DAYS FROM (select TO_DATE('" + sdf.format(dNow) + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + sdf.format(dNow) + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE <= TO_DATE('" + sEndWorkTime + "','YYYY-MM-DD HH24,MI') " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) ";
            } else if ("SQLSERVER".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*) DAYS FROM (select top 1000 ID,DATEADD(dd, ID, '" + sdf.format(dNow) + "') AS VDATE, DATEPART(DW, DATEADD(dd, ID, '" + sdf.format(dNow) + "'))-1 AS WEEKDAY " + "from CK " + "where ID < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR CONVERT(char(16), VDATE, 20) IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE <= '" + sEndWorkTime + "' " + "AND CONVERT(char(16), VDATE, 20) NOT IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=1) ";
            } else if ("MYSQL".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*) DAYS FROM (select TO_DATE('" + sdf.format(dNow) + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + sdf.format(dNow) + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE <= TO_DATE('" + sEndWorkTime + "','YYYY-MM-DD HH24,MI') " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) ";
            } else {
                cmdStr = "SELECT COUNT(*) DAYS FROM (select TO_DATE('" + sdf.format(dNow) + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + sdf.format(dNow) + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE <= TO_DATE('" + sEndWorkTime + "','YYYY-MM-DD HH24,MI') " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) ";
            }
        } else {
            // 已超时，工作日记
            if ("SYBASE".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*)*-1 DAYS FROM (select top 1000 ID,DATEADD(dd, ID, '" + sdf.format(dEndTime) + "') AS VDATE, DATEPART(DW, DATEADD(dd, ID, '" + sdf.format(dEndTime) + "'))-1 AS WEEKDAY " + "from CK " + "where ID < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR CONVERT(char(16), VDATE, 20) IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE < '" + sdf.format(addDays(dNow, 1)) + "' " + "AND CONVERT(char(16), VDATE, 20) NOT IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=1) ";
            } else if ("ORACLE".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*)*-1 DAYS FROM (select TO_DATE('" + sdf.format(dEndTime) + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + sdf.format(dEndTime) + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE < TO_DATE('" + sdf.format(addDays(dNow, 1)) + "','YYYY-MM-DD HH24,MI') " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) ";
            } else if ("SQLSERVER".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*)*-1 DAYS FROM (select top 1000 ID,DATEADD(dd, ID, '" + sdf.format(dEndTime) + "') AS VDATE, DATEPART(DW, DATEADD(dd, ID, '" + sdf.format(dEndTime) + "'))-1 AS WEEKDAY " + "from CK " + "where ID < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR CONVERT(char(16), VDATE, 20) IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE < '" + sdf.format(addDays(dNow, 1)) + "' " + "AND CONVERT(char(16), VDATE, 20) NOT IN (SELECT CONVERT(char(16), WDATE, 20) FROM G_WEEKDAY WHERE STATUS=1) ";
            } else if ("MYSQL".equals(DBMS)) {
                cmdStr = "SELECT COUNT(*)*-1 DAYS FROM (select TO_DATE('" + sdf.format(dEndTime) + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + sdf.format(dEndTime) + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE < TO_DATE('" + sdf.format(addDays(dNow, 1)) + "','YYYY-MM-DD HH24,MI') " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) ";
            } else {
                cmdStr = "SELECT COUNT(*)*-1 DAYS FROM (select TO_DATE('" + sdf.format(dEndTime) + "','YYYY-MM-DD HH24:MI')+rownum VDATE,TRUNC(MOD(MOD(TO_DATE('" + sdf.format(dEndTime) + "','YYYY-MM-DD HH24:MI')+rownum-TO_DATE('20000102','YYYYMMDD'),7)+7,7)) WEEKDAY " + "from CK " + "where rownum < 365 ) A " + "WHERE ((A.WEEKDAY<>0 AND A.WEEKDAY<>6 ) " + "OR TO_CHAR(VDATE,'YYYYMMDD') IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=0)) " + "AND VDATE < TO_DATE('" + sdf.format(addDays(dNow, 1)) + "','YYYY-MM-DD HH24,MI') " + "AND TO_CHAR(VDATE,'YYYYMMDD') NOT IN (SELECT TO_CHAR(WDATE,'YYYYMMDD') FROM G_WEEKDAY WHERE STATUS=1) ";
            }
        }
        long iDays = DBManager.getFieldLongValue(cmdStr);
        if (iDays < 0) {
            // 超期
            return "<font color=red>超" + Math.abs(iDays) + "天</font>";
        } else {
            // 未超期
            return "剩" + iDays + "天";
        }
    }

    // 根据时间差和时间类型，获取办结期限 (sNowWorkTime起始时间;iTimeSpan时间差;iTimeType时间类型;返回办理期限)
    public static String getEndTime(String sNowWorkTime, int iTimeSpan, int iTimeType) throws Exception {
        if (iTimeSpan == 0) {
            return "0";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
        SimpleDateFormat day = new SimpleDateFormat("yyyy-M-dd");
        String sStartWorkTime = ConfigurationSettings.AppSettings("WorkStartTime");
        String sEndWorkTime = ConfigurationSettings.AppSettings("WorkEndTime");
        Date now = sdf.parse(sNowWorkTime);
        Date dNowStartWorkTime = sdf.parse(day.format(now) + " " + sStartWorkTime);
        Date dNowEndWorkTime = sdf.parse(day.format(now) + " " + sEndWorkTime);
        TimeSpan tDiff;
        TimeSpan tDiffSE = TimeSpan.toCm(dNowEndWorkTime, dNowStartWorkTime);
        int iDiffMin = tDiffSE.getTotalMinutes();
        switch (iTimeType) {
        case 0:// 天
            return addWorkDays(sdf.format(now), iTimeSpan);
        case 1:// 小时
            iTimeSpan = iTimeSpan * 60;
            if (now.getTime() > dNowEndWorkTime.getTime()) {
                // 已经下班啦
                now = addMinutes(dNowEndWorkTime, iTimeSpan);
            } else if (now.getTime() < dNowStartWorkTime.getTime()) {
                // 还没上班
                now = addMinutes(dNowStartWorkTime, iTimeSpan);
            }
            tDiff = TimeSpan.toCm(now, dNowEndWorkTime);
            if (tDiff.getTotalMinutes() < 0) {
                // 当天
                return sdf.format(now);
            } else {
                // 隔天
                int iDays = (tDiff.getTotalMinutes() / iDiffMin) + 1;
                int iMins = (tDiff.getTotalMinutes() % iDiffMin);
                return addWorkDays(sdf.format(addMinutes(dNowStartWorkTime, iMins)), iDays);
            }
        case 2:// 分
            if (now.getTime() > dNowEndWorkTime.getTime()) {
                // 已经下班啦
                now = addMinutes(dNowEndWorkTime, iTimeSpan);
            } else if (now.getTime() < dNowStartWorkTime.getTime()) {
                // 还没上班
                now = addMinutes(dNowStartWorkTime, iTimeSpan);
            }
            tDiff = TimeSpan.toCm(now, dNowEndWorkTime);
            if (tDiff.getTotalMinutes() < 0) {
                // 当天
                return sdf.format(now);
            } else {
                // 隔天
                int iDays = (tDiff.getTotalMinutes() / iDiffMin) + 1;
                int iMins = (tDiff.getTotalMinutes() % iDiffMin);
                return addWorkDays(sdf.format(addMinutes(dNowStartWorkTime, iMins)), iDays);
            }
        case 3:// 自然日
            return sdf.format(addDays(now, iTimeSpan));
        }
        return "0";
    }

    public static String processDate(String columns, String DBMS, Date now) {
        if (dsoap.tools.ConfigurationSettings.timeStandard == 1) {
            if ("SYBASE".equals(DBMS)) {
            } else if ("ORACLE".equals(DBMS)) {
                columns = columns.replaceAll("NOW", "TO_DATE('" + DB_DATE_FORMAT.format(now) + "','YYYY-MM-DD HH24:MI:SS')");
            } else if ("SQLSERVER".equals(DBMS)) {
                columns = columns.replaceAll("NOW", "CAST('" + DB_DATE_FORMAT.format(now) + "' AS DATETIME)");
            } else if ("MYSQL".equals(DBMS)) {
                columns = columns.replaceAll("NOW", "'" + DB_DATE_FORMAT.format(now) + "'");
            } else {
                columns = columns.replaceAll("NOW", "TO_DATE('" + DB_DATE_FORMAT.format(now) + "','YYYY-MM-DD HH24:MI:SS')");
            }
        } else if (dsoap.tools.ConfigurationSettings.timeStandard == 0) {
            if ("SYBASE".equals(DBMS)) {
            } else if ("ORACLE".equals(DBMS)) {
                columns = columns.replaceAll("NOW", "SYSDATE");
            } else if ("SQLSERVER".equals(DBMS)) {
                columns = columns.replaceAll("NOW", "GETDATE()");
            } else if ("MYSQL".equals(DBMS)) {
                columns = columns.replaceAll("NOW", "NOW()");
            } else {
                columns = columns.replaceAll("NOW", "SYSDATE");
            }
        }
        return columns;
    }

    private static Date addMinutes(Date date, int Minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.roll(Calendar.MINUTE, Minutes);
        return cal.getTime();
    }

    private static Date addDays(Date date, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.roll(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

	public static void addLogInfo(String string) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * 通过wf_id,t.wfnode_id得到该节点的提醒方式
	 */
	private void setRemind(String wf_id,String wfnode_id){
		Sql sql = new Sql("SELECT WFNODE_XML FROM WFNODELIST T " +
				"WHERE T.WF_ID = ?WF_ID AND T.WFNODE_ID = ?WFNODE_ID");
		sql.getParameters().add(new Parameter("WF_ID", wf_id));
		sql.getParameters().add(new Parameter("WFNODE_ID", wfnode_id));
		String setXml = DBManager.getFieldStringValue(sql);
		//格式化xml
        String formatXml = MessageParser.formatXML(setXml);
        //获取sql语句
        try {
        	
        	String forword = MessageParser.getBodyFromXml(formatXml, "/Root/BaseInfo/IsForword");
        	String email = MessageParser.getBodyFromXml(formatXml, "/Root/BaseInfo/IsEMail");
        	String tray = MessageParser.getBodyFromXml(formatXml, "/Root/BaseInfo/IsTray");
        	
        	this.isForword = "1".equals(forword);
        	this.isEMail = "1".equals(email);
        	this.isTray = "1".equals(tray);
        	this.msg_lock=getMsg_Lock();
        	try {
				this.sSmsContent = getSmsContent();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (DocumentException e) {
			
			throw new RuntimeException(e);
			
		}
	} 

	private boolean getMsg_Lock(){
		Sql sql = new Sql("SELECT VALUE FROM G_CONFIG WHERE NAME='DSOA_MSG_LOCK'");
		String value = DBManager.getFieldStringValue(sql);
		if(value==null||"".equals(value)||"0".equals(value)){
			return false;	
		}else {
			return true;
		}
		
	}

	public static void initUserlist() {
		Sql sql = new Sql("select distinct mainunit from g_orguser");
		xsf.data.DataTable dtmainunit = DBManager.getDataTable(sql);
		if(dtmainunit!=null && dtmainunit.getRowCount()>0){
			for(xsf.data.DataRow dr:dtmainunit.getRows()){
				String mainunit = dr.getString("MAINUNIT");
				if(StringHelper.isNotNullAndEmpty(mainunit)){
					xsf.data.DataTable dt = null;
					IDataSource sqlDataSource = SysDataSource.getSysDataSource();
			        sqlDataSource.setParameter("MAINUNIT",mainunit);
			        sqlDataSource.setParameter("MAINUNIT2",mainunit);
			        sqlDataSource.setParameter("MAINUNIT3",mainunit);
			        dt = sqlDataSource.query("getUserDeptGroupInfo");
			        wholeOrgMap.put(mainunit, dt);
				}
			}
		}
		
	}

	public static void initUserRoleDataSource() {
		IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        String sRole_IDList = "0";
        String _cmdStr = "";
   	 	if (!dsoap.tools.ConfigurationSettings.isRoleDept) {
            _cmdStr = "getRoleUsers";
        } else {
            _cmdStr = "getRoleDeptUsers";
        }
        sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("ROLE_IDS", sRole_IDList);
        dtUserRole = sqlDataSource.query(_cmdStr);
	}
	
}
