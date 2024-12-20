package com.service.admin.Mail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class MailBody {

    private String mailTo;
    private String body;
    private String from;
    private String subject;
}
