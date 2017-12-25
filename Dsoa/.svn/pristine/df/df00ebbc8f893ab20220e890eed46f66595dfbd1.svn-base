package dsoap.web.action;

import java.text.SimpleDateFormat;

import xsf.data.DBManager;
import dsoap.tools.webwork.Action;

public class ViewFlowAction extends Action {

    private static final long serialVersionUID = -9073681757992744656L;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
    public String errStr = "";
    public String wf_id = "";

    @Override
    public String execute() throws Exception {
        super.execute();
        boolean IsPostBack = false;
        if (!IsPostBack) {
            String info_id = request.getParameter("info_id");
            if (info_id != null && !"".equals(info_id)) {
                wf_id = request.getParameter("wf_id");
            }
            String labName = this.GetInfoBt(info_id);
            String path = request.getContextPath();
            String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
            if (!this.getPID(info_id).equals("0")) {
                String labScript = "<script language='javascript'>";
                labScript += "document.all.DS_Pane.Url='" + basePath + "service/wfservice?wsdl';";
                labScript += "document.all.DS_Pane.setViewFromInfoId('" + info_id + "','" + wf_id + "');";
                labScript += "document.all.DS_Pane.Resize();";
                labScript += "</script>";
                String dsPanel = "<OBJECT id='DS_Pane' style='WIDTH: 100%; HEIGHT: 100%' codeBase='DS_WF_UI.CAB#version=3,0,0,10' classid='CLSID:300BF8C7-1570-4DD3-86DC-D62C98A9E020' VIEWASTEXT><PARAM NAME='_ExtentX' VALUE='25294'><PARAM NAME='_ExtentY' VALUE='12171'></OBJECT>";
                request.setAttribute("labName", labName);
                request.setAttribute("labScript", labScript);
                request.setAttribute("dsPanel", dsPanel);
            } else {
                request.setAttribute("labName", labName);
                request.setAttribute("labScript", "");
                request.setAttribute("dsPanel", "");
            }
        }
        return SUCCESS;
    }

    private String GetInfoBt(String info_id) {
        String _cmdStr;
        String sReturnStr = "";
        _cmdStr = "SELECT BT FROM G_INFOS B WHERE ID=" + info_id;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
            sReturnStr = dr.getString("BT");
        }
        return sReturnStr;
    }

    public String getPID(String id) {
        String _cmdStr;
        String sReturnStr = "";
        _cmdStr = "SELECT count(PID) AS COUNT FROM G_PNODES WHERE INFO_ID = " + id;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        for (xsf.data.DataRow dr : dt.getRows()) {
            sReturnStr = dr.getString("COUNT");
        }
        return sReturnStr;
    }

}
