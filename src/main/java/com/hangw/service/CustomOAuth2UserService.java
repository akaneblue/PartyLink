package com.hangw.service;

import java.util.Collections;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
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

		System.out.println("Registration ID: " + registrationId); // registrationId 확인
		System.out.println("OAuth2 User Attributes: " + oAuth2User.getAttributes());

		Map<String, Object> attributes = oAuth2User.getAttributes();
		System.out.println("Google Attributes: " + attributes); // Google attributes 확인

		String email = (String) attributes.get("email");
		String name = (String) attributes.get("name");

		// 사용자 저장 또는 조회
		Users user = userRepository.findByEmail(email).orElseGet(() -> {
			System.out.println("User not found. Creating new Google user.");
			return userRepository.save(new Users(name, email));
		});

		return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes,
				"email");
	}

	@Override
	public void setAttributesConverter(
			Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
		// TODO Auto-generated method stub
		super.setAttributesConverter(attributesConverter);
	}

}
