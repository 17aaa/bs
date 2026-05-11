package com.zhm.springboot.userservice.nft.controller;

import com.zhm.springboot.userservice.common.ApiResponse;
import com.zhm.springboot.userservice.nft.entity.NftAsset;
import com.zhm.springboot.userservice.nft.entity.NftVersion;
import com.zhm.springboot.userservice.nft.service.NftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NFT 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/nft")
@RequiredArgsConstructor
public class NftController {

    private final NftService nftService;

    /**
     * 铸造 NFT
     */
    @PostMapping("/mint")
    public ResponseEntity<ApiResponse<Long>> mintNft(@RequestBody MintRequest request) {
        try {
            log.info("收到 NFT 铸造请求：creatorAddress={}, name={}, category={}",
                    request.getCreatorAddress(), request.getName(), request.getCategory());

            // 参数验证
            if (request.getCreatorAddress() == null || request.getCreatorAddress().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("创作者地址不能为空"));
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("作品名称不能为空"));
            }

            Long nftAssetId = nftService.mintNft(
                    request.getCreatorAddress(),
                    request.getName(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getMetadataUrl(),
                    request.getRoyaltyFee()
            );
            return ResponseEntity.ok(ApiResponse.success(nftAssetId));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to mint NFT", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("铸造失败：" + e.getMessage()));
        }
    }

    /**
     * 更新 NFT 元数据
     */
    @PostMapping("/{id}/update")
    public ResponseEntity<ApiResponse<Integer>> updateMetadata(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam String changeDesc,
            @RequestParam String updaterAddress) {
        try {
            Integer newVersion = nftService.updateMetadata(
                    id, name, description, changeDesc, updaterAddress
            );
            return ResponseEntity.ok(ApiResponse.success(newVersion));
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update NFT metadata", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 获取 NFT 详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NftAsset>> getNftAsset(@PathVariable Long id) {
        try {
            NftAsset nftAsset = nftService.getNftAsset(id);
            if (nftAsset == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(nftAsset));
        } catch (Exception e) {
            log.error("Failed to get NFT asset", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取版本历史
     */
    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<NftVersion>>> getVersionHistory(@PathVariable Long id) {
        try {
            List<NftVersion> versions = nftService.getVersionHistory(id);
            return ResponseEntity.ok(ApiResponse.success(versions));
        } catch (Exception e) {
            log.error("Failed to get version history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询版本历史失败：" + e.getMessage()));
        }
    }

    /**
     * 获取用户持有的 NFT
     */
    @GetMapping("/owner/{address}")
    public ResponseEntity<ApiResponse<List<NftAsset>>> getNftsByOwner(@PathVariable String address) {
        try {
            List<NftAsset> nftAssets = nftService.getNftsByOwner(address);
            return ResponseEntity.ok(ApiResponse.success(nftAssets));
        } catch (Exception e) {
            log.error("Failed to get NFTs by owner", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询用户 NFT 失败：" + e.getMessage()));
        }
    }

    /**
     * 获取用户创作的 NFT
     */
    @GetMapping("/creator/{address}")
    public ResponseEntity<ApiResponse<List<NftAsset>>> getNftsByCreator(@PathVariable String address) {
        try {
            List<NftAsset> nftAssets = nftService.getNftsByCreator(address);
            return ResponseEntity.ok(ApiResponse.success(nftAssets));
        } catch (Exception e) {
            log.error("Failed to get NFTs by creator", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询创作者 NFT 失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有 NFT（分页）
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<NftAsset>>> getAllNfts(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {
        try {
            // 限制最大查询数量
            int safeLimit = Math.min(Math.max(limit, 1), 100);
            int safeOffset = Math.max(offset, 0);

            List<NftAsset> nftAssets = nftService.getAllNfts(safeLimit, safeOffset);
            return ResponseEntity.ok(ApiResponse.success(nftAssets));
        } catch (Exception e) {
            log.error("Failed to get all NFTs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询 NFT 列表失败：" + e.getMessage()));
        }
    }
}