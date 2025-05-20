package com.hangw.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hangw.model.Interest;
import com.hangw.repository.InterestRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AICategoryService {

    private final InterestRepository interestRepository;

    public AIKeywordResult getCategoryFromAI(String description) throws IOException {
        String aiUrl = "http://localhost:8000/keyword";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<String> candidateInterests = interestRepository.findAll()
            .stream()
            .map(Interest::getName)
            .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("text", description);
        body.put("candidate_interests", candidateInterests);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(aiUrl, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            String keyword = root.get("matched").asText();
            boolean isNew = root.get("is_new").asBoolean();

            return new AIKeywordResult(keyword, isNew);
        } else {
            throw new IOException("AI 서버 응답 실패: " + response.getBody());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class AIKeywordResult {
        private String keyword;
        private boolean isNew;
    }
}

