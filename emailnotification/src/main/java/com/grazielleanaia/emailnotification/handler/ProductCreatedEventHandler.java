package com.grazielleanaia.emailnotification.handler;

import com.grazielleanaia.core.ProductCreatedEvent;
import com.grazielleanaia.emailnotification.error.NotRetryableException;
import com.grazielleanaia.emailnotification.error.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component

//Define methods or classes that listen to messages from Kafka topics
@KafkaListener(topics = "product-created-event-topic")
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private RestTemplate restTemplate;

    public ProductCreatedEventHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //Handle specific message types
    @KafkaHandler
    public void handle(ProductCreatedEvent productCreatedEvent) {
//        if (true) throw new NotRetryableException("Error occurred. No need to consume message again.");
        LOGGER.info("Received a new event: " + productCreatedEvent.getTitle());
        String requestUrl = "http://localhost:8083/response/200";

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
    }
}
