package com.example.quickplan_ai.controller;

import com.example.quickplan_ai.Service.OcrReminderService;
import com.example.quickplan_ai.entity.OcrReminder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR识别与提醒Controller
 * 处理OCR文本并创建提醒
 */
@RestController
@RequestMapping("/api/ai/ocr")
@CrossOrigin(origins = "*")
public class OcrController {

    @Autowired
    private OcrReminderService ocrReminderService;

    /**
     * OCR文本创建提醒
     * POST /api/ai/ocr/reminder
     */
    @PostMapping("/reminder")
    public ResponseEntity<Map<String, Object>> createReminderFromOcr(@RequestBody Map<String, String> request) {
        String conversationId = request.get("memoryId");
        String userId = request.get("userId");
        String ocrText = request.get("ocrText");

        // 参数校验
        if (userId == null || userId.isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户ID不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (ocrText == null || ocrText.isBlank()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "OCR文本不能为空");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 解析OCR文本创建提醒
        List<OcrReminder> reminders = parseOcrTextToReminders(ocrText, userId, conversationId);

        if (reminders.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "未识别到有效的提醒内容");
            response.put("data", null);
            return ResponseEntity.ok(response);
        }

        // 保存提醒到数据库
        List<OcrReminder> createdReminders = new ArrayList<>();
        for (OcrReminder reminder : reminders) {
            OcrReminder created = ocrReminderService.createReminder(reminder);
            createdReminders.add(created);
        }

        // 返回最新创建的提醒(或全部提醒)
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "成功创建 " + createdReminders.size() + " 条提醒");

