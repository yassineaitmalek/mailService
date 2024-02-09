package com.test.mail.exception.config;

public class ApiException extends RuntimeException {

  /**
   * @param message
   */
  public ApiException(String message) {
    super(message);

  }

  /**
   * @param message
   * @param cause
   */
  public ApiException(String message, Throwable cause) {
    super(message, cause);

  }

}
