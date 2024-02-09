package com.test.mail.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.mail.dto.MailDTO;
import com.test.mail.service.EmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public void sendMailWithFiles(@ModelAttribute MailDTO mail) {
        emailService.sendMail(mail);
    }

}
