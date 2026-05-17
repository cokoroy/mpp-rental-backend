package com.mpp.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillResponse {

    private String billCode;
    private String paymentUrl;   // full redirect URL → https://dev.toyyibpay.com/{billCode}
    private Integer paymentId;
    private String paymentAmount;
}