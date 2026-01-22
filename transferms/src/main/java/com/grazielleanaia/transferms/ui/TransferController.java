package com.grazielleanaia.transferms.ui;


import com.grazielleanaia.transferms.model.TransferModel;
import com.grazielleanaia.transferms.service.TransferServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")

public class TransferController {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private TransferServiceImpl transferService;

    public TransferController(TransferServiceImpl transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public boolean transfer(@RequestBody TransferModel transferModel) {
        return transferService.transfer(transferModel);
    }
}
