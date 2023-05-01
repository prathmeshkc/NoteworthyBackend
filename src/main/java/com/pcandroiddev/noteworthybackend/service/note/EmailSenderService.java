package com.pcandroiddev.noteworthybackend.service.note;

import com.pcandroiddev.noteworthybackend.model.note.ImgUrl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    @Autowired
    private JavaMailSender javaMailSender;

    public String sendEmailWithAttachments(
            String recipientEmail,
            String body,
            List<ImgUrl> imgUrls
    ) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom("pcdeveloper94@gmail.com");
            mimeMessageHelper.setTo(recipientEmail);
            mimeMessageHelper.setText(body);
            mimeMessageHelper.setSubject("A Note from pcdeveloper94@gmail.com");

            //Add images as attachments
            for (ImgUrl imageUrl : imgUrls) {
                URL url = new URL(imageUrl.getPublic_url());
                byte[] imageData = IOUtils.toByteArray(url);
                ByteArrayResource imageResource = new ByteArrayResource(imageData);
                mimeMessageHelper.addAttachment(url.getFile(), imageResource);
            }

            javaMailSender.send(mimeMessage);
            return "Email Sent!";
        } catch (MessagingException messagingException) {
            System.out.println("MessagingException");
            return null;
        } catch (IOException ioException) {
            System.out.println("IOException");
            return null;
        } catch (MailAuthenticationException mailAuthenticationException) {
            System.out.println("MailAuthenticationException " + mailAuthenticationException.getMessage());
            return null;
        } catch (MailException mailException) {
            System.out.println("MailException " + mailException.getMessage());
            return null;
        }

    }


}
