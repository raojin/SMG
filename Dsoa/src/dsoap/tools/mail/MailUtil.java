package dsoap.tools.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xsf.data.DBManager;

public class MailUtil {
    static String title = "";
    static String content = "";
    static {
        String _cmdStr = "select * from SYSTEMOPTIONS where OPCLASS ='邮件提醒格式'";
        // System.out.println("-----------------------" + sql);
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            String[] temp = dt.getRows().get(0).getString("VALUE").split("&");
            title = temp[0];
            content = temp[1];
        }
    }

    public static void sendMailByAddress(final List<String> emailAddrs, final String mailTitle, final String mailConcept) {
        Email email = new Email();
        email.setSubject(mailTitle);
        email.setContent(mailConcept);
        String[] addresses = new String[emailAddrs.size()];
        for (int i = 0; i < emailAddrs.size(); i++) {
            addresses[i] = emailAddrs.get(i);
        }
        email.setTo(addresses);
        MailSender.sendMailByAsynchronousMode(email);
    }

    public static void sendMailByUserId(final List<String> userIds, final String mailTitle, final String mailConcept) {
        String _cmdStr = "select EMAIL from G_USERS where id in ({in}) and IS_MAIL_ATTENTION=1";
        String in = "";
        for (String id : userIds) {
            if (id != null && !"".equals(id)) {
                in += id + ",";
            }
        }
        if (!"".equals(in)) {
            in = in.substring(0, in.length() - 1);
        }
        _cmdStr = _cmdStr.replace("{in}", in);
        List<String> emailAddrs = new ArrayList<String>();
        String emailAddr;
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            emailAddr = dt.getRows().get(0).getString("EMAIL");
            if (emailAddr != null && !"".equals(emailAddr)) {
                emailAddrs.add(emailAddr);
            }
        }
        if (emailAddrs.size() <= 0) {
            return;
        }
        sendMailByAddress(emailAddrs, mailTitle, mailConcept);
    }

    public static void sendMailByUserId(final List<String> userIds, String infoId) {
        String _cmdStr = "select G_INFOS.BT,G_INFOS.USER_ID,G_USERS.UNAME,G_OBJS.NAME AS ObjectName,G_INFOS.MODULE_ID from G_INFOS,G_USERS,G_OBJS where G_INFOS.USER_ID = G_USERS.ID and G_OBJS.OBJCLASS = G_INFOS.OBJCLASS and G_INFOS.ID='" + infoId + "'";
        String type = "";
        String BT = "";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            type = dr.getString("ObjectName") == null ? "" : dr.getString("ObjectName");
            BT = dr.getString("BT") == null ? "" : dr.getString("BT");
        }
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
        // sdf.format(new Date());
        String mailTitle = title.replaceFirst("FILENAME", BT).replaceFirst("TYPE", type);
        String mailContent = content.replaceAll("<ENTER>", "\r\n").replaceFirst("FILENAME", BT).replaceFirst("TYPE", type);
        sendMailByUserId(userIds, mailTitle, mailContent);
    }

    public static void sendMails(Map<String, String> mails) {
        List<String> l;
        for (String key : mails.keySet()) {
            l = new ArrayList<String>();
            l.add(key);
            sendMailByUserId(l, mails.get(key));
        }
    }

    public static void main(String[] args) {
        List<String> a = new ArrayList<String>();
        a.add("jfwu@ssetest.com");
        String mailTitle = title.replaceFirst("FILENAME", "测试文件").replaceFirst("TYPE", "测试类型");
        String mailContent = content.replaceAll("<ENTER>", "\r\n").replaceFirst("FILENAME", "测试文件").replaceFirst("TYPE", "测试类型");
        MailUtil.sendMailByAddress(a, mailTitle, mailContent);
        // System.out.println("<<<< OVER >>>>");
    }
}
