package org.qbynet.chat.entity.dto;

import lombok.Data;

@Data
public class FetchMessageDTO {
    private String conversation;
    private String since;
    private Integer page;
    private Integer size;
}

