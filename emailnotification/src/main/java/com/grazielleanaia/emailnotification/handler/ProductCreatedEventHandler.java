package com.grazielleanaia.emailnotification.handler;

import com.grazielleanaia.core.ProductCreatedEvent;
import com.grazielleanaia.emailnotification.error.NotRetryableException;
import com.grazielleanaia.emailnotification.error.RetryableException;
import com.grazielleanaia.emailnotification.io.ProcessedEventEntity;
import com.grazielleanaia.emailnotification.io.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component

//Define methods or classes that listen to messages from Kafka topics
@KafkaListener(topics = "product-created-event-topic")
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate;
    private final ProcessedEventRepository repository;

    public ProductCreatedEventHandler(RestTemplate restTemplate, ProcessedEventRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    //Handle message type of ProductCreatedEvent
    @KafkaHandler
    @Transactional
    public void handle(@Payload ProductCreatedEvent productCreatedEvent,
                       @Header(value = "messageID", required = false) String messageID,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        LOGGER.info("Received a new event: " + productCreatedEvent.getTitle() +
                " with productID: " + productCreatedEvent.getProductId());
        String requestUrl = "http://localhost:8082/response/200";

        ProcessedEventEntity existingRecord = repository.findByMessageID(messageID);

        if (existingRecord != null) {
            LOGGER.info("Found duplicated message ID: {}", existingRecord.getMessageID());
            return;
        }

        try {
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);
            if (response.getStatusCode().value() == HttpStatus.OK.value()) {
                LOGGER.info("Received response from remote service: " + response.getBody());
            }
        } catch (ResourceAccessException e) {
            LOGGER.error(e.getMessage());
            throw new RetryableException(e);
        } catch (HttpServerErrorException e) {
            LOGGER.error(e.getMessage());
            throw new NotRetryableException(e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new NotRetryableException(e);
        }

        //Save unique messageID in the DB
        try {
            repository.save(new ProcessedEventEntity(messageID, productCreatedEvent.getProductId()));
        } catch (DataIntegrityViolationException e) {
            throw new NotRetryableException(e.getMessage());
        }

    }
}
