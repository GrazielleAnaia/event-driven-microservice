package com.grazielleanaia.emailnotification.io;

import jakarta.persistence.*;

import java.io.Serializable;

/*Serializable is not really needed because
 * entity is not stored in an HTTP session
 * or passed across the network
 */
@Entity
@Table(name = "processed-events")
public class ProcessedEventEntity implements Serializable {

    private static final long serialVersionUID = 1545416546714546L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String messageID;

    @Column(nullable = false)
    private String productID;

    public ProcessedEventEntity() {
    }

    public ProcessedEventEntity(String messageID, String productID) {
        this.messageID = messageID;
        this.productID = productID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }
}
