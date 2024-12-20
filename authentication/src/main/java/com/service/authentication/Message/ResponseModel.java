package com.service.authentication.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter@Setter@AllArgsConstructor@NoArgsConstructor
public class ResponseModel {

    private String responseCode;
    private String responseMessage;
    private Object data;
    private HashMap<String, Object> error;
}
