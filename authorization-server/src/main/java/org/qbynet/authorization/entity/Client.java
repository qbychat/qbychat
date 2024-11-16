package org.qbynet.authorization.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

@Data
@Document
public class Client {
    @Id
    private String id;

    @DBRef
    private Account owner;
    private RegisteredClient origin;
}
