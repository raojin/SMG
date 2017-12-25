package dsoap.tools.mail;

import java.util.Properties;

import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class MailSenderImpl extends BaseMailSender {
    protected MailSender getMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(stmpServer);
        sender.setUsername(userName);
        sender.setPassword(password);
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", stmpServer);
        try {
            // props.put("mail.smtp.localhost", java.net.InetAddress.getLocalHost().getHostAddress());
            // props.put("mail.smtp.localhost", "172.18.35.40");
            props.put("mail.smtp.localhost", IP.getLocalIP());
        } catch (Exception e) {
            e.printStackTrace();
        }
        sender.setJavaMailProperties(props);
        return sender;
    }
}
