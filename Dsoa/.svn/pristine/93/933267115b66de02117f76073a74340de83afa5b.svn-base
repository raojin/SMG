package dsoap.web.action;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import xsf.Config;
import xsf.data.DBManager;
import dsoap.dsflow.TimeSpan;
import dsoap.dsflow.model.DataRow;
import dsoap.dsflow.model.DataTable;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class ShowFlow2Action extends Action {
    private static final long serialVersionUID = 8252751140659731426L;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
    private int N = 1;// 一个节点超过多少人就显示 ”共（x）人“
    public String errStr = "";

    @Override
    public String execute() throws Exception {
        super.execute();
        Page page = new Page();
        String cmdStr = "";
        String wf_id = "0";
        Connection _myConn = null;
        Statement _myRead = null;
        String _cmdStr = "";
        boolean IsPostBack = false;
        ResultSet _myDs = null;
        DataTable TblPercent = new DataTable("TblPercent");
        DataTable TblOver = new DataTable("TblOver");
        DataTable TblErr = new DataTable("TblErr");
        DataTable TblPercent1 = new DataTable("TblPercent1");
        boolean LblOver = false;
        boolean LblErr = false;
        String text = "";
        try {
            _myConn = DBManager.getConnection(Config.CONNECTION_KEY);
            page._Id = Long.parseLong(request.getParameter("id"));
            if (!IsPostBack) {
                page._Content = new String[100];
                page._Status = new int[100];
                page._Nodes = new int[100];
                page._UNodes = new int[100];
                page._Yj = new int[100];
                page._UList = new String[100];
                page._Fid = new String[100];
                page._NodeName = new String[100];
                try {
                    String DBMS = ConfigurationSettings.AppSettings("DBMS");
                    if ("SYBASE".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,CONVERT(CHAR(10),BLQX,102) || ' ' || CONVERT(CHAR(5),BLQX,108) EDATE,CONVERT(CHAR(10),BJRQ,102) || ' ' || CONVERT(CHAR(5),BJRQ,108) BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else if ("ORACLE".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,TO_CHAR(BLQX,'YYYY.MM.DD HH24:MI') EDATE,TO_CHAR(BJRQ,'YYYY.MM.DD HH24:MI') BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else if ("SQLSERVER".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,CONVERT(CHAR(10),BLQX,102) + ' ' + CONVERT(CHAR(5),BLQX,108) EDATE,CONVERT(CHAR(10),BJRQ,102) + ' ' + CONVERT(CHAR(5),BJRQ,108) BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else if ("MYSQL".equals(DBMS)) {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,STATUS,DATE_FORMAT(BLQX,'YYYY.MM.DD HH24:MI') EDATE,DATE_FORMAT(BJRQ,'YYYY.MM.DD HH24:MI') BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    } else {
                        cmdStr = "SELECT WF_ID,BT,JC_ID,CONVERT(CHAR(10),BLQX,102) || ' ' || CONVERT(CHAR(5),BLQX,108) EDATE,CONVERT(CHAR(10),BJRQ,102) || ' ' || CONVERT(CHAR(5),BJRQ,108) BJRQ FROM G_INFOS WHERE ID = " + page._Id;
                    }
                    String sDate = "";
                    String eDate = "";
                    boolean bIsEnd = false;
                    _myDs = _myRead.executeQuery(cmdStr);
                    if (_myDs.next()) {
                        page._Bt = _myDs.getString("BT");
                        page.strTitle = page._Bt + " - 察看流程";
                        page._Pid = _myDs.getLong("JC_ID");
                        if (_myDs.getObject("EDATE") != null) {
                            eDate = _myDs.getString("EDATE");
                        }
                        if (_myDs.getInt("STATUS") > 1) {
                            bIsEnd = true;
                            if (_myDs.getObject("BJRQ") != null) {
                                eDate = _myDs.getString("BJRQ");
                            }
                        }
                        wf_id = _myDs.getString("WF_ID");
                    }
                    _myDs.close();
                    if (!"0".equals(request.getParameter("wf_id"))) {
                        wf_id = request.getParameter("wf_id");
                    }
                    // 流程图
                    if ("SYBASE".equals(DBMS)) {
                        cmdStr = "select CONVERT(CHAR(10),RDATE,102) || ' ' || CONVERT(CHAR(5),RDATE,108) SDATE ";
                    } else if ("ORACLE".equals(DBMS)) {
                        cmdStr = "select TO_CHAR(RDATE,'YYYY.MM.DD HH24:MI') SDATE ";
                    } else if ("SQLSERVER".equals(DBMS)) {
                        cmdStr = "select CONVERT(CHAR(10),RDATE,102) + ' ' + CONVERT(CHAR(5),RDATE,108) SDATE ";
                    } else if ("MYSQL".equals(DBMS)) {
                        cmdStr = "select DATE_FORMAT(RDATE,'YYYY.MM.DD HH24:MI') SDATE ";
                    } else {
                        cmdStr = "select CONVERT(CHAR(10),RDATE,102) || ' ' || CONVERT(CHAR(5),RDATE,108) SDATE ";
                    }
                    cmdStr += "from G_PNODES where WF_ID=" + wf_id + " and INFO_ID=" + page._Id + " and ID=1";
                    _myDs = _myRead.executeQuery(cmdStr);
                    if (_myDs.next()) {
                        if (_myDs.getObject("SDATE") != null) {
                            sDate = _myDs.getString("SDATE");
                        }
                    }
                    _myDs.close();
                    DataRow mRow;
                    TblPercent1.setVisible(false);
                    if (!"".equals(eDate) && !"".equals(sDate)) {
                        TblPercent1.setVisible(true);
                        // int sYear = 0;
                        // int sMonth = 0;
                        // int sDay = 0;
                        int sHour = 0;
                        int sMin = 0;
                        // int eYear = 0;
                        // int eMonth = 0;
                        // int eDay = 0;
                        // int eHour = 0;
                        // int eMin = 0;
                        String[] arrSDate = sDate.substring(0, 10).split(".");
                        if (arrSDate.length == 3) {
                            // sYear = Integer.parseInt(arrSDate[0]);
                            // sMonth = Integer.parseInt(arrSDate[1]);
                            // sDay = Integer.parseInt(arrSDate[2]);
                        }
                        // System.out.println(sDate.length());
                        String[] arrSTime = sDate.substring(11, sDate.length()).split(":");
                        Calendar cl = Calendar.getInstance();
                        cl.setTime(sdf.parse(sDate.replace(".", "-")));
                        if (arrSTime.length == 2) {
                            sHour = Integer.parseInt(arrSTime[0]);
                            sMin = Integer.parseInt(arrSTime[1]);
                            cl.set(Calendar.HOUR_OF_DAY, sHour);
                            cl.set(Calendar.MINUTE, sMin);
                        }
                        cl.set(Calendar.MILLISECOND, 0);
                        Date dtSDate = cl.getTime();
                        String[] arrEDate = eDate.substring(0, 10).split(".");
                        if (arrEDate.length == 3) {
                            // eYear = Integer.parseInt(arrEDate[0]);
                            // eMonth = Integer.parseInt(arrEDate[1]);
                            // eDay = Integer.parseInt(arrEDate[2]);
                        }
                        String[] arrETime = eDate.substring(11, eDate.length()).split(":");
                        cl.setTime(sdf.parse(eDate.replace(".", "-")));
                        if (arrETime.length == 2) {
                            // eHour = Integer.parseInt(arrETime[0]);
                            // eMin = Integer.parseInt(arrETime[1]);
                            cl.set(Calendar.HOUR_OF_DAY, sHour);
                            cl.set(Calendar.MINUTE, sMin);
                        }
                        Date dtEDate = cl.getTime();
                        Date dtNDate = new Date();
                        double dAll;
                        double dStart;
                        double dEnd;
                        boolean bIsOver = false;
                        if (dtNDate.getTime() <= dtEDate.getTime()) {
                            dAll = TimeSpan.toCm(dtEDate, dtSDate).getTotalMinutes();
                            dStart = TimeSpan.toCm(dtNDate, dtSDate).getTotalMinutes();
                            dEnd = TimeSpan.toCm(dtEDate, dtNDate).getTotalMinutes();
                        } else {
                            dAll = TimeSpan.toCm(dtNDate, dtSDate).getTotalMinutes();
                            dStart = TimeSpan.toCm(dtEDate, dtSDate).getTotalMinutes();
                            dEnd = TimeSpan.toCm(dtNDate, dtEDate).getTotalMinutes();
                            bIsOver = true;
                        }
                        if (bIsEnd) {
                            dAll = TimeSpan.toCm(dtEDate, dtSDate).getTotalMinutes();
                            dStart = TimeSpan.toCm(dtEDate, dtSDate).getTotalMinutes();
                            dEnd = 0;
                        }
                        if (dAll == 0) {
                            dAll = 1;
                        }
                        int iPer = (int) (dStart / dAll * 100);
                        mRow = new DataRow();
                        mRow.setHeight("20");
                        /*
                         * mCell.Attributes["width"] = (iPer) + "%"; mCell.Attributes["background"] = "../images/flow2.gif"; mCell.BorderStyle = BorderStyle.Solid; mCell.BorderWidth = Unit.Pixel(1); mCell.BorderColor = Color.Black; mRow.Cells.Add(mCell);
                         * 
                         * if (!bIsEnd) { mCell = new TableCell(); mCell.Attributes["width"] = ((100-iPer)) + "%"; if (bIsOver) mCell.Attributes["background"] = "../images/flow3.gif"; else mCell.Attributes["background"] = "../images/flow1.gif"; mRow.Cells.Add(mCell); } mCell.BorderStyle = BorderStyle.Solid; mCell.BorderWidth = Unit.Pixel(1); mCell.BorderColor = Color.Black;
                         */
                        String strPercent;
                        strPercent = "<span style='width:605;height:18;border:1px solid black;padding:1;'><span style='width:" + (iPer * 6) + "; background:url(/images/flow2.gif);'></span>";
                        if (!bIsEnd) {
                            if (bIsOver) {
                                strPercent += "<span style='width:" + ((100 - iPer) * 6) + "; background:url(/images/flow3.gif);'></span></span>";
                            } else {
                                strPercent += "<span style='width:" + ((100 - iPer) * 6) + "; background:url(/images/flow1.gif);'></span></span>";
                            }
                        }
                        // mCell.Text = strPercent;
                        // mRow.Cells.Add(mCell);
                        mRow.put("mCell", strPercent);
                        TblPercent.rows.add(mRow);
                        mRow = new DataRow();
                        // mCell = new TableCell();
                        // mCell.Attributes["colspan"] = "2";
                        mRow.setColspan("2");
                        text = "<table width=100%><tr><td width=33%>开始流转时间：" + sDate + "</td>";
                        cl.setTime(dtNDate);
                        if (!bIsEnd) {
                            text += "<td width=33% align=left>当前时间：" + cl.get(Calendar.YEAR) + "." + cl.get(Calendar.MONTH) + "." + cl.get(Calendar.DAY_OF_MONTH) + " " + cl.get(Calendar.HOUR_OF_DAY) + ":" + cl.get(Calendar.MINUTE) + "</td>";
                            text += "<td align=left>预计结束时间：" + eDate + "</td></tr></table>";
                        } else {
                            text += "<td align=left>结束时间：" + eDate + "</td></tr></table>";
                        }
                        mRow.put("mCell", text);
                        TblPercent.rows.add(mRow);
                        mRow = new DataRow();
                        // mCell = new TableCell();
                        mRow.setColspan("2");
                        // mCell.Attributes["colspan"] = "2";
                        int iHour = (int) (dStart / 60);
                        int iMin = (int) (dStart % 60);
                        int iDay = (int) (iHour / 24);
                        iHour = (int) (iHour % 24);
                        text = "<table width=100%><tr><td width=33%>已运行时间：" + iDay + "天" + iHour + "小时" + iMin + "分钟</td>";
                        if (!bIsEnd) {
                            iHour = (int) (dEnd / 60);
                            iMin = (int) (dEnd % 60);
                            iDay = (int) (iHour / 24);
                            iHour = (int) (iHour % 24);
                            if (bIsOver) {
                                text += "<td align=left><font color=red>超期时间：" + iDay + "天" + iHour + "小时" + iMin + "分钟</font></td></tr></table>";
                            } else {
                                text += "<td align=left><font color=green>剩余时间：" + iDay + "天" + iHour + "小时" + iMin + "分钟</font></td></tr></table>";
                            }
                        }
                        mRow.put("mCell", text);
                        TblPercent.rows.add(mRow);
                    }
                    // -----------------------------------------------------------
                    // 超期节点列表
                    if ("SYBASE".equals(DBMS)) {
                        cmdStr = "SELECT A.STATUS,CONVERT(CHAR(10),A.RDATE,102) || ' ' || CONVERT(CHAR(5),A.RDATE,108) RDATE,CONVERT(CHAR(10),A.PDATE,102) || ' ' || CONVERT(CHAR(5),A.PDATE,108) PDATE,CONVERT(CHAR(10),A.EDATE,102) || ' ' || CONVERT(CHAR(5),A.EDATE,108) EDATE,A.ACTNAME,A.UNAME FROM G_PNODES A WHERE WF_ID=" + wf_id + "  AND A.INFO_ID = " + page._Id + " AND ((A.STATUS=-1 AND A.PDATE>A.EDATE) OR (A.STATUS<>-1 AND GETDATE()>A.EDATE)) AND A.WFNODE_WAIT=0 ORDER BY A.ID";
                    } else if ("ORACLE".equals(DBMS)) {
                        cmdStr = "SELECT A.STATUS,TO_CHAR(A.RDATE,'YYYY.MM.DD HH24:MI') RDATE,TO_CHAR(A.PDATE,'YYYY.MM.DD HH24:MI') PDATE,TO_CHAR(A.EDATE,'YYYY.MM.DD HH24:MI') EDATE,A.ACTNAME,A.UNAME FROM G_PNODES A WHERE WF_ID=" + wf_id + "  AND A.INFO_ID = " + page._Id + " AND ((A.STATUS=-1 AND A.PDATE>A.EDATE) OR (A.STATUS<>-1 AND SYSDATE>A.EDATE)) AND A.WFNODE_WAIT=0 ORDER BY A.ID";
                    } else if ("SQLSERVER".equals(DBMS)) {
                        cmdStr = "SELECT A.STATUS,CONVERT(CHAR(10),A.RDATE,102) + ' ' + CONVERT(CHAR(5),A.RDATE,108) RDATE,CONVERT(CHAR(10),A.PDATE,102) + ' ' + CONVERT(CHAR(5),A.PDATE,108) PDATE,CONVERT(CHAR(10),A.EDATE,102) + ' ' + CONVERT(CHAR(5),A.EDATE,108) EDATE,A.ACTNAME,A.UNAME FROM G_PNODES A WHERE WF_ID=" + wf_id + "  AND A.INFO_ID = " + page._Id + " AND ((A.STATUS=-1 AND A.PDATE>A.EDATE) OR (A.STATUS<>-1 AND GETDATE()>A.EDATE)) AND A.WFNODE_WAIT=0 ORDER BY A.ID";
                    } else if ("MYSQL".equals(DBMS)) {
                        cmdStr = "SELECT A.STATUS,DATE_FORMAT(RDATE,'YYYY.MM.DD HH24:MI')RDATE,DATE_FORMAT(PDATE,'YYYY.MM.DD HH24:MI')PDATE,DATE_FORMAT(EDATE,'YYYY.MM.DD HH24:MI')EDATE,A.ACTNAME,A.UNAME FROM G_PNODES A WHERE WF_ID=" + wf_id + "  AND A.INFO_ID = " + page._Id + " AND ((A.STATUS=-1 AND A.PDATE>A.EDATE) OR (A.STATUS<>-1 AND NOW()>A.EDATE)) AND A.WFNODE_WAIT=0 ORDER BY A.ID";
                    } else {
                        cmdStr = "SELECT A.STATUS,CONVERT(CHAR(10),A.RDATE,102) || ' ' || CONVERT(CHAR(5),A.RDATE,108) RDATE,CONVERT(CHAR(10),A.PDATE,102) || ' ' || CONVERT(CHAR(5),A.PDATE,108) PDATE,CONVERT(CHAR(10),A.EDATE,102) || ' ' || CONVERT(CHAR(5),A.EDATE,108) EDATE,A.ACTNAME,A.UNAME FROM G_PNODES A WHERE WF_ID=" + wf_id + "  AND A.INFO_ID = " + page._Id + " AND ((A.STATUS=-1 AND A.PDATE>A.EDATE) OR (A.STATUS<>-1 AND SYSDATE>A.EDATE)) AND A.WFNODE_WAIT=0 ORDER BY A.ID";
                    }
                    _myDs = _myRead.executeQuery(cmdStr);
                    TblOver.setVisible(false);
                    LblOver = false;
                    while (_myDs.next()) {
                        TblOver.setVisible(true);
                        LblOver = true;
                        mRow = new DataRow();
                        mRow.setHeight("20");
                        mRow.put("UNAME", _myDs.getString("UNAME"));
                        mRow.put("ACTNAME", _myDs.getString("ACTNAME"));
                        String status = _myDs.getString("STATUS");
                        if (status != null) {
                            if ("1".equals(status)) {
                                text = "<font color=red>未拆封</font>";
                            } else if ("2".equals(status)) {
                                text = "<font color=red>已拆封</font>";
                            } else {
                                text = "<font color=green>已完成</font>";
                            }
                        }
                        mRow.put("STATUS", text);
                        mRow.put("RDATE", _myDs.getString("RDATE"));
                        mRow.put("EDATE", _myDs.getString("EDATE"));
                        mRow.put("PDATE", _myDs.getString("PDATE"));
                        TblOver.rows.add(mRow);
                    }
                    _myDs.close();
                    // -----------------------------------------------------------
                    // 异常处理列表
                    cmdStr = "SELECT A.ACTNAME,A.ACT,B.BT,C.UNAME,D.UNAME ACTUNAME,E.NAME,A.BZ,";
                    if ("SYBASE".equals(DBMS)) {
                        cmdStr += "convert(char(16),A.ACTTIME,20) ACTTIME ";
                    } else if ("ORACLE".equals(DBMS)) {
                        cmdStr += "TO_CHAR(A.ACTTIME,'YYYY.MM.DD HH24:MI') ACTTIME ";
                    } else if ("SQLSERVER".equals(DBMS)) {
                        cmdStr += "convert(char(16),A.ACTTIME,20) ACTTIME ";
                    } else if ("MYSQL".equals(DBMS)) {
                        cmdStr += "DATE_FORMAT(A.ACTTIME,'YYYY.MM.DD HH24:MI') ACTTIME ";
                    } else {
                        cmdStr += "TO_CHAR(A.ACTTIME,'YYYY.MM.DD HH24:MI') ACTTIME ";
                    }
                    cmdStr += "FROM G_ACT_LOG A,G_INFOS B,G_USERS C,G_USERS D,G_MODULE E ";
                    cmdStr += "WHERE A.INFO_ID=B.ID AND A.USER_ID=C.ID AND A.ACT_USER_ID=D.ID AND B.MODULE_ID=E.ID ";
                    cmdStr += "AND A.ACT IN ('回收','退回','文件否决','文件撤销') AND B.ID=" + page._Id;
                    cmdStr += " ORDER BY A.ACTTIME DESC";
                    // System.out.println(cmdStr);
                    _myDs = _myRead.executeQuery(cmdStr);
                    TblErr.setVisible(false);
                    LblErr = false;
                    while (_myDs.next()) {
                        TblErr.setVisible(true);
                        LblErr = true;
                        mRow = new DataRow();
                        mRow.setHeight("20");
                        mRow.put("ACT", _myDs.getString("ACT"));
                        mRow.put("UNAME", _myDs.getString("UNAME"));
                        mRow.put("ACTUNAME", _myDs.getString("ACTUNAME"));
                        mRow.put("ACTNAME", _myDs.getString("ACTNAME"));
                        mRow.put("ACTTIME", _myDs.getString("ACTTIME"));
                        mRow.put("BZ", _myDs.getString("BZ"));
                        TblErr.rows.add(mRow);
                    }
                    _myDs.close();
                    // -----------------------------------------------------------
                    page._Count = 0;
                    cmdStr = "SELECT A.PDATE,B.UNAME,E.UNAME DNAME,A.STATUS";
                    cmdStr += " FROM G_PNODES A,G_USERS B,G_USERS E";
                    cmdStr += " WHERE WF_ID=" + wf_id + " and A.PID = " + page._Pid + " AND A.ID = 1";
                    cmdStr += " AND A.USER_ID = B.ID AND A.DEPT_ID = E.ID";
                    // myCmd.CommandText = cmdStr;
                    String UName;
                    String DName;
                    int Status;
                    String Content = "";
                    Date PDate;
                    // myRead = myCmd.ExecuteReader();
                    _myDs = _myRead.executeQuery(cmdStr);
                    if (_myDs.next()) {
                        UName = _myDs.getString("UNAME");
                        DName = _myDs.getString("DNAME");
                        Status = _myDs.getInt("STATUS");
                        page._Count++;
                        page._Status[page._Count - 1] = Status;
                        Content = DName + "<Br>" + UName;
                        if (_myDs.getObject("PDATE") != null) {
                            PDate = sdf.parse(_myDs.getString("PDATE"));
                            Calendar cl = Calendar.getInstance();
                            cl.setTime(PDate);
                            Content += "<Br>" + cl.get(Calendar.MONTH) + "." + cl.get(Calendar.DAY_OF_MONTH) + " " + cl.get(Calendar.HOUR_OF_DAY) + ":" + cl.get(Calendar.MINUTE);
                        }
                        String Space = "&nbsp;&nbsp;&nbsp;&nbsp;";
                        Content = "<table width=100% border=0 align=center><tr><td>" + Space + "</td><td><table width=100% border=0 align=center><tr><td align=center nowrap>" + Content + "</td></tr></table></td><td>" + Space + "</td></tr></table>";
                        page._Content[page._Count - 1] = Content;
                        page._Nodes[page._Count - 1] = 1;
                        page._UNodes[page._Count - 1] = 0;
                        page._UList[page._Count - 1] = "";
                        page._Yj[page._Count - 1] = 0;
                    }
                    _myDs.close();
                    // cmdStr = "SELECT A.ID ID,B.FID,D.UNAME,G.UNAME
                    // DNAME,C.NAME
                    // ACTNAME,A.STATUS,H.CONTENT,A.RDATE,A.PDATE";
                    // cmdStr += " FROM G_PNODES A,G_PROUTE B,G_ACTS C,G_USERS
                    // D,G_DEPT E,G_DEPT F,G_USERS G,LONGTEXT H";
                    // cmdStr += " WHERE A.ID = B.ID AND A.PID = B.PID";
                    // cmdStr += " AND A.PID = " + _Pid;
                    // cmdStr += " AND A.ACT_ID = C.ID AND A.USER_ID = D.ID";
                    // cmdStr += " AND D.ID = E.USER_ID AND E.FID = F.ID";
                    // cmdStr += " AND F.USER_ID = G.ID";
                    // cmdStr += " AND A.TYJ = H.ID(+)";
                    //-------------------------------------------------------------------------------------------------------------
                    //cmdStr = "SELECT A.ID,A.FID,A.UNAME,'' DNAME,C.WFNODE_MEMO ACTNAME,A.STATUS,A.RDATE,A.PDATE,A.WAITCOUNT";
                    cmdStr = "SELECT A.ID,A.FID,A.UNAME,'' DNAME,A.ACTNAME,A.STATUS,A.RDATE,A.PDATE,A.WAITCOUNT";
                    cmdStr += " FROM G_PNODES A,WFNODELIST C";
                    cmdStr += " WHERE A.WF_ID=" + wf_id;
                    cmdStr += " AND A.info_ID=" + page._Id;
                    cmdStr += " AND A.WF_ID=C.WF_ID AND A.WFNODE_ID=C.WFNODE_ID ";
                    //-------------------------------------------------------------------------------------------------------------
                    // cmdStr = "SELECT A.ID,B.FID,A.UNAME,G.UNAME
                    // DNAME,C.WFNODE_MEMO
                    // ACTNAME,A.STATUS,A.RDATE,A.PDATE,A.WAITCOUNT";
                    // cmdStr += " FROM G_PNODES A,G_PROUTE B,WFNODELIST
                    // C,G_USERS
                    // G";
                    // cmdStr += " WHERE A.WF_ID="+wf_id+" and A.ID = B.ID AND
                    // A.PID
                    // = B.PID ";
                    // cmdStr += " AND A.info_ID = " + _Id ;
                    // cmdStr += " AND A.WF_ID = C.WF_ID AND A.WFNODE_ID =
                    // C.WFNODE_ID ";
                    // cmdStr += " AND A.DEPT_ID = G.ID";
                    // myDa = new OleDbDataAdapter(cmdStr, _myConn);
                    // myDa.Fill(_myDs, "LIST");
                    _cmdStr = "SELECT * FROM G_OPINION WHERE PID in (select pid from g_pnodes where info_id=" + page._Id + " and wf_id=" + wf_id + ") ";
                    // myDa.SelectCommand.CommandText = cmdStr;
                    // myDa.Fill(_myDs, "OPINION");
                    initList("1", cmdStr, _cmdStr, page, _myConn);
                    CreateHtml(page, request);
                } catch (Exception err) {
                    err.printStackTrace();
                    String htmStr;
                    htmStr = "<script language='javascript'>";
                    htmStr += "alert('生成流程图时产生错误，错误原因：" + err.getMessage() + "，当前SQL语句：" + cmdStr + "');";
                    htmStr += "top.close();";
                    htmStr += "</script>";
                    errStr = htmStr;
                }
            }
            request.setAttribute("TblPercent", TblPercent);
            request.setAttribute("TblOver", TblOver);
            request.setAttribute("TblErr", TblErr);
            request.setAttribute("TblPercent1", TblPercent1);
            request.setAttribute("LblOver", LblOver);
            request.setAttribute("LblErr", LblErr);
            return SUCCESS;
        } catch (Exception w) {
            w.printStackTrace();
            errStr = w.getMessage();
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

    private void initList(String fidStr, String _myDs1Sql, String _myDs2Sql, Page page, Connection _myConn) throws SQLException, ParseException {
        System.out.println(_myDs1Sql);
        int Id, Fid, Status = 0;
        String UName;
        // String DName;
        String actName;
        String Yj;
        String pDate, rDate;
        int UNode = 0;
        String UList = "";
        boolean findRow;
        // char[] mySplit = new char[] { ',' };
        String[] strSplit = fidStr.split(",");
        String idStr = "";
        Date PDate;
        String pDateStr = "";
        int yjNum = 0;
        int waitStatus = 0;
        int receiveStatus = 0;
        int endStatus = 0;
        String Content = "";
        String rStart = "";
        String rEnd = "";
        int nodeCount = 0;
        String nodeYj = "";
        String nodeName = "";
        int waitCount = 0;
        ResultSet _myDs1 = _myConn.createStatement().executeQuery(_myDs1Sql);
        // #region 0:处理节点
        while (_myDs1.next()) {
            // #region 0.1:判断当前记录是不是当前节点的子节点，不是的话则继续下一条记录的处理
            Fid = _myDs1.getInt("FID");// Int32.Parse(myRow["FID"].ToString());
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
            // #endregion
            nodeCount++;
            yjNum = 0;
            Id = _myDs1.getInt("ID");
            Status = _myDs1.getInt("STATUS");
            waitCount = _myDs1.getInt("WAITCOUNT");
            if (waitCount > 0) {
                Status = 0;
            }
            UName = _myDs1.getString("UNAME") == null ? "" : _myDs1.getString("UNAME");
            // DName = _myDs1.getString("DNAME");
            actName = _myDs1.getString("ACTNAME");
            if (_myDs1.getObject("RDATE") == null) {
                rDate = null;
            } else {
                rDate = _myDs1.getString("RDATE");
            }
            if (_myDs1.getObject("PDATE") == null) {
                pDate = null;
            } else {
                pDate = _myDs1.getString("PDATE");
            }
            if (idStr.length() > 0) {
                idStr += "," + Id;
            } else {
                idStr = String.valueOf(Id);
            }
          //修正吴红亮的根据节点名称是否包含“抄送”判断是否抄送节点
//            if (actName.indexOf("抄送") >= 0) {
//                UNode++;
//                if (UList.length() > 0) {
//                    UList += UName;
//                } else {
//                    UList += "<Br>" + UName;
//                }
//                continue;
//            }
            if (Status == 0 && waitStatus == 0) {
                waitStatus = 1;
            }
            if (Status == -1 && endStatus == 0) {
                endStatus = 1;
            }
            if ((Status == 1 || Status == 2) && receiveStatus == 0) {
                receiveStatus = 1;
            }
            Yj = "";
            // --liuzq 原始逻辑过于影响性能 进行了简单修改
            // --吴红亮 修改 开始
            // ResultSet _myDs2 = _myConn.createStatement().executeQuery(_myDs2Sql + " and PNID=" + Id + " ORDER BY PDATE");
            ResultSet _myDs2 = _myConn.createStatement().executeQuery(_myDs2Sql + " and PNID=" + Id + " ORDER BY LASTUPDATEDATE");
            // --吴红亮 修改 结束
            while (_myDs2.next()) {
                String temp = _myDs2.getString("CONTENT");
                if (temp == null) {
                    nodeYj = null;
                } else {
                    nodeYj = temp.toString();
                }
                // nodeYj = "圈阅";
                if (Yj.length() > 0) {
                    Yj += "[BR]";
                }
                Yj += nodeYj;
            }
            _myDs2.close();
            if (Yj == null) {
                Yj = "";
            } else {
                Yj = Yj.trim().replace("\r\n", "[BR]");
                // Yj = Yj.Replace(" ","&nbsp;");
            }
            if (Status == -1) {
                PDate = sdf.parse(pDate);
            } else {
                PDate = sdf.parse(rDate);
            }
            Calendar cl = Calendar.getInstance();
            cl.setTime(PDate);
            // pDateStr = cl.get(Calendar.MONTH) + "." + cl.get(Calendar.DAY_OF_MONTH) + " " + cl.get(Calendar.HOUR_OF_DAY) + ":" + cl.get(Calendar.MINUTE);
            pDateStr = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(cl.getTime());
            if (Status == 1 || Status == 2) {
                rStart = "<tr><td align=center width=18><img src='/images/light.gif'></td><td align=center>";
            } else {
                rStart = "<tr><td align=center colspan=2>";
            }
            rEnd = "</td></tr>";
            Yj = "null".equals(Yj) ? "" : Yj;
            //if (Yj.length() > 0) {
                //yjNum = 1;
                // Content += rStart + actName + "<Br>" + DName + "<Br>" + "<a href=# onMouseOver=\"popup('" + Yj + "','white');\" onMouseOut='kill()'>" + UName + "</a>" + rEnd;
                Content += rStart + actName + "<Br>" + "<a href='javascript:void(0);' onMouseOver=\"popup('" + Yj + "','white');\" onMouseOut='kill()'>" + UName + "</a>" + rEnd;
                // Content += rStart + actName + "<Br>" + DName + "<Br>" + "<a href=# onMouseOver=popup('" + Yj + "','white'); onMouseOut='kill()' target='_blank'>" + UName + "</a>" + rEnd;
            //} else {
            //    Content += rStart + actName + "<Br>" + UName + rEnd;
            //}
            if (nodeName.length() <= 0) {
                nodeName = actName;
            }
        }
        // #endregion
        if (Content.length() > 0) {
            Content += "<tr><td align=center colspan=2>" + pDateStr + "</td></tr>";
        }
        if (nodeCount > 0) {
            page._Count++;
            if (nodeCount < 2) {
                if (Status == 1 || Status == 2) {
                    Status = 1;
                }
                page._Status[page._Count - 1] = Status;
            } else {
                if (receiveStatus == 1) {
                    page._Status[page._Count - 1] = 1;
                } else {
                    if (waitStatus == 1) {
                        page._Status[page._Count - 1] = 0;
                    } else {
                        page._Status[page._Count - 1] = 1;
                    }
                }
            }
            String Space = "&nbsp;&nbsp;&nbsp;&nbsp;";
            Content = "<table width=100% border=0 align=center><tr><td>" + Space + "</td><td><table width=100% border=0 align=center nowrap>" + Content + "</table></td><td>" + Space + "</td></tr></table>";
            page._Content[page._Count - 1] = Content;
            page._Nodes[page._Count - 1] = nodeCount;
            page._UNodes[page._Count - 1] = UNode;
            page._Yj[page._Count - 1] = yjNum;
            page._Fid[page._Count - 1] = fidStr;
            page._NodeName[page._Count - 1] = nodeName;
            if (UList.length() > 0) {
                page._UList[page._Count - 1] = UList;
            } else {
                page._UList[page._Count - 1] = "";
            }
        }
        _myDs1.close();
        if (nodeCount > 0) {
            // System.out.println(page._Content.length);
            initList(idStr, _myDs1Sql, _myDs2Sql, page, _myConn);
        }
        return;
    }

    private void CreateHtml(Page page, HttpServletRequest request) throws Exception {
        int Rows;
        String btStr;
        String Content = "";
        String backGround;
        String arStr = "";
        String arAlt = "";
        String arStr1 = "";
        int curRow;
        String htmStr;
        if (page._Count % page._Column == 0) {
            Rows = page._Count / page._Column;
        } else {
            Rows = page._Count / page._Column + 1;
        }
        btStr = "<table border=0 width=100%>";
        btStr += "<tr><td align=middle><b><font color=#ff0000 size=4>" + page._Bt + "</font></b></td></tr>";
        btStr += "</table>";
        for (int i = 0; i < Rows; i++) {
            curRow = i + 1;
            if (curRow % 2 == 0) {
                // #region 0:偶数行
                for (int j = (curRow * page._Column - 1); j >= ((curRow - 1) * page._Column); j--) {
                    if (j == (curRow * page._Column - 1)) {
                        switch (page._Status[(curRow - 1) * page._Column]) {
                        case 1:
                            arStr = "/images/ar.gif";
                            arAlt = "正在办理";
                            break;
                        case -1:
                            arStr = "/images/ag.gif";
                            arAlt = "办理完毕";
                            break;
                        case 0:
                        default:
                            arStr = "/images/ab.gif";
                            arAlt = "尚未到达";
                            break;
                        }
                        Content += "<tr height=40><td colspan=" + (page._Column * 2) + " height=32>&nbsp;</td>";
                        if (page._UList[(curRow - 1) * page._Column].length() > 0) {
                            Content += "<td vAlign=center align=middle height=32><img src='" + arStr + "' height=23 width=12 border=0 alt='" + arAlt + "'>" + page._UList[(i - 1) * page._Column] + "</td>";
                        } else {
                            Content += "<td vAlign=center align=middle width=104 height=32><img src='" + arStr + "' height=23 width=12 border=0 alt='" + arAlt + "'></td>";
                        }
                        Content += "<tr><td colspan=2>&nbsp;</td>";
                    }
                    if (j > (page._Count - 1)) {
                        Content += "<td colspan=2>&nbsp;</td>";
                    } else {
                        if (page._Status[j] == 1) {
                            backGround = page._Red;
                        } else if (page._Status[j] == 0) {
                            backGround = page._Yellow;
                        } else {
                            if ((page._Nodes[j] - page._UNodes[j]) > 1) {
                                backGround = page._Green;
                            } else {
                                if (page._Yj[j] > 0) {
                                    backGround = page._Blue;
                                } else {
                                    backGround = "White";
                                }
                            }
                        }
                        if (page._Nodes[j] > this.N) {// 超过多少节点就显示 ”共（x）人“
                            Content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=1 width=120><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle><br>" + page._NodeName[j] + "<BR><a href=javascript:OpenList(" + page._Pid + ",'" + page._Fid[j] + "');>共" + page._Nodes[j] + "人</a><br><br></td></td></tr><tbody></table></td>";
                        } else {
                            Content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=1><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle>" + page._Content[j] + "</td></tr><tbody></table></td>";
                        }
                        if (j != ((curRow - 1) * page._Column)) {
                            switch (page._Status[j]) {
                            case 1:
                                arStr = "/images/arl.gif";
                                arAlt = "正在办理";
                                break;
                            case -1:
                                arAlt = "办理完毕";
                                arStr = "/images/agl.gif";
                                break;
                            case 0:
                            default:
                                arAlt = "尚未到达";
                                arStr = "/images/abl.gif";
                                break;
                            }
                            if (page._UList[j].length() > 0) {
                                Content += "<td vAlign=center align=middle height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'>" + page._UList[j] + "</td>";
                            } else {
                                Content += "<td vAlign=center align=middle width=25 height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'></td>";
                            }
                        }
                    }
                }
                // #endregion
            } else {
                // #region 1:奇数行
                for (int j = ((curRow - 1) * page._Column); j <= (curRow * page._Column - 1); j++) {
                    if (j > page._Count - 1) {
                        Content += "<td colspan=2>&nbsp;</td>";
                    } else {
                        if (page._Status[j] == 1) {
                            backGround = page._Red;
                        } else if (page._Status[j] == 0) {
                            backGround = page._Yellow;
                        } else {
                            if ((page._Nodes[j] - page._UNodes[j]) > 1) {
                                backGround = page._Green;
                            } else {
                                if (page._Yj[j] > 0) {
                                    backGround = page._Blue;
                                } else {
                                    backGround = "White";
                                }
                            }
                        }
                        switch (page._Status[j]) {
                        case -1:
                            arAlt = "办理完毕";
                            arStr = "/images/agr.gif";
                            arStr1 = "/images/ag.gif";
                            break;
                        case 1:
                            arAlt = "正在办理";
                            arStr = "/images/arr.gif";
                            arStr1 = "/images/ar.gif";
                            break;
                        case 0:
                        default:
                            arAlt = "尚未到达";
                            arStr = "/images/abr.gif";
                            arStr1 = "/images/ab.gif";
                            break;
                        }
                        if (j == 0) {
                            Content += "<tr><td align=middle valign=middle><img src='/images/face.gif'></td>";
                            Content += "<td vAlign=center align=middle width=25 height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'></td>";
                        } else {
                            if (j == (curRow - 1) * page._Column) {
                                Content += "<tr height=40><td colspan=2>&nbsp;</td>";
                                if (page._UList[j].length() < 0) {
                                    Content += "<td vAlign=center align=middle height=32><img src='" + arStr1 + "' height=23 width=12 border=0 alt='" + arAlt + "'>" + page._UList[j] + "</td>";
                                } else {
                                    Content += "<td vAlign=center align=middle width=104 height=32><img src='" + arStr1 + "' height=23 width=12 border=0 alt='" + arAlt + "'></td>";
                                }
                                Content += "<td colspan=" + (page._Column * 2 - 3) + " height=32>&nbsp;</td>";
                                Content += "<tr><td colspan=2>&nbsp;</td>";
                            } else {
                                if (page._UList[j].length() > 0) {
                                    Content += "<td vAlign=center align=middle height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'>" + page._UList[j] + "</td>";
                                } else {
                                    Content += "<td vAlign=center align=middle width=25 height=80><img src='" + arStr + "' height=12 width=23 border=0 alt='" + arAlt + "'></td>";
                                }
                            }
                        }
                        if (page._Nodes[j] > this.N) {
                            Content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=1 width=120><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle><br>" + page._NodeName[j] + "<BR><a href=javascript:OpenList(" + page._Pid + ",'" + page._Fid[j] + "');>共" + page._Nodes[j] + "人</a><br><br></td></td></tr><tbody></table></td>";
                        } else {
                            Content += "<td align=middle valign=middle><table cellSpacing=0 cellPadding=0 border=1><tbody><tr bgcolor='" + backGround + "'><td align=middle valign=middle>" + page._Content[j] + "</td></tr><tbody></table></td>";
                        }
                    }
                }
            }
        }
        if (Content.length() > 0) {
            Content = "<table border=0>" + Content + "</table>";
        } else {
            htmStr = "<script language='javascript'>";
            htmStr += "alert('该文件没有流转信息!');";
            htmStr += "top.close();";
            htmStr += "</script>";
            errStr = htmStr;
            throw new Exception();
        }
        DataTable tabTitle = new DataTable("tabTitle");
        DataTable tabView = new DataTable("tabView");
        DataRow dr = tabTitle.NewRow();
        dr.put("btStr", btStr);
        tabTitle.rows.add(dr);
        dr = tabView.NewRow();
        dr.put("Content", Content);
        tabView.rows.add(dr);
        request.setAttribute("tabTitle", btStr);
        request.setAttribute("tabView", Content);
        // myRow = new TableRow();
        // myCell = new TableCell();
        // myCell.Text = Content;
        // myRow.Cells.Add(myCell);
        // tabView.Rows.Add(myRow);
        htmStr = "<script language='javascript'></script>";
    }

    class Page {
        long _Id = 0;
        String _Bt = "";
        long _Pid = 0;
        String _Red = "#ffd9d2";
        String _Blue = "#caebff";
        String _Green = "#deffe1";
        String _Yellow = "#FFFF99";
        int _Column = 3;
        String[] _Content;
        int[] _Status;
        int[] _Nodes;
        int[] _UNodes;
        int[] _Yj;
        String[] _UList;
        String[] _Fid;
        String[] _NodeName;
        int _Count = 0;
        String strTitle = "察看流程";
    }
}