        // 返回最后一条提醒作为data,或返回全部
        if (createdReminders.size() == 1) {
            response.put("data", createdReminders.get(0));
        } else {
            // 返回最后一条作为主数据,全部作为额外字段
            response.put("data", createdReminders.get(createdReminders.size() - 1));
            response.put("allReminders", createdReminders);
            response.put("createdCount", createdReminders.size());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户所有提醒
     * GET /api/ai/ocr/reminders/{userId}
     */
    @GetMapping("/reminders/{userId}")
    public ResponseEntity<Map<String, Object>> getUserReminders(@PathVariable String userId) {
        List<OcrReminder> reminders = ocrReminderService.getUserReminders(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reminders);
        response.put("total", reminders.size());
        response.put("message", null);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取未完成的提醒
     * GET /api/ai/ocr/reminders/uncompleted/{userId}
     */
    @GetMapping("/reminders/uncompleted/{userId}")
    public ResponseEntity<Map<String, Object>> getUncompletedReminders(@PathVariable String userId) {
        List<OcrReminder> reminders = ocrReminderService.getUncompletedReminders(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reminders);
        response.put("total", reminders.size());
        response.put("message", null);

        return ResponseEntity.ok(response);
    }

    /**
     * 标记提醒为完成
     * PUT /api/ai/ocr/reminder/complete/{reminderId}
     */
    @PutMapping("/reminder/complete/{reminderId}")
    public ResponseEntity<Map<String, Object>> markReminderCompleted(@PathVariable String reminderId) {
        boolean success = ocrReminderService.markAsCompleted(reminderId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "标记成功");
            response.put("data", null);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "标记失败");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除提醒
     * DELETE /api/ai/ocr/reminder/delete/{reminderId}
     */
    @DeleteMapping("/reminder/delete/{reminderId}")
    public ResponseEntity<Map<String, Object>> deleteReminder(@PathVariable String reminderId) {
        boolean success = ocrReminderService.deleteReminder(reminderId);

        Map<String, Object> response = new HashMap<>();
        if (success) {
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
     * 解析OCR文本为提醒列表
     * 简单的解析逻辑:
     * - 按行分割
     * - 识别时间模式(如"明早9点"、"下午3点"、"2025-10-27 14:00")
     * - 提取任务描述
     */
    private List<OcrReminder> parseOcrTextToReminders(String ocrText, String userId, String conversationId) {
        List<OcrReminder> reminders = new ArrayList<>();

        // 按行分割
        String[] lines = ocrText.split("\\r?\\n");

        // 时间正则模式
        Pattern timePattern = Pattern.compile("(明天|后天|明早|明晚|下午|上午|晚上|中午)?(\\d{1,2})[点时:]?(\\d{0,2})?分?");
        Pattern dateTimePattern = Pattern.compile("(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2})\\s+(\\d{1,2}):(\\d{2})");

        // 注意：完整日期时间解析在匹配时使用单独的 dateFormatter（支持不带前导零的日期）

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            // 移除编号前缀(如"1. "、"- "等)
            line = line.replaceAll("^[\\d\\-•*]+[.、\\s]+", "");

            OcrReminder reminder = new OcrReminder();
            reminder.setUserId(userId);
            reminder.setConversationId(conversationId);
            reminder.setDescription("由 OCR 自动生成");

            // 尝试匹配完整日期时间格式
            Matcher dateTimeMatcher = dateTimePattern.matcher(line);
            if (dateTimeMatcher.find()) {
                String dateStr = dateTimeMatcher.group(1).replace("/", "-");
                String hourStr = dateTimeMatcher.group(2);
                String minuteStr = dateTimeMatcher.group(3);

                try {
                    // 先将 hour/minute 转为整数，处理 OCR 可能识别出非标准小时（如 24,25,28）
                    int hour = Integer.parseInt(hourStr);
                    int minute = Integer.parseInt(minuteStr);

                    // 处理超过 24 小时的情况：将多出的天数加到日期上
                    int extraDays = 0;
                    if (hour >= 24) {
                        extraDays = hour / 24;
                        hour = hour % 24;
                    }

                    // 解析日期（支持不带前导零的情况）
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
                    LocalDate date = LocalDate.parse(dateStr, dateFormatter).plusDays(extraDays);
                    LocalTime time = LocalTime.of(hour, minute);
                    LocalDateTime remindTime = LocalDateTime.of(date, time);

                    reminder.setRemindTime(remindTime);

                    // 移除时间部分,剩余作为标题
                    String title = line.replaceAll(dateTimePattern.pattern(), "").trim();
                    reminder.setTitle(title.isEmpty() ? "待办事项" : title);
                } catch (Exception e) {
                    // 出现解析异常时，不抛出，保留整行作为标题以保证健壮性
                    reminder.setTitle(line);
                }
            }
            // 尝试匹配简单时间格式(如"明早9点")
            else {
                Matcher timeMatcher = timePattern.matcher(line);
                if (timeMatcher.find()) {
                    String timePrefix = timeMatcher.group(1); // 明天、下午等
                    int hour = Integer.parseInt(timeMatcher.group(2));
                    String minuteStr = timeMatcher.group(3);
                    int minute = (minuteStr != null && !minuteStr.isEmpty()) ? Integer.parseInt(minuteStr) : 0;

                    // 根据时间前缀计算提醒时间
                    LocalDateTime baseTime = LocalDateTime.now();
                    if ("明天".equals(timePrefix) || "明早".equals(timePrefix) || "明晚".equals(timePrefix)) {
                        baseTime = baseTime.plusDays(1);
                    } else if ("后天".equals(timePrefix)) {
                        baseTime = baseTime.plusDays(2);
                    }

                    // 调整小时(下午+12,晚上+12等)
                    if ("下午".equals(timePrefix) && hour < 12) {
                        hour += 12;
                    } else if ("晚上".equals(timePrefix) && hour < 12) {
                        hour += 12;
                    }

                    // 处理因 OCR 错误识别导致的 hour >= 24 的情况：转为相应的天数
                    if (hour >= 24) {
                        int extraDays = hour / 24;
                        hour = hour % 24;
                        baseTime = baseTime.plusDays(extraDays);
                    }

                    LocalDateTime remindTime = baseTime.withHour(hour).withMinute(minute).withSecond(0);
                    reminder.setRemindTime(remindTime);

                    // 移除时间部分,剩余作为标题
                    String title = line.replaceAll(timePattern.pattern(), "").trim();
                    reminder.setTitle(title.isEmpty() ? "待办事项" : title);
                } else {
                    // 没有时间信息,整行作为标题
                    reminder.setTitle(line);
                }
            }

            reminders.add(reminder);
        }

        return reminders;
    }
}
