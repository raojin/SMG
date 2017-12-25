package dsoap.webservice;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

import xsf.Config;
import xsf.Value;
import xsf.data.DBManager;
import xsf.data.DataRowCollections;
import xsf.data.ICommand;
import xsf.data.IDataSource;
import xsf.util.StringHelper;
import dsoap.dsflow.DS_FLOWClass;
import dsoap.dsflow.model.DataRow;
import dsoap.dsflow.model.DataTable;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.Dom4jTools;
import dsoap.tools.SysDataSource;
import dsoap.tools.TypeConversion;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
@WebService
public class WFServiceImpl implements WFService {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
    private String[] strArrUid;
    private String[] strArrFUid;
    private String[] strArrUName;
    private String[] strArrUType;
    private String[] strArrUDept;
    private int nUserCount = 0;
    // private String[] strArrRUid;
    // private String[] strArrRFUid;
    // private String[] strArrRUName;
    // private String[] strArrRUType;
    // private String[] strArrRUDept;
    // private int nRUserCount = 0;
    private SAXReader saxreader = new SAXReader();

    // private DocumentFactory df = saxreader.getDocumentFactory();
    public WFServiceImpl() throws Exception {
        System.out.println("---------------------------------WFServiceImpl.WFServiceImpl()");
        try {
            InitializeComponent();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 增加工作日的天数
    // nowTime:起始日期 “YYYY-MM-DD HH24:MI”
    // iDays:天数
    // 返回增加工作日后的日期 “YYYY-MM-DD HH24:MI”
    @WebMethod
    public String addWorkDays(String nowTime, int iDays) throws Exception {
        System.out.println("---------------------------------WFServiceImpl.addWorkDays()");
        return DS_FLOWClass.addWorkDays(nowTime, iDays);
    }

    @WebMethod
    public String SaveWF(String xml) {
        System.out.println("---------------------------------WFServiceImpl.SaveWF()\n" + xml);
        Connection _myConn = null;
        Statement _myRead = null;
        try {
            Document mydoc = DocumentHelper.parseText(xml);
            _myConn = DBManager.getConnection(xsf.Config.CONNECTION_KEY);
            _myConn.setAutoCommit(false);
            _myRead = _myConn.createStatement();
            // --------------------------------------------------
            Node tempxml = mydoc.selectSingleNode("//WF");
            int iWFID = Integer.parseInt(tempxml.valueOf("@ID"));
            // --------------------------------------------------
            Map<String, String> m0 = loadNodes(String.valueOf(iWFID), _myRead);
            DeleteWFA(String.valueOf(iWFID), _myRead);
            // --------------------------------------------------
            tempxml = mydoc.selectSingleNode("//WF/BaseInfo");
            iWFID = SaveWFBaseInfo(tempxml, iWFID, _myConn);
            // --------------------------------------------------
            tempxml = mydoc.selectSingleNode("//WF/NodeInfo");
            SaveWFNodeInfo(tempxml, iWFID, _myConn);
            // --------------------------------------------------
            tempxml = mydoc.selectSingleNode("//WF/LineInfo");
            SaveWFLineInfo(tempxml, iWFID, _myConn);
            // --------------------------------------------------
            Map<String, String> m1 = loadNodes(String.valueOf(iWFID), _myRead);
            // --------------------------------------------------
            String nodeName = checkInBox(String.valueOf(iWFID), m0, m1, _myRead);
            if (nodeName != null && !"".equals(nodeName)) {
                throw new Exception(nodeName + " 节点存在文件，请先处理完毕！");
            }
            // --------------------------------------------------
            setTSNode(String.valueOf(iWFID), m0, m1, _myConn);// 设置新增节点为可增发节点
            // --------------------------------------------------
            Map<String, String> gTemp = loadGTemp(String.valueOf(iWFID), _myRead);
            Map<String, String> gForm = loadGForm(String.valueOf(iWFID), _myRead);
            Map<String, String> wfnodelist = loadWfnodelist(String.valueOf(iWFID), _myRead);
            Map<String, String> wordPrintList = loadWordPrintFormats(String.valueOf(iWFID), _myRead);
            /**
             * 增加word打印
             */
            syncWFNode(wordPrintList, gTemp, gForm, wfnodelist, m0, m1, _myConn);
            /**
             * 同步G_PNODES 数据 2012.5.6 taolb
             */
            syncGpNodes(String.valueOf(iWFID), m0, m1, _myConn);
            // if (true) {
            // throw new Exception();
            // }
            _myConn.commit();
        } catch (Exception e) {
            if (_myConn != null) {
                try {
                    _myConn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
            return "保存失败：" + e.getMessage();
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
        return "保存成功";
    }

    /**
     * 同步G_PNODES 数据
     * 
     * @param WF_ID
     * @param m0
     * @param m1
     * @param connection
     */
    private static void syncGpNodes(String WF_ID, Map<String, String> m0, Map<String, String> m1, Connection connection) throws Exception {
        PreparedStatement ps = null;
        // 查找不同节点
        java.util.Set<String> nodes = m1.keySet(); // 设置流程节点ID
        for (String node : nodes) {
            String nodeName = m1.get(node); // 新节点名称
            String oldNodeName = m0.get(node); // 旧节点名称
            // 设置节点名称 和 旧节点名称 不同情况下才需要更新
            if (nodeName != null && oldNodeName != null && !nodeName.equals(oldNodeName)) {
                ps = connection.prepareStatement("update G_PNODES set ACTNAME = ? , WFNODE_CAPTION = ?  where WF_ID = ? and WFNODE_ID = ? ");
                ps.setString(1, nodeName);
                ps.setString(2, nodeName);
                ps.setString(3, WF_ID);
                ps.setString(4, node);
                ps.executeUpdate();
            }
        }
    }

    @WebMethod
    public String DeleteWF(String WF_ID) {
        System.out.println("---------------------------------WFServiceImpl.DeleteWF()");
        Connection _myConn = null;
        Statement _myRead = null;
        String _cmdStr = "";
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myConn.setAutoCommit(false);
            _myRead = _myConn.createStatement();
            _cmdStr = "DELETE FROM WFDEFINITION WHERE WF_ID=" + WF_ID;
            _myRead.executeUpdate(_cmdStr.toUpperCase());
            _cmdStr = "DELETE FROM WFNODELIST WHERE WF_ID=" + WF_ID;
            _myRead.executeUpdate(_cmdStr.toUpperCase());
            // _cmdStr = "DELETE FROM WFNODEBUTTON WHERE WF_ID="+WF_ID;
            // _myRead.executeUpdate(_cmdStr.toUpperCase());
            _cmdStr = "DELETE FROM WFLINELIST WHERE WF_ID=" + WF_ID;
            _myRead.executeUpdate(_cmdStr.toUpperCase());
            _myConn.commit();
        } catch (Exception e) {
            try {
                _myConn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return "删除失败" + e.getMessage();
        } finally {
            try {
                _myRead.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (_myConn != null) {
                try {
                    _myConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return "删除成功";
    }

    @WebMethod
    public String SetRegCode(String sCode) {
        System.out.println("---------------------------------WFServiceImpl.SetRegCode()");
        try {
            setKey("DS_FLOW", "REG_CODE", sCode);
            return "注册成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "注册失败:" + e.getMessage();
        }
    }

    @WebMethod
    public String GetRegCode() {
        System.out.println("---------------------------------WFServiceImpl.GetRegCode()");
        String ReturnValue = "0";
        try {
            ReturnValue = getKey("DS_FLOW", "REG_CODE");
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return ReturnValue;
    }

    @WebMethod
    public String GetSysDate() throws Exception {
        System.out.println("---------------------------------WFServiceImpl.GetSysDate()");
        String sNowDate = "2007-01-01";
        String _cmdStr = "";
        String DBMS = ConfigurationSettings.DBMS;
        if ("SYBASE".equals(DBMS)) {
            _cmdStr = "SELECT convert(char(10),GETDATE(),20) NOWDATE FROM systemoptions where opclass='PSD'";
        } else if ("ORACLE".equals(DBMS)) {
            _cmdStr = "SELECT TO_CHAR(SYSDATE,'YYYY-MM-DD') NOWDATE FROM systemoptions where opclass='PSD'";
        } else if ("SQLSERVER".equals(DBMS)) {
            _cmdStr = "SELECT convert(char(10),GETDATE(),20) NOWDATE FROM systemoptions where opclass='PSD'";
        } else if ("MYSQL".equals(DBMS)) {
            _cmdStr = "SELECT DATE_FORMAT(now(),'%Y-%m-%d') NOWDATE FROM systemoptions where opclass='PSD'";
        } else {
            _cmdStr = "SELECT convert(char(10),GETDATE(),20) NOWDATE FROM systemoptions where opclass='PSD'";
        }
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
            sNowDate = dr.getString("NOWDATE");
        }
        return sNowDate;
    }

    @WebMethod
    public String GetWF(String sWFID) {
        System.out.println("---------------------------------WFServiceImpl.GetWF()");
        try {
            String WFXML = "<?xml version='1.0' encoding='UTF-8'?>";
            WFXML += "<WF ID='" + sWFID + "'>";
            WFXML += GetWFBaseInfo(sWFID);
            WFXML += GetWFNodeInfo(sWFID);
            WFXML += GetWFLineInfo(sWFID);
            WFXML += "</WF>";
            System.out.println(WFXML);
            return WFXML;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    @WebMethod
    public String GetCompNameCode() throws Exception {
        System.out.println("---------------------------------WFServiceImpl.GetCompNameCode()");
        String sName = "";
        // String _cmdStr = "select UNAME from G_USERS where UTYPE=8 and ISNATIVE=1";
        // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        // for (xsf.data.DataRow dr : dt.getRows()) {
        // sName = dr.getString("UNAME");
        // }
        // sName = getMD5Str(sName);
        return sName;
    }

    @WebMethod
    public String GetCompNameCodeFromNAME(String sName) {
        System.out.println("---------------------------------WFServiceImpl.GetCompNameCodeFromNAME()");
        try {
            sName = dsoap.tools.MD5.str2MD5(sName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sName;
    }

    // 获取角色信息XML
    @WebMethod
    public String GetAllUserList(String sObjClass) {
        System.out.println("---------------------------------WFServiceImpl.GetAllUserList()" +  sObjClass);
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><NewDataSet>") ;
        
        String mainUnit = DBManager.getFieldStringValue("select MAINUNIT from G_MODULE WHERE ID = " + sObjClass) ;
        if(!StringUtils.isEmpty(mainUnit)){
        	StringBuilder sql = new StringBuilder() ;
        	sql.append("select ID ROLEID,NAME ROLENAME from G_ROLE where ROWSTATE >=0 and (ispublic=2 or (ispublic=0 and MAINUNIT = ").append(mainUnit).append("))") ;
        	xsf.data.DataTable table = DBManager.getDataTable(sql.toString()) ;
        	DataRowCollections dataRows = table.getRows() ;
        	int size = dataRows.size() ;
        	
        	List<String> roles = new ArrayList<String>() ;
        	if(size > 0){
        		xml.append("<role><ROLEID>-1</ROLEID><ROLENAME>========本单位角色========</ROLENAME></role>") ;
        		for(xsf.data.DataRow dataRow : dataRows){
        			String ROLEID = dataRow.getString("ROLEID");
        			String ROLENAME = dataRow.getString("ROLENAME");
        			if (ROLEID != null && !"".equals(ROLEID)) {
        				if(!roles.contains(ROLEID)){
        					xml.append("<role><ROLEID>").append(ROLEID).append("</ROLEID><ROLENAME>").append(ROLENAME).append("</ROLENAME></role>") ;
        					roles.add(ROLEID) ;
        				}
        			}
            	}
        	}
        	
        	sql.setLength(0) ;
        	sql.append("select a.ID ROLEID,a.NAME ROLENAME from G_ROLE a inner join G_GRADE_SET b on a.ID = b.RID and b.GRADETYPE = 7 and a.ROWSTATE >=0 and  b.MAINUNIT = ").append(mainUnit) ;
        	table = DBManager.getDataTable(sql.toString()) ;
        	dataRows = table.getRows() ;
        	size = dataRows.size() ;
        	if(size > 0){
        		xml.append("<role><ROLEID>-2</ROLEID><ROLENAME>========共享角色========</ROLENAME></role>") ;
        		for(xsf.data.DataRow dataRow : dataRows){
            		String ROLEID = dataRow.getString("ROLEID");
            		String ROLENAME = dataRow.getString("ROLENAME");
            		if (ROLEID != null && !"".equals(ROLEID)) {
            			if(!roles.contains(ROLEID)){
            				xml.append("<role><ROLEID>").append(ROLEID).append("</ROLEID><ROLENAME>").append(ROLENAME).append("</ROLENAME></role>") ;
            				roles.add(ROLEID) ;
            			}
                    }
            	}
        	}
        }
        xml.append("</NewDataSet>");
        
//        // String sql = "SELECT DISTINCT G_USERS.ID AS ROLEID,G_USERS.UNAME AS ROLENAME FROM G_UROLES,G_USERS WHERE G_UROLES.USER_ID=G_USERS.ID AND G_USERS.UTYPE=3 AND STATUS=0 AND (OBJCLASS='-1' OR OBJCLASS='" + sObjClass + "')";
//        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
//        ICommand command = (ICommand) sqlDataSource.getSelectCommands().get("getRoles");
//        String sql = command.getCommandText().trim();
//        return getXml(sql, "role");
//        System.out.println(xml.toString() + "****************");
        return xml.toString() ;
    }

    // 获取按钮字典表XML
    @WebMethod
    public String GetButtonDic() {
        System.out.println("---------------------------------WFServiceImpl.GetButtonDic()");
        // String sql = "SELECT ID,NAME,PARMHELP,BUTTONIMAGE,BUTTONWHERE FROM WF_BUT_TEMP";
        // return getXml(sql, "button");
        return "";
    }

    @WebMethod
    public String GetSubFLow(String sObjclass) {
        System.out.println("---------------------------------WFServiceImpl.GetSubFLow()");
        String sql = "SELECT WF_ID,WF_CAPTION FROM WFDEFINITION WHERE WF_TYPEID='" + sObjclass + "' ORDER BY WF_ID";
        return getXml(sql, "SUBFLOW");
    }

    // 得到部门,小组树XML
    @WebMethod
    public String GetTreeUsers() {
        System.out.println("---------------------------------WFServiceImpl.GetTreeUsers()");
        // String _cmdStr = "";
        String sFid = "";
        Document domUsers = DocumentHelper.createDocument();
        Node nodUser = domUsers.addElement("User");
        // 根节点（只能有一个，多个取最后一个）
        // _cmdStr = "SELECT A.ID,A.UNAME FROM G_USERS A,G_DEPT B WHERE A.ID=B.USER_ID AND B.FID=0 AND A.ISNATIVE=1";
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        xsf.data.DataTable dt = sqlDataSource.query("getDeptRoot");
        if (dt != null && dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            sFid = dr.getString("ID");
            ((Element) nodUser).addAttribute("UserName", dr.getString("UNAME"));
            ((Element) nodUser).addAttribute("UserID", dr.getString("ID"));
            ((Element) nodUser).addAttribute("UserType", "1");
        }
        // 子节点
        // // _cmdStr = "SELECT A.ID,A.UTYPE,A.UNAME,B.DEPT,C.USER_ID FROM G_USERS A,G_DEPT B,G_DEPT C WHERE A.STATUS>=0 AND (A.UTYPE=2 OR (A.UTYPE=0 AND HAVE_LOGIN=1) OR A.UTYPE=9 OR A.UTYPE=1) AND A.ID=B.USER_ID AND (A.UTYPE=2 OR A.UTYPE=1) AND A.ID=B.USER_ID AND B.FID=C.ID AND A.ISNATIVE=1 ORDER BY C.SHORDER ASC,B.SHORDER ASC";
        // _cmdStr = "SELECT A.ID,A.UTYPE,A.UNAME,B.DEPT,C.USER_ID FROM G_USERS A,G_DEPT B,G_DEPT C WHERE A.STATUS>=0 AND (A.UTYPE=2 OR A.UTYPE=1) AND A.ID=B.USER_ID AND B.FID=C.ID AND A.ISNATIVE=1";
        // dt = DBManager.getDataTable("select count(*) as num from (" + _cmdStr + ") derivedtbl_1");
        // if (dt.getRows().size() > 0) {
        // nUserCount = dt.getRows().get(0).getInt("num");
        // }
        // _cmdStr += " ORDER BY C.SHORDER ASC,B.SHORDER ASC";
        // dt = DBManager.getDataTable(_cmdStr);
        dt = sqlDataSource.query("getDepts");
        if (dt != null) {
            nUserCount = dt.getRows().size();
        }
        strArrUid = new String[nUserCount];
        strArrFUid = new String[nUserCount];
        strArrUName = new String[nUserCount];
        strArrUType = new String[nUserCount];
        strArrUDept = new String[nUserCount];
        int i = 0;
        for (xsf.data.DataRow dr : dt.getRows()) {
            strArrUid[i] = dr.getString("ID");
            strArrFUid[i] = dr.getString("USER_ID");
            strArrUName[i] = dr.getString("UNAME");
            strArrUType[i] = dr.getString("UTYPE");
            strArrUDept[i] = dr.getString("DEPT");
            i++;
        }
        AddUserNode(nodUser, domUsers, Integer.parseInt(sFid));
        String xml = domUsers.asXML();
        System.out.println(xml);
        return xml;
    }

    @WebMethod
    public String GetRemoteTreeUsers() {
        System.out.println("---------------------------------WFServiceImpl.GetRemoteTreeUsers()");
        // String _cmdStr = "";
        // String sFid = "";
        Document domUsers = DocumentHelper.createDocument();
        // domUsers.add(DocumentHelper.createElement("User"));
        // Node nodUser = domUsers.selectSingleNode("User");
        // // 远程单位根节炄1�7
        // _cmdStr = "SELECT A.ID,A.UNAME FROM G_USERS A,G_DEPT B WHERE A.ID=B.USER_ID AND B.FID=0 AND A.ISNATIVE=0";
        // xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        // if (dt.getRows().size() > 0) {
        // xsf.data.DataRow dr = dt.getRows().get(0);
        // sFid = dr.getString("ID");
        // ((Element) nodUser).addAttribute("UserName", dr.getString("UNAME"));
        // ((Element) nodUser).addAttribute("UserID", dr.getString("ID"));
        // ((Element) nodUser).addAttribute("UserType", "1");
        // }
        // // 变更 原因支持mysql
        // _cmdStr = "SELECT A.ID,A.UTYPE,A.UNAME,B.DEPT,C.USER_ID FROM G_USERS A,G_DEPT B,G_DEPT C WHERE A.STATUS>=0 ";
        // // _cmdStr += "AND ( A.UTYPE = 2 OR A.UTYPE = 0 OR A.UTYPE = 9 OR A.UTYPE=1 ) AND A.ID = B.USER_ID ";
        // _cmdStr += "AND (A.UTYPE=2 OR A.UTYPE=1) AND A.ID=B.USER_ID ";
        // _cmdStr += "AND B.FID=C.ID AND A.ISNATIVE=0";
        // dt = DBManager.getDataTable("select count(*) as num from (" + _cmdStr + ") derivedtbl_1");
        // if (dt.getRows().size() > 0) {
        // nRUserCount = dt.getRows().get(0).getInt("num");
        // }
        // _cmdStr += " ORDER BY C.SHORDER ASC,B.SHORDER ASC";
        // dt = DBManager.getDataTable(_cmdStr);
        // strArrRUid = new String[nRUserCount];
        // strArrRFUid = new String[nRUserCount];
        // strArrRUName = new String[nRUserCount];
        // strArrRUType = new String[nRUserCount];
        // strArrRUDept = new String[nRUserCount];
        // int i = 0;
        // for (xsf.data.DataRow dr : dt.getRows()) {
        // strArrRUid[i] = dr.getString("ID");
        // strArrRFUid[i] = dr.getString("USER_ID");
        // strArrRUName[i] = dr.getString("UNAME");
        // strArrRUType[i] = dr.getString("UTYPE");
        // strArrRUDept[i] = dr.getString("DEPT");
        // i++;
        // }
        // AddRemoteUserNode(nodUser, domUsers, Integer.parseInt(sFid));
        return domUsers.asXML();
    }

    @WebMethod
    public String GetWFSetInfo() {
        System.out.println("---------------------------------WFServiceImpl.GetWFSetInfo()");
        try {
            String url = WFServiceImpl.class.getResource("").getPath().replaceAll("%20", " ");
            String path = url.substring(0, url.indexOf("WEB-INF")) + "WEB-INF/config/WF.xml";
            Document dom = saxreader.read(new File(path));
            String xml = dom.asXML();
            System.out.println(xml);
            return xml;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // 从wdzx.wftypedic中获取流程信息XML
    @WebMethod
    public String GetWfTypeDic() {
        System.out.println("---------------------------------WFServiceImpl.GetWfTypeDic()");
        // String sql = "SELECT NAME AS WF_NAME,OBJCLASS AS WF_TYPEID FROM G_OBJS";
        // 必须有记录，否则“是否启用流程”无法勾选
        String sql = "SELECT NAME AS WF_NAME,ID AS WF_TYPEID FROM G_MODULE WHERE ROWSTATE > -1";
        return getXml(sql, "WFTYPE");
    }

    // 节点流转信息
    @WebMethod
    public String GetG_PNodesInfo(String INFO_ID, String WF_ID) {
        System.out.println("---------------------------------WFServiceImpl.GetG_PNodesInfo()");
        String returnxml = "";
        String _cmdStr = "";
        if ("0".equals(WF_ID)) {
            WF_ID = "(SELECT WF_ID FROM G_INFOS WHERE ID=" + INFO_ID + ")";
        }
        // ---------------------------------------------2012.07.11吴红亮
        _cmdStr = "SELECT WFNODE_ID,WFNODE_CAPTION FROM WFNODELIST WHERE WFNODE_NODETYPE=0 AND WF_ID=" + WF_ID;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        Map<String, String> endNodes = new HashMap<String, String>();
        for (xsf.data.DataRow dr : dt.getRows()) {
            endNodes.put(dr.getString("WFNODE_ID"), dr.getString("WFNODE_CAPTION"));
        }
        // ---------------------------------------------
        // String DBMS = ConfigurationSettings.DBMS;
        // if ("SYBASE".equals(DBMS)) {
        // _cmdStr = "SELECT A.ATYPE,A.MUSER_ID,DATEDIFF(dd,A.PDATE,A.EDATE) AS OUTTIME1,DATEDIFF(dd,GETDATE(),A.EDATE) AS OUTTIME2,convert(char(16),RDATE,20) AS CRDATE,convert(char(16),PDATE,20) AS CPDATE,A.* FROM G_PNODES A WHERE A.WF_ID=" + WF_ID + " AND WFNODE_WAIT=0 and INFO_ID=" + INFO_ID + " ORDER BY A.WFNODE_ID,A.STATUS,A.ID";
        // } else if ("ORACLE".equals(DBMS)) {
        // // _cmdStr = "SELECT A.ATYPE,A.MUSER_ID,A.PDATE-A.EDATE AS OUTTIME1,SYSDATE-A.EDATE AS OUTTIME2,TO_CHAR(RDATE,'YYYY.MM.DD HH24:MI') AS CRDATE,TO_CHAR(PDATE,'YYYY.MM.DD HH24:MI') AS CPDATE,A.* FROM G_PNODES A WHERE A.WF_ID =" + WF_ID + " AND wfnode_wait=0 and INFO_ID=" + INFO_ID + " ORDER BY A.WFNODE_ID,A.STATUS,A.ID";
        // _cmdStr = "SELECT A.WF_ID,A.WFNODE_ID,A.STATUS,case when A.MUSER_ID is null then A.USER_ID else A.MUSER_ID end MUSER_ID,A.ACTNAME,A.PID,A.ID,A.PDATE-A.EDATE AS OUTTIME1,SYSDATE-A.EDATE AS OUTTIME2,TO_CHAR(RDATE,'YYYY.MM.DD HH24:MI') AS CRDATE,TO_CHAR(PDATE,'YYYY.MM.DD HH24:MI') AS CPDATE FROM G_PNODES A inner join WFNODELIST B on A.WF_ID=B.WF_ID and A.WFNODE_ID=B.WFNODE_ID WHERE A.WF_ID=" + WF_ID + " AND A.WFNODE_WAIT=0 and A.INFO_ID=" + INFO_ID;
        // } else if ("SQLSERVER".equals(DBMS)) {
        // _cmdStr = "SELECT A.ATYPE,A.MUSER_ID,DATEDIFF(dd,A.PDATE,A.EDATE) AS OUTTIME1,DATEDIFF(dd,GETDATE(),A.EDATE) AS OUTTIME2,convert(char(16),RDATE,20) AS CRDATE,convert(char(16),PDATE,20) AS CPDATE,A.* FROM G_PNODES A WHERE A.WF_ID=" + WF_ID + " AND WFNODE_WAIT=0 and INFO_ID=" + INFO_ID + " ORDER BY A.WFNODE_ID,A.STATUS,A.ID";
        // } else if ("MYSQL".equals(DBMS)) {
        // _cmdStr = "SELECT A.ATYPE,A.MUSER_ID,DATEDIFF(A.PDATE,A.EDATE) AS OUTTIME1,DATEDIFF(NOW(),A.EDATE) AS OUTTIME2,RDATE AS CRDATE,PDATE AS CPDATE,A.* FROM G_PNODES A WHERE A.WF_ID=" + WF_ID + " AND WFNODE_WAIT=0 and INFO_ID=" + INFO_ID + " ORDER BY A.WFNODE_ID,A.STATUS,A.ID";
        // } else {
        // _cmdStr = "SELECT A.ATYPE,A.MUSER_ID,A.PDATE-A.EDATE AS OUTTIME1,SYSDATE-A.EDATE AS OUTTIME2,TO_CHAR(RDATE,'YYYY.MM.DD HH24:MI') AS CRDATE,TO_CHAR(PDATE,'YYYY.MM.DD HH24:MI') AS CPDATE,A.* FROM G_PNODES A WHERE A.WF_ID=" + WF_ID + " AND WFNODE_WAIT=0 and INFO_ID=" + INFO_ID + " ORDER BY A.WFNODE_ID,A.STATUS,A.ID";
        // }
        // // -------------------------------------
        // // _cmdStr = "select A.*,B.UNAME,C.CONTENT from(" + _cmdStr + ")A left join G_USERS B on A.MUSER_ID=B.ID left join G_OPINION C on C.PNID=A.ID AND C.PID=A.PID ORDER BY A.WFNODE_ID,A.STATUS,A.ID";
        // _cmdStr = "select A.*,B.NAME UNAME,C.CONTENT from(" + _cmdStr + ")A left join G_USERINFO B on A.MUSER_ID=B.ID left join G_OPINION C on C.PNID=A.ID AND C.PID=A.PID ORDER BY A.WFNODE_ID,A.STATUS,A.ID";
        // // -------------------------------------
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("INFO_ID", INFO_ID);
        sqlDataSource.setParameter("WF_ID", WF_ID);
        dt = sqlDataSource.query("getGPnodes");
        String tempNodeId = "";
        String str = "";
        String status = "0";// 输出要求:在办2，未办0， 已办1( G_PNODES.STATUS: 已办-1,未读1,已读2)
        String sName = "";
        Map<String, String> c = new HashMap<String, String>();
        for (xsf.data.DataRow dr : dt.getRows()) {
            WF_ID = dr.getString("WF_ID");
            String WFNODE_ID = dr.getString("WFNODE_ID");
            String MUSER_ID = dr.getString("MUSER_ID");
            String UNAME = dr.getString("UNAME");
            // String ATYPE = dr.getString("ATYPE");
            String ACTNAME = dr.getString("ACTNAME");
            // Date CRDATE = dr.getDate("CRDATE");
            Date CPDATE = dr.getDate("CPDATE");
            String OUTTIME1 = dr.getString("OUTTIME1");
            String OUTTIME2 = dr.getString("OUTTIME2");
            // String PID = dr.getString("PID");
            // String ID = dr.getString("ID");
            String CONTENT = dr.getString("CONTENT");
            if (!WFNODE_ID.equals(tempNodeId)) {// 不同节点（因为 order by WFNODE_ID）
                if (!"".equals(str)) {// 如果上一节点不为空（输出）
                    int count = c.get(tempNodeId).split(",").length - 1;
                    sName = count > 1 ? "共" + count + "人" : sName;
                    if (endNodes.containsKey(tempNodeId)) {
                        returnxml += "<Node ID='" + tempNodeId + "'><Status>" + status + "</Status><UserName>" + endNodes.get(tempNodeId) + "</UserName><Text></Text></Node>";
                    } else {
                        returnxml += "<Node ID='" + tempNodeId + "'><Status>" + status + "</Status><UserName>" + sName + "</UserName><Text>" + str + "</Text></Node>";
                    }
                }
                tempNodeId = WFNODE_ID;
                status = dr.getString("STATUS");
                // -------------------------------------
                sName = dr.getString("UNAME");// getUserName(UNAME, ATYPE, MUSER_ID);
                str = "环节名称：" + (ACTNAME != null ? ACTNAME : "");
                str += "^br^----------------------------------------------";
                str += "^br^办理人员：" + (UNAME != null ? UNAME : "");
                str += "^br^处理时间：" + (CPDATE != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(CPDATE) : "");
                Map<String, String> map = processTimeOut(status, CPDATE, OUTTIME1, OUTTIME2);
                str += map.get("INFO");
                status = map.get("STATUS");
                if (null != CONTENT && !"".equals(CONTENT)) {
                    str += "^br^处理意见：" + CONTENT.replace("\r\n", "^br^"); // GetOpinion(PID, ID);
                }
                // -------------------------------------统计
                String t = c.get(tempNodeId);
                if (null == t) {
                    c.put(tempNodeId, "," + MUSER_ID + ",");
                } else {
                    if (t.indexOf("," + MUSER_ID + ",") < 0) {
                        c.put(tempNodeId, t + MUSER_ID + ",");
                    }
                }
                // -------------------------------------
            } else {
                status = dr.getString("STATUS");
                // -------------------------------------
                sName = dr.getString("UNAME");// getUserName(UNAME, ATYPE, MUSER_ID);
                str += "^br^----------------------------------------------";
                str += "^br^办理人员：" + UNAME;
                str += "^br^处理时间：" + (CPDATE != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(CPDATE) : "");
                Map<String, String> map = processTimeOut(status, CPDATE, OUTTIME1, OUTTIME2);
                str += map.get("INFO");
                status = map.get("STATUS");
                // taolb 修改
                if (null != CONTENT && !"".equals(CONTENT)) {
                    str += "^br^处理意见：" + CONTENT.replace("\r\n", "^br^");// GetOpinion(PID, ID);
                }
                // -------------------------------------
                String t = c.get(tempNodeId);
                if (null == t) {
                    c.put(tempNodeId, "," + MUSER_ID + ",");
                } else {
                    if (t.indexOf("," + MUSER_ID + ",") < 0) {
                        c.put(tempNodeId, t + MUSER_ID + ",");
                    }
                }
            }
        }
        if (!"".equals(str)) {// 最后一节点(前面while处理0~n-1个，这里处理第n个)
            int count = c.get(tempNodeId).split(",").length - 1;
            sName = count > 1 ? "共" + count + "人" : sName;
            returnxml += "<Node ID='" + tempNodeId + "'><Status>" + status + "</Status><UserName>" + sName + "</UserName><Text>" + str + "</Text></Node>";
        }
        // --------------------------------------------
        // 判断是否办结
        // _cmdStr = "SELECT A.WFNODE_ID FROM G_PNODES A,WFNODELIST B WHERE B.WFNODE_NODETYPE=0 AND A.WFNODE_ID=B.WFNODE_ID AND A.WF_ID=" + WF_ID + " AND A.WF_ID=B.WF_ID AND A.INFO_ID=" + INFO_ID;
        // dt = DBManager.getDataTable(_cmdStr);
        // for (xsf.data.DataRow dr : dt.getRows()) {
        // returnxml += "<Node ID='" + dr.getString("WFNODE_ID") + "'><Status>1</Status><UserName>办结</UserName><Text></Text></Node>";
        // }
        // 得到完成的线信息
        _cmdStr = "select WFHEADVERTER,WFTAILVERTER from G_PNODES a,WFLINELIST b,G_PNODES c where a.WFNODE_ID=b.WFTAILVERTER and a.INFO_ID=" + INFO_ID + " and a.WF_ID=b.WF_ID and c.INFO_ID=" + INFO_ID + " and a.FID=c.ID and c.WFNODE_ID=b.WFHEADVERTER and a.WF_ID=" + WF_ID;
        dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
            returnxml += "<Line><Start>" + dr.getString("wfheadverter") + "</Start><End>" + dr.getString("wftailverter") + "</End></Line>";
        }
        // --------------------------------------------
        returnxml = "<?xml version='1.0' encoding='UTF-8'?><WFINFO WFID='" + WF_ID + "'>" + returnxml + "</WFINFO>";
        return returnxml;
    }

    // 获取两节点时间的工作日差值
    // sNowWorkTime:当前时间
    // sEndWorkTime:预计结束时间
    @WebMethod
    public String getDiffTime(String sNowWorkTime, String sEndWorkTime) throws Exception {
        System.out.println("---------------------------------WFServiceImpl.getDiffTime()");
        return DS_FLOWClass.getDiffTime(sNowWorkTime, sEndWorkTime);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    @WebMethod
    public String GetFileTranServiceXML() {
        try {
            Document domXmlFiles = DocumentHelper.createDocument();
            Node basenod = DocumentHelper.createElement("FileTranServiceXML");
            domXmlFiles.add(basenod);
            String[] fNames = {};
            for (String filename : fNames) {
                try {
                    Node nod = DocumentHelper.createElement("XMLFile");
                    nod.setText(filename);
                    ((Element) nod).addAttribute("FileName", filename.substring(filename.lastIndexOf("\\") + 1));
                    ((Element) basenod).add(nod);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
            return domXmlFiles.asXML();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getXml(String sql, String name) {
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myRead = _myConn.createStatement();
            _cmdStr = sql;
            _myDs = _myRead.executeQuery(_cmdStr);
            String temp = TypeConversion.convertResultSetToXML(_myDs, name);
            _myDs.close();
            System.out.println(temp);
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                _myRead.close();
            } catch (SQLException e) {
                e.printStackTrace();
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

    // 获得人员，部门根节点
    // Uname:返回根节点的名字
    // user_id:返回根节点的ID
    public User GetBoot(String Uname, String user_id) {
        User user = new User();
        // String _cmdStr = "";
        // _cmdStr = "SELECT C.ID,C.UNAME FROM G_DEPT A,G_USERS C WHERE C.ID=A.USER_ID AND A.DEPT=-1 AND C.ISNATIVE=1";
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        xsf.data.DataTable dt = sqlDataSource.query("getDeptRoot");
        for (xsf.data.DataRow dr : dt.getRows()) {
            user.setUname(dr.getString("ID"));
            user.setUser_id(dr.getString("UNAME"));
        }
        return user;
    }

    // 获得部门下的部门
    public DataTable GetChild_Dept(String user_id) {
        DataRow newrow;
        DataTable result_table = new DataTable("user");
        // String _cmdStr = "SELECT A.UTYPE,A.DEPT_PROPERTY,A.ID,A.UNAME,C.DEPT FROM G_USERS A,G_DEPT B,G_DEPT C WHERE A.ID=C.USER_ID AND C.FID=B.ID AND C.DEPT=1 AND B.USER_ID=" + user_id + " AND A.STATUS=0 ORDER BY C.SHORDER";
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("PID", user_id);
        xsf.data.DataTable dt = sqlDataSource.query("getSubDepts");
        for (xsf.data.DataRow dr : dt.getRows()) {
            newrow = result_table.NewRow();
            newrow.put("ID", dr.getString("ID"));
            newrow.put("UNAME", dr.getString("UNAME"));
            newrow.put("UTYPE", dr.getString("UTYPE"));
            newrow.put("DEPT", dr.getString("DEPT"));
            newrow.put("DEPT_PROPERTY", dr.getString("DEPT_PROPERTY"));
            result_table.rows.add(newrow);
        }
        return result_table;
    }

    private void InitializeComponent() {
    }

    private void setKey(String dir, String key, String value) {
        Preferences pref = Preferences.userRoot().node(dir);
        // System.out.println(key+":"+ value);
        // Microsoft.Win32.RegistryKey k;
        // k = Microsoft.Win32.Registry.CurrentUser.CreateSubKey(dir);
        // k.SetValue(key, value);
        // k.Close();
        pref.put(key, value);
    }

    private String getKey(String dir, String key) {
        // Preferences pref = Preferences.userRoot().node(dir);
        // Microsoft.Win32.RegistryKey k;
        // k = Microsoft.Win32.Registry.CurrentUser.CreateSubKey(dir);
        // String s = (pref.get(key, "") != null) ? pref.get(key, "") : "";
        // return s;
        /**
         * 修改不用注册表验证 温法科 <修改> 2010/06/02
         */
        return "45826156537611480028125042643";
    }

    private String GetWFBaseInfo(String sWFID) throws Exception {
        String sReturnXML = "<BaseInfo>";
        String sCount = "0";
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myRead = _myConn.createStatement();
            _cmdStr = "SELECT WF_CAPTION,WF_TYPEID,WF_XML,WF_FLAG FROM WFDEFINITION WHERE WF_ID=" + sWFID;
            _myDs = _myRead.executeQuery(_cmdStr);
            while (_myDs.next()) {
                String sCaption = _myDs.getString("WF_CAPTION");
                // -----------------------------------------------------------
                sCaption = xsf.Value.getString(sCaption, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
                String sTypeid = _myDs.getString("WF_TYPEID");
                // -----------------------------------------------------------
                sTypeid = xsf.Value.getString(sTypeid, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
                String wfXml = _myDs.getString("WF_XML");
                // -----------------------------------------------------------
                wfXml = xsf.Value.getString(wfXml, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
                String wfFlag = _myDs.getString("WF_FLAG");
                wfFlag = "1".equals(wfFlag) ? "1" : "0";
                // -----------------------------------------------------------
                wfXml = syncWF(sWFID, sCaption, wfXml, _myConn);
                wfXml = wfXml == null || "null".equalsIgnoreCase(wfXml) ? "" : wfXml;
                sReturnXML += wfXml;
                if ("<BaseInfo>".equals(sReturnXML)) {
                    sReturnXML += "<Name>" + sCaption + "</Name>";
                    sReturnXML += "<ObjClass>" + sTypeid + "</ObjClass>";
                    sReturnXML += "<Enabled>" + wfFlag + "</Enabled>";
                    sReturnXML += "<StartTime>2003-2-13</StartTime>";
                    sReturnXML += "<EndTime>2003-2-13</EndTime>";
                    sReturnXML += "<LimitTime>0</LimitTime>";
                    sReturnXML += "<LimitTimeCondition></LimitTimeCondition>";
                    sReturnXML += "<LimitTimeType>0</LimitTimeType>";
                }
                sCount = IsEnabledFlag(sWFID, sTypeid);
            }
            _myDs.close();
            sReturnXML += "<IsEnabledFlag>" + sCount + "</IsEnabledFlag>";
            sReturnXML += "</BaseInfo>";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                _myRead.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (_myConn != null) {
                try {
                    _myConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return sReturnXML;
    }

    private String GetWFNodeInfo(String sWFID) throws Exception {
        String sReturnXML = "<NodeInfo>";
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        String _cmdStr = "";
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myRead = _myConn.createStatement();
            Map<String, String> roleMap = loadRoles(_myRead);
            _cmdStr = "SELECT WFNODE_ID,WFNODE_XML FROM WFNODELIST WHERE WF_ID=" + sWFID + " ORDER BY WFNODE_ID";
            _myDs = _myRead.executeQuery(_cmdStr);
            while (_myDs.next()) {
                String WFNODE_XML = _myDs.getString("WFNODE_XML");
                // -----------------------------------------------------------
                WFNODE_XML = xsf.Value.getString(WFNODE_XML, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
                String WFNODE_ID = _myDs.getString("WFNODE_ID");
                WFNODE_XML = syncRole(sWFID, WFNODE_ID, WFNODE_XML, roleMap, _myConn);
                sReturnXML += "<Node>" + WFNODE_XML + "</Node>";
            }
            _myDs.close();
            sReturnXML += "</NodeInfo>";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                _myRead.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (_myConn != null) {
                try {
                    _myConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return sReturnXML;
    }

    private String GetWFLineInfo(String sWFID) throws Exception {
        String sReturnXML = "<LineInfo>";
        String _cmdStr = "";
        _cmdStr = "SELECT WFLINE_XML FROM WFLINELIST WHERE WF_ID=" + sWFID + " ORDER BY WFLINE_ID";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
            sReturnXML = sReturnXML + "<Line>" + dr.getString("WFLINE_XML") + "</Line>";
        }
        sReturnXML = sReturnXML + "</LineInfo>";
        return sReturnXML;
    }

    private String IsEnabledFlag(String sWFID, String sTypeid) throws Exception {
        if (true) {
            return "0";
        }
        int count = 0;
        String _cmdStr = "";
        _cmdStr = "SELECT COUNT(*) AS EXISTCOUNT FROM WFDEFINITION WHERE WF_TYPEID='" + sTypeid + "' AND WF_FLAG=1 AND WF_ID<>" + sWFID;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            count = dt.getRows().get(0).getInt("EXISTCOUNT");
        }
        return String.valueOf(count);
    }

    private String DeleteWFA(String WF_ID, Statement _myRead) throws Exception {
        try {
            String sSql = "DELETE FROM WFDEFINITION WHERE WF_ID=" + WF_ID;
            _myRead.executeUpdate(sSql.toUpperCase());
            sSql = "DELETE FROM WFNODELIST WHERE WF_ID=" + WF_ID;
            _myRead.executeUpdate(sSql.toUpperCase());
            sSql = "DELETE FROM WFLINELIST WHERE WF_ID=" + WF_ID;
            _myRead.executeUpdate(sSql.toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return "0";
    }

    private int SaveWFBaseInfo(Node mydoc, int iWF_ID, Connection _myConn) throws Exception {
        int iWFID = iWF_ID;
        try {
            if (iWFID == 0) {
                iWFID = Integer.parseInt(ds_GetPriKey("WF_ID", _myConn));
            }
            String sSql;
            Long mainUnit=null;
	        String mainUnitStr= DBManager.getFieldStringValue("SELECT MAINUNIT FROM WFDEFINITION WHERE WF_ID= "+iWFID);//山东烟草新增   获取子机构id
	        String isPublic= DBManager.getFieldStringValue("SELECT ISPUBLIC FROM WFDEFINITION WHERE WF_ID= "+iWFID);//山东烟草新增   获取子机构id
	        if(!StringHelper.isNullOrEmpty(mainUnitStr)){
	        	mainUnit=Value.getLong(mainUnitStr);
	        }
            sSql = "INSERT INTO WFDEFINITION(WF_ID,WF_TypeID,WF_Caption,WF_Flag,WF_Type,STime,ETime,WF_TimeSpan,WF_TimeType,WF_XML,WF_TIMECONDITION,MAINUNIT,ISPUBLIC) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = _myConn.prepareStatement(sSql.toUpperCase());
            ps.setLong(1, iWFID);
            String WF_TypeID = mydoc.selectSingleNode("ObjClass").getText();
            // -----------------------------------------------------------
            WF_TypeID = xsf.Value.getString(WF_TypeID, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(2, WF_TypeID);
            String Name = mydoc.selectSingleNode("Name").getText();
            // -----------------------------------------------------------
            Name = xsf.Value.getString(Name, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(3, Name);
            ps.setLong(4, Long.parseLong(mydoc.selectSingleNode("Enabled").getText()));
            ps.setLong(5, 2);
            if (mydoc.selectSingleNode("StartTime").getText() == "") {
                ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));
            } else {
            	try{
            	//如果日期格式为yyyy-M-dd转换异常则使用日期格式yyyy/M/dd转换 杨龙修改2013/6/20
                ps.setDate(6, new java.sql.Date(sdf.parse(mydoc.selectSingleNode("StartTime").getText()).getTime()));
            	}
            	catch (Exception e) {
            		SimpleDateFormat sf = new SimpleDateFormat("yyyy/M/dd");
            		ps.setDate(6, new java.sql.Date(sf.parse(mydoc.selectSingleNode("StartTime").getText()).getTime()));
				}
            }
            if (mydoc.selectSingleNode("StartTime").getText() == "") {
                ps.setDate(7, new java.sql.Date(new java.util.Date().getTime()));
            } else {
            	try{
            	//如果日期格式为yyyy-M-dd转换异常则使用日期格式yyyy/M/dd转换 杨龙修改2013/6/20
                ps.setDate(7, new java.sql.Date(sdf.parse(mydoc.selectSingleNode("EndTime").getText()).getTime()));
            	}catch (Exception e) {
            		SimpleDateFormat sf = new SimpleDateFormat("yyyy/M/dd");
            		ps.setDate(7, new java.sql.Date(sf.parse(mydoc.selectSingleNode("EndTime").getText()).getTime()));
				}
            }
            //索引8应该存储LimitTime，而不是LimitTimeType，杨龙修改 2013/6/24
            ps.setLong(8, Long.parseLong(mydoc.selectSingleNode("LimitTime").getText()));
            //ps.setLong(8, Long.parseLong(mydoc.selectSingleNode("LimitTimeType").getText()));
            ps.setLong(9, Long.parseLong(mydoc.selectSingleNode("LimitTimeType").getText()));
            String str1 = Dom4jTools.getSingleNodeInnerXml(mydoc, "/");
            char[] chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(10, str1);
            str1 = mydoc.selectSingleNode("LimitTimeCondition").getText();
            chrM1 = null;
            chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(11, str1);
            ps.setObject(12, mainUnit);
            ps.setObject(13, isPublic);
            // System.out.println("执行的流程sql:" + sSql);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return iWFID;
    }

    private int SaveWFNodeInfo(Node mydoc, int iWF_ID, Connection _myConn) throws Exception {
        List<?> nodelist = mydoc.selectNodes("Node");
        for (Object obj : nodelist) {
            Node tnode = (Node) obj;
            SaveWFNode(tnode, iWF_ID, _myConn);
        }
        return 0;
    }

    private int SaveWFNode(Node mydoc, int iWF_ID, Connection _myConn) throws Exception {
        try {
            String sSql;
            sSql = "INSERT INTO WFNODELIST(WFNODE_ID,WF_ID,PWF_ID,WFNODE_CAPTION,WFNODE_MEMO,WFNODE_NODEORDER,WFNODE_TIMETYPE,WFNODE_TIMESPAN,WFNODE_NODETYPE,WFNODE_ACL,WFNODE_INLINEFLAG,WFNODE_INRATE,WFNODE_OUTLINEFLAG,WFNODE_WAIT,WFNODE_SENDMETHOD,WFNODE_FORWARD,WFNODE_BEFORESENDSQL,WFNODE_AFTERSENDSQL,WFNODE_BACKSENDSQL,WFNODE_XML,WFNODE_ISMULTIUSER,WFNODE_ISSELECTED,WFNODE_ISONLYONEUSER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = _myConn.prepareStatement(sSql.toUpperCase());
            ps.setInt(1, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/Index").getText()));
            ps.setInt(2, iWF_ID);
            ps.setInt(3, iWF_ID);
            String WFNODE_CAPTION = mydoc.selectSingleNode("BaseInfo/Caption").getText();
            // -----------------------------------------------------------
            WFNODE_CAPTION = xsf.Value.getString(WFNODE_CAPTION, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(4, WFNODE_CAPTION);
            String WFNODE_MEMO = mydoc.selectSingleNode("BaseInfo/Desc").getText();
            // -----------------------------------------------------------
            WFNODE_MEMO = xsf.Value.getString(WFNODE_MEMO, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(5, WFNODE_MEMO);
            try {
                ps.setInt(6, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/Order").getText()));
            } catch (Exception e) {
                e.printStackTrace();
                ps.setInt(6, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/Index").getText()));
            }
            if (Integer.parseInt(mydoc.selectSingleNode("BaseInfo/IsExpiry").getText()) == 0) {
                ps.setInt(7, 0);
                ps.setInt(8, 0);
            } else {
                ps.setInt(7, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/TimeType").getText()));
                ps.setInt(8, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/Expiry").getText()));
            }
            ps.setInt(9, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/Type").getText()));
            // ------------------------------------------------------------------------------------------- WFNODE_ACL
            String str1 = Dom4jTools.getSingleNodeInnerXml(mydoc, "ACL");
            char[] chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(10, str1);
            // -------------------------------------------------------------------------------------------
            ps.setInt(11, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/InLineFlag").getText()));
            ps.setInt(12, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/Rate").getText()));
            ps.setInt(13, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/OutLineFlag").getText()));
            ps.setInt(14, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/IsWait").getText()));
            ps.setInt(15, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/SendMethod").getText()));
            ps.setInt(16, Integer.parseInt(mydoc.selectSingleNode("BaseInfo/IsForword").getText()));
            str1 = Dom4jTools.getSingleNodeInnerXml(mydoc, "OtherControl/BeforeSendSql");
            chrM1 = null;
            chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(17, str1);
            // ------------------------------------------------------------------------------------------- WFNODE_AFTERSENDSQL
            str1 = Dom4jTools.getSingleNodeInnerXml(mydoc, "OtherControl/AfterSendSql");
            chrM1 = null;
            chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(18, str1);
            // -------------------------------------------------------------------------------------------
            str1 = Dom4jTools.getSingleNodeInnerXml(mydoc, "OtherControl/BackSendSql");
            chrM1 = null;
            chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(19, str1);
            // ------------------------------------------------------------------------------------------- WFNODE_XML
            str1 = Dom4jTools.getSingleNodeInnerXml(mydoc, "/");
            chrM1 = null;
            chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(20, str1);
            // -------------------------------------------------------------------------------------------
            ps.setInt(21, Integer.parseInt(mydoc.selectSingleNode("ACL/IsMultiUser").getText()));
            ps.setInt(22, Integer.parseInt(mydoc.selectSingleNode("ACL/IsSelected").getText()));
            // 增加判断 如果没有传isOnlyOneUser节点，不报错， 陶修改
            int isOnlyOneUser = 0;
            try {
                Node isOnlyOneUserNode = mydoc.selectSingleNode("ACL/IsOnlyOneUser");
                if (isOnlyOneUserNode != null) {
                    isOnlyOneUser = Integer.parseInt(isOnlyOneUserNode.getText());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // ps.setInt(23, 0);
            }
            ps.setInt(23, isOnlyOneUser);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return 0;
    }

    private int SaveWFLineInfo(Node mydoc, int iWF_ID, Connection _myConn) throws Exception {
        List<?> nodelist = mydoc.selectNodes("Line");
        for (Object obj : nodelist) {
            Node tnode = (Node) obj;
            SaveWFLine(tnode, iWF_ID, _myConn);
        }
        return 0;
    }

    private int SaveWFLine(Node mydoc, int iWF_ID, Connection _myConn) throws Exception {
        try {
            String sSql;
            int iWFLINE_ID = 0;
            iWFLINE_ID = Integer.parseInt(ds_GetPriKey("WFLINELIST", _myConn));
            sSql = "INSERT INTO WFLINELIST(WFLINE_ID,WF_ID,WFLINE_CAPTION,WFHEADVERTER,WFTAILVERTER,ACONDITION,WFLINE_TYPE,WFLINE_XML,ACONDITIONFLAG)VALUES(?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = _myConn.prepareStatement(sSql.toUpperCase());
            ps.setInt(1, iWFLINE_ID);
            ps.setInt(2, iWF_ID);
            String WFLINE_CAPTION = mydoc.selectSingleNode("Name").getText();
            // -----------------------------------------------------------
            WFLINE_CAPTION = xsf.Value.getString(WFLINE_CAPTION, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(3, WFLINE_CAPTION);
            ps.setInt(4, Integer.parseInt(mydoc.selectSingleNode("StartNode").getText()));
            ps.setInt(5, Integer.parseInt(mydoc.selectSingleNode("EndNode").getText()));
            String ACONDITION = Dom4jTools.getSingleNodeInnerXml(mydoc, "Condition");
            // -----------------------------------------------------------
            ACONDITION = xsf.Value.getString(ACONDITION, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(6, ACONDITION);
            String WFLINE_TYPE = mydoc.selectSingleNode("Type").getText();
            // -----------------------------------------------------------
            WFLINE_TYPE = xsf.Value.getString(WFLINE_TYPE, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(7, WFLINE_TYPE);
            String str1 = Dom4jTools.getSingleNodeInnerXml(mydoc, "/");
            char[] chrM1 = str1.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    str1 += " ";
                }
            }
            // -----------------------------------------------------------
            str1 = xsf.Value.getString(str1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            ps.setString(8, str1);
            try {
                ps.setInt(9, Integer.parseInt(mydoc.selectSingleNode("ConditionFlag").getText()));
            } catch (Exception e) {
                e.printStackTrace();
                ps.setInt(9, 0);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return 0;
    }

    private String ds_GetPriKey(String sTag, Connection _myConn) throws SQLException {
        String sReturnID = "";
        String sSql = "";
      //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 开始
    	String DBMS = ConfigurationSettings.AppSettings("DBMS");
    	if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
    		sSql = "select MAXID from `MAXVALUE` where TAG='" + sTag + "'";
        } else {
        	sSql = "select MAXID from MAXVALUE where TAG='" + sTag + "'";
        }
        //sSql = "select MAXID from MAXVALUE where TAG='" + sTag + "'";
    	
        ResultSet _myDs = _myConn.createStatement().executeQuery(sSql);
        while (_myDs.next()) {
            sReturnID = _myDs.getString("MAXID");
        }
        _myDs.close();
        if ("MYSQL".equals(DBMS)) {// 添加（冗余数据）BT，DELETED，HASCONTENT，URGENT，SWH，WH，OBJCLASS
        	sSql = "update `MAXVALUE` set MAXID=" + (Integer.parseInt(sReturnID) + 1) + " where TAG='" + sTag + "'";
        } else {
        	sSql = "update MAXVALUE set MAXID=" + (Integer.parseInt(sReturnID) + 1) + " where TAG='" + sTag + "'";
        }
        //sSql = "update MAXVALUE set MAXID=" + (Integer.parseInt(sReturnID) + 1) + " where TAG='" + sTag + "'";
      //增加mysql保留字MAXVALUE的验证  杨龙修改 2013.2.27 结束
        _myConn.createStatement().executeUpdate(sSql);
        return String.valueOf(Integer.parseInt(sReturnID) + 1);
    }

    // MD5 加密
    // private String getMD5Str(String str) {
    // java.security.MessageDigest messageDigest = null;
    // try {
    // messageDigest = java.security.MessageDigest.getInstance("MD5");
    // messageDigest.reset();
    // messageDigest.update(str.getBytes("UTF-8"));
    // } catch (java.security.NoSuchAlgorithmException e) {
    // e.printStackTrace();
    // System.exit(-1);
    // } catch (java.io.UnsupportedEncodingException e) {
    // e.printStackTrace();
    // }
    // byte[] byteArray = messageDigest.digest();
    // StringBuffer md5StrBuff = new StringBuffer();
    // for (int i = 0; i < byteArray.length; i++) {
    // if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
    // md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
    // } else {
    // md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
    // }
    // }
    // return md5StrBuff.toString();
    // }

    // private void AddRemoteUserNode(Node nod, Document dom, int fid) {
    // for (int k = 0; k < nRUserCount; k++) {
    // if (String.valueOf(fid).equals(strArrRFUid[k])) {
    // // DataRow dataRow = childTable.Rows[k];
    // // int UserType = Integer.parseInt(strArrRUType[k]);
    // String UserDept = strArrRUDept[k];
    // String strUid = strArrRUid[k];
    // String strUName = strArrRUName[k];
    // Node nodUser = DocumentHelper.createElement("User");
    // ((Element) nodUser).addAttribute("UserName", strUName);
    // ((Element) nodUser).addAttribute("UserID", strUid);
    // ((Element) nodUser).addAttribute("UserType", UserDept);
    // if (nod == null) {
    // dom.add(nodUser);
    // } else {
    // ((Element) nod).add(nodUser);
    // }
    // AddRemoteUserNode(nodUser, dom, Integer.parseInt(strUid));
    // }
    // }
    // }

    private void AddUserNode(Node nod, Document dom, long fid) {
        for (int k = 0; k < nUserCount; k++) {
            if (String.valueOf(fid).equals(strArrFUid[k])) {
                // DataRow dataRow = childTable.Rows[k];
                // int UserType = Integer.parseInt(strArrUType[k]);
                String UserDept = strArrUDept[k];
                String strUid = strArrUid[k];
                String strUName = strArrUName[k];
                Node nodUser = DocumentHelper.createElement("User");
                ((Element) nodUser).addAttribute("UserName", strUName);
                ((Element) nodUser).addAttribute("UserID", strUid);
                ((Element) nodUser).addAttribute("UserType", UserDept);
                if (nod == null) {
                    dom.add(nodUser);
                } else {
                    ((Element) nod).add(nodUser);
                }
                AddUserNode(nodUser, dom, Long.parseLong(strUid));
            }
        }
    }

    @SuppressWarnings("unused")
    private String getUserName(String User_Name, String ATYPE, String DUser_ID) {
        // String _cmdStr = "";
        String DUser_Name = "";
        // _cmdStr = "select UNAME from G_USERS where ID=" + DUser_ID;
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("USER_ID", DUser_ID);
        xsf.data.DataTable dt = sqlDataSource.query("getUserName");
        if (dt.getRows().size() > 0) {
            DUser_Name = dt.getRows().get(0).getString(1);
        }
        if (DUser_Name.length() > 0 && !User_Name.equals(DUser_Name)) {
            if ("1".equals(ATYPE)) { // 长期代办
                ;// User_Name = User_Name;
            } else if ("2".equals(ATYPE)) {// 委托代办
                User_Name = DUser_Name + "(" + User_Name + "代)";
            } else { // 临时代办
                User_Name += "(代)";
            }
        }
        return User_Name;
    }

    @SuppressWarnings("unused")
    private String GetOpinion(String pid, String pnid) {
        String sReturnStr = "";
        String _cmdStr = "";
        _cmdStr = "SELECT B.CONTENT FROM G_OPINION B WHERE B.PNID=" + pnid + " AND B.PID=" + pid + " ORDER BY PDATE";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            String opinion = dt.getRows().get(0).getString("CONTENT");
            // sReturnStr = "^br^处理意见：" + (null == opinion || "".equals(opinion) ? "已阅" : opinion.replace("\r\n", "^br^"));
            // taolb 修改
            if (null != opinion && !"".equals(opinion)) {
                sReturnStr = "^br^处理意见：" + opinion.replace("\r\n", "^br^");
            }
        }
        return sReturnStr;
    }

    private Map<String, String> processTimeOut(String status, Date CPDATE, String OUTTIME1, String OUTTIME2) {
        Map<String, String> map = new HashMap<String, String>();
        String str = "";
        if ("-1".equals(status)) {
            str += "^br^办结时间：" + (CPDATE != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(CPDATE) : "");
            if (OUTTIME1 != null && !"".equals(OUTTIME1)) {
                int ext = Float.floatToIntBits(Float.parseFloat(OUTTIME1));
                if (ext > 0) {
                    str += "(超时" + ext + "天)";
                }
            }
            status = "1";
        } else {
            if (OUTTIME2 != null && !"".equals(OUTTIME2)) {
                int ext = Float.floatToIntBits(Float.parseFloat(OUTTIME2));
                if (ext > 0) {
                    str += "^br^超时：" + ext + "天";
                }
            }
            status = "2";
        }
        map.put("STATUS", status);
        map.put("INFO", str);
        return map;
    }

    public static String syncWF(String WF_ID, String sCaption, String WF_XML, Connection connection) throws Exception {
        if (WF_XML == null || WF_XML.equals("")) {
            return WF_XML;
        }
        // System.out.println(WF_XML);
        String xml = "<WF>" + WF_XML + "</WF>";
        // System.out.println(xml);
        Document wf = DocumentHelper.parseText(xml);
        // System.out.println(wf.asXML());
        String sCaption0 = wf.selectSingleNode("WF/Name").getText();
        // System.out.println(sCaption0);
        if (sCaption != null && !sCaption.equals(sCaption0)) {
            wf.selectSingleNode("WF/Name").setText(sCaption);
            // System.out.println(wf.asXML());
            WF_XML = Dom4jTools.getSingleNodeInnerXml(wf, "WF");
            // System.out.println(WF_XML);
            // -----------------------------------------------------------
            WF_XML = xsf.Value.getString(WF_XML, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            PreparedStatement ps = connection.prepareStatement("update WFDEFINITION set WF_XML=? where WF_ID=" + WF_ID);
            ps.setString(1, WF_XML);
            ps.executeUpdate();
        }
        return WF_XML;
    }

    public static void syncWFNode(Map<String, String> wordPrintList, Map<String, String> gTemp, Map<String, String> gForm, Map<String, String> wfnodelist, Map<String, String> m0, Map<String, String> m1, Connection connection) throws Exception {
        for (String xformId : gTemp.keySet()) {
            String gTempXML = gTemp.get(xformId);
            if (gTempXML == null || "".equals(gTempXML)) {
                continue;
            }
            Document ControlXML = DocumentHelper.parseText(gTempXML);
            // System.out.println(ControlXML.asXML());
            boolean isSync = false;
            for (Object obj : ControlXML.selectNodes("ControlXML/Control")) {
                Node control = (Node) obj;
                String outProcessor = control.selectSingleNode("Html").getText();
                if (outProcessor.indexOf("<out:processor=\"xsf.processor.plus.ViewOpinion\"") != -1) {
                    isSync = opinionControl(control, m0, m1) || isSync;
                } else if (outProcessor.indexOf("<out:processor=\"xsf.processor.plus.SimpleOpinion2\"") != -1) {
                    isSync = opinionControl(control, m0, m1) || isSync;
                } else if (outProcessor.indexOf("<out:processor=\"xsf.processor.plus.oa.SignatureControl\"") != -1) { // 增加对签名控件 G_TEMP表流程节点的修改 taolb 2012.4.23
                    isSync = signControl(control, m0, m1) || isSync;
                }
            }
            // System.out.println(ControlXML.asXML());
            if (isSync) {
                String CONTENT = "<ControlXML>" + Dom4jTools.getSingleNodeInnerXml(ControlXML, "ControlXML") + "</ControlXML>";
                // System.out.println(CONTENT);
                char[] chrM1 = CONTENT.toCharArray();
                for (int i = 0; i < chrM1.length; i++) {
                    if ((int) chrM1[i] > 1000) {
                        CONTENT += " ";
                    }
                }
                // -----------------------------------------------------------
                CONTENT = xsf.Value.getString(CONTENT, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                // -----------------------------------------------------------
                PreparedStatement ps = connection.prepareStatement("update G_TEMP set CONTENT=? where EX_1=" + xformId + " and NAME='xml'");
                ps.setString(1, CONTENT);
                ps.executeUpdate();
            }
        }
        // System.out.println("\n\n\n\n\n");
        for (String xformId : gForm.keySet()) {
            String gFormHtml = gForm.get(xformId);
            if (gFormHtml == null || "".equals(gFormHtml)) {
                continue;
            }
            Parser parser = new Parser();
            parser.setInputHTML(gFormHtml);
            // parser.setEncoding("gb2312");
            NodeFilter filter = new AndFilter(new TagNameFilter("out:processor"), new HasAttributeFilter("processExp"));
            NodeList ControlHTML = parser.parse(filter);
            boolean isSync = false;
            for (int i = 0; i < ControlHTML.size(); i++) {
                org.htmlparser.Node control = ControlHTML.elementAt(i);
                String t0 = control.toHtml();
                if (t0.indexOf("xsf.processor.plus.ViewOpinion") != -1) {
                    // System.out.println(xformId + " " + control.toHtml());
                    isSync = opinionControl(control, m0, m1) || isSync;
                    String t1 = control.toHtml();
                    // System.out.println(t1);
                    gFormHtml = gFormHtml.replace(t0, t1);
                } else if (t0.indexOf("xsf.processor.plus.SimpleOpinion2") != -1) {
                    // System.out.println(xformId + " " + control.toHtml());
                    isSync = opinionControl(control, m0, m1) || isSync;
                    String t1 = control.toHtml();
                    // System.out.println(t1);
                    gFormHtml = gFormHtml.replace(t0, t1);
                } else if (t0.indexOf("xsf.processor.plus.oa.SignatureControl") != -1) { // 增加对签名控件 G_FORM表流程节点的修改 taolb 2012.4.23
                    isSync = opinionControl(control, m0, m1) || isSync;
                    String t1 = control.toHtml();
                    gFormHtml = gFormHtml.replace(t0, t1);
                }
            }
            if (isSync) {
                // Lexer lexer = parser.getLexer();
                // lexer.getPage().getText();
                // System.out.println(lexer.getPage().getText());
                // System.out.println(gFormHtml);
                // -----------------------------------------------------------
                gFormHtml = xsf.Value.getString(gFormHtml, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                // -----------------------------------------------------------
                PreparedStatement ps = connection.prepareStatement("update G_FORM set TEMPLATE=? where ID=" + xformId);
                ps.setString(1, gFormHtml);
                ps.executeUpdate();
            }
        }
        // System.out.println("\n\n\n\n\n");
        for (String wfnodeId : wfnodelist.keySet()) {
            String xml = wfnodelist.get(wfnodeId);
            if (xml == null || "".equals(xml)) {
                continue;
            }
            Document WFNODE_XML = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Root>" + xml + "</Root>");
            boolean isSync = false;
            for (Object obj : WFNODE_XML.selectNodes("Root/OtherControl/AfterSendSql")) {
                Node afterSendSql = (Node) obj;
                isSync = syncAfterSendSql(afterSendSql, m0, m1) || isSync;
            }
            if (isSync) {
                // System.out.println(WFNODE_XML.asXML());
                // System.out.println(Dom4jTools.getSingleNodeInnerXml(WFNODE_XML, "Root"));
                // System.out.println(Dom4jTools.getSingleNodeInnerXml(WFNODE_XML, "Root/OtherControl/AfterSendSql"));
                // System.out.println(wfnodeId.split("_")[0]);
                // System.out.println(wfnodeId.split("_")[1]);
                String t0 = Dom4jTools.getSingleNodeInnerXml(WFNODE_XML, "Root");
                char[] chrM1 = t0.toCharArray();
                for (int i = 0; i < chrM1.length; i++) {
                    if ((int) chrM1[i] > 1000) {
                        t0 += " ";
                    }
                }
                // -----------------------------------------------------------
                t0 = xsf.Value.getString(t0, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                // -----------------------------------------------------------
                String t1 = Dom4jTools.getSingleNodeInnerXml(WFNODE_XML, "Root/OtherControl/AfterSendSql");
                chrM1 = t1.toCharArray();
                for (int i = 0; i < chrM1.length; i++) {
                    if ((int) chrM1[i] > 1000) {
                        t1 += " ";
                    }
                }
                // -----------------------------------------------------------
                t1 = xsf.Value.getString(t1, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                // -----------------------------------------------------------
                PreparedStatement ps = connection.prepareStatement("update WFNODELIST set WFNODE_XML=?,WFNODE_AFTERSENDSQL=? where WF_ID=? and WFNODE_ID=?");
                ps.setString(1, t0);
                ps.setString(2, t1);
                ps.setString(3, wfnodeId.split("_")[0]);
                ps.setString(4, wfnodeId.split("_")[1]);
                ps.executeUpdate();
            }
        }
        // 增加对打印项节点的同步
        java.util.Set<String> wordPrintElements = wordPrintList.keySet();
        for (String elementId : wordPrintElements) {
            // 配合m1处理
            String wordPrintFormat = wordPrintList.get(elementId); // 格式:节点名称1,节点名称2,...-id1,id2,...
            String newWordPrintFormat = replaceWordPrintNodes(wordPrintFormat, m1);
            // 更新
            PreparedStatement ps = connection.prepareStatement("update G_PRINT_ELEMENT set INDICATOR_COLUMN =  ? where ID = ? ");
            ps.setString(1, newWordPrintFormat);
            ps.setString(2, elementId);
            ps.executeUpdate();
        }
    }

    public static String syncRole(String WF_ID, String WFNODE_ID, String WFNODE_XML, Map<String, String> roleMap, Connection connection) throws Exception {
        // System.out.println(WFNODE_XML);
        String WFNODE_ACL = null;
        String xml = "<Node>" + WFNODE_XML + "</Node>";
        Document wfNode = DocumentHelper.parseText(xml);
        // System.out.println(wfNode.asXML());
        Node deptrole = wfNode.selectSingleNode("Node/ACL/deptrole");
        if (null != deptrole) {
            // System.out.println(deptrole.asXML());
            Node role = deptrole.selectSingleNode("role");
            // System.out.println(role.asXML());
            String roleId = role.getText();
            String roleName0 = role.valueOf("@Name");
            String roleName1 = roleMap.get(roleId);
            // System.out.println(roleId + " " + roleName0 + " " + roleName1);
            if (roleName1 != null && !roleName1.equals(roleName0)) {
                role.selectSingleNode("@Name").setText(roleName1);
                Node text = deptrole.selectSingleNode("text");
                text.setText(text.getText().replace(roleName0, roleName1));
                // System.out.println(wfNode.asXML());
                WFNODE_XML = Dom4jTools.getSingleNodeInnerXml(wfNode, "/Node");
                WFNODE_ACL = Dom4jTools.getSingleNodeInnerXml(wfNode, "/Node/ACL");
                char[] chrM1 = WFNODE_XML.toCharArray();
                for (int i = 0; i < chrM1.length; i++) {
                    if ((int) chrM1[i] > 1000) {
                        WFNODE_XML += " ";
                    }
                }
                // -----------------------------------------------------------
                WFNODE_XML = xsf.Value.getString(WFNODE_XML, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                // -----------------------------------------------------------
                // -----------------------------------------------------------
                WFNODE_ACL = xsf.Value.getString(WFNODE_ACL, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
                // -----------------------------------------------------------
                PreparedStatement ps = connection.prepareStatement("update WFNODELIST set WFNODE_XML=?,WFNODE_ACL=? where WF_ID=? and WFNODE_ID=?");
                ps.setString(1, WFNODE_XML);
                ps.setString(2, WFNODE_ACL);
                ps.setString(3, WF_ID);
                ps.setString(4, WFNODE_ID);
                ps.executeUpdate();
            }
        }
        return WFNODE_XML;
    }

    private static Map<String, String> loadRoles(Statement statement) throws Exception {
        Map<String, String> roleMap = new HashMap<String, String>();
        // String _cmdStr = "SELECT DISTINCT G_USERS.ID AS ROLEID,G_USERS.UNAME AS ROLENAME FROM G_UROLES,G_USERS WHERE G_UROLES.USER_ID=G_USERS.ID AND G_USERS.UTYPE=3 AND STATUS=0";
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        ICommand command = (ICommand) sqlDataSource.getSelectCommands().get("getRoles");
        String _cmdStr = command.getCommandText().trim();
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        while (_myDs.next()) {
            String ROLEID = _myDs.getString("ROLEID");
            String ROLENAME = _myDs.getString("ROLENAME");
            // -----------------------------------------------------------
            ROLENAME = xsf.Value.getString(ROLENAME, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            if (ROLEID != null && !"".equals(ROLEID)) {
                // System.out.println(ROLEID + " " + ROLENAME);
                roleMap.put(ROLEID, ROLENAME);
            }
        }
        _myDs.close();
        return roleMap;
    }

    private static Map<String, String> loadNodes(String WF_ID, Statement statement) throws Exception {
        Map<String, String> nameMap = new HashMap<String, String>();
        String _cmdStr = "SELECT WFNODE_ID,WFNODE_CAPTION,WFNODE_MEMO FROM WFNODELIST WHERE WF_ID=" + WF_ID;
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        while (_myDs.next()) {
            String WFNODE_ID = _myDs.getString("WFNODE_ID");
            String WFNODE_CAPTION = _myDs.getString("WFNODE_CAPTION");
            // -----------------------------------------------------------
            WFNODE_CAPTION = xsf.Value.getString(WFNODE_CAPTION, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            if (WFNODE_ID != null && !"".equals(WFNODE_ID)) {
                // System.out.println(WFNODE_ID + " " + WFNODE_CAPTION);
                nameMap.put(WFNODE_ID, WFNODE_CAPTION);
            }
        }
        _myDs.close();
        return nameMap;
    }

    private static Map<String, String> loadGTemp(String WF_ID, Statement statement) throws Exception {
        Map<String, String> gTemp = new HashMap<String, String>();
        // MySql不支持
        // String _cmdStr = "select EX_1,CONTENT from G_TEMP where NAME='xml' and EX_1 in(select FORMID from G_FORMFORMGROUP where FORMGROUPID in(select A.RID from G_MODULERELATION A inner join G_MODULE B on A.MODULEID=B.ID where A.MODULEID in(select MODULEID from G_MODULERELATION where RID=" + WF_ID + " and RFLAG=200112) and A.RFLAG=200104))";
        String _cmdStr = "select EX_1,CONTENT from G_TEMP where NAME='xml' and EX_1 in(select FORMID from G_FORMFORMGROUP A inner join(select A.RID from G_MODULERELATION A inner join G_MODULE B on A.MODULEID=B.ID and A.RFLAG=200104 inner join (select MODULEID from G_MODULERELATION where RID=" + WF_ID + " and RFLAG=200112)C on A.MODULEID=C.MODULEID)B on A.FORMGROUPID=B.RID)";
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        while (_myDs.next()) {
            String xformId = _myDs.getString("EX_1");
            // -----------------------------------------------------------
            xformId = xsf.Value.getString(xformId, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            String content = _myDs.getString("CONTENT");
            // -----------------------------------------------------------
            content = xsf.Value.getString(content, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            if (xformId != null && !"".equals(xformId)) {
                // System.out.println(xformId + " " + content);
                gTemp.put(xformId, content);
            }
        }
        _myDs.close();
        return gTemp;
    }

    private static Map<String, String> loadGForm(String WF_ID, Statement statement) throws Exception {
        Map<String, String> gTemp = new HashMap<String, String>();
        // MySql不支持
        // String _cmdStr = "select ID,TEMPLATE from G_FORM where ID in(select FORMID from G_FORMFORMGROUP where FORMGROUPID in(select A.RID from G_MODULERELATION A inner join G_MODULE B on A.MODULEID=B.ID where A.MODULEID in(select MODULEID from G_MODULERELATION where RID=" + WF_ID + " and RFLAG=200112) and A.RFLAG=200104))";
        String _cmdStr = "select ID,TEMPLATE from G_FORM where ID in(select FORMID from G_FORMFORMGROUP A inner join(select A.RID from G_MODULERELATION A inner join G_MODULE B on A.MODULEID=B.ID and A.RFLAG=200104 inner join (select MODULEID from G_MODULERELATION where RID=" + WF_ID + " and RFLAG=200112)C on A.MODULEID=C.MODULEID)B on A.FORMGROUPID=B.RID)";
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        while (_myDs.next()) {
            String xformId = _myDs.getString("ID");
            // -----------------------------------------------------------
            xformId = xsf.Value.getString(xformId, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            String content = _myDs.getString("TEMPLATE");
            // -----------------------------------------------------------
            content = xsf.Value.getString(content, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            if (xformId != null && !"".equals(xformId)) {
                // System.out.println(xformId + " " + content);
                gTemp.put(xformId, content);
            }
        }
        _myDs.close();
        return gTemp;
    }

    /**
     * 导入打印意见和签名控件格式
     * 
     * @param WF_ID
     * @param statement
     * @return
     * @throws Exception
     */
    private static Map<String, String> loadWordPrintFormats(String WF_ID, Statement statement) throws Exception {
        Map<String, String> printElements = new HashMap<String, String>();
        String _cmdStr = "select ID,INDICATOR_COLUMN from G_PRINT_ELEMENT where INDICATOR_TYPE in (1,4) and PRINT_ID in (select A.RID from G_MODULERELATION A inner join G_MODULE B on A.MODULEID=B.ID where A.MODULEID in(select MODULEID from G_MODULERELATION where RID=" + WF_ID + " and RFLAG=200112) and A.RFLAG=200113)";
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        while (_myDs.next()) {
            String elementId = _myDs.getString("ID");
            String content = _myDs.getString("INDICATOR_COLUMN");
            if (elementId != null && content != null && !"".equals(elementId) && !"".equals(content)) {
                printElements.put(elementId, content);
            }
        }
        _myDs.close();
        return printElements;
    }

    private static Map<String, String> loadWfnodelist(String WF_ID, Statement statement) throws Exception {
        Map<String, String> wfnodelist = new HashMap<String, String>();
        String _cmdStr = "select WFNODE_ID,WFNODE_XML from WFNODELIST where WF_ID=" + WF_ID;
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        while (_myDs.next()) {
            String WFNODE_ID = _myDs.getString("WFNODE_ID");
            String WFNODE_XML = _myDs.getString("WFNODE_XML");
            // -----------------------------------------------------------
            WFNODE_XML = xsf.Value.getString(WFNODE_XML, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            if (WFNODE_ID != null && !"".equals(WFNODE_ID)) {
                // System.out.println(xformId + " " + content);
                wfnodelist.put(WF_ID + "_" + WFNODE_ID, WFNODE_XML);
            }
        }
        _myDs.close();
        return wfnodelist;
    }

    private static String replaceWordPrintNodes(String content, Map<String, String> nodes) {
        String[] nodeNames = content.split("-");
        if (nodeNames.length == 2) {
            String[] _printIds = nodeNames[1].split(",");
            String[] _printNames = nodeNames[0].split(",");
            int len = _printIds.length;
            if (len > 0 && len == _printNames.length) {
                StringBuffer _nodeNames = new StringBuffer();
                StringBuffer _nodeIds = new StringBuffer();
                boolean first = true;
                for (int i = 0; i < len; i++) {
                    String printId = _printIds[i];
                    String newNodeName = nodes.get(printId);// 数据内容
                    if (newNodeName != null) {
                        if (!first) {
                            _nodeIds.append(",");
                            _nodeNames.append(",");
                        }
                        _nodeIds.append(printId);
                        _nodeNames.append(newNodeName);
                        first = false;
                    }
                }
                if (_nodeNames.length() > 0) {
                    content = _nodeNames.append("-").append(_nodeIds).toString();
                } else {
                    content = "";
                }
            }
        }
        return content;
    }

    private static boolean opinionControl(Node control, Map<String, String> m0, Map<String, String> m1) throws Exception {
        // System.out.println("**********************************************************************************************************************");
        Node n1 = control.selectSingleNode("Parms/FWType");
        Node n2 = control.selectSingleNode("ReplaceHtml");
        // System.out.println(n1.asXML());
        String s0 = n1.valueOf("@Desc");
        String s1 = replaceNodeName(s0, m0, m1);
        if (!s0.equals(s1)) {
            n1.selectSingleNode("@Desc").setText(s1);
            ((Element) n1).clearContent();
            ((Element) n1).add(DocumentHelper.createCDATA(s1));
            // System.out.println(n1.asXML());
            // System.out.println(n2.asXML());
            s1 = n2.getText().replace(s0, s1);
            ((Element) n2).clearContent();
            ((Element) n2).add(DocumentHelper.createCDATA(s1));
            // System.out.println(n2.asXML());
            return true;
        }
        return false;
    }

    /**
     * 处理签名控件在g_temp表中 节点名称的替换
     * 
     * @param control
     * @param m0
     * @param m1
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static boolean signControl(Node control, Map<String, String> m0, Map<String, String> m1) throws Exception {
        // System.out.println("**********************************************************************************************************************");
        Node n1 = control.selectSingleNode("Parms/NodesName");
        String s0 = null;
        Node checkNode = null;
        String type = n1.valueOf("@Type");
        if ("List".equals(type)) { // 判断是列表
            List<Element> nodes = n1.selectNodes("ListItem/Item");
            for (Node node : nodes) {
                String check = node.valueOf("@IsCheck");
                if ("1".equals(check)) {
                    checkNode = node;
                    s0 = node.valueOf("@ShowValue");
                }
            }
        } else {
            s0 = n1.getStringValue();
        }
        Node n2 = control.selectSingleNode("ReplaceHtml");
        if (s0 != null) {
            String s1 = replaceNodeName(s0, m0, m1);
            if (!s0.equals(s1)) {
                if (checkNode == null) { // 文本
                    ((Element) n1).clearContent();
                    ((Element) n1).add(DocumentHelper.createCDATA(s1));
                } else { // 下拉框情况
                    checkNode.selectSingleNode("@ShowValue").setText(s1);
                    checkNode.selectSingleNode("@Value").setText(s1);
                }
                s1 = n2.getText().replace(s0, s1);
                ((Element) n2).clearContent();
                ((Element) n2).add(DocumentHelper.createCDATA(s1));
                // System.out.println(n2.asXML());
                return true;
            }
        }
        return false;
    }

    private static boolean opinionControl(org.htmlparser.Node control, Map<String, String> m0, Map<String, String> m1) throws Exception {
        // System.out.println("**********************************************************************************************************************");
        String t = control.toHtml();
        int t0 = t.indexOf("<NodesName>");
        int t1 = t.indexOf("</NodesName>");
        String s0 = t.substring(t0 + "<NodesName>".length(), t1);
        String s1 = replaceNodeName(s0, m0, m1);
        // System.out.println(t0 + " " + t1 + " " + s0 + " " + s1);
        if (!s0.equals(s1)) {
            // System.out.println(t);
            t = t.substring(0, t0 + "<NodesName>".length()) + s1 + t.substring(t1, t.length());
            // System.out.println(t);
            control.setText(t);
            return true;
        }
        return false;
    }

    private static boolean syncAfterSendSql(Node afterSendSql, Map<String, String> m0, Map<String, String> m1) throws Exception {
        // System.out.println("**********************************************************************************************************************");
        boolean isChange = false;
        String s0 = afterSendSql.getText();
        if (s0 == null || "".equals(s0)) {
            return isChange;
        }
        for (String nodeId : m0.keySet()) {
            String t0 = m0.get(nodeId);
            if (t0 == null || "".equals(t0)) {
                continue;
            }
            if (s0.indexOf("'" + t0 + "'") > -1) {
                String t1 = m1.get(nodeId);
                if (t1 != null && !"".equals(t1) && !t1.equals(t0)) {
                    s0 = s0.replaceAll("'" + t0 + "'", "'" + t1 + "'");
                    afterSendSql.setText(s0);
                    isChange = true;
                }
            }
        }
        return isChange;
    }

    private static String replaceNodeName(String s0, Map<String, String> m0, Map<String, String> m1) {
        // System.out.println("1--------------" + s0);
        String[] nodelist = s0.split(",");
        s0 = "";
        for (int i = 0; i < nodelist.length; i++) {
            String ti = nodelist[i];
            boolean isHas = false;
            for (String id : m0.keySet()) {
                // System.out.println(nodelist[i] + " " + m0.get(id));
                String t0 = m0.get(id);
                if (t0.equals(ti)) {
                    String t1 = m1.get(id);
                    if (!t0.equals(t1) && t1 != null) {
                        nodelist[i] = t1;
                    }
                    isHas = true;
                    break;
                }
            }
            if (!isHas) {
                nodelist[i] = null;
            }
            if (nodelist[i] != null) {
                s0 += nodelist[i] == null ? "" : (nodelist[i] + ",");
            }
        }
        if (s0.length() > 0) {
            while (",".equals("" + s0.charAt(s0.length() - 1))) {
                s0 = s0.substring(0, s0.length() - 1);
                // System.out.println(s0);
            }
        }
        // System.out.println("2--------------" + s0);
        return s0;
    }

    private static String checkInBox(String WF_ID, Map<String, String> m0, Map<String, String> m1, Statement statement) throws Exception {
        String ids = "";
        for (String OID : m0.keySet()) {
            if (!m1.containsKey(OID)) {
                ids += OID + ",";
            }
        }
        if ("".equals(ids)) {
            return null;
        }
        m1 = new HashMap<String, String>();
        ids = ids.substring(0, ids.length() - 1);
        String _cmdStr = "select WFNODE_ID from G_INBOX where WF_ID=" + WF_ID + " and WFNODE_ID in(" + ids + ")";
        ResultSet _myDs = statement.executeQuery(_cmdStr);
        while (_myDs.next()) {
            String WFNODE_ID = _myDs.getString("WFNODE_ID");
            m1.put(WFNODE_ID, WFNODE_ID);
        }
        _myDs.close();
        String names = "";
        for (String WFNODE_ID : m1.keySet()) {
            if (m0.containsKey(WFNODE_ID)) {
                names += m0.get(WFNODE_ID) + ",";
            }
        }
        if ("".equals(names)) {
            return null;
        }
        return names.substring(0, names.length() - 1);
    }

    private static void setTSNode(String WF_ID, Map<String, String> m0, Map<String, String> m1, Connection connection) throws Exception {
        String ids = "";
        for (String NID : m1.keySet()) {
            if (!m0.containsKey(NID)) {
                ids += NID + ",";
            }
        }
        if ("".equals(ids)) {
            return;
        }
        String sTSNodeList = "";
        String _cmdStr = "SELECT WFNODE_ID,WFNODE_XML FROM WFNODELIST WHERE WF_ID=" + WF_ID + " AND WFNODE_NODETYPE=1";
        ResultSet _myDs = connection.createStatement().executeQuery(_cmdStr);
        if (_myDs.next()) {
            String WFNODE_ID = _myDs.getString("WFNODE_ID");
            String WFNODE_XML = _myDs.getString("WFNODE_XML");
            // -----------------------------------------------------------
            WFNODE_XML = xsf.Value.getString(WFNODE_XML, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
            // -----------------------------------------------------------
            Document wfNode = DocumentHelper.parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Node>" + WFNODE_XML + "</Node>");
            sTSNodeList = wfNode.selectSingleNode("Node/OtherControl/TSNodeList").getText();
            sTSNodeList = sTSNodeList.trim();
            sTSNodeList += ids;
            wfNode.selectSingleNode("Node/OtherControl/TSNodeList").setText(sTSNodeList);
            WFNODE_XML = Dom4jTools.getSingleNodeInnerXml(wfNode, "/Node");
            char[] chrM1 = WFNODE_XML.toCharArray();
            for (int i = 0; i < chrM1.length; i++) {
                if ((int) chrM1[i] > 1000) {
                    WFNODE_XML += " ";
                }
            }
            // -----------------------------------------------------------
            WFNODE_XML = xsf.Value.getString(WFNODE_XML, xsf.Config.CHARSET, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"));
            // -----------------------------------------------------------
            PreparedStatement ps = connection.prepareStatement("update WFNODELIST set WFNODE_XML=? where WF_ID=? and WFNODE_ID=?");
            ps.setString(1, WFNODE_XML);
            ps.setString(2, WF_ID);
            ps.setString(3, WFNODE_ID);
            ps.executeUpdate();
        }
    }

    class User {
        public String Uname;
        public String user_id;

        public String getUname() {
            return Uname;
        }

        public void setUname(String uname) {
            Uname = uname;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }
    }

    public static void main(String[] args) throws Exception {
        // WFService ws = new WFServiceImpl();
        // System.out.println(ws.addWorkDays("", 3));
        // ws.setKey("/dsflow", "ss", "12312313");
        // System.out.println(ws.getKey("/dsflow", "ss"));
        // --------------------------------------------------------------------
        // String s = "<Position><X>564</X><Y>162</Y></Position><BaseInfo><Index>10</Index><Caption>办公室核稿</Caption><Order>10</Order><Desc>办公室核稿</Desc><Type>3</Type><SubFlowID /><SendMethod>0</SendMethod><IsWait>0</IsWait><IsForword>0</IsForword><IsEMail>0</IsEMail><IsTray>0</IsTray><IsExpiry>0</IsExpiry><Expiry>0</Expiry><TimeType>0</TimeType><InLineFlag>0</InLineFlag><OutLineFlag>0</OutLineFlag><Rate>0</Rate></BaseInfo><ACL><IsRoleSelect>1</IsRoleSelect><IsSelected>0</IsSelected><IsOnlyOneUser>0</IsOnlyOneUser><IsAutoSend>0</IsAutoSend><IsAutoExpand>1</IsAutoExpand><IsMultiUser>0</IsMultiUser><type>0</type><deptrole><dept Name=\"办公室（研究室） \" Level=\"761112\">761112</dept><role Name=\"办公室核稿人\">517871</role><text>办公室（研究室）-办公室核稿人</text></deptrole></ACL><ButtonInfo /><ADSet><HZNode
        // /></ADSet><OtherControl><TSNodeList>,</TSNodeList><BeforeSendSql /><AfterSendSql /><BackSendSql /><AutoSelectUserSql /></OtherControl>";
        // Map<String, String> roleMap = null;
        // Connection _myConn = null;
        // Statement _myRead = null;
        // try {
        // _myConn = DBManager.getConnection(xsf.Config.CONNECTION_KEY);
        // _myRead = _myConn.createStatement();
        // roleMap = loadRoles(_myRead);
        // syncRole("858", "10", s, roleMap, _myConn);
        // } catch (Exception e) {
        // e.printStackTrace();
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
        // --------------------------------------------------------------------
        // Connection _myConn = null;
        // Statement _myRead = null;
        // try {
        // _myConn = DBManager.getConnection(xsf.Config.CONNECTION_KEY);
        // _myRead = _myConn.createStatement();
        // Map<String, String> m0 = loadNodes("858", _myRead);
        // Map<String, String> m1 = new HashMap<String, String>();
        // m1.put("1", "拟稿人");
        // m1.put("2", "处室领导审核");
        // m1.put("9", "编号111");
        // m1.put("10", "办公室核稿");
        // m1.put("15", "部门会签");
        // m1.put("18", "处室内部人");
        // m1.put("19", "分管局长签发");
        // m1.put("20", "局长签发");
        // m1.put("23", "领导及收发员");
        // m1.put("24", "办结");
        // m1.put("25", "送回处室领导");
        // m1.put("26", "处室人员");
        // m1.put("27", "退回拟稿人");
        // Map<String, String> content = loadGTemp("858", _myRead);
        // syncWFNode(content, m0, m1, _myConn);
        // } catch (Exception e) {
        // e.printStackTrace();
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
        // --------------------------------------------------------------------
        // String sCaption = "下行文2";
        // String wfXml = "<Name>下行文</Name><ObjClass>964915</ObjClass><Enabled>1</Enabled><StartTime>2011-4-14 10:52:28</StartTime><EndTime>2011-4-14 10:52:28</EndTime><LimitTime>0</LimitTime><LimitTimeCondition/><LimitTimeType>0</LimitTimeType><SMS><Sql/><Content/></SMS> ";
        // Connection _myConn = null;
        // try {
        // _myConn = DBManager.getConnection(xsf.Config.CONNECTION_KEY);
        // syncWF("858", sCaption, wfXml, _myConn);
        // } catch (Exception e) {
        // e.printStackTrace();
        // throw e;
        // } finally {
        // if (_myConn != null) {
        // try {
        // _myConn.close();
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
        // }
        // }
        // --------------------------------------------------------------------
        // String s0 = "领导,节点名称1,登记人,";
        // while (",".equals("" + s0.charAt(s0.length() - 1))) {
        // s0 = s0.substring(0, s0.length() - 1);
        // System.out.println(s0);
        // }
        // System.out.println(s0);
        // System.out.println("NOW,NOW,".replaceAll("NOW", "NOW()"));
        // --------------------------------------------------------------------
        Connection _myConn = null;
        Statement _myRead = null;
        try {
            Map<String, String> m0 = new HashMap<String, String>();
            Map<String, String> m1 = new HashMap<String, String>();
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            setTSNode("858", m0, m1, _myConn);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                _myRead.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (_myConn != null) {
                try {
                    _myConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        // --------------------------------------------------------------------
    }

}