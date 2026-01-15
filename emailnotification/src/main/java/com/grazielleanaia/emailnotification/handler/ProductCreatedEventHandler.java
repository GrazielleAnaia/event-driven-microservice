package com.grazielleanaia.emailnotification.handler;

import com.grazielleanaia.core.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component

//Define methods or classes that listen to messages from Kafka topics
@KafkaListener(topics = "product-created-event-topic")
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    //Handle specific message types
    @KafkaHandler
    public void handle(ProductCreatedEvent productCreatedEvent) {
        LOGGER.info("Received a new event: " + productCreatedEvent.getTitle());

    }


}
