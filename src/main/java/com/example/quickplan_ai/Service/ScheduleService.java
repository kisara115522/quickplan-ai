package com.example.quickplan_ai.Service;

import com.example.quickplan_ai.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

/**
 * 日程Service接口
 */
public interface ScheduleService {

    /**
     * 创建日程
     */
    Schedule createSchedule(Schedule schedule);

    /**
     * 根据ID获取日程
     */
    Schedule getScheduleById(String id);

    /**
     * 获取用户所有日程
     */
    List<Schedule> getUserSchedules(String userId);

    /**
     * 获取用户指定日期范围内的日程
     */
    List<Schedule> getSchedulesByDateRange(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取用户指定日期的日程
     */
    List<Schedule> getSchedulesByDate(String userId, LocalDate date);

    /**
     * 更新日程
     */
    boolean updateSchedule(Schedule schedule);

    /**
     * 删除日程
     */
    boolean deleteSchedule(String id);

    /**
     * 统计用户日程数量
     */
    long countUserSchedules(String userId);
}
