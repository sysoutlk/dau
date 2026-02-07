package com.example.dautracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DAU服务类
 * 使用Redis Bitmap实现高效的日活跃用户统计
 @author lk
 @create 2026/02/07-23:03
 */
@Slf4j
@Service
public class DAUService {

    private static final String DAU_KEY_PREFIX = "dau:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    //数据保留7天
    private static final int EXPIRE_DAYS = 7;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成 DAU Redis Key
     * @param date 日期
     * @return 改造后的key
     */
    private String generateDauKey(LocalDate date) {
        return DAU_KEY_PREFIX + date.format(DATE_FORMATTER);
    }

    /**
     * 记录用户活跃状况
     * @param userId 用户id
     * @param date 日期，为null时使用当前日期
     * @return 是否记录成功
     */
    public boolean recordUserActive(Long userId, LocalDate date) {
        if (userId == null || userId <= 0) {
            log.warn("无效的用户ID:{}", userId);
            return false;
        }

        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDauKey(date);

        try {
            //使用SETBIT设置用户活跃标记
            Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                return connection.setBit(key.getBytes(), userId, true);
            });

            //设置过期时间
            redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);

            log.debug("用户{}在{}的活跃度已记录, key:{}", userId, date, key);
            return result != null;

        } catch (Exception e) {
            log.error("记录用户活跃状态失败: userId={}, date={}", userId, date, e);
            return false;
        }
    }

    /**
     * 批量记录用户活跃状态
     * @param userIds 用户Id列表
     * @param date 日期
     * @return 成功记录的数量
     */
    public int batchRecordUserActive(Long[] userIds, LocalDate date) {
        if (userIds == null || userIds.length == 0) {
            return 0;
        }

        if (date == null) {
            date = LocalDate.now();
        }

        int successCount = 0;
        String key = generateDauKey(date);
        for (Long userId : userIds) {
            if (userId != null && userId > 0) {
                try {
                    redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                        return connection.setBit(key.getBytes(), userId, true);
                    });
                    successCount++;
                } catch (Exception e) {
                    log.error("批量记录失败: userId={}", userId, e);
                }
            }
        }

        //设置过期时间
        redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);

        log.info("批量记录用户活跃: 总数={},成功={},日期={}", userIds.length, successCount, date);
        return successCount;
    }

    /**
     * 检查用户是否活跃
     * @param userId 用户ID
     * @param date 日期
     * @return 是否活跃
     */
    public boolean isUserActive(Long userId, LocalDate date) {
        if (userId == null || userId <= 0) {
            return false;
        }

        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDauKey(date);

        try {
            Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                return connection.getBit(key.getBytes(), userId);
            });

            return result != null && result;
        } catch (Exception e) {
            log.error("查询用户是否活跃失败:userId={}, date={}", userId, date, e);
            return false;
        }
    }

    /**
     * 获取指定日期的DAU
     * @param date 日期
     * @return DAU数量
     */
    public Long getDauCount(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDauKey(date);

        try {
            //使用BITCOUNT 统计活跃用户数
            Long count = redisTemplate.execute((RedisCallback<Long>) connection -> {
                return connection.bitCount(key.getBytes());
            });

            log.debug("日期{}的DAU: {}", date, count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("获取DAU失败:date={}", date, e);
            return 0L;
        }
    }

    /**
     * 获取日期范围内的DAU统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期->DAU的映射
     */
    public Map<String, Long> getDauCountRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> result = new LinkedHashMap<>();

        if (startDate == null || endDate == null) {
            return result;
        }

        LocalDate currentDate = startDate;
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(displayFormatter);
            Long count = getDauCount(currentDate);
            result.put(dateStr, count);
            currentDate = currentDate.plusDays(1);
        }

        log.info("日期范围{}到{}的DAU统计完成", startDate, endDate);
        return result;
    }

    /**
     * 获取Redis的Key占用的内存大小(字节)
     * @param date 日期
     * @return 内存大小(字节)
     */
    public Long getKeyMemoryUsage(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDauKey(date);

        try {
            return redisTemplate.execute((RedisCallback<Long>) connection -> {
                Object result = connection.execute("MEMORY", "USAGE".getBytes(), key.getBytes());
                return result != null ? (Long) result : 0L;
            });
        } catch (Exception e) {
            log.error("获取内存使用大小失败:date={}", date, e);
            return 0L;
        }
    }
}
