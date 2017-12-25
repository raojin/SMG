package dsoap.web.action;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Node;

import dsoap.dsflow.DS_FLOWClass;
import dsoap.tools.ConfigurationSettings;
import dsoap.tools.webwork.Action;

/**
 * 
 * @author liuzhq ps:类逻辑有任何疑问请找.net的原始开发人员。
 */
public class SendResultAction extends Action {
    private static final long serialVersionUID = 1L;

    @Override
    public String execute() throws Exception {
        super.execute();
        DS_FLOWClass dsFlow = (DS_FLOWClass) session.get("DSFLOW");
        if (dsFlow == null) {
            throw new Exception("流程信息错误！");// error.jsp
        }
        if (dsFlow.iErrorCode > 0) {
            throw new Exception("文件处理失败：" + dsFlow.sErrorMessage + "   ErrorCode：" + dsFlow.iErrorCode);// error.jsp
        } else {
            if (dsFlow.iSendType == 9 || dsFlow.iSendType == 19 || dsFlow.iSendType == 39) {
                // 执行办结
                if (dsFlow.sendToEnd()) {
                    ShowMsg("1", "该文件已成功办结", request);
                } else {
                    ShowMsg("0", "办结失败", request);
                }
            } else if (dsFlow.iSendType == 4 || dsFlow.iSendType == 29) {
                String sNodeName = "";
                for (Object obj : dsFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                    Node myNode = (Node) obj;
                    if ("1".equals(myNode.valueOf("@Enabled"))) {
                        sNodeName = myNode.valueOf("@NodeCaption");// Attributes["NodeCaption"].Value;
                        break;
                    }
                }
                ShowMsg("1", "该文件已成功发送到 " + sNodeName, request);
            } else if (dsFlow.iSendType == 14) {
                String sNodeName = "";
                for (Object obj : dsFlow.ds_ParentFlow.NextNodeInfoXml.selectNodes("Nodes/Node")) {
                    Node myNode = (Node) obj;
                    if ("1".equals(myNode.valueOf("@Enabled"))) {
                        sNodeName = myNode.valueOf("@NodeCaption");// Attributes["NodeCaption"].Value;
                        break;
                    }
                }
                ShowMsg("1", "该文件已成功发送到 " + sNodeName, request);
            } else if (dsFlow.iSendType == 2 || dsFlow.iSendType == 12) {
                ShowMsg("0", "该文件处理完毕", request);
            } else {
                ShowMsg("1", "该文件已成功发送到下列用户：", request);
                if (dsFlow.iSendType >= 10) {
                    ShowUserList(dsFlow.ds_ParentFlow.SendUserListXml.selectSingleNode("Nodes"), request);
                } else {
                    ShowUserList(dsFlow.SendUserListXml.selectSingleNode("Nodes"), request);
                }
            }
            request.setAttribute("txtRetualValue", "OK");
            request.setAttribute("INFO_ID", String.valueOf(dsFlow.iInfoID));
            // this.txtRetualValue.Value ="OK";
            // if (!Page.IsPostBack) {
            // -- 吴红亮 修改 开始
            // String IsPostBack = "<script language=javascript>\n" + "setTimeout(\"top.close()\",3000)\n" + "</script>\n";
            // String IsPostBack = "<script language=javascript>setTimeout(function(){top.window.location.href='" + session.get("sendBackUrl") + "';},3000);</script>";
            // String IsPostBack = "<script language=javascript>setTimeout(function(){var isNew = " + session.get("isNewFile") + ";var a = '" + session.get("sendBackUrl") + "';if(!isNew){var b = top.window.Cookie.get('DaiBanBack');top.window.Cookie.set('DaiBanBack','');if(b == null || b == ''){b=a;}top.window.location.href = b;}else{top.window.location.href = a;}},3000);</script>";
            // request.setAttribute("IsPostBack", IsPostBack);
            // -- 吴红亮 修改 结束
            // }
            String result = null;
            if (dsFlow.iSendType == 3 || dsFlow.iSendType == 39) {
                String url = ConfigurationSettings.getServerInfo(null) + "/action?IssueAction=14&Info_ID=" + dsFlow.iInfoID;
                result = DS_FLOWClass.httpService(url, "GET", null, null);
            }
            return "1".equals(result) ? "zf" : SUCCESS;
            // ServletContext sc = getServletContext();
            // RequestDispatcher rd = null;
            // rd = sc.getRequestDispatcher("/ds_flow_wsj11/ds_Flow/SendResult.jsp"); // 定向的页面
            // rd.forward(request, response);
        }
    }

