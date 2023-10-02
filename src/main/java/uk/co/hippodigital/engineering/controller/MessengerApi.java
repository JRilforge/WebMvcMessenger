package uk.co.hippodigital.engineering.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uk.co.hippodigital.engineering.domain.UserMessage;
import uk.co.hippodigital.engineering.dto.MessageDto;
import uk.co.hippodigital.engineering.dto.MessageRequest;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@AllArgsConstructor
@RestController
public class MessengerApi {

  private final MongoOperations mongoOperations;

  @PostMapping("/send-message")
  public MessageDto sendMessage(@RequestBody MessageRequest messageRequest) {
    if (Objects.equals(messageRequest.fromUserId(), messageRequest.toUserId())) {
      throw new RuntimeException("fromUserId can't equal toUserId");
    }

    var userMessage = new UserMessage();
    userMessage.setFromUserId(messageRequest.fromUserId());
    userMessage.setToUserId(messageRequest.toUserId());
    userMessage.setContent(messageRequest.content());
    userMessage.setCreated(new Date());

    return toMessageDto(mongoOperations.insert(userMessage));
  }

  @GetMapping("/messages-between")
  public List<MessageDto> getMessagesBetween(@RequestParam("a") String aUserId, @RequestParam("b") String bUserId) {
    if (Objects.equals(aUserId, bUserId)) {
      throw new RuntimeException("'a' can't equal 'b'");
    }

    var users = List.of(aUserId, bUserId);

    return mongoOperations.find(new Query(Criteria.where("toUserId").in(users)
                    .and("fromUserId").in(users)), UserMessage.class).stream()
            .map(this::toMessageDto).toList();
  }

  private MessageDto toMessageDto(ChangeStreamEvent<UserMessage> event) {
    return toMessageDto(Objects.requireNonNull(event.getBody()));
  }

  private MessageDto toMessageDto(UserMessage userMsg) {
    return new MessageDto(userMsg.getId().toHexString(), userMsg.getFromUserId(),
            userMsg.getToUserId(), userMsg.getContent(), userMsg.getCreated().getTime());
  }

  private MessageDto toMessageDto(Document userMsg) {
    return new MessageDto(userMsg.getObjectId("_id").toHexString(), userMsg.get("fromUserId").toString(),
            userMsg.get("toUserId").toString(), userMsg.get("content").toString(), userMsg.getDate("created").getTime());
  }
}
