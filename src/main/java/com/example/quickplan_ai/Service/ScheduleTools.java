package com.example.quickplan_ai.Service;

import com.example.quickplan_ai.entity.Schedule;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日程管理工具类
 * 为AI提供日程管理能力
 */
@Component
public class ScheduleTools {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleTools.class);

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 添加日程工具
     * 当用户说"帮我添加日程"时,AI会调用此方法
     * 
     * @param userId      用户ID (必需)
     * @param title       日程标题/任务 (必需)
     * @param date        日期(格式: yyyy-MM-dd) (必需)
     * @param time        时间(格式: HH:mm) (必需)
     * @param location    地点 (可选,默认"未指定")
     * @param description 描述 (可选,默认为空)
     * @return 添加结果
     */
    @Tool("添加日程到用户日历。必需参数: userId(用户ID), title(标题), date(日期yyyy-MM-dd), time(时间HH:mm)。可选参数: location(地点,默认'未指定'), description(描述,默认空)")
    public String addSchedule(
            String userId,
            String title,
            String date,
            String time,
            String location,
            String description) {

        try {
            logger.info("✓ AI工具调用: 添加日程");
            logger.info("  - userId: {}", userId);
            logger.info("  - title: {}", title);
            logger.info("  - date: {}", date);
            logger.info("  - time: {}", time);
            logger.info("  - location: {}", location != null && !location.isEmpty() ? location : "未指定");
            logger.info("  - description: {}", description != null && !description.isEmpty() ? description : "(无)");

            // 处理可选参数的默认值
            if (location == null || location.trim().isEmpty()) {
                location = "未指定";
                logger.info("  - 地点未提供,使用默认值: 未指定");
            }
            if (description == null) {
                description = "";
            }

            // 解析日期和时间
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            java.time.LocalDate scheduleDate = java.time.LocalDate.parse(date, dateFormatter);
            java.time.LocalTime scheduleTime = java.time.LocalTime.parse(time, timeFormatter);

            // 创建日程对象
            Schedule schedule = new Schedule();
            schedule.setUserId(userId);
            schedule.setTitle(title);
            schedule.setDate(scheduleDate);
            schedule.setTime(scheduleTime);
            schedule.setLocation(location);
            schedule.setDescription(description.isEmpty() ? null : description);

            // 保存到数据库
            scheduleService.createSchedule(schedule);

            logger.info("✅ 日程添加成功: scheduleId={}", schedule.getId());

            return String.format("✅ 日程添加成功!\n标题: %s\n日期: %s\n时间: %s\n地点: %s",
                    title, date, time, location);

        } catch (DateTimeParseException e) {
            logger.error("时间格式解析失败: {}", e.getMessage());
            return "❌ 时间格式错误,日期格式: yyyy-MM-dd (如2025-10-30), 时间格式: HH:mm (如14:00)";

        } catch (Exception e) {
            logger.error("添加日程失败: {}", e.getMessage(), e);
            return "❌ 添加日程失败: " + e.getMessage();
        }
    }

    /**
     * 查询用户的日程列表
     * 
     * @param userId 用户ID
     * @param date   日期(格式: yyyy-MM-dd)
     * @return 日程列表
     */
    @Tool("查询用户指定日期的日程安排。当用户询问某天有什么安排、今天要做什么时使用此工具。")
    public String getSchedulesByDate(String userId, String date) {
        try {
            logger.info("AI工具调用: 查询日程 - userId={}, date={}", userId, date);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            java.time.LocalDate targetDate = java.time.LocalDate.parse(date, dateFormatter);

            var schedules = scheduleService.getSchedulesByDate(userId, targetDate);

            if (schedules.isEmpty()) {
                return String.format("📅 %s 没有安排任何日程", date);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("📅 %s 的日程安排:\n\n", date));

            int index = 1;
            for (Schedule schedule : schedules) {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                result.append(String.format("%d. %s\n", index++, schedule.getTitle()));
                result.append(String.format("   时间: %s\n",
                        schedule.getTime().format(timeFormatter)));
                if (schedule.getLocation() != null) {
                    result.append(String.format("   地点: %s\n", schedule.getLocation()));
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            logger.error("查询日程失败: {}", e.getMessage(), e);
            return "❌ 查询日程失败: " + e.getMessage();
        }
    }

    /**
     * 删除日程
     * 
     * @param userId     用户ID
     * @param scheduleId 日程ID
     * @return 删除结果
     */
    @Tool("删除用户的日程。当用户要求取消、删除某个日程时使用此工具。")
    public String deleteSchedule(String userId, String scheduleId) {
        try {
            logger.info("AI工具调用: 删除日程 - userId={}, scheduleId={}", userId, scheduleId);

            // 验证日程是否属于该用户
            Schedule schedule = scheduleService.getScheduleById(scheduleId);
            if (schedule == null) {
                return "❌ 日程不存在";
            }

            if (!schedule.getUserId().equals(userId)) {
                return "❌ 无权删除此日程";
            }

            scheduleService.deleteSchedule(scheduleId);

            return String.format("✅ 已删除日程: %s", schedule.getTitle());

        } catch (Exception e) {
            logger.error("删除日程失败: {}", e.getMessage(), e);
            return "❌ 删除日程失败: " + e.getMessage();
        }
    }
}
