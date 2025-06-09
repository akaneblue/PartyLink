package com.hangw.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import com.hangw.service.CustomOAuth2UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig {
//	private final CustomOAuth2UserService customOAuth2UserService;
//	
//	@Bean
//	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http.authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
//				.requestMatchers(new AntPathRequestMatcher("/**")).permitAll().anyRequest().authenticated())
//				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
//				.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/**")))
//				.formLogin(formLogin -> formLogin.loginPage("/user/login").usernameParameter("email")
//						.defaultSuccessUrl("/"))
//				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
//						.logoutSuccessUrl("/").invalidateHttpSession(true))
//				.oauth2Login(oauth2 -> oauth2.loginPage("/user/login").defaultSuccessUrl("/")
//						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))); // 소셜 로그인 설정 customOAuth2UserService에서 로그인 플렛폼에 따라 처리함
//
//		return http.build();
//	}
//	
//	@Bean
//	PasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	}
//}

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomOAuth2UserService customOAuth2UserService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(request -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedOrigins(List.of("http://localhost:5173"));
			config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
			config.setAllowCredentials(true);
			config.setAllowedHeaders(List.of("*"));
			return config;
		})).authorizeHttpRequests(auth -> auth.requestMatchers("/api/home",  "/api/user/**").permitAll()
				.requestMatchers("/api/user/status").permitAll().requestMatchers("/api/**").authenticated().anyRequest()
				.permitAll())
				.formLogin(form -> form.loginProcessingUrl("/api/user/login").defaultSuccessUrl("http://localhost:5173", true).failureUrl("/user/login?error=true").permitAll().successHandler((req, res, auth) -> {
					res.setContentType("application/json");
					res.setCharacterEncoding("UTF-8");
					res.getWriter().write("{\"message\":\"login success\"}");
				}).failureHandler((req, res, ex) -> {
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					res.setContentType("application/json");
					res.setCharacterEncoding("UTF-8");
					res.getWriter().write("{\"error\":\"invalid credentials\"}");
				}).permitAll())
				.logout(logout -> logout.logoutUrl("/api/user/logout").logoutSuccessUrl("http://localhost:5173")
						.invalidateHttpSession(true))
				.oauth2Login(oauth -> oauth.loginPage("/user/login").defaultSuccessUrl("http://localhost:5173", true)
						.userInfoEndpoint(info -> info.userService(customOAuth2UserService)));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
