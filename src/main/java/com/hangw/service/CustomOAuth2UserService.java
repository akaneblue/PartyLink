package com.hangw.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.hangw.model.Users;
import com.hangw.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        System.out.println("Registration ID: " + registrationId);  // registrationId 확인
        System.out.println("OAuth2 User Attributes: " + oAuth2User.getAttributes());  // 전체 attributes 확인

        if ("naver".equals(registrationId)) {
            // 네이버 사용자 응답 처리
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            System.out.println("Response Attributes: " + response);  // response 객체 확인

            String email = (String) response.get("email");
            String name = (String) response.get("name");
            

            // 사용자가 없으면 DB에 추가
            Users user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        System.out.println("User not found. Creating new user.");
                        return userRepository.save(new Users(name, email));
                    });

            // OAuth2User 반환 (Principal로 사용됨)
            Map<String, Object> attributes = Map.of(
                    "id", response.get("id"),
                    "name", name,
                    "email", email,
                    "response", response
            );
            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email"
            );

        } else if ("google".equals(registrationId)) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            System.out.println("Google Attributes: " + attributes);  // Google attributes 확인

            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");


            // 사용자 저장 또는 조회
            Users user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        System.out.println("User not found. Creating new Google user.");
                        return userRepository.save(new Users(name, email));
                    });

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email"
            );

        } else {
            throw new OAuth2AuthenticationException("지원되지 않는 OAuth2 로그인 방식입니다.");
        }
    }
}
