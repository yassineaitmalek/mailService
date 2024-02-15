package com.test.mail.dto;

import java.io.File;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailDTO {

  private List<String> to;

  private List<String> copy;

  private String subject;

  private String body;

  private List<MultipartFile> uploadedFiles;

  private List<File> localFiles;

}
