package com.zhm.springboot.userservice.blockchain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhm.springboot.userservice.blockchain.entity.IpfsFileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * IPFS 文件记录 Mapper
 */
@Mapper
public interface IpfsFileRecordMapper extends BaseMapper<IpfsFileRecord> {

    /**
     * 根据 CID 查询记录
     */
    @Select("SELECT * FROM ipfs_file_record WHERE cid = #{cid} AND deleted = 0")
    IpfsFileRecord selectByCid(@Param("cid") String cid);

    /**
     * 根据用户ID查询文件列表
     */
    @Select("SELECT * FROM ipfs_file_record WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<IpfsFileRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据钱包地址查询文件列表
     */
    @Select("SELECT * FROM ipfs_file_record WHERE owner_address = #{ownerAddress} AND deleted = 0 ORDER BY created_at DESC")
    List<IpfsFileRecord> selectByOwnerAddress(@Param("ownerAddress") String ownerAddress);

    /**
     * 查询未固定的文件列表
     */
    @Select("SELECT * FROM ipfs_file_record WHERE pinned = 0 AND deleted = 0 ORDER BY created_at DESC")
    List<IpfsFileRecord> selectUnpinnedFiles();

    /**
     * 更新访问次数和最后访问时间
     */
    @Update("UPDATE ipfs_file_record SET access_count = access_count + 1, last_accessed_at = NOW() WHERE cid = #{cid}")
    int incrementAccessCount(@Param("cid") String cid);

    /**
     * 更新固定状态
     */
    @Update("UPDATE ipfs_file_record SET pinned = #{pinned}, pinned_at = CASE WHEN #{pinned} = 1 THEN NOW() ELSE NULL END WHERE cid = #{cid}")
    int updatePinStatus(@Param("cid") String cid, @Param("pinned") Boolean pinned);

    /**
     * 根据文件类型统计
     */
    @Select("SELECT file_type, COUNT(*) as count, SUM(file_size) as total_size FROM ipfs_file_record WHERE deleted = 0 GROUP BY file_type")
    List<java.util.Map<String, Object>> selectStatisticsByType();

    /**
     * 获取总存储统计
     */
    @Select("SELECT COUNT(*) as total_files, SUM(file_size) as total_size, SUM(access_count) as total_access FROM ipfs_file_record WHERE deleted = 0")
    java.util.Map<String, Object> selectTotalStatistics();
}
