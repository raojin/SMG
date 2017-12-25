package dsoap.tools;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import xsf.data.DBManager;

public class SendMail {
    public static String mailServer;
    public static String sysUserName;
    public static String sysUserPass;
    public static String isAuth;
    public static String from;
    static {
        String _cmdStr = "";
        String s = null;
        _cmdStr = "select * from SYSTEMOPTIONS where OPCLASS ='邮件'";
        xsf.data.DataTable dt = DBManager.getDataTable(_cmdStr);
        if (dt.getRows().size() > 0) {
            s = dt.getRows().get(0).getString("VALUE");
            mailServer = s.split(",")[0];
            sysUserName = s.split(",")[1];
            sysUserPass = s.split(",")[2];
            isAuth = s.split(",")[3];
            from = s.split(",")[4];
        }
    }

    @SuppressWarnings("static-access")
    public static boolean send(String mailTo, String mailSubject, String mailBody, String serverMsn) {
        try {
            String smtpServer = mailServer;
            String smtpAuth = isAuth;
            String smtpUser = sysUserName;
            String smtpPassword = sysUserPass;
            String From = from;
            String To = mailTo;
            String Subject = mailSubject;
            String Text = mailBody;
            if (serverMsn != null && !serverMsn.equals("")) {
                String[] serverMsns = serverMsn.split(",");
                smtpServer = serverMsns[0];
                smtpUser = serverMsns[1];
                smtpPassword = serverMsns[2];
                smtpAuth = serverMsns[3];
                From = serverMsns[4];
            }
            Properties props = new Properties();
            Session sendMailSession;
            Transport transport;
            props.put("mail.smtp.host", smtpServer);
            props.put("mail.smtp.auth", smtpAuth);
            props.put("mail.smtp.localhost", java.net.InetAddress.getLocalHost().getHostAddress());
            if ("true".equals(smtpAuth)) {
                SimpleAuthenticator auth = new SimpleAuthenticator(smtpUser, smtpPassword);
                sendMailSession = Session.getInstance(props, auth);
            } else {
                sendMailSession = Session.getInstance(props);
            }
            sendMailSession.setDebug(true);
            MimeMessage newMessage = new MimeMessage(sendMailSession);
            newMessage.setFrom(new InternetAddress(From));

            // 以html格式发送email
            String al[] = { "gb2312" };
            Multipart mp = htmlMail(Text);
            newMessage.setContent(mp);
            newMessage.setContentLanguage(al);
            newMessage.setContent(Text, "text/html; charset=gb2312");

            // 设置收件人,并设置其接收类型为TO,还有3种预定义类型如下：
            newMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(To));
            newMessage.setSubject(Subject);
            newMessage.setSentDate(new Date());

            // 以text格式发送email
            newMessage.saveChanges();// 存储邮件信息
            transport = sendMailSession.getTransport("smtp");
            transport.send(newMessage, newMessage.getAllRecipients());
            transport.close();
        } catch (Exception mailEx) {
            mailEx.printStackTrace();
            return false;
        }
        return true;
    }

    public static Multipart htmlMail(String sb) {
        MimeBodyPart mbp1 = new MimeBodyPart();
        Multipart mp = new MimeMultipart();
        String al[] = { "gb2312" };
        try {
            mbp1.setContentLanguage(al);
            mbp1.setContent(sb, "text/html; charset=gb2312");
            mp.addBodyPart(mbp1);
        } catch (MessagingException e) {
            e.printStackTrace();
            return null;
        }
        return mp;
    }

}
