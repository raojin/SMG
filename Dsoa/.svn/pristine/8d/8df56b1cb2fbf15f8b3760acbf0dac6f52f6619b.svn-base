package dsoap.web.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xsf.Config;
import xsf.data.DBManager;
import xsf.data.ICommand;
import xsf.data.IDataSource;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.SysDataSource;
import dsoap.tools.webwork.Action;

public class FormStatusAction extends Action {
    private static final long serialVersionUID = 1664880343099818293L;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
    public String errStr = "";
    List hzID=null; //汇总中的pnid

    @Override
    public String execute() throws Exception {
        super.execute();
        String infoID = request.getParameter("id");
        String show = request.getParameter("formStatus");
        String wfID = request.getParameter("wf_id");
        String listMode = request.getParameter("listMode");
        Connection _myConn = null;
        Statement _myRead = null;
        ResultSet _myDs = null;
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            _myRead = _myConn.createStatement();
            String cmdStr = "";
            String wf_id = "0";
            Page page = new Page();
            page.CONTEXT_PATH = request.getContextPath();
            page._Id = Long.parseLong(infoID);// INFO_ID
            page._Content = new String[100];
            page._Status = new int[100];
            page._Nodes = new int[100];
            page._Column = 5; // 每行显示5
            try {
                if (!"0".equals(wfID)) {
                    wf_id = wfID;
                } else {
                    String DBMS = ConfigurationSettings.AppSettings("DBMS");
                    if ("SYBASE".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,CONVERT(CHAR(10),BLQX,102) || ' ' || CONVERT(CHAR(5),BLQX,108) EDATE,CONVERT(CHAR(10),BJRQ,102) || ' ' || CONVERT(CHAR(5),BJRQ,108) BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else if ("ORACLE".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,TO_CHAR(BLQX,'YYYY.MM.DD HH24:MI') EDATE,TO_CHAR(BJRQ,'YYYY.MM.DD HH24:MI') BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else if ("CLOUD".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,TO_CHAR(BLQX,'YYYY.MM.DD HH24:MI') EDATE,TO_CHAR(BJRQ,'YYYY.MM.DD HH24:MI') BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else if ("SQLSERVER".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,CONVERT(CHAR(10),BLQX,102) + ' ' + CONVERT(CHAR(5),BLQX,108) EDATE,CONVERT(CHAR(10),BJRQ,102) + ' ' + CONVERT(CHAR(5),BJRQ,108) BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else if ("MYSQL".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,DATE_FORMAT(BLQX,'YYYY.MM.DD HH24:MI') EDATE,DATE_FORMAT(BJRQ,'YYYY.MM.DD HH24:MI') BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,CONVERT(CHAR(10),BLQX,102) + ' ' + CONVERT(CHAR(5),BLQX,108) EDATE,CONVERT(CHAR(10),BJRQ,102) + ' ' + CONVERT(CHAR(5),BJRQ,108) BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    }
                    _myDs = _myRead.executeQuery(cmdStr);
                    if (_myDs.next()) {
                        wf_id = _myDs.getString("WF_ID");
                    }
                    _myDs.close();
                }
                // ---------------------------------
                // cmdStr = "SELECT A.ID,A.FID,A.UNAME,'' DNAME,C.WFNODE_MEMO ACTNAME,A.STATUS,A.RDATE,A.PDATE,A.WAITCOUNT ";
                // cmdStr += "FROM G_PNODES A,WFNODELIST C ";
                // cmdStr += "WHERE A.WF_ID=" + wf_id + " AND A.info_ID = " + page._Id + " AND A.WF_ID = C.WF_ID AND A.WFNODE_ID = C.WFNODE_ID";
                IDataSource sqlDataSource = SysDataSource.getSysDataSource();
                ICommand command = (ICommand) sqlDataSource.getSelectCommands().get("getFormStatus");
                cmdStr = command.getCommandText().trim();
                cmdStr = cmdStr.replace("?WF_ID", wf_id).replace("?INFO_ID", String.valueOf(page._Id));
                Map<String, Node> nodes = new HashMap<String, Node>();
                String root = loadTree(nodes, cmdStr, _myConn);
              //查询正在汇总中的节点ID
                cmdStr="select t.PNID from g_inbox t where t.wf_id="+wf_id+" and t.INFO_ID="+infoID+" and t.STATUS=3";
                _myDs = _myRead.executeQuery(cmdStr);
                hzID=new ArrayList<String>();
                while (_myDs.next()) {
                	hzID.add(_myDs.getString("PNID")); 
                }
                // ---------------------------------
                cmdStr = "select * from G_OPINION where trim(to_char(content))!='' and PID in (select PID from G_PNODES where INFO_ID=" + page._Id + " and WF_ID=" + wf_id + ")";
                if (listMode != null) {
                    initList1(root, nodes, cmdStr, page, _myConn);
                    request.setAttribute("tabView", page.getHtml());
                    request.setAttribute("itemList", page.items);
                    if (show == null) {
                        return "success2";
                    } else {
                        return "success3";
                    }
                } else {
                    initList(root, nodes, cmdStr, page, _myConn);
                }
                // ---------------------------------
                if (page._Count > 0) {
                    request.setAttribute("tabView", page.getHtml());
                    request.setAttribute("nodesInfo", page.nodesInfo);
                } else {
                    errStr = "<script language='javascript'>alert('该文件没有流转信息!');//top.close();</script>";
                    return ERROR;
                }
            } catch (Exception e) {
                e.printStackTrace();
                errStr = "<script language='javascript'>alert('生成流程图时产生错误，错误原因：" + e.getMessage() + "，当前SQL语句：" + cmdStr + "');//top.close();</script>";
                return ERROR;
            }
            if (show == null) {
                return "success1";
            }
            return SUCCESS;
        } catch (Exception e1) {
            e1.printStackTrace();
            errStr = e1.getMessage();
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
    }

    // 将相同父亲的G_PNODES放到同一个流程图节点中
    private void initList(String fidStr, Map<String, Node> nodes, String _myDs2Sql, Page page, Connection _myConn) throws SQLException, ParseException {
        // System.out.println(_myDs2Sql);
        int Id, Fid, Status = 0;
        // System.out.println("查找流程图节点--" + fidStr);
        String UName;
        String actName;
        String Yj;
        String pDate, rDate;
        boolean findRow;
        String[] strSplit = fidStr.split(",");// 此次的所有父节点id
        String idStr = "";// 下一次的父节点（是此次所有父节点的所有子节点）
        // Date PDate;
        String pDateStr = "";
        int waitStatus = 0;
        int receiveStatus = 0;
        int endStatus = 0;
        String Content = "";
        // -----------------------
        String nodesInfoHtml = "";
        String nodesInfoId = "";
        String nodesName = "";
        // -----------------------
        int nodeCount = 0;
        String nodeYj = "";
        int waitCount = 0;
        // 处理节点
        for (String key : nodes.keySet()) {
            Node node = nodes.get(key);
          //判断该节点是否汇总中。
        	if(hzID.contains(String.valueOf(node.getId())))
        	{
        		continue;
        	}
            // 判断当前记录是不是当前节点的子节点，不是的话则继续下一条记录的处理
            Fid = node.getFid();
            findRow = false;
            for (String str : strSplit) {
                if (Integer.parseInt(str) == Fid) {
                    findRow = true;
                    break;
                }
            }
            if (!findRow) {
                continue;
            }
            // 父ID的记录在指定id内
            nodeCount++;
            Id = node.getId();
            Status = node.getStatus();// ---------------------------------------------------
            waitCount = node.getWaitCount();
            UName = node.getUName() + (node.isProxy ? "(代)" : "");
            actName = node.getActName();
            nodesName = actName.startsWith("抄送") ? nodesName : actName;
            rDate = node.getRDate();
            pDate = node.getPDate();
            idStr += (idStr.length() > 0 ? "," : "") + Id;
            Status = waitCount > 0 ? 0 : Status;
            if (Status == 0 && waitStatus == 0) {
                waitStatus = 1;
            }
            if (Status == -1 && endStatus == 0) {
                endStatus = 1;
            }
            if ((Status == 1 || Status == 2) && receiveStatus == 0) {
                receiveStatus = 1;
            }
            // -------------------------------------------------
            Yj = "";
            String sql = _myDs2Sql + " and PNID=" + Id + " ORDER BY LASTUPDATEDATE";
            // System.out.println("查找流程图节点（意见）--" + sql);
            ResultSet _myDs = _myConn.createStatement().executeQuery(sql);
            while (_myDs.next()) {
                String temp = _myDs.getString("CONTENT");
               
               
                // -----------------------------------------------------------
                if (temp != null && !"".equals(temp)) {
                    temp = xsf.Value.getString(temp, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                }
                // -----------------------------------------------------------
                if (temp == null) {
                    nodeYj = "[BR]";
                } else {
                    nodeYj = temp.toString();
                }
                // "圈阅"
                if (Yj.length() > 0) {
                    Yj += "[BR]";
                }
                Yj += nodeYj;
            }
            _myDs.close();
            if (Yj == null) {
                Yj = "";
            } else {
                Yj = Yj.trim().replace("\r\n", "[BR]");
            }
            // -------------------------------------------------
            Calendar cl = Calendar.getInstance(); // 时间
            if(pDate==null)
            {
            	pDate=rDate;
            }
            cl.setTime(sdf.parse(Status == -1 ? pDate : rDate));// ------------------------日志出错
            pDateStr = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(cl.getTime());
            // -------------------------------------------------多人时浮动层数据
            String bgColor = "";
            if (Yj.length() > 0) {
                bgColor = " bgcolor='white'";
            }
            String temp = UName + (Status == -1 ? "(办)" : "");
            String js = "";
            if (Yj.length() > 0) {
                temp = "<a href='javascript:void(0);' style='padding-left:10px;'>" + temp + "</a><span class='form_status_node_YJ2' style='cursor:pointer;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
                js = " onmouseover=\"javascript:com.syc.oa.formStatus.popup('" + Yj + "','white');\"  onmouseout='javascript:com.syc.oa.formStatus.kill();'";
            }
            if ((nodeCount - 1) % 3 == 0) {
                nodesInfoHtml += "</tr>";
                nodesInfoHtml += "<tr>";
            }
            nodesInfoHtml += "<td" + bgColor + " height='100%' align='center' style='font-size:12px;" + (nodeCount - 1 < 3 ? "" : "padding-top:20px;") + "'" + js + ">" + "<div style='font-size:12px;width:116px'>" + actName + "</div>" + temp + "<Br>" + pDateStr + "</td>";
            nodesInfoId = Fid + "_" + Id;
            // -------------------------------------------------当前节点
            Content = "<tr>";
            if (Status == 1 || Status == 2) {
                Content += "<td align=center colspan=2 style='font-size:12px;'>";
            } else {
                Content += "<td align=center colspan=3 style='font-size:12px;padding-bottom:0px;padding-top:0px;'>";
            }
            if (nodeCount > 1) {
                Content += nodesName + "<Br>";
                Content += "<table border=0 width='100%' cellspacing='0' cellpadding='0'>" + "<tr>" + "<td align=center style='font-size:12px;padding-bottom:0px;padding-top:0px;'>" + "<span style='padding-left:15px;'>共(" + nodeCount + ")人</span>" + "</td>" + "<td class='form_status_node_info' width=10 onclick=\"javascript:com.syc.oa.formStatus.createYJDIV(this,'" + nodesInfoId + "');\">" + "</td>" + "</tr>" + "</table>";
                Content += "</td></tr>";
            } else {
                Content += actName +"<Br>";
                if (Yj.length() > 0) {
                	//添加隐藏事件--lvxd
                	Content += "<div style='width:100%;' onmouseover=\"javascript:com.syc.oa.formStatus.popup('" + Yj + "','white');\" onmouseout='javascript:com.syc.oa.formStatus.kill();'>" + "<a href='javascript:void(0);' style='padding-left:10px;'>" + UName + "</a><span class='form_status_node_YJ2' style='cursor:pointer;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>" + "</div>";
                } else {
                    Content += UName;
                }
                Content += "</td></tr>";
                Content += "<tr><td align=center colspan=2 height=12 style='font-size:12px;'>" + pDateStr + "</td></tr>";
            }
        }
        // -------------------------------------------------------------------------------------------------------------------------------------------------------
        Content = "<table width=100% border=0 align=center class='tablelist_'>" + Content + "</table>";
        if (nodeCount > 1) {
            String t = "<div id='nodesInfo_" + nodesInfoId + "' style='background-color:White;display:none;position:absolute;left:0;top:250;border: solid 1px #83bfe6;'>" + "<table cellSpacing=1 cellPadding=0 border=0><tr style='display:none;'>" + nodesInfoHtml + "" + "</tr>" + "</table>" + "</div>";
            page.nodesInfo.put(nodesInfoId, t);
        }
        // -------------------------------------------------------------------------------------------------------------------------------------------------------
        if (nodeCount > 0) {
            if (nodeCount < 2) {
                if (Status == 1 || Status == 2) {
                    Status = 1;
                }
            } else {
                if (receiveStatus == 1) {
                    Status = 1;
                } else {
                    if (waitStatus == 1) {
                        Status = 0;
                    } else {
                        Status = -1;
                    }
                }
            }
            page._Count++;
            page._Status[page._Count - 1] = Status;
            page._Nodes[page._Count - 1] = nodeCount;// 发文数量
            page._Content[page._Count - 1] = Content;// 发文（收文人，时间）
            // ---------------------------------------------------处理子节点
            initList(idStr, nodes, _myDs2Sql, page, _myConn);
        }
    }

    // 将相同父亲的G_PNODES放到同一个流程图节点中
    @SuppressWarnings("unchecked")
    private void initList1(String fidStr, Map<String, Node> nodes, String _myDs2Sql, Page page, Connection _myConn) throws SQLException, ParseException {
        // System.out.println(_myDs2Sql);
        Map<String, Node> t = new HashMap<String, Node>();
        int maxId = 0;
        int maxFid = 0;
        for (String key : nodes.keySet()) {
            Node item = nodes.get(key);
            int fid = item.getFid();
            int id = item.getId();
            String key1 = fid + "_" + id;
            maxId = id > maxId ? id : maxId;
            maxFid = fid > maxFid ? fid : maxFid;
            t.put(key1, item);
        }
        for (int i = 0; i <= maxFid; i++) {
            for (int j = i + 1; j <= maxId; j++) {
                Node item = t.get(i + "_" + j);
                if (item != null) {
                    Node parent = nodes.get(String.valueOf(i));
                    if (parent != null) {
                        String a = parent.getUName() + "->" + item.getUName();
                        // System.out.println(a);
                        page.items.add(a);
                        page._Count++;
                    }
                }
            }
        }
    }

    private String loadTree(Map<String, Node> nodes, String getGPNodesSql, Connection conn) {
        // System.out.println(getGPNodesSql);
        ResultSet _myDs = null;
        int root = 1;
        try {
            _myDs = conn.createStatement().executeQuery(getGPNodesSql);
            while (_myDs.next()) {
            	//过滤掉G_PNODES数据的Atype=0(非正常发送)并且wf_linetype=2(汇总线的数据为汇总节点的不选人发送)的数据,汇总节点不显示多人 杨龙修改 2013/4/23
            	if(_myDs.getInt("WFLINE_TYPE") == 2&&(_myDs.getString("ATYPE")==null||"".equals(_myDs.getString("ATYPE"))))
            	{
            		continue;
            	}
                if (_myDs.getInt("FID") == 0) {
                    String actName = _myDs.getString("ACTNAME");
                    // -----------------------------------------------------------
                    actName = xsf.Value.getString(actName, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                    // -----------------------------------------------------------
                    if (actName != null && !"".equals(actName)) {
                        root = 0;
                    }
                }
                Node node = new Node();
                node.setId(_myDs.getInt("ID"));
                node.setFid(_myDs.getInt("FID"));
                node.setStatus(_myDs.getInt("STATUS"));
                node.setWaitCount(_myDs.getInt("WAITCOUNT"));
                String UNAME = _myDs.getString("UNAME");
                // -----------------------------------------------------------
                UNAME = xsf.Value.getString(UNAME, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
                node.setUName(UNAME);
                String ACTNAME = _myDs.getString("ACTNAME");
                // -----------------------------------------------------------
                ACTNAME = xsf.Value.getString(ACTNAME, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                // -----------------------------------------------------------
                node.setActName(ACTNAME);
                node.setRDate(_myDs.getString("RDATE"));
                node.setPDate(_myDs.getString("PDATE"));
                String USER_ID = _myDs.getString("USER_ID");
                String MUSER_ID = _myDs.getString("MUSER_ID");
                node.setProxy(MUSER_ID != null && !"".equals(MUSER_ID) && !"0".equals(MUSER_ID) && !USER_ID.equals(MUSER_ID));
                nodes.put(String.valueOf(node.getId()), node);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                _myDs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return String.valueOf(root);
    }

    class Page {
        private long _Id = 0;
        private String _Red = "#f09992";
        private String _Yellow = "#FFFF99";
        private int _Column = 3;
        private int _Count = 0;
        private int[] _Status;
        private int[] _Nodes;
        private String[] _Content;
        private Map<String, String> nodesInfo = new HashMap<String, String>();
        private List<String> items = new ArrayList<String>();
        private String CONTEXT_PATH = "";

        private String getHtml() {
            String content = "<table border='0' cellspacing='0' cellpadding='0' style='margin:auto;'>";
            String backGround = "";
            String arStr = "";
            String arAlt = "";
            String arStr1 = "";
            int curRow;
            int Rows = this._Count / this._Column + (this._Count % this._Column == 0 ? 0 : 1);
            for (int i = 0; i < Rows; i++) {
                curRow = i + 1;
                if (curRow % 2 == 0) {
                    // 偶数行
                    for (int j = (curRow * this._Column - 1); j >= ((curRow - 1) * this._Column); j--) {
                        if (j == (curRow * this._Column - 1)) {
                            switch (this._Status[(curRow - 1) * this._Column]) {
                            case 1:
                                arStr = this.CONTEXT_PATH + "/images/ar.gif";
                                arAlt = "正在办理";
                                break;
                            case -1:
                                arStr = this.CONTEXT_PATH + "/images/ag.gif";
                                arAlt = "办理完毕";
                                break;
                            case 0:
                            default:
                                arStr = this.CONTEXT_PATH + "/images/ab.gif";
                                arAlt = "尚未到达";
                                break;
                            }
                            content += "</tr><tr height=40><td colspan=" + (this._Column * 2) + " height=32>&nbsp;</td>";
                            content += "<td vAlign=center align=middle width=104 height=32><img src='" + arStr + "' height=23 width=12 border=0 alt='" + arAlt + "'></td>";
                            content += "</tr><tr><td colspan=2>&nbsp;</td>";
                        }
                        if (j > (this._Count - 1)) {
                            content += "<td colspan=2>&nbsp;</td>";
                        } else {
                            if (this._Status[j] == 1) {
                                backGround = this._Red;
                            } else if (this._Status[j] == 0) {
                                backGround = this._Yellow;
                            } else {
                                backGround = "White";
                            }
                            if (this._Nodes[j] > 3) {
                                if (backGround.equals("White")) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px #83bfe6'><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                } else if (backGround.equals(this._Red)) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 2px red;'><tbody><tr><td align=middle valign=middle style='word-break:break-all; width:120px;padding:2px'><div style='background-color:" + backGround + "'>" + this._Content[j] + "</div></td></tr><tbody></table></td>";
                                } else {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px " + backGround + "'><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                }
                            } else {
                                if (backGround.equals("White")) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px #83bfe6'><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                } else if (backGround.equals(this._Red)) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 2px red;'><tbody><tr><td align=middle valign=middle style='word-break:break-all; width:120px;padding:2px'><div style='background-color:" + backGround + "'>" + this._Content[j] + "</div></td></tr><tbody></table></td>";
                                } else {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px " + backGround + "'><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                }
                            }
                            if (j != ((curRow - 1) * this._Column)) {
                                switch (this._Status[j]) {
                                case 1:
                                    arStr = this.CONTEXT_PATH + "/images/arl.gif";
                                    arAlt = "正在办理";
                                    break;
                                case -1:
                                    arAlt = "办理完毕";
                                    arStr = this.CONTEXT_PATH + "/images/agl.gif";
                                    break;
                                case 0:
                                default:
                                    arAlt = "尚未到达";
                                    arStr = this.CONTEXT_PATH + "/images/abl.gif";
                                    break;
                                }
                                content += "<td vAlign=center align=middle width=25 height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'></td>";
                            }
                        }
                    }
                } else {
                    for (int j = ((curRow - 1) * this._Column); j <= (curRow * this._Column - 1); j++) {
                        if (j > this._Count - 1) {
                            content += "<td colspan=2>&nbsp;</td>";
                        } else {
                            if (this._Status[j] == 1) {
                                backGround = this._Red;
                            } else if (this._Status[j] == 0) {
                                backGround = this._Yellow;
                            } else {
                                backGround = "White";
                            }
                            switch (this._Status[j]) {
                            case -1:
                                arAlt = "办理完毕";
                                arStr = this.CONTEXT_PATH + "/images/agr.gif";
                                arStr1 = this.CONTEXT_PATH + "/images/ag.gif";
                                break;
                            case 1:
                                arAlt = "正在办理";
                                arStr = this.CONTEXT_PATH + "/images/arr.gif";
                                arStr1 = this.CONTEXT_PATH + "/images/ar.gif";
                                break;
                            case 0:
                            default:
                                arAlt = "尚未到达";
                                arStr = this.CONTEXT_PATH + "/images/abr.gif";
                                arStr1 = this.CONTEXT_PATH + "/images/ab.gif";
                                break;
                            }
                            if (j == 0) {
                                // 笑脸
                                content += "<tr><td align=middle valign=middle><img src='" + this.CONTEXT_PATH + "/images/face.gif'></td>";
                                // 箭头
                                content += "<td vAlign=center align=middle width=25 height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'></td>";
                            } else {
                                if (j == (curRow - 1) * this._Column) {
                                    content += "</tr><tr height=40><td colspan=2>&nbsp;</td>";
                                    content += "<td vAlign=center align=middle width=104 height=32><img src='" + arStr1 + "' height=23 width=12 border=0 alt='" + arAlt + "'></td>";
                                    content += "<td colspan=" + (this._Column * 2 - 3) + " height=32>&nbsp;</td>";
                                    content += "</tr><tr><td colspan=2>&nbsp;</td>";
                                } else {
                                    content += "<td vAlign=center align=middle width=25 height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'></td>";
                                }
                            }
                            if (this._Nodes[j] > 3) {
                                if (backGround.equals("White")) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px #83bfe6' ><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                } else if (backGround.equals(this._Red)) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 2px red ;' ><tbody><tr><td align=middle valign=middle style='word-break:break-all; width:120px;padding:2px;'><div style='background-color:" + backGround + "'>" + this._Content[j] + "</div></td></tr><tbody></table></td>";
                                } else {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px " + backGround + " ' ><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                }
                            } else {
                                if (backGround.equals("White")) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px #83bfe6' ><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                } else if (backGround.equals(this._Red)) {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 2px red ;' ><tbody><tr><td align=middle valign=middle style='word-break:break-all; width:120px;padding:2px;'><div style='background-color:" + backGround + "'>" + this._Content[j] + "</div></td></tr><tbody></table></td>";
                                } else {
                                    content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=0 style='border:solid 1px " + backGround + " ' ><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle style='word-break:break-all; width:120px'>" + this._Content[j] + "</td></tr><tbody></table></td>";
                                }
                            }
                        }
                    }
                }
            }
            return content + "</tr></table>";
        }
    }

    class Node {
        private int id;
        private int fid;
        private int status;
        private int waitCount;
        private String uName;
        private String actName;
        private String rDate;
        private String pDate;
        private boolean isProxy;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getFid() {
            return fid;
        }

        public void setFid(int fid) {
            this.fid = fid;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getWaitCount() {
            return waitCount;
        }

        public void setWaitCount(int waitCount) {
            this.waitCount = waitCount;
        }

        public String getUName() {
            return uName;
        }

        public void setUName(String name) {
            uName = name == null ? "" : name;
        }

        public String getActName() {
            return actName;
        }

        public void setActName(String actName) {
            this.actName = actName;
        }

        public String getRDate() {
            return rDate;
        }

        public void setRDate(String date) {
            rDate = date;
        }

        public String getPDate() {
            return pDate;
        }

        public void setPDate(String date) {
            pDate = date;
        }

        public boolean isProxy() {
            return isProxy;
        }

        public void setProxy(boolean isProxy) {
            this.isProxy = isProxy;
        }
    }
}
