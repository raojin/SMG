package dsoap.web.action;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import xsf.data.IDataSource;
import dsoap.dsflow.TimeSpan;
import dsoap.dsflow.model.DataRow;
import dsoap.dsflow.model.DataTable;
import dsoap.tools.SysDataSource;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class ViewFlow1Action extends Action {
    private static final long serialVersionUID = 6372146076413422557L;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
    public String errStr = "";

    @Override
    public String execute() throws Exception {
        super.execute();
        // String strSelect = "";
        // String wf_id = request.getParameter("wf_id");
        String info_id = request.getParameter("info_id");
        // String DBMS = ConfigurationSettings.AppSettings("DBMS");
        // if ("SYBASE".equals(DBMS)) {
        // if ("0".equals(wf_id)) {
        // wf_id = "(SELECT WF_ID FROM G_INFOS WHERE ID=" + info_id + ")";
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,PDATE,SDATE,PID,PNID from " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,'' FSR,A.UNAME as JSR,A.status STATUS,'' PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A Where A.WF_ID=" + wf_id + " AND A.ID=1 And A.INFO_ID=" + info_id + ") AS T1 " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,convert(char(16),A.QSRQ,20) PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID and A.INFO_ID=" + info_id + " ) " + "Order By ID";
        // } else {
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,QSRQ,SDATE,PID,PNID from " + "(select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,convert(char(16),A.QSRQ,20) PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " and A.PARENTFLOWPID=C.PID AND A.PARENTFLOWPNID=C.ID and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + ") AS T1 " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,convert(char(16),A.QSRQ,20) PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + " ) " + "Order By PID,ID";
        // }
        // } else if ("ORACLE".equals(DBMS)) {
        // // if ("0".equals(wf_id)) {
        // // wf_id = "(SELECT WF_ID FROM G_INFOS WHERE ID=" + info_id + ")";
        // // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,PDATE,SDATE,PID,PNID from " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,'' FSR,A.UNAME as JSR,A.status STATUS,'' PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A Where A.WF_ID=" + wf_id + " AND A.ID=1 And A.INFO_ID=" + info_id + ") " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,TO_CHAR(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID and A.INFO_ID=" + info_id + " ) " + "Order By ID";
        // // } else {
        // // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,QSRQ,SDATE,PID,PNID from " + "(select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,TO_CHAR(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " and A.PARENTFLOWPID=C.PID AND A.PARENTFLOWPNID=C.ID and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + ") " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,TO_CHAR(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + " ) " + "Order By PID,ID";
        // // }
        // // 2011.8------------------
        // // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,PDATE,SDATE,PID,PNID from(select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,'' FSR,A.UNAME as JSR,A.STATUS,TO_CHAR(A.SDATE,'YYYY-MM-DD HH24:MI') PDATE,NULL SDATE,A.PID,A.ID AS PNID from G_PNODES A where A.ID=1 and A.INFO_ID=" + info_id + ")union(select A.ATYPE,A.MUSER_ID,A.ID,A.ACTNAME,C.UNAME as FSR,A.UNAME as JSR,A.STATUS,TO_CHAR(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,C.SDATE,A.PID,A.ID AS PNID from G_PNODES A, G_PNODES C where A.FID=C.ID and C.PID=A.PID and A.INFO_ID=" + info_id + ")order by ID";
        // // 2011.9------------------
        // // strSelect = "select S.PID,S.ID PNID,S.MUSER_ID,S.USER_ID,R.MUSER_ID MUSER_ID1,R.USER_ID USER_ID1,S.UNAME FSR,U.UNAME DBR,R.ACTNAME,R.STATUS,R.ATYPE,TO_CHAR(R.QSRQ, 'YYYY-MM-DD HH24:MI') PDATE,S.SDATE,R.UNAME JSR from G_PNODES S inner join G_PNODES R on R.FID=S.ID and R.PID=S.PID left join G_USERS U on S.USER_ID = U.ID where S.INFO_ID=" + info_id + " order by S.ID";
        // // 2011.12------------------
        // strSelect = "select S.PID,S.ID PNID,S.MUSER_ID,S.USER_ID,R.MUSER_ID MUSER_ID1,R.USER_ID USER_ID1,case when R.SENDTYPE=3 then U1.NAME else S.UNAME end FSR,U.NAME DBR,R.ACTNAME,R.STATUS,R.ATYPE,TO_CHAR(R.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,case when R.SENDTYPE=3 then R.RDATE else S.SDATE end SDATE,R.UNAME JSR from G_PNODES S inner join G_PNODES R on R.FID=S.ID and R.PID=S.PID left join G_USERINFO U on S.USER_ID=U.ID left join G_USERINFO U1 on R.WHOHANDLE=U1.ID left join G_ORGANIZE B on R.DEPT_ID=B.ID left join G_ORGUSER C on R.USER_ID=C.USERINFOID and R.DEPT_ID=C.ORGANIZEID where S.INFO_ID=" + info_id + " order by S.FID,S.SDATE,B.ITEMINDEX,C.ITEMINDEX";
        // } else if ("SQLSERVER".equals(DBMS)) {
        // if ("0".equals(wf_id)) {
        // wf_id = "(SELECT WF_ID FROM G_INFOS WHERE ID=" + info_id + ")";
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,PDATE,SDATE,PID,PNID from " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,'' FSR,A.UNAME as JSR,A.status STATUS,'' PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A Where A.WF_ID=" + wf_id + " AND A.ID=1 And A.INFO_ID=" + info_id + ") AS T1 " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,convert(char(16),A.QSRQ,20) PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID and A.INFO_ID=" + info_id + " ) " + "Order By ID";
        // } else {
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,QSRQ,SDATE,PID,PNID from " + "(select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,convert(char(16),A.QSRQ,20) PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " and A.PARENTFLOWPID=C.PID AND A.PARENTFLOWPNID=C.ID and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + ") AS T1 " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,convert(char(16),A.QSRQ,20) PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + " ) " + "Order By PID,ID";
        // }
        // } else if ("MYSQL".equals(DBMS)) {
        // if ("0".equals(wf_id)) {
        // wf_id = "(select WF_ID from G_INFOS where ID=" + info_id + ")";
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,PDATE,SDATE,PID,PNID from " + "(select A.ATYPE,A.MUSER_ID,A.ID,A.ACTNAME,'' FSR,A.UNAME as JSR,A.STATUS,'' PDATE,A.SDATE,A.PID,A.ID as PNID from G_PNODES A where A.WF_ID=" + wf_id + " and A.ID=1 and A.INFO_ID=" + info_id + " union select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.UNAME as FSR,A.UNAME as JSR,A.STATUS,A.QSRQ PDATE,A.SDATE,A.PID,A.ID as PNID from G_PNODES A,G_PNODES C where A.WF_ID=" + wf_id + " and C.WF_ID=" + wf_id + " and A.FID=C.ID and C.INFO_ID=A.INFO_ID and A.INFO_ID=" + info_id + " )A " + "order by ID";
        // } else {
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,QSRQ,SDATE,PID,PNID from " + "(select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,DATE_FORMAT(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID from G_PNODES A,G_PNODES C where A.WF_ID=" + wf_id + " and A.PARENTFLOWPID=C.PID AND A.PARENTFLOWPNID=C.ID and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + ") " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.STATUS STATUS,DATE_FORMAT(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID from G_PNODES A,G_PNODES C where A.WF_ID=" + wf_id + " and C.WF_ID=" + wf_id + " and A.FID=C.ID and C.INFO_ID=A.INFO_ID and A.INFO_ID=" + info_id + ")"
        // + "order by PID,ID";
        // }
        // } else {
        // if ("0".equals(wf_id)) {
        // wf_id = "(SELECT WF_ID FROM G_INFOS WHERE ID=" + info_id + ")";
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,PDATE,SDATE,PID,PNID from " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,'' FSR,A.UNAME as JSR,A.status STATUS,'' PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A Where A.WF_ID=" + wf_id + " AND A.ID=1 And A.INFO_ID=" + info_id + ") " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,TO_CHAR(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID and A.INFO_ID=" + info_id + " ) " + "Order By ID";
        // } else {
        // strSelect = "select ATYPE,MUSER_ID,ID,ACTNAME,FSR,JSR,STATUS,QSRQ,SDATE,PID,PNID from " + "(select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,TO_CHAR(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " and A.PARENTFLOWPID=C.PID AND A.PARENTFLOWPNID=C.ID and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + ") " + "union " + "(Select A.ATYPE,A.MUSER_ID,A.ID as ID,A.ACTNAME,C.uname as FSR,A.UNAME as JSR,A.status STATUS,TO_CHAR(A.QSRQ,'YYYY-MM-DD HH24:MI') PDATE,A.SDATE,A.PID,A.ID AS PNID From G_PNODES A,g_pnodes C Where A.WF_ID=" + wf_id + " AND C.WF_ID=" + wf_id + " AND A.fid=C.id and C.info_id=A.INFO_ID And A.INFO_ID=" + info_id + " ) " + "Order By PID,ID";
        // }
        // }
        DataTable dt = new DataTable("TABLE");
        DataRow dr = null;
        int i = 1;
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("INFO_ID", info_id);
        xsf.data.DataTable l = sqlDataSource.query("ViewFlow1");
        for (xsf.data.DataRow o : l.getRows()) {
            dr = dt.NewRow();
            // String sPid = o.getString("PID");
            String sPnid = o.getString("PNID");
            String actName = o.getString("ACTNAME");
            String mUserId = o.getString("MUSER_ID");
            String userId = o.getString("USER_ID");
            String sender = o.getString("FSR");
            String sender1 = o.getString("DBR");
            // String aType = _myDs.getString("ATYPE");
            String jsr = o.getString("JSR");
            Date pDate = o.getDate("PDATE");
            Date sDate = o.getDate("SDATE");
            int status = o.getInt("STATUS");
            boolean isZB = (-1 != status);
            if (isZB) {
                dr.setForeColor("Red");
            }
            if (i % 2 == 0) {
                dr.setBackColor("#ECF8FF");
            } else {
                dr.setBackColor("#ffffff");
            }
            String sOpinion = o.getString("CONTENT");
            if (sOpinion != null && !"".equals(sOpinion)) {
                dr.put("title", sOpinion);
                dr.setStyle("CURSOR: hand;");
            }
            dr.put("ordinal", String.valueOf(i));
            dr.put("status", getStatus(String.valueOf(status), sPnid, info_id));
            dr.put("ACTNAME", actName != null ? actName : "");
            dr.put("MUSER_ID", mUserId != null ? mUserId : "");
            dr.put("USER_ID", userId != null ? userId : "");
            dr.put("DBR", sender1 != null ? sender1 : "");
            dr.put("FSR", sender != null ? sender : "");
            // dr.put("USER", getUserName(sender, aType, mUserId, _myConn));
            dr.put("USER", jsr != null ? jsr : "");
            dr.put("PDATE", pDate != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(pDate) : "");
            dr.put("SDATE", sDate != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(sDate) : "");
            dt.rows.add(dr);
            i++;
        }
        request.setAttribute("tbl", dt);
        return SUCCESS;
    }

    // public String getOpition(String sPid, String sPnid) {
    // String retValue = DBManager.getFieldStringValue("select CONTENT from G_OPINION where PID=" + sPid + " and PNID=" + sPnid);
    // retValue = ((null == retValue || "".equals(retValue)) && !"1".equals(sPnid)) ? "" : retValue;
    // return retValue;
    // }

    protected Date ChechWeekDay(Date dtTemp) throws ParseException {
        Date dtReturn = dtTemp;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dtReturn);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dtStart = cal.getTime();
        int iDays = 0;
        int iCountDay = 0;
        String[] strArrDays = {};
        String[] strArrDayStatus = {};
        // 排除特殊日期
        if (dtTemp.getTime() >= dtStart.getTime()) {
            for (int j = 0; j < iCountDay; j++) {
                Date dtTT = sdf.parse(strArrDays[j]);// Convert.ToDateTime(strArrDays[j]);
                if (dtTT.getTime() >= dtStart.getTime() && dtTT.getTime() <= dtTemp.getTime()) {
                    if ("0".equals(strArrDayStatus[j])) {
                        if (getWeekDay(dtTT) != 6 && getWeekDay(dtTT) != 0) {
                            iDays--;
                        }
                    } else {
                        if (getWeekDay(dtTT) == 6 || getWeekDay(dtTT) == 0) {
                            iDays++;
                        }
                    }
                }
            }
            while (dtTemp.getTime() >= new Date().getTime()) {
                if (getWeekDay(dtTemp) == 6 || getWeekDay(dtTemp) == 0) {
                    iDays--;
                }
                cal.setTime(dtTemp);
                cal.roll(Calendar.DAY_OF_MONTH, -1);
                dtTemp = cal.getTime();
                // dtTemp = dtTemp.AddDays(-1);
            }
            cal.setTime(dtReturn);
            cal.roll(Calendar.DAY_OF_MONTH, -iDays);
            dtReturn = cal.getTime();
            // dtReturn = dtReturn.AddDays(iDays);
        } else {
            for (int j = 0; j < iCountDay; j++) {
                Date dtTT = sdf.parse(strArrDays[j]);
                if (dtTT.getTime() >= dtTemp.getTime() && dtTT.getTime() <= dtStart.getTime()) {
                    if ("0".equals(strArrDayStatus[j])) {
                        if (getWeekDay(dtTT) != 6 && getWeekDay(dtTT) != 0) {
                            iDays--;
                        }
                    } else {
                        if (getWeekDay(dtTT) == 6 || getWeekDay(dtTT) == 0) {
                            iDays++;
                        }
                    }
                }
            }
            while (dtTemp.getTime() <= new Date().getTime()) {
                if (getWeekDay(dtTemp) == 6 || getWeekDay(dtTemp) == 0) {
                    iDays--;
                }
                cal.setTime(dtTemp);
                cal.roll(Calendar.DAY_OF_MONTH, -1);
                dtTemp = cal.getTime();
                // dtTemp = dtTemp.AddDays(1);
            }
            cal.setTime(dtReturn);
            cal.roll(Calendar.DAY_OF_MONTH, -iDays);
            dtReturn = cal.getTime();
            // dtReturn = dtReturn.AddDays(-iDays);
        }
        return dtReturn;
    }

    private String getStatus(String sStatus, String sID, String sInfo_ID) throws Exception {
        String retValue = "<img title='已处理' src='../images/FLOWSTATUS_1.gif' width='21' height='21'>";
        if (sStatus == "-1") {// 办结
            return retValue;
        }
        // String DBMS = ConfigurationSettings.AppSettings("DBMS");
        // sSql = "select STATUS,";
        // if ("SYBASE".equals(DBMS)) {
        // sSql += "convert(char(16),EDATE,20) ED1 ";
        // } else if ("ORACLE".equals(DBMS)) {
        // sSql += "TO_CHAR(EDATE,'YYYY.MM.DD HH24:MI') ED1 ";
        // } else if ("SQLSERVER".equals(DBMS)) {
        // sSql += "convert(char(16),EDATE,20) ED1 ";
        // } else if ("MYSQL".equals(DBMS)) {
        // sSql += "DATE_FORMAT(EDATE,'YYYY.MM.DD HH24:MI') ED1 ";
        // } else {
        // sSql += "TO_CHAR(EDATE,'YYYY.MM.DD HH24:MI') ED1 ";
        // }
        // sSql += "from G_INBOX where INFO_ID=" + sInfo_ID + " AND PNID=" + sID;
        // _myDs = _myRead.executeQuery(sSql);
        IDataSource sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("INFO_ID", sInfo_ID);
        sqlDataSource.setParameter("PNID", sID);
        xsf.data.DataTable dt = sqlDataSource.query("getGInboxStatus");
        for (xsf.data.DataRow dr : dt.getRows()) {
            int iHours = 1;
            if (dr.get("ED1") != null) {
                Date dtEnd = dr.getDate("ED1");
                TimeSpan dsDiff = TimeSpan.toCm(ChechWeekDay(dtEnd), new Date());
                iHours = dsDiff.getTotalHours();
            }
            if (iHours >= 0) {
                if ("1".equals(dr.getString("STATUS"))) {
                    retValue = "<img title='未打开' src='../images/FLOWSTATUS_3.gif' width='21' height='21'>";
                } else {
                    retValue = "<img title='已打开' src='../images/FLOWSTATUS_4.gif' width='21' height='21'>";
                }
            } else {
                retValue = "<img title='已超时' src='../images/FLOWSTATUS_2.gif' width='21' height='21'>";
            }
        }
        return retValue == null ? "" : retValue;
    }

    // private String getUserName(String User_Name, String ATYPE, String DUser_ID, Connection _myConn) throws Exception {
    // Statement _myRead = _myConn.createStatement();
    // ResultSet _myDs = null;
    // if (User_Name == null) {
    // User_Name = "";
    // }
    // try {
    // String sql = "select UNAME from G_USERS where ID=" + DUser_ID;
    // _myDs = _myRead.executeQuery(sql);
    // String DUser_Name = "";
    // if (_myDs.next()) {
    // DUser_Name = _myDs.getString(1);
    // }
    // _myDs.close();
    // User_Name = DUser_Name;
    // // if (DUser_Name.length() > 0 && !User_Name.equals(DUser_Name)) {
    // // if ("1".equals(ATYPE)) {// 长期代办
    // // User_Name = User_Name;
    // // } else if ("2".equals(ATYPE)) {// 委托代办
    // // User_Name = DUser_Name + "(" + User_Name + "代)";
    // // } else { // 临时代办
    // // User_Name += "(代)";
    // // }
    // // }
    // if (User_Name == null) {
    // return "";
    // }
    // return User_Name;
    // } catch (Exception e) {
    // e.printStackTrace();
    // throw e;
    // } finally {
    // }
    // }

    // private DataTable TblList_Add(String sql, String info_id, Connection _myConn) throws Exception {
    // DataTable dt = new DataTable("TABLE");
    // Statement _myRead = _myConn.createStatement();
    // ResultSet _myDs = null;
    // _myDs = _myRead.executeQuery(sql);
    // DataRow dr = null;
    // int i = 1;
    // while (_myDs.next()) {
    // dr = dt.NewRow();
    // String sPid = _myDs.getString("PID");
    // String sPnid = _myDs.getString("PNID");
    // String actName = _myDs.getString("ACTNAME");
    // String mUserId = _myDs.getString("MUSER_ID");
    // String userId = _myDs.getString("USER_ID");
    // String sender = _myDs.getString("FSR");
    // String sender1 = _myDs.getString("DBR");
    // // String aType = _myDs.getString("ATYPE");
    // String jsr = _myDs.getString("JSR");
    // String pDate = _myDs.getString("PDATE");
    // String sDate = _myDs.getString("SDATE");
    // int status = _myDs.getInt("STATUS");
    // boolean isZB = (-1 != status);
    // if (isZB) {
    // dr.setForeColor("Red");
    // }
    // if (i % 2 == 0) {
    // dr.setBackColor("#ECF8FF");
    // } else {
    // dr.setBackColor("#ffffff");
    // }
    // String sOpinion = getOpition(sPid, sPnid, _myConn);
    // if (!"".equals(sOpinion)) {
    // dr.put("title", sOpinion);
    // dr.setStyle("CURSOR: hand;");
    // }
    // if (sDate != null) {
    // Calendar cl = Calendar.getInstance(); // 时间
    // cl.setTime(sdf.parse(sDate));
    // sDate = sDate == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(cl.getTime());
    // } else {
    // sDate = "";
    // }
    // dr.put("ordinal", String.valueOf(i));
    // dr.put("status", getStatus(String.valueOf(status), sPnid, info_id, _myConn));
    // dr.put("ACTNAME", actName != null ? actName : "");
    // dr.put("MUSER_ID", mUserId != null ? mUserId : "");
    // dr.put("USER_ID", userId != null ? userId : "");
    // dr.put("DBR", sender1 != null ? sender1 : "");
    // dr.put("FSR", sender != null ? sender : "");
    // // dr.put("USER", getUserName(sender, aType, mUserId, _myConn));
    // dr.put("USER", jsr != null ? jsr : "");
    // dr.put("PDATE", pDate != null ? pDate : "");
    // dr.put("SDATE", sDate);
    // dt.rows.add(dr);
    // i++;
    // }
    // return dt;
    // }

    private int getWeekDay(Date date) {
        Calendar aCalendar = Calendar.getInstance();
        // 里面野可以直接插入date类型
        aCalendar.setTime(date);
        // 计算此日期是一周中的哪一天
        int x = aCalendar.get(Calendar.DAY_OF_WEEK);
        return x;
    }

}
