package com.zhm.springboot.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@TableName("users")
public class User {
   @TableId
   private Long userId;
   private String username;
   private String password;
   private String email;
   private String phone;
   private String avatar;
   private String school;
   private String major;
   private String bio;
   private Timestamp gmtCreate = new Timestamp(System.currentTimeMillis());

   /**
    * 用户状态：1-正常 2-禁用
    */
   private Integer status;

   /**
    * 封禁原因
    */
   private String banReason;

   /**
    * 封禁时间
    */
   private Timestamp bannedAt;

   /**
    * 平台积分余额（Wei 单位，100 积分 = 100 * 10^18）
    */
   private BigInteger platformBalance;
}