package dsoap.web.action;

import java.net.URLDecoder;

import xsf.data.DBManager;
import dsoap.tools.webui.HtmlInputButton;
import dsoap.tools.webui.TextBox;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class ReturnReasonAction extends Action {
    private static final long serialVersionUID = -3155066901329136656L;
    public HtmlInputButton BtnOk = new HtmlInputButton();
    public TextBox TxtReason = new TextBox();
    public String errStr = "";
    public String pid = "";
    public String pnid = "";
    public String info_id = "";
    public String isall = "";
    public String fpnid = "";
    public String userid = "";
    public String isth = "";
    public String url = "";

    @Override
    public String execute() throws Exception {
        super.execute();
        if (request.getParameter("FlowParms") != null) {
            String FlowParms = URLDecoder.decode(request.getParameter("FlowParms"), "utf-8");
            url = FlowParms;
            String[] temps = FlowParms.split("&");
            for (String tempStr : temps) {
                if (tempStr.indexOf("=") > 0) {
                    if (tempStr.split("=")[0].equals("pid")) {
                        this.pid = tempStr.split("=")[1];
                    } else if (tempStr.split("=")[0].equals("pnid")) {
                        this.pnid = tempStr.split("=")[1];
                    } else if (tempStr.split("=")[0].equals("info_id")) {
                        this.info_id = tempStr.split("=")[1];
                    } else if (tempStr.split("=")[0].equals("isall")) {
                        this.isall = tempStr.split("=")[1];
                    } else if (tempStr.split("=")[0].equals("fpnid")) {
                        this.fpnid = tempStr.split("=")[1];
                    } else if (tempStr.split("=")[0].equals("userid")) {
                        this.userid = tempStr.split("=")[1];
                    } else if (tempStr.split("=")[0].equals("isth")) {
                        this.isth = tempStr.split("=")[1];
                    }
                }
            }
        }
        // 在此处放置用户代码以初始化页面
        if (request.getParameter("isview") != null && "1".equals(request.getParameter("isview"))) {
            TxtReason.setReadOnly(true);
            BtnOk.setVisible(false);
            // 在此处放置用户代码以初始化页面
            String _cmdStr = "SELECT BACKREASON FROM G_PNODES WHERE PID=" + request.getParameter("pid") + " AND ID=" + request.getParameter("pnid");
            System.out.println(_cmdStr);
            xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
            if (dt.getRows().size() > 0) {
                TxtReason.setText(dt.getRows().get(0).getString("BACKREASON"));
            }
            return SUCCESS;
        }
        return SUCCESS;
    }

    // private String BtnOk_ServerClick() throws Exception {
    // String strReason = TxtReason.getText().trim();
    // if (strReason == "") {
    // errStr = "<script language=javascript>\n";
    // errStr = "alert('请输入退回原因！')\n";
    // errStr = "</script>\n";
    // return ERROR;
    // }
    // return SUCCESS;
    // // Response.Redirect("ReturnInbox.aspx?"+Server.UrlDecode(Request.QueryString.ToString())+"&reason="+Server.UrlEncode(strReason));
    // }

}
