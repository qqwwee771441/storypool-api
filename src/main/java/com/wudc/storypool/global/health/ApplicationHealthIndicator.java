package com.wudc.storypool.global.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

@Component
@Slf4j
public class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // 시스템 리소스 정보 수집
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            long totalMemory = memoryBean.getHeapMemoryUsage().getMax();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;
            
            // 메모리 사용률이 90% 이상이면 DOWN
            if (memoryUsagePercent > 90) {
                return Health.down()
                    .withDetail("application", "High memory usage")
                    .withDetail("memory-usage-percent", String.format("%.2f%%", memoryUsagePercent))
                    .withDetail("used-memory-mb", usedMemory / 1024 / 1024)
                    .withDetail("total-memory-mb", totalMemory / 1024 / 1024)
                    .build();
            }
            
            return Health.up()
                .withDetail("application", "Running normally")
                .withDetail("memory-usage-percent", String.format("%.2f%%", memoryUsagePercent))
                .withDetail("used-memory-mb", usedMemory / 1024 / 1024)
                .withDetail("total-memory-mb", totalMemory / 1024 / 1024)
                .withDetail("available-processors", osBean.getAvailableProcessors())
                .build();
                
        } catch (Exception e) {
            log.error("Application health check failed: {}", e.getMessage());
            return Health.down()
                .withDetail("application", "Health check failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}