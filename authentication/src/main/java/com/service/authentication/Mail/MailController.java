package com.service.authentication.Mail;

import com.service.authentication.Message.ResponseModel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("6am/mail/api/")
public class MailController {

    @Autowired
    private JavaMailSender mailSender;




    @PostMapping("sendMail")
    public ResponseEntity<ResponseModel> send(@RequestBody MailBody mailBody){
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom("evento17@outlook.com");
            helper.setTo(mailBody.getMailTo());
            helper.setSubject(mailBody.getSubject());
            helper.setText(mailBody.getBody(), true);

            mailSender.send(message);
            return new ResponseEntity<>(new ResponseModel("00", "SUCCESS", null, null), HttpStatus.OK);
        } catch (MessagingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ResponseModel("96", "FAILED", null, null), HttpStatus.BAD_REQUEST);
        }

    }


}
