package com.test.mail.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@With
@Data
// @Entity
@Immutable
@Builder
@Table(name = "audit4j")
@NoArgsConstructor
@AllArgsConstructor
public class Audit4jDB {

  @Id
  @Column(name = "id")
  private String id;

  @Column(columnDefinition = "TEXT")
  private String elements;

  @Column
  private String origin;

  @Column
  private String actor;

  @Column
  private String action;

  @Column(columnDefinition = "TIMESTAMP")
  private Date timestamp;

  public static final List<String> header() {
    return Arrays.asList("actor", "origin", "action", "elements", "time");
  }

}
