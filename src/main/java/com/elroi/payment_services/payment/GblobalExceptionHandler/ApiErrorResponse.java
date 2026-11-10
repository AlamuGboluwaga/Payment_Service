package com.elroi.payment_services.payment.GblobalExceptionHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {
    public Integer status;
    public String error;
    public String message;

}
