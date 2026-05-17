package com.mpp.rental.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Custom MultipartResolver configuration.
 *
 * Problem: ToyyibPay sends the payment callback as multipart/form-data
 * with 12 fields. Spring's default MultipartResolver tries to parse it
 * but Tomcat's maxPartCount (default: 10) causes a 413 / 500 error.
 *
 * Fix: Override the MultipartResolver bean to skip multipart parsing
 * for the /api/payment/callback endpoint. The controller reads the
 * raw InputStream directly instead.
 */
@Configuration
public class MultipartConfig {

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver() {
            @Override
            public boolean isMultipart(HttpServletRequest request) {
                String path = request.getRequestURI();

                // Skip multipart parsing for ToyyibPay callback —
                // the controller reads raw InputStream directly
                if (path != null && path.contains("/api/payment/callback")) {
                    return false;
                }

                // Use default behaviour for all other requests
                return super.isMultipart(request);
            }
        };
    }
}