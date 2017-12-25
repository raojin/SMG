package dsoap.tools.mail;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class MailSender {
    static ThreadPoolTaskExecutor taskExecutor;
    static {
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(2);
        taskExecutor.setCorePoolSize(1);
        taskExecutor.initialize();
    }

    public static void sendMailByAsynchronousMode(final Email email) {
        // System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n###############################################################################" + taskExecutor);
        taskExecutor.execute(new Runnable() {
            public void run() {
                try {
                    MailSenderImpl sender = new MailSenderImpl();
                    sender.sendMail(email.getTo(), email.getSubject(), email.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