    private void ShowUserList(Node tdroot, HttpServletRequest request) {
        // try {
        // String sUserList = "<table cellspacing=\"0\" cellpadding=\"4\" rules=\"cols\" bordercolor=\"#DEDFDE\" border=\"1\" style=\"border-color:#DEDFDE;border-width:1px;border-style:solid;width:100%;border-collapse:collapse;BORDER-RIGHT:#dedfde 1px; BORDER-TOP:#dedfde 1px; BORDER-LEFT:#dedfde 1px; COLOR:black; BORDER-BOTTOM:#dedfde 0px; BORDER-COLLAPSE:collapse; BACKGROUND-COLOR:white\">";
        // sUserList += "<tr class='line_title_right' height=45 align=center>";
        // sUserList += "<td align=center width=20%>办理动作</td>";
        // sUserList += "<td align=center colspan=4>办理人员</td>";
        // sUserList += "</td>";
        // sUserList += "</tr>";
        // int curRow = 0;
        // for (Object obj : tdroot.selectNodes("Node")) {
        // Node tdnode = (Node) obj;
        // int j = 0;
        // String sNodeCaption = tdnode.valueOf("@NodeCaption");// Attributes["NodeCaption"].Value;
        // for (Object tdobj : tdnode.selectNodes("Node")) {
        // Node td = (Node) tdobj;
        // if (j % 4 == 0) {
        // if (j != 0) {
        // sUserList += "</tr>";
        // }
        // if (curRow % 2 == 0) {
        // sUserList += "<tr align=center style=\"background-color:#F4F6F6;\" height=20 align=center valign=middle>";
        // } else {
        // sUserList += "<tr align=center style=\"background-color:white;\" height=20 align=center valign=middle>";
        // }
        // sUserList += "<td width=20%><b>" + sNodeCaption + "</b></td>";
        // sNodeCaption = "";
        // curRow++;
        // }
        // j++;
        // sUserList += "<td width=20%>" + td.valueOf("@UName") + "</td>";
        // }
        // if (tdnode.selectNodes("Node").size() % 4 != 0) {
        // int addCount = 4 - (tdnode.selectNodes("Node").size() % 4);
        // for (int k = 0; k < addCount; k++) {
        // sUserList += "<td width=20%>&nbsp;</td>";
        // }
        // }
        // }
        // sUserList += "</tr></table>";
        // request.setAttribute("labSendMsg", sUserList);
        // } catch (Exception es) {
        // try {
        // throw es;
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        try {
            String sUserList = "";
            for (Object obj : tdroot.selectNodes("Node")) {
                Node tdnode = (Node) obj;
                for (Object tdobj : tdnode.selectNodes("Node")) {
                    Node td = (Node) tdobj;
                    // 吴红亮 修改 江苏财政厅 不发送给自己和正在办理的人
                    // sUserList += td.valueOf("@UName") + ",";
                    if (ConfigurationSettings.isFilterPerson) {
                        if (!"1".equals(td.valueOf("@filter"))) {
                            sUserList += td.valueOf("@UName") + "、";
                        }
                    } else {
                        sUserList += td.valueOf("@UName") + "、";
                    }
                    // 吴红亮 修改 江苏财政厅 不发送给自己和正在办理的人
                }
            }
            if (sUserList.length() > 0) {
                sUserList = sUserList.substring(0, sUserList.length() - "、".length());
            }
            request.setAttribute("labSendMsg", sUserList);
        } catch (Exception es) {
            try {
                throw es;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ShowMsg(String status, String msgStr, HttpServletRequest request) {
        // TableRow myRow;
        // TableCell myCell;
        // myRow = new TableRow();
        // myCell = new TableCell();
        // myCell.Text = "<b>" + msgStr + "</b>";
        // myRow.Cells.Add(myCell);
        // request.setAttribute("TabCaption", "<tr><td><b>" + msgStr + "</b></td></tr>");
        request.setAttribute("TabCaption", "<b>" + msgStr + "</b>");
        request.setAttribute("SEND_STATUS", status);
        // this.TabCaption.Rows.Add(myRow);
    }

}
