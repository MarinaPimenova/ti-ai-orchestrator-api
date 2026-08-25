package com.wk.ti.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.wk.ti.exception.IntegrationException;
import com.wk.ti.exception.model.ClientErrorResponse;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

/**
 * @noinspection unchecked, rawtypes, ConstantConditions, unused
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntegrationRestClient {
    public static final String AUTHORIZATION_HEADER_VALUE = "Bearer %s";
    protected final ObjectMapper mapper;
    protected final RestTemplate restTemplate;

    public HttpHeaders getHttpHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("cache-control", "no-cache");
        if (token != null && !token.isEmpty()) {
            headers.add(HttpHeaders.AUTHORIZATION, format(AUTHORIZATION_HEADER_VALUE, token));
        }

        return headers;
    }

    private <V> HttpEntity<V> initHttpEntity(Optional<V> body, HttpHeaders headers) {
        Objects.requireNonNull(headers, "headers can't be null");
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        if (!StringUtils.isBlank(headers.getFirst("Content-Type"))
                && MediaType.APPLICATION_FORM_URLENCODED_VALUE
                .equalsIgnoreCase(headers.getFirst("Content-Type"))
                && body.isPresent()) {
            HttpEntity<MultiValueMap<String, Object>> httpEntity =
                    new HttpEntity(body.get(), headers);

            return (HttpEntity<V>) httpEntity;
        }
        if (!headers.getAccept().isEmpty()
                && MediaType.APPLICATION_OCTET_STREAM.equals(headers.getAccept().getFirst())) {
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

            return (HttpEntity<V>) httpEntity;
        }

        return body.map(v -> new HttpEntity<>(v, headers)).orElseGet(() -> new HttpEntity<>(headers));
    }

    public <T, V> T executeRequest(
            String url,
            HttpHeaders headers,
            Optional<V> body,
            HttpMethod httpMethod, Class<T> targetType) {
        HttpEntity httpEntity = initHttpEntity(body, headers);
        try {
            ResponseEntity<?> response = restTemplate.exchange(url, httpMethod, httpEntity, String.class);
            return processResponse(response, url, targetType);
        } catch (Throwable ex) {
            String message = format("Action: request %s failed. Caused by: %s", url, ex.getMessage());
            log.error(message);
            if (ex.getMessage().contains("429")) {
                throw new IntegrationException(
                        getClientErrorResponse(ex.getMessage(),
                                HttpStatus.TOO_MANY_REQUESTS.value(), url));
            }
            if (ex.getMessage().contains("401")) {
                throw new IntegrationException(getClientErrorResponse("Not authorized",
                        HttpStatus.UNAUTHORIZED.value(), url));
            }
            throw new IntegrationException(getClientErrorResponse(ex.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), url));
        }
    }

    protected <T> T processResponse(ResponseEntity<?> responseEntity, String url, Class<T> targetType) throws IOException, IntegrationException {
        if (responseEntity.getStatusCode() == HttpStatus.FORBIDDEN
                || responseEntity.getStatusCode() == HttpStatus.UNAUTHORIZED) {

            throw new IntegrationException(getClientErrorResponse("Not authorized",
                    responseEntity.getStatusCode().value(), url));
        }

        if (responseEntity.getStatusCode().is2xxSuccessful()) {

            Object body = responseEntity.getBody();

            if (targetType == String.class) {
                return targetType.cast(body);
            }

            if (body instanceof String stringBody) {
                return mapper.readValue(stringBody, targetType);
            }

            return mapper.convertValue(body, targetType);
        }
        throw new IntegrationException(getClientErrorResponse("", responseEntity.getStatusCode().value(), url));
    }

    private ClientErrorResponse getClientErrorResponse(String error, int status, String url) {
        return ClientErrorResponse.builder()
                .status(status)
                .error("Failed to process.")
                .message(format("Failed to process URL: %s. Caused by: %s", url, error))
                .build();
    }

    public <T> T postMultipart(String url, MultiValueMap<String, Object> body, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return executeRequest(
                url,
                headers,
                Optional.of(body),
                HttpMethod.POST,
                type
        );
    }
}