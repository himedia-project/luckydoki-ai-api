package com.himedia.luckydokiaiapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Slf4j
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 스케줄링 관련 추가 설정이 필요하면 여기에 추가할 수 있습니다.
    // 예: 스케줄러 풀 크기 설정, 예외 처리 등

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // 동시에 실행할 수 있는 스케줄 작업 수
        scheduler.setThreadNamePrefix("product-sync-scheduler-");
        scheduler.setAwaitTerminationSeconds(60); // 스케줄러 종료 대기 시간
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 스케줄러 종료 시 모든 작업 완료 대기
        scheduler.setErrorHandler(throwable -> {
            // 스케줄러 작업 중 발생한 예외 처리
            log.error("Scheduler error: {}", throwable.getMessage());
            throwable.printStackTrace();
        });
        return scheduler;
    }
}
