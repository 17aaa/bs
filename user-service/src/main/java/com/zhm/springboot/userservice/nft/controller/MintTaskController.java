package com.zhm.springboot.userservice.nft.controller;

import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.nft.dto.MintTaskCreateDTO;
import com.zhm.springboot.userservice.nft.entity.MintTask;
import com.zhm.springboot.userservice.nft.service.MintTaskService;
import com.zhm.springboot.userservice.nft.vo.MintTaskVO;
import com.zhm.springboot.userservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * NFT 铸造任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/nft/mint-task")
@RequiredArgsConstructor
public class MintTaskController {

    private final MintTaskService mintTaskService;
    private final JwtUtil jwtUtil;

    /**
     * 创建铸造任务
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MintTaskVO>> createTask(
            @Valid @RequestBody MintTaskCreateDTO dto,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.parseToken(token.replace("Bearer ", ""));

            MintTask task = mintTaskService.createTask(
                    userId,
                    dto.getCreatorAddress(),
                    dto.getName(),
                    dto.getDescription(),
                    dto.getCategory(),
                    dto.getImageUrl(),
                    dto.getRoyaltyFee()
            );

            // 异步执行铸造
            mintTaskService.executeMintTask(task.getTaskId());

            return ResponseEntity.ok(ApiResponse.success(convertToVO(task)));
        } catch (Exception e) {
            log.error("创建铸造任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    /**
     * 查询铸造任务进度
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<MintTaskVO>> getTaskProgress(@PathVariable String taskId) {
        try {
            MintTask task = mintTaskService.getTask(taskId);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(convertToVO(task)));
        } catch (Exception e) {
            log.error("查询任务进度失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 重试铸造任务
     */
    @PostMapping("/{taskId}/retry")
    public ResponseEntity<ApiResponse<MintTaskVO>> retryTask(@PathVariable String taskId) {
        try {
            MintTask task = mintTaskService.retryTask(taskId);
            return ResponseEntity.ok(ApiResponse.success(convertToVO(task)));
        } catch (Exception e) {
            log.error("重试任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("重试失败：" + e.getMessage()));
        }
    }

    /**
     * 取消铸造任务
     */
    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<ApiResponse<MintTaskVO>> cancelTask(@PathVariable String taskId) {
        try {
            mintTaskService.cancelTask(taskId);
            MintTask task = mintTaskService.getTask(taskId);
            return ResponseEntity.ok(ApiResponse.success(convertToVO(task)));
        } catch (Exception e) {
            log.error("取消任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("取消失败：" + e.getMessage()));
        }
    }

    /**
     * 回滚铸造任务
     */
    @PostMapping("/{taskId}/rollback")
    public ResponseEntity<ApiResponse<MintTaskVO>> rollbackTask(@PathVariable String taskId) {
        try {
            mintTaskService.rollbackTask(taskId);
            MintTask task = mintTaskService.getTask(taskId);
            return ResponseEntity.ok(ApiResponse.success(convertToVO(task)));
        } catch (Exception e) {
            log.error("回滚任务失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("回滚失败：" + e.getMessage()));
        }
    }

    /**
     * 转换为VO
     */
    private MintTaskVO convertToVO(MintTask task) {
        MintTaskVO vo = new MintTaskVO();
        vo.setTaskId(task.getTaskId());
        vo.setName(task.getName());
        vo.setCurrentStep(task.getCurrentStep());
        vo.setStepName(task.getStepName());
        vo.setStepStatus(task.getStepStatus());
        vo.setProgressPercent(task.getProgressPercent());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setNftAssetId(task.getNftAssetId());
        vo.setTxHash(task.getTxHash());
        vo.setCompleted(task.getCurrentStep() == MintTask.STEP_COMPLETED);
        vo.setFailed(MintTask.STATUS_FAILED.equals(task.getStepStatus()));
        vo.setRollbackStatus(task.getRollbackStatus());
        vo.setRollbackError(task.getRollbackError());
        vo.setRetryCount(task.getRetryCount());
        vo.setCanRetry(MintTask.STATUS_FAILED.equals(task.getStepStatus()) &&
                (task.getRetryCount() == null || task.getRetryCount() < MintTask.MAX_RETRY_COUNT));

        // 构建步骤列表
        List<MintTaskVO.StepInfo> steps = new ArrayList<>();
        String[] stepNames = {"文件上传", "元数据生成", "链上铸造", "铸造完成"};
        for (int i = 1; i <= 4; i++) {
            MintTaskVO.StepInfo step = new MintTaskVO.StepInfo();
            step.setStep(i);
            step.setName(stepNames[i - 1]);

            if (i < task.getCurrentStep()) {
                step.setStatus("completed");
            } else if (i == task.getCurrentStep()) {
                step.setStatus(task.getStepStatus());
            } else {
                step.setStatus("pending");
            }
            steps.add(step);
        }
        vo.setSteps(steps);

        return vo;
    }
}