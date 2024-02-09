package com.test.mail.service;

import java.io.IOException;
import java.util.Objects;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.test.mail.dto.MailDTO;
import com.test.mail.utils.Utils;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String from;

    private final Session session;

    public void sendMail(MailDTO mail) {

        Try.run(() -> sendMailImpl(mail)).onFailure(this::onFailure);

    }

    public void sendMailImpl(MailDTO mail) throws MessagingException {

        MimeMessage message = new MimeMessage(session);
        message.setFrom(from);
        message.setSubject(mail.getSubject());
        Multipart multipart = new MimeMultipart();
        // add the text part
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(mail.getBody(), "text/html; charset=utf-8");
        multipart.addBodyPart(textBodyPart);

        // add the attachment part

        Utils.checkStream(mail.getFiles()).filter(Objects::nonNull)
                .forEach(file -> Try.run(() -> sendFile(file, multipart)).onFailure(this::onFailure));
        message.setContent(multipart);

        // add TO and CC
        Utils.checkStream(mail.getTo()).distinct()
                .forEach(dest -> Try.run(() -> message.addRecipients(RecipientType.TO, dest))
                        .onFailure(this::onFailure));
        Utils.checkStream(mail.getCopy()).distinct()
                .forEach(dest -> Try.run(() -> message.addRecipients(RecipientType.CC, dest))
                        .onFailure(this::onFailure));

        Transport.send(message);

    }

    public void sendFile(MultipartFile file, Multipart multipart) throws MessagingException, IOException {

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        DataSource ds = new ByteArrayDataSource(file.getBytes(), file.getContentType());
        attachmentBodyPart.setDataHandler(new DataHandler(ds));
        attachmentBodyPart.setFileName(file.getOriginalFilename());
        attachmentBodyPart.setDisposition(Part.ATTACHMENT);
        multipart.addBodyPart(attachmentBodyPart);

    }

    public void onFailure(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
    }

}
