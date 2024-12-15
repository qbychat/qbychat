package org.qbynet.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramRestBean<T> {
    private boolean ok;
    private T result = null;
    @JsonProperty("error_code")
    private int errorCode = 0;
    private String description = null;
}
