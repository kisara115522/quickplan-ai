package com.example.quickplan_ai.controller;

import com.example.quickplan_ai.Service.ScheduleService;
import com.example.quickplan_ai.entity.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日程管理Controller
 * 提供日程的增删改查功能
 */
@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 获取用户所有日程
     * GET /api/schedule/list/{userId}
     */
    @GetMapping("/list/{userId}")
    public ResponseEntity<Map<String, Object>> getScheduleList(@PathVariable String userId) {
        List<Schedule> schedules = scheduleService.getUserSchedules(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", schedules);
        response.put("total", schedules.size());
        response.put("message", null);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定日期范围内的日程
     * GET /api/schedule/range?userId=xxx&startDate=2025-10-26&endDate=2025-10-31
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getSchedulesByRange(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        List<Schedule> schedules = scheduleService.getSchedulesByDateRange(userId, startDate, endDate);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", schedules);
        response.put("total", schedules.size());
        response.put("message", null);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取指定日期的日程
     * GET /api/schedule/date?userId=xxx&date=2025-10-26
     */
    @GetMapping("/date")
    public ResponseEntity<Map<String, Object>> getSchedulesByDate(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<Schedule> schedules = scheduleService.getSchedulesByDate(userId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", schedules);
        response.put("total", schedules.size());
        response.put("message", null);

        return ResponseEntity.ok(response);
    }

    /**
     * 创建新日程
     * POST /api/schedule/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSchedule(@RequestBody Schedule schedule) {
        // 参数校验
        if (schedule.getUserId() == null || schedule.getUserId().isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (schedule.getTitle() == null || schedule.getTitle().isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "日程标题不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (schedule.getDate() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "日期不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Schedule created = scheduleService.createSchedule(schedule);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "创建成功");
        response.put("data", created);

        return ResponseEntity.ok(response);
    }

    /**
     * 更新日程
     * PUT /api/schedule/update
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateSchedule(@RequestBody Schedule schedule) {
        // 参数校验
        if (schedule.getId() == null || schedule.getId().isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "日程ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 检查日程是否存在
        Schedule existing = scheduleService.getScheduleById(schedule.getId());
        if (existing == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "日程不存在");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        boolean updated = scheduleService.updateSchedule(schedule);

        Map<String, Object> response = new HashMap<>();
        if (updated) {
            Schedule updatedSchedule = scheduleService.getScheduleById(schedule.getId());
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedSchedule);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "更新失败");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除日程(软删除)
     * DELETE /api/schedule/delete/{scheduleId}
     */
    @DeleteMapping("/delete/{scheduleId}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable String scheduleId) {
        boolean deleted = scheduleService.deleteSchedule(scheduleId);

        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("message", "删除成功");
            response.put("data", null);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "删除失败");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取日程详情
     * GET /api/schedule/detail/{scheduleId}
     */
    @GetMapping("/detail/{scheduleId}")
    public ResponseEntity<Map<String, Object>> getScheduleDetail(@PathVariable String scheduleId) {
        Schedule schedule = scheduleService.getScheduleById(scheduleId);

        Map<String, Object> response = new HashMap<>();
        if (schedule != null) {
            response.put("success", true);
            response.put("data", schedule);
            response.put("message", null);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "日程不存在");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
