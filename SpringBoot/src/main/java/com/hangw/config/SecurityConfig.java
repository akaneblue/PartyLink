package com.hangw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.hangw.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomOAuth2UserService customOAuth2UserService;
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
				.requestMatchers(new AntPathRequestMatcher("/**")).permitAll().anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
				.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/**")))
				.formLogin(formLogin -> formLogin.loginPage("/user/login").usernameParameter("email")
						.defaultSuccessUrl("/"))
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
						.logoutSuccessUrl("/").invalidateHttpSession(true))
				.oauth2Login(oauth2 -> oauth2.loginPage("/user/login").defaultSuccessUrl("/")
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))); // 소셜 로그인 설정 customOAuth2UserService에서 로그인 플렛폼에 따라 처리함

		return http.build();
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
