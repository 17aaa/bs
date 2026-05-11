package com.zhm.springboot.userservice.scheduled;

import com.zhm.springboot.userservice.config.RateLimitConfig;
import com.zhm.springboot.userservice.nft.service.NftReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 系统定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduledTasks {

    private final RateLimitConfig rateLimitConfig;
    private final NftReviewService nftReviewService;

    /**
     * 清理限流记录 - 每5分钟
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupRateLimitRecords() {
        try {
            rateLimitConfig.cleanup();
            log.debug("限流记录清理完成");
        } catch (Exception e) {
            log.error("清理限流记录失败", e);
        }
    }

    /**
     * 更新Gas价格 - 每30秒
     */
    @Scheduled(fixedRate = 30000)
    public void updateGasPrice() {
        try {
            // GasPriceService已有定时任务，这里只是日志
            log.debug("Gas价格更新任务执行");
        } catch (Exception e) {
            log.error("更新Gas价格失败", e);
        }
    }

    /**
     * 检查待审核数量 - 每小时
     */
    @Scheduled(fixedRate = 3600000)
    public void checkPendingReviews() {
        try {
            long pendingCount = nftReviewService.getPendingCount();
            if (pendingCount > 0) {
                log.info("当前待审核NFT数量: {}", pendingCount);
            }
        } catch (Exception e) {
            log.error("检查待审核数量失败", e);
        }
    }

    /**
     * 系统健康检查 - 每5分钟
     */
    @Scheduled(fixedRate = 300000)
    public void healthCheck() {
        try {
            // 记录系统状态
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            long maxMemory = runtime.maxMemory() / 1024 / 1024;
            double memoryUsage = (double) usedMemory / maxMemory * 100;

            if (memoryUsage > 80) {
                log.warn("内存使用率较高: {}%, 已用: {}MB, 最大: {}MB",
                        String.format("%.2f", memoryUsage), usedMemory, maxMemory);
            } else {
                log.debug("系统内存使用: {}%", String.format("%.2f", memoryUsage));
            }
        } catch (Exception e) {
            log.error("健康检查失败", e);
        }
    }
}