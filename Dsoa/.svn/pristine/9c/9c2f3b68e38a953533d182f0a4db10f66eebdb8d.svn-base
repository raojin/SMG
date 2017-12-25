package dsoap.tools.mail;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import xsf.data.DBManager;

public abstract class BaseMailSender {
    static String userName;
    static String password;
    static String stmpServer;
    static String senderEmailAddr;
    static {
        String _cmdStr = "";
        String s = null;
        _cmdStr = "select * from SYSTEMOPTIONS where OPCLASS ='邮件'";
        // System.out.println("-----------------------"+sql);
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            s = dt.getRows().get(0).getString("VALUE");
            stmpServer = s.split(",")[0];
            userName = s.split(",")[1];
            password = s.split(",")[2];
            // isAuth = s.split(",")[3];
            senderEmailAddr = s.split(",")[4];
        }
    }

    public final void sendMail(String[] to, String subject, String concept) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(senderEmailAddr);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(concept);
        MailSender sender = getMailSender();
        sender.send(msg);
    }

    protected abstract MailSender getMailSender();
}
