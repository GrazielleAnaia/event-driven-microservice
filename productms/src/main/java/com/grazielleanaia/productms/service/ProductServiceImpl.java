package com.grazielleanaia.productms.service;

import com.grazielleanaia.core.ProductCreatedEvent;
import com.grazielleanaia.productms.controller.CreateProductRequest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductRequest product) throws Exception {

        String productId = UUID.randomUUID().toString();

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId,
                product.getTitle(), product.getPrice(), product.getQuantity());
        LOGGER.info("Before publishing a ProductCreatedEvent");

        ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>("product-created-event-topic", productId,
                productCreatedEvent);
        record.headers().add("messageID", UUID.randomUUID().toString().getBytes());

        //Send message synchronously
        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(record).get();

        LOGGER.info("Partition: " + result.getRecordMetadata().partition());
        LOGGER.info("Topic name: " + result.getRecordMetadata().topic());
        LOGGER.info("Offset: " + result.getRecordMetadata().offset());
        LOGGER.info("****** Returning product ID");
        return productId;
    }
}
