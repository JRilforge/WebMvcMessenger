package uk.co.hippodigital.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MessageDto {

  private String id;

  private String fromUserId;

  private String toUserId;

  private String content;

  private long created;
}
