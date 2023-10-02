package uk.co.hippodigital.engineering.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter @Setter
@Document("messages")
public class UserMessage {
  @Id
  @JsonAlias("_id")
  private ObjectId id;

  @Indexed
  private String fromUserId;

  @Indexed
  private String toUserId;

  private String content;

  private Date created;
}
