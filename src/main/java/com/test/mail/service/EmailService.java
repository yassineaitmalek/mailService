package com.test.mail.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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

import com.test.mail.constants.Constants;
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
        Multipart multipart = new MimeMultipart();

        // add From
        message.setFrom(from);

        // add Subject
        Try.of(() -> mail).filter(Objects::nonNull).map(MailDTO::getSubject).mapTry(e -> addSubject(e, message))
                .onFailure(this::onFailure);

        // add the text part
        Try.of(() -> mail).filter(Objects::nonNull).map(MailDTO::getBody).mapTry(e -> sendText(e, multipart, message))
                .onFailure(this::onFailure);

        // add the attachments part
        Try.of(() -> mail).filter(Objects::nonNull).map(MailDTO::getLocalFiles)
                .mapTry(e -> sendFiles(e, multipart, message))
                .onFailure(this::onFailure);

        Try.of(() -> mail).filter(Objects::nonNull).map(MailDTO::getUploadedFiles)
                .mapTry(e -> sendMulipartFiles(e, multipart, message))
                .onFailure(this::onFailure);

        // add TO
        Try.of(() -> mail).filter(Objects::nonNull).map(MailDTO::getTo).getOrElse(new ArrayList<>())
                .stream().distinct()
                .forEach(dest -> Try.run(() -> message.addRecipients(RecipientType.TO, dest))
                        .onFailure(this::onFailure));

        // add CC
        Try.of(() -> mail).filter(Objects::nonNull).map(MailDTO::getCopy).getOrElse(new ArrayList<>())
                .stream().distinct()
                .forEach(dest -> Try.run(() -> message.addRecipients(RecipientType.TO, dest))
                        .onFailure(this::onFailure));

        // send Message
        Transport.send(message);

    }

    public MimeMessage addSubject(String subject, MimeMessage message)
            throws MessagingException {

        message.setSubject(subject);
        return message;

    }

    public MimeMessage sendText(String text, Multipart multipart, MimeMessage message)
            throws MessagingException {

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(text, "text/html; charset=utf-8");
        multipart.addBodyPart(textBodyPart);
        return message;

    }

    public MimeMessage sendMulipartFiles(List<MultipartFile> files, Multipart multipart, MimeMessage message)
            throws MessagingException, IOException {

        if (Utils.checkStream(files).filter(Objects::nonNull).map(MultipartFile::getSize)
                .reduce(0l, (a, b) -> a + b) >= Constants.EMAIL_LIMIT_SIZE) {
            log.error("sending an multiple files greater or equal 20 MB");
            return message;
        }

        Utils.checkStream(files).filter(Objects::nonNull)
                .forEach(file -> Try.run(() -> sendFile(file, multipart)).onFailure(this::onFailure));
        message.setContent(multipart);
        return message;
    }

    public MimeMessage sendFiles(List<File> files, Multipart multipart, MimeMessage message)
            throws MessagingException, IOException {

        if (Utils.checkStream(files).filter(Objects::nonNull).map(File::length)
                .reduce(0l, (a, b) -> a + b) >= Constants.EMAIL_LIMIT_SIZE) {
            log.error("sending an multiple files greater or equal 20 MB");
            return message;
        }

        Utils.checkStream(files).filter(Objects::nonNull)
                .forEach(file -> Try.run(() -> sendFile(file, multipart)).onFailure(this::onFailure));
        message.setContent(multipart);
        return message;
    }

    public Multipart sendFile(MultipartFile file, Multipart multipart) throws MessagingException, IOException {

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        DataSource ds = new ByteArrayDataSource(file.getBytes(), file.getContentType());
        attachmentBodyPart.setDataHandler(new DataHandler(ds));
        attachmentBodyPart.setFileName(file.getOriginalFilename());
        attachmentBodyPart.setDisposition(Part.ATTACHMENT);
        multipart.addBodyPart(attachmentBodyPart);
        return multipart;

    }

    public Multipart sendFile(File file, Multipart multipart) throws MessagingException {

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
        attachmentBodyPart.setFileName(file.getName());
        attachmentBodyPart.setDisposition(Part.ATTACHMENT);
        multipart.addBodyPart(attachmentBodyPart);
        return multipart;

    }

    public void onFailure(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
    }

}
