package org.qbynet.chat.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
public class Report {
    @Id
    private String id;

    @DBRef
    private User user;
    @DBRef
    private Message message;

    private String additionMessage;
}
