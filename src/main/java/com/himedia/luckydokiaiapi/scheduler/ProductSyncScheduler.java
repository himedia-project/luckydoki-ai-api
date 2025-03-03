package com.himedia.luckydokiaiapi.scheduler;

import com.himedia.luckydokiaiapi.client.ProductApiClient;
import com.himedia.luckydokiaiapi.config.LuckyDokiLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final LuckyDokiLoader luckyDokiLoader;
    private final ProductApiClient productApiClient;
    
    // 매일 새벽 3시에 전체 동기화 (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "0 0 3 * * ?")
    public void fullSync() {
        log.info("Starting full product sync at {}", LocalDateTime.now());
        int updated = luckyDokiLoader.updateProductData(null);
        log.info("Full product sync completed. Updated {} products", updated);
    }
    
    // 10분(600,000ms)마다 최근 변경된 상품만 동기화
    @Scheduled(fixedRate = 600000)
    public void incrementalSync() {
        log.info("Starting incremental product sync at {}", LocalDateTime.now());
        
        // 최근 10분 내에 변경된 상품 ID 목록 조회
        List<Long> changedProductIds = productApiClient.getRecentlyChangedProducts(10);
        
        // 최근 10분 내에 추가된 상품 ID 목록 조회
        List<Long> addedProductIds = productApiClient.getRecentlyAddedProducts(10);
        
        // 두 목록 합치기 (중복 제거)
        Set<Long> allProductIds = new HashSet<>();
        allProductIds.addAll(changedProductIds);
        allProductIds.addAll(addedProductIds);
        
        if (allProductIds.isEmpty()) {
            log.info("No products changed or added in the last 10 minutes");
            return;
        }
        
        int totalUpdated = 0;
        for (Long productId : allProductIds) {
            int updated = luckyDokiLoader.updateProductData(productId);
            totalUpdated += updated;
        }
        
        log.info("Incremental sync completed. Updated {} products", totalUpdated);
    }


//    @Scheduled(cron = "0 0/3 * * * ?")
    // 테스트용 스케줄러 - 1분마다 실행 (개발 중에만 사용)
//     @Scheduled(fixedRate = 60000)
    public void testSync() {
        log.info("Starting test sync at {}", LocalDateTime.now());
        // 테스트할 상품 ID 지정
        Long testProductId = 1L;
        int updated = luckyDokiLoader.updateProductData(testProductId);
        log.info("Test sync completed for product ID {}. Updated: {}", testProductId, updated);
    }
} 