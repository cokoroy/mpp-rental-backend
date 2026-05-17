package com.mpp.rental.service;

import com.mpp.rental.dto.CreateBillResponse;
import com.mpp.rental.exception.ApplicationException;
import com.mpp.rental.exception.ResourceNotFoundException;
import com.mpp.rental.model.FacilityApplication;
import com.mpp.rental.model.Payment;
import com.mpp.rental.repository.FacilityApplicationRepository;
import com.mpp.rental.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FacilityApplicationRepository applicationRepository;

    @Value("${toyyibpay.secret-key}")
    private String secretKey;

    @Value("${toyyibpay.category-code}")
    private String categoryCode;

    @Value("${toyyibpay.base-url}")
    private String toyyibPayBaseUrl;

    @Value("${toyyibpay.callback-url}")
    private String callbackUrl;

    @Value("${toyyibpay.return-url}")
    private String returnUrl;

    @Value("${toyyibpay.cancel-url}")
    private String cancelUrl;

    // ==================== CREATE BILL ====================

    @Transactional
    public CreateBillResponse createBill(Integer applicationId) {

        Payment payment = paymentRepository.findByApplication_ApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment record not found for application ID: " + applicationId));

        if (payment.getPaymentStatus() != Payment.PaymentStatus.UNPAID) {
            throw new ApplicationException("Payment has already been processed for this application");
        }

        FacilityApplication app = payment.getApplication();
        String eventName    = app.getEventFacility().getEvent().getEventName();
        String facilityName = app.getEventFacility().getFacility().getFacilityName();
        String businessName = app.getBusiness().getBusinessName();
        String ownerName    = app.getBusiness().getUser().getUserName();
        String ownerEmail   = app.getBusiness().getUser().getUserEmail();
        String ownerPhone   = app.getBusiness().getUser().getUserPhoneNumber();

        // ToyyibPay uses sen (cents) — multiply RM by 100
        int amountInSen = payment.getPaymentAmount()
                .multiply(new java.math.BigDecimal("100"))
                .intValue();

        String billName = eventName;
        if (billName.length() > 30) billName = billName.substring(0, 27) + "...";

        String billDescription = facilityName + " | " + businessName;
        if (billDescription.length() > 100) billDescription = billDescription.substring(0, 97) + "...";

        // Order ID used to identify payment in callback: PAY-{paymentId}-{applicationId}
        String orderId = "PAY-" + payment.getPaymentId() + "-" + applicationId;

        String billCode = callToyyibPayCreateBill(
                billName, billDescription, amountInSen,
                orderId, ownerName, ownerEmail, ownerPhone
        );

        String paymentUrl = toyyibPayBaseUrl + "/" + billCode;
        log.info("ToyyibPay bill created: billCode={}, url={}", billCode, paymentUrl);

        return new CreateBillResponse(
                billCode,
                paymentUrl,
                payment.getPaymentId(),
                payment.getPaymentAmount().toString()
        );
    }

    // ==================== CALLBACK HANDLER ====================

    @Transactional
    public void handleCallback(String billCode, String orderId, String statusId, String transactionId) {
        log.info("Processing callback: billCode={}, orderId={}, statusId={}", billCode, orderId, statusId);

        if (orderId == null || orderId.isEmpty()) {
            log.warn("Callback received with empty orderId");
            return;
        }

        Integer paymentId = parsePaymentIdFromOrderId(orderId);
        if (paymentId == null) {
            log.warn("Could not parse paymentId from orderId: {}", orderId);
            return;
        }

        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment == null) {
            log.warn("Payment not found for paymentId: {}", paymentId);
            return;
        }

        if ("1".equals(statusId)) {
            payment.setPaymentStatus(Payment.PaymentStatus.PAID);
            log.info("Payment {} marked as PAID (transactionId: {})", paymentId, transactionId);
        } else if ("3".equals(statusId)) {
            payment.setPaymentStatus(Payment.PaymentStatus.UNPAID);
            log.info("Payment {} marked as FAILED", paymentId);
        } else {
            log.info("Payment {} status is PENDING (status_id={}), no update", paymentId, statusId);
            return;
        }

        paymentRepository.save(payment);
    }

    // ==================== TOYYIBPAY API CALL ====================

    /**
     * Calls ToyyibPay createBill API using Java's built-in HttpClient.
     *
     * Why not RestTemplate / WebClient?
     * ToyyibPay returns JSON with Content-Type: text/html — all Spring
     * HTTP message converters in 4.x either reject it or are deprecated.
     * Java's HttpClient reads the raw String response and we parse manually.
     */
    private String callToyyibPayCreateBill(
            String billName,
            String billDescription,
            int amountInSen,
            String orderId,
            String buyerName,
            String buyerEmail,
            String buyerPhone) {

        // Build form-encoded body manually
        Map<String, String> params = new LinkedHashMap<>();
        params.put("userSecretKey",           secretKey);
        params.put("categoryCode",            categoryCode);
        params.put("billName",                billName);
        params.put("billDescription",         billDescription);
        params.put("billPriceSetting",        "1");
        params.put("billPayorInfo",           "1");
        params.put("billAmount",              String.valueOf(amountInSen));
        params.put("billReturnUrl",           returnUrl);
        params.put("billCallbackUrl",         callbackUrl);
        params.put("billExternalReferenceNo", orderId);
        params.put("billTo",                  buyerName);
        params.put("billEmail",               buyerEmail);
        params.put("billPhone",               buyerPhone);
        params.put("billSplitPayment",        "0");
        params.put("billSplitPaymentArgs",    "");
        params.put("billPaymentChannel",      "0");
        params.put("billContentEmail",        "Thank you for your payment to MPP Business Rental.");
        params.put("billChargeToCustomer",    "1");

        String formBody = params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(toyyibPayBaseUrl + "/index.php/api/createBill"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            log.info("ToyyibPay raw response: {}", responseBody);

            // Response looks like: [{"BillCode":"abc123"}]
            // Parse BillCode manually — no Jackson needed
            String billCode = extractBillCode(responseBody);
            if (billCode == null || billCode.isEmpty()) {
                throw new RuntimeException("BillCode not found in response: " + responseBody);
            }

            return billCode;

        } catch (Exception e) {
            log.error("ToyyibPay createBill failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create ToyyibPay bill: " + e.getMessage());
        }
    }

    // ==================== HELPERS ====================

    /**
     * Extract BillCode from ToyyibPay JSON string without Jackson.
     * Response format: [{"BillCode":"abc123"}]
     * or error: [{"Error":"..."}]
     */
    private String extractBillCode(String json) {
        if (json == null || json.isBlank()) return null;

        // Look for "BillCode":"VALUE"
        String key = "\"BillCode\":\"";
        int start = json.indexOf(key);
        if (start == -1) {
            // Check for error message
            String errorKey = "\"Error\":\"";
            int errorStart = json.indexOf(errorKey);
            if (errorStart != -1) {
                int valueStart = errorStart + errorKey.length();
                int valueEnd = json.indexOf("\"", valueStart);
                String errorMsg = json.substring(valueStart, valueEnd);
                throw new RuntimeException("ToyyibPay error: " + errorMsg);
            }
            return null;
        }

        int valueStart = start + key.length();
        int valueEnd = json.indexOf("\"", valueStart);
        if (valueEnd == -1) return null;

        return json.substring(valueStart, valueEnd);
    }

    /**
     * Parse paymentId from orderId format: PAY-{paymentId}-{applicationId}
     */
    private Integer parsePaymentIdFromOrderId(String orderId) {
        try {
            String[] parts = orderId.split("-");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse paymentId from orderId: {}", orderId);
        }
        return null;
    }
}