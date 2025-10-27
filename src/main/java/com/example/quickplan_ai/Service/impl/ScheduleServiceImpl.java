package com.example.quickplan_ai.Service.impl;

import com.example.quickplan_ai.Service.ScheduleService;
import com.example.quickplan_ai.entity.Schedule;
import com.example.quickplan_ai.mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 日程Service实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Override
    public Schedule createSchedule(Schedule schedule) {
        if (schedule.getId() == null || schedule.getId().isBlank()) {
            schedule.setId(UUID.randomUUID().toString());
        }
        schedule.setIsDeleted(0);
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());

        scheduleMapper.insert(schedule);
        return schedule;
    }

    @Override
    public Schedule getScheduleById(String id) {
        return scheduleMapper.selectById(id);
    }

    @Override
    public List<Schedule> getUserSchedules(String userId) {
        return scheduleMapper.selectByUserId(userId);
    }

    @Override
    public List<Schedule> getSchedulesByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public List<Schedule> getSchedulesByDate(String userId, LocalDate date) {
        return scheduleMapper.selectByUserIdAndDate(userId, date);
    }

    @Override
    public boolean updateSchedule(Schedule schedule) {
        schedule.setUpdatedAt(LocalDateTime.now());
        return scheduleMapper.updateById(schedule) > 0;
    }

    @Override
    public boolean deleteSchedule(String id) {
        return scheduleMapper.deleteById(id) > 0;
    }

    @Override
    public long countUserSchedules(String userId) {
        return scheduleMapper.countByUserId(userId);
    }
}
