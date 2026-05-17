package com.mpp.rental.controller;

import com.mpp.rental.dto.ApiResponse;
import com.mpp.rental.dto.CreateBillResponse;
import com.mpp.rental.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a ToyyibPay bill for an approved application
     * POST /api/payment/create-bill?applicationId=5
     */
    @PostMapping("/create-bill")
    public ResponseEntity<ApiResponse<CreateBillResponse>> createBill(
            @RequestParam Integer applicationId) {
        try {
            CreateBillResponse response = paymentService.createBill(applicationId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bill created successfully", response));
        } catch (Exception e) {
            log.error("Failed to create bill for application {}: {}", applicationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * ToyyibPay callback — called by ToyyibPay server after payment
     * POST /api/payment/callback
     *
     * ToyyibPay sends multipart/form-data with ~12 fields.
     * We read the raw InputStream directly and parse it ourselves —
     * this completely bypasses Tomcat's multipart parser (no 413 risk).
     *
     * status_id: 1 = success, 2 = pending, 3 = failed
     */
    @PostMapping("/callback")
    public ResponseEntity<String> paymentCallback(HttpServletRequest request) {
        try {
            // Read raw body from InputStream — works for any content type
            // and never triggers Tomcat's multipart file-count limit
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            String rawBody = sb.toString();
            log.info("ToyyibPay callback raw body:\n{}", rawBody);

            String contentType = request.getContentType();
            log.info("ToyyibPay callback content-type: {}", contentType);

            Map<String, String> params = new HashMap<>();

            if (contentType != null && contentType.contains("multipart/form-data")) {
                // Extract boundary from content-type header
                // e.g. multipart/form-data; boundary=----abc123
                String boundary = null;
                for (String part : contentType.split(";")) {
                    part = part.trim();
                    if (part.startsWith("boundary=")) {
                        boundary = part.substring("boundary=".length()).trim();
                        break;
                    }
                }

                if (boundary != null) {
                    params = parseMultipart(rawBody, boundary);
                } else {
                    log.warn("Could not find boundary in content-type: {}", contentType);
                }
            } else {
                // application/x-www-form-urlencoded fallback
                params = parseUrlEncoded(rawBody.trim());
            }

            log.info("ToyyibPay parsed params: {}", params);

            String billCode      = params.get("billcode");
            String orderId       = params.get("order_id");
            String statusId      = params.get("status_id");
            String transactionId = params.get("transaction_id");

            log.info("Callback — billCode={}, orderId={}, statusId={}, transactionId={}",
                    billCode, orderId, statusId, transactionId);

            paymentService.handleCallback(billCode, orderId, statusId, transactionId);

        } catch (Exception e) {
            log.error("Error handling ToyyibPay callback: {}", e.getMessage(), e);
        }

        // Always return 200 OK — ToyyibPay expects "ok"
        return ResponseEntity.ok("ok");
    }

    // ==================== PARSE HELPERS ====================

    /**
     * Parse multipart/form-data body manually.
     * Extracts name/value pairs from each part.
     *
     * ToyyibPay format:
     * --boundary\r\n
     * Content-Disposition: form-data; name="field_name"\r\n
     * \r\n
     * field_value\r\n
     * --boundary--\r\n
     */
    private Map<String, String> parseMultipart(String body, String boundary) {
        Map<String, String> result = new HashMap<>();

        // Pattern to extract name and value from each part
        // name="xxx" followed by blank line then value
        Pattern namePattern = Pattern.compile(
                "Content-Disposition:[^\n]*name=\"([^\"]+)\"[^\n]*\r?\n(?:[^\n]*\r?\n)*?\r?\n(.*?)(?:\r?\n--)",
                Pattern.DOTALL
        );

        Matcher matcher = namePattern.matcher(body);
        while (matcher.find()) {
            String name  = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            result.put(name, value);
            log.debug("Parsed multipart field: {} = {}", name, value);
        }

        return result;
    }

    /**
     * Parse application/x-www-form-urlencoded body.
     * key=value&key2=value2
     */
    private Map<String, String> parseUrlEncoded(String body) {
        Map<String, String> result = new HashMap<>();
        if (body == null || body.isBlank()) return result;

        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    String key   = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    result.put(key, value);
                } catch (Exception e) {
                    log.warn("Could not decode param pair: {}", pair);
                }
            }
        }
        return result;
    }
}