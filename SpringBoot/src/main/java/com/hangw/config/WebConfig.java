package com.hangw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 업로드한 파일을 웹 경로로 접근 가능하게
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		 registry.addMapping("/**") // 모든 경로에 대해
         .allowedOrigins("http://localhost:5173") // React 개발 서버
         .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
         .allowedHeaders("*")
         .allowCredentials(true);
	}
	
}
