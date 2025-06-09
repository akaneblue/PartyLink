package com.hangw.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hangw.model.Crews;
import com.hangw.model.Post;
import com.hangw.repository.CrewRepository;
import com.hangw.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CrewRepository crewRepository;
    private final RestTemplate restTemplate;

    public Post createPost(Long crewId, String content) {
        Crews crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found"));

        // FastAPI로 분위기 분석 요청
        String tone = analyzeToneWithLLM(content);

        // Post 저장
        Post post = new Post();
        post.setCrew(crew);
        post.setContent(content);
        post.setTone(tone);
        postRepository.save(post);

        // Crew 분위기 업데이트
        updateCrewToneTag(crew);

        return post;
    }

    public String analyzeToneWithLLM(String content) {
        String apiUrl = "http://localhost:8000/analyze-tone"; // FastAPI 서버 주소
        Map<String, String> request = Map.of("content", content);
        try {
            Map<String, String> response = restTemplate.postForObject(apiUrl, request, Map.class);
            return response.get("tone");
        } catch (Exception e) {
            throw new RuntimeException("분위기 분석 실패: " + e.getMessage());
        }
    }

    public void updateCrewToneTag(Crews crew) {
        List<Post> posts = postRepository.findByCrew(crew);
        if (posts.isEmpty()) return;

        Map<String, Long> toneCount = posts.stream()
                .filter(p -> p.getTone() != null)
                .collect(Collectors.groupingBy(Post::getTone, Collectors.counting()));

        String dominantTone = toneCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        crew.setTag(dominantTone);
        crewRepository.save(crew);
    }
}

