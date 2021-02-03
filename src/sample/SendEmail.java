package sample;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class SendEmail {
    private            Message  message        = null;
    protected  static  String   EMAIL_FROM     = null;
    protected  static  String   FILE_PATH      = null;
    protected  static  String   REPLY_TO       = null;
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public SendEmail(final String emailTo, final String thema)
    {
        try {
            InternetAddress email_from = new InternetAddress(EMAIL_FROM);
            InternetAddress email_to   = new InternetAddress(emailTo);
            InternetAddress reply_to   = (REPLY_TO != null) ?
                    new InternetAddress(REPLY_TO) : null;
            message = new MimeMessage(Controller.session);
            message.setFrom(email_from);
            message.setRecipient(Message.RecipientType.TO, email_to);
            message.setSubject(thema);
            if (reply_to != null)
                message.setReplyTo (new Address[] {reply_to});
        } catch (AddressException e) {
            System.err.println(e.getMessage());
        } catch (MessagingException e) {
            System.err.println(e.getMessage());
        }
    }

    private MimeBodyPart createFileAttachment(String filepath)
            throws MessagingException
    {
        // Создание MimeBodyPart
        MimeBodyPart mbp = new MimeBodyPart();

        // Определение файла в качестве контента
        FileDataSource fds = new FileDataSource(filepath);
        mbp.setDataHandler(new DataHandler(fds));
        mbp.setFileName(fds.getName());
        return mbp;
    }

    public boolean sendMessage (final String text)
    {
        boolean result = false;
        try {
            // Содержимое сообщения
            Multipart mmp = new MimeMultipart();
            // Текст сообщения
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(text, "text/plain; charset=utf-8");
            mmp.addBodyPart(bodyPart);
            // Вложение файла в сообщение
            if (FILE_PATH != null) {
                MimeBodyPart mbr = createFileAttachment(FILE_PATH);
                mmp.addBodyPart(mbr);
            }
            // Определение контента сообщения
            message.setContent(mmp);
            // Отправка сообщения
            Transport.send(message);
            result = true;
        } catch (MessagingException e){
            // Ошибка отправки сообщения
            e.printStackTrace();
            //System.err.println(e.getMessage());
        }
        return result;
    }
}