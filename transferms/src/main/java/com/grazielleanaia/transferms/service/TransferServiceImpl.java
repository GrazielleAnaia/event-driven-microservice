package com.grazielleanaia.transferms.service;

import com.grazielleanaia.core.DepositRequestedEvent;
import com.grazielleanaia.core.WithdrawalRequestedEvent;
import com.grazielleanaia.transferms.error.TransferServiceException;
import com.grazielleanaia.transferms.io.TransferEntity;
import com.grazielleanaia.transferms.io.TransferRepository;
import com.grazielleanaia.transferms.model.TransferModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private Environment environment;
    private RestTemplate restTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private TransferRepository transferRepository;

    public TransferServiceImpl(KafkaTemplate<String, Object> kafkaTemplate,
                               Environment environment,
                               RestTemplate restTemplate,
                               TransferRepository transferRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.environment = environment;
        this.restTemplate = restTemplate;
        this.transferRepository = transferRepository;
    }

    @Transactional("transactionManager")
    @Override
    public boolean transfer(TransferModel transferModel) {
        WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(transferModel.getSenderId(), transferModel.getRecipientId(), transferModel.getAmount());
        DepositRequestedEvent depositEvent = new DepositRequestedEvent(transferModel.getSenderId(), transferModel.getSenderId(), transferModel.getAmount());

        TransferEntity transferEntity = new TransferEntity();
        BeanUtils.copyProperties(depositEvent, transferEntity);
        transferEntity.setTransferId(UUID.randomUUID().toString());
        transferRepository.save(transferEntity);

        //Uses kafka template to produce 2 kafka messages
        //Send them in a single transaction
        try {
            kafkaTemplate.send(environment.getProperty("withdraw-money-topic", "withdraw-money-topic"), withdrawalEvent);
            LOGGER.info("Sent event to withdrawal topic");

            //Business logic that causes error
            callRemoteService();

            kafkaTemplate.send(environment.getProperty("deposit-money-topic", "deposit-money-topic"), depositEvent);
            LOGGER.info("Sent event to deposit topic");

        } catch (Exception e) {
            throw new TransferServiceException(e.getMessage());
        }
        return true;

    }


    private ResponseEntity<String> callRemoteService() throws Exception {
        String requestUrl = "http://localhost:8082/response/200";
        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);

        if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            throw new Exception("Destination microservice is not available");
        }

        if (response.getStatusCode().value() == HttpStatus.OK.value()) {
            LOGGER.info("Received response from mock service: " + response.getBody());
        }
        return response;
    }
}
