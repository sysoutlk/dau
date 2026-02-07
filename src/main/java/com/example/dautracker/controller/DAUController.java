package com.example.dautracker.controller;

import com.example.dautracker.model.DAUStatistics;
import com.example.dautracker.service.DAUService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 @author lk
 @create 2026/02/07-23:43
 */
@Slf4j
@RestController
@RequestMapping("/api/dau")
public class DAUController {

    @Autowired
    private DAUService dauService;

    /**
     * 记录用户活跃
     * @param userId 用户id
     * @param date 日期
     * @return POST /api/dau/record?userId=123
     */
    @PostMapping("/record")
    public ResponseEntity<DAUStatistics> recordUserActive(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean success = dauService.recordUserActive(userId, date);

        DAUStatistics stats = DAUStatistics.builder()
                .date(date != null ? date.toString() : LocalDate.now().toString())
                .message(success ? "用户活跃记录成功" : "用户活跃记录失败")
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * 批量记录用户活跃
     * @param userIds 用户id列表
     * @param date 日期
     * @return POST /api/dau/batch-record
     * Body: [1, 2, 3, 100, 500]
     */
    @PostMapping("/batch-record")
    public ResponseEntity<DAUStatistics> batchRecordUserActive(
            @RequestBody Long[] userIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        int count = dauService.batchRecordUserActive(userIds, date);

        DAUStatistics stats = DAUStatistics.builder()
                .date(date != null ? date.toString() : LocalDate.now().toString())
                .message(String.format("批量记录成功: %d/%d", count, userIds.length))
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * 查询用户是否活跃
     * @param userId 用户id
     * @param date 日期
     * @return GET /api/dau/check?userId=123&date=2026-02-07
     */
    @GetMapping("/check")
    public ResponseEntity<DAUStatistics> checkUserActive(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean isActive = dauService.isUserActive(userId, date);

        DAUStatistics stats = DAUStatistics.builder()
                .date(date != null ? date.toString() : LocalDate.now().toString())
                .isActive(isActive)
                .message(isActive ? "用户活跃" : "用户不活跃")
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取指定日期的DAU
     * @param date 指定日期
     * @return GET /api/dau/count?date=2026-02-07
     */
    @GetMapping("/count")
    public ResponseEntity<DAUStatistics> getDauCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        Long count = dauService.getDauCount(date);

        DAUStatistics stats = DAUStatistics.builder()
                .date(date.toString())
                .dauCount(count)
                .message("DAU 查询成功")
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取日期范围的DAU统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    @GetMapping("/range")
    public ResponseEntity<DAUStatistics> getDauCountRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Long> rangeStats = dauService.getDauCountRange(startDate, endDate);

        DAUStatistics stats = DAUStatistics.builder()
                .dateRangeStats(rangeStats)
                .message("DAU 范围查询成功")
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取Key的内存占用
     * @param date 日期
     * @return 占用日期->大小
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryUsage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        Long memoryBytes = dauService.getKeyMemoryUsage(date);

        Map<String, Object> response = new HashMap<>();
        response.put("date", date.toString());
        response.put("memoryBytes", memoryBytes);
        response.put("memoryKB", String.format("%.2f KB", memoryBytes / 1024.0));
        response.put("message", "内存查询成功");

        return ResponseEntity.ok(response);
    }
}
