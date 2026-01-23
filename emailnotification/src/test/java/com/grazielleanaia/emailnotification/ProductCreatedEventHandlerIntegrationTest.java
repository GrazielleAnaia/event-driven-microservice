package com.grazielleanaia.emailnotification;

import com.grazielleanaia.core.ProductCreatedEvent;
import com.grazielleanaia.emailnotification.handler.ProductCreatedEventHandler;
import com.grazielleanaia.emailnotification.io.ProcessedEventEntity;
import com.grazielleanaia.emailnotification.io.ProcessedEventRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.verification.VerificationMode;
import org.mockito.verification.VerificationWithTimeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//@ContextConfiguration(classes = ProductCreatedEventHandler.class)
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka

public class ProductCreatedEventHandlerIntegrationTest {

    @MockitoBean
    ProcessedEventRepository repository;
    @MockitoBean
    RestTemplate restTemplate;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    ProductCreatedEventHandler productCreatedEventHandler;

    @Test
    public void testProductCreatedEventHandler_OnProductCreated_HandlesEvent() throws ExecutionException, InterruptedException {

        //Arrange
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        productCreatedEvent.setPrice(new BigDecimal(1000));
        productCreatedEvent.setTitle("Test Product");
        productCreatedEvent.setQuantity(10);
        productCreatedEvent.setProductId(UUID.randomUUID().toString());

        String messageId = UUID.randomUUID().toString();
        String messageKey = productCreatedEvent.getProductId();

        ProducerRecord<String, Object> record = new ProducerRecord<>("product-created-event-topic", messageKey,
                productCreatedEvent);
        record.headers().add("messageID", messageId.getBytes());
        record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        ProcessedEventEntity processedEventEntity = new ProcessedEventEntity();
        when(repository.findByMessageID(anyString())).thenReturn(processedEventEntity);
        when(repository.save(any(ProcessedEventEntity.class))).thenReturn(null);

        String responseBody = "{\"key\":\"value\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), isNull(), eq(String.class))).thenReturn(responseEntity);

        //Act
        kafkaTemplate.send(record).get();

        //Assert
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductCreatedEvent> productCreatedEvenCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(productCreatedEventHandler, timeout(5000).times(1)).handle(productCreatedEvenCaptor.capture(),
                messageIdCaptor.capture(), messageKeyCaptor.capture());
        assertEquals(productCreatedEvent, productCreatedEvenCaptor.getValue());
        assertEquals(messageId, messageIdCaptor.getValue());
        assertEquals(messageKey, messageKeyCaptor.getValue());

    }
}
