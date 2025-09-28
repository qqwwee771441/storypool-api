package com.wudc.storypool.global.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LlmApiResponse {
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("message")
    private String message;
}