package com.test.mail.exception.config;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExceptionResponse {

  private String message;

  @JsonIgnore
  private Throwable throwable;

  private HttpStatus httpStatus;

  @Builder.Default
  private ZonedDateTime timestamp = ZonedDateTime.now(ZoneId.of("Z"));

}
