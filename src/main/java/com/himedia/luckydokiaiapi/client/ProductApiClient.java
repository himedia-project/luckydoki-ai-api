package com.himedia.luckydokiaiapi.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${api.url}")
    private String apiUrl;

    
    /**
     * 최근 변경된 상품 ID 목록을 조회
     * @param minutes 최근 몇 분 이내에 변경된 상품을 조회할지 지정
     * @return 변경된 상품 ID 목록
     */
    public List<Long> getRecentlyChangedProducts(int minutes) {
        try {
            LocalDateTime fromTime = LocalDateTime.now().minusMinutes(minutes);
            String fromTimeStr = fromTime.format(DateTimeFormatter.ISO_DATE_TIME);
            
            // API에서 최근 변경된 상품 목록을 조회하는 엔드포인트 호출
            String url = apiUrl + "/api/product/recent-changes?since=" + fromTimeStr;
            log.info("Calling API to get recently changed products: {}", url);
            
            Long[] productIds = restTemplate.getForObject(url, Long[].class);
            
            if (productIds == null || productIds.length == 0) {
                return Collections.emptyList();
            }
            
            return Arrays.asList(productIds);
        } catch (Exception e) {
            log.error("Error fetching recently changed products: ", e);
            throw new RuntimeException("Error fetching recently changed products", e);
        }
    }
    
    /**
     * 최근 추가된 상품 ID 목록을 조회
     * @param minutes 최근 몇 분 이내에 추가된 상품을 조회할지 지정
     * @return 추가된 상품 ID 목록
     */
    public List<Long> getRecentlyAddedProducts(int minutes) {
        try {
            LocalDateTime fromTime = LocalDateTime.now().minusMinutes(minutes);
            String fromTimeStr = fromTime.format(DateTimeFormatter.ISO_DATE_TIME);
            
            // API에서 최근 추가된 상품 목록을 조회하는 엔드포인트 호출
            String url = apiUrl + "/api/product/recent-additions?since=" + fromTimeStr;
            log.info("Calling API to get recently added products: {}", url);
            
            Long[] productIds = restTemplate.getForObject(url, Long[].class);
            
            if (productIds == null || productIds.length == 0) {
                return Collections.emptyList();
            }
            
            return Arrays.asList(productIds);
        } catch (Exception e) {
            log.error("Error fetching recently added products: ", e);
            throw new RuntimeException("Error fetching recently added products", e);
        }
    }
} 