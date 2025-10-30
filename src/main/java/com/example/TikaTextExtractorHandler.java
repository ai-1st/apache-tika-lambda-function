package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TikaTextExtractorHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(TikaTextExtractorHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Tika tika = new Tika();
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        
        try {
            // Set CORS headers
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
            headers.put("Access-Control-Allow-Methods", "POST,OPTIONS");
            response.setHeaders(headers);
            
            // Handle preflight OPTIONS request
            if ("OPTIONS".equals(input.getHttpMethod())) {
                response.setStatusCode(200);
                response.setBody("{}");
                return response;
            }
            
            // Parse request body
            String requestBody = input.getBody();
            if (requestBody == null || requestBody.trim().isEmpty()) {
                return createErrorResponse(response, 400, "Request body is required");
            }
            
            JsonNode requestJson = objectMapper.readTree(requestBody);
            String url = requestJson.get("url").asText();
            
            if (url == null || url.trim().isEmpty()) {
                return createErrorResponse(response, 400, "URL is required in request body");
            }
            
            logger.info("Processing URL: {}", url);
            
            // Download content from URL
            byte[] content = downloadContent(url);
            if (content == null) {
                return createErrorResponse(response, 400, "Failed to download content from URL");
            }
            
            // Extract text using Apache Tika
            String extractedText = extractText(content);
            
            // Create response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("url", url);
            responseBody.put("text", extractedText);
            responseBody.put("contentLength", content.length);
            responseBody.put("success", true);
            
            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(responseBody));
            
            logger.info("Successfully processed URL: {}, extracted text length: {}", url, extractedText.length());
            
        } catch (Exception e) {
            logger.error("Error processing request", e);
            return createErrorResponse(response, 500, "Internal server error: " + e.getMessage());
        }
        
        return response;
    }
    
    private byte[] downloadContent(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent", "Apache Tika Lambda Function/1.0");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return EntityUtils.toByteArray(response.getEntity());
                } else {
                    logger.error("HTTP error downloading URL: {} - Status: {}", url, statusCode);
                    return null;
                }
            }
        } catch (IOException e) {
            logger.error("Error downloading content from URL: {}", url, e);
            return null;
        }
    }
    
    private String extractText(byte[] content) throws IOException, TikaException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            return tika.parseToString(inputStream);
        }
    }
    
    private APIGatewayProxyResponseEvent createErrorResponse(APIGatewayProxyResponseEvent response, int statusCode, String message) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("success", false);
        errorBody.put("error", message);
        
        try {
            response.setStatusCode(statusCode);
            response.setBody(objectMapper.writeValueAsString(errorBody));
        } catch (Exception e) {
            logger.error("Error creating error response", e);
            response.setStatusCode(500);
            response.setBody("{\"success\":false,\"error\":\"Internal server error\"}");
        }
        
        return response;
    }
}
