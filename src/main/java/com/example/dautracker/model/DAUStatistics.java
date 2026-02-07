package com.example.dautracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DAU统计数据模型
 @author lk
 @create 2026/02/07-23:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DAUStatistics {
    /**
     * 日期
     */
    private String date;

    /**
     * 日活跃用户
     */
    private Long dauCount;

    /**
     * 是否活跃
     */
    private Boolean isActive;

    /**
     * 日期统计范围
     */
    private Map<String, Long> dateRangeStats;

    /**
     * 消息
     */
    private String message;
}
