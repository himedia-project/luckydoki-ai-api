package com.himedia.luckydokiaiapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")  // 모든 요청에 대해 CORS 허용
                .allowedOrigins("http://localhost:3000", "http://localhost:3001",
                        "http://15.165.150.61:3000", "http://15.165.150.61:3001",
                        "http://luckydoki.shop:3000", "http://luckydoki.shop:3001",
                        "http://luckydoki.shop", "https://luckydoki.shop",
                        "http://www.luckydoki.shop", "https://www.luckydoki.shop",
                        "https://luckydoki.shop/", "https://www.luckydoki.shop/"
                )    // 허용할 출처: 리액트 url 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS") // OPTIONS 추가: preflight 요청 허용
                .allowedHeaders("*")        // 모든 header 허용
                // content-disposition 허용 설정 -> excel,pdf 파일 다운로드시, 제목노출을 위해 필요!
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true);    // 쿠키를 주고 받을 수 있게 설정

    }
}
