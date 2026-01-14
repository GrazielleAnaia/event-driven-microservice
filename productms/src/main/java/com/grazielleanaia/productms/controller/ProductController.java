package com.grazielleanaia.productms.controller;


import com.grazielleanaia.productms.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/products")

public class ProductController {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Object> createProduct(@RequestBody CreateProductRequest product) {

        String productId = null;
        try {
            productId = productService.createProduct(product);
        } catch (Exception e) {
//            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(new Date(),
                    e.getMessage(), "/products"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("Product successfully created. " + productId);
    }

}
