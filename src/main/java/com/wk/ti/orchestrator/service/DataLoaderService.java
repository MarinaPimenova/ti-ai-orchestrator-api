package com.wk.ti.orchestrator.service;

import com.wk.ti.agents.tool.document.DocumentApiConfig;
import com.wk.ti.integration.IntegrationRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final IntegrationRestClient restClient;
    private final DocumentApiConfig config;

    public String uploadKnowledge(MultipartFile file) {
        try {
            String url = config.getUpload();

            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            return restClient.executeRequest(
                    url,
                    headers,
                    Optional.of(body),
                    HttpMethod.POST,
                    String.class
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Document Agent", e);
        }
    }

    public String loadFromUrl(String url) {

        String endpoint = config.getLoadUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("url", url);

        return restClient.executeRequest(
                endpoint,
                headers,
                Optional.of(requestBody),
                HttpMethod.POST,
                String.class
        );
    }
}