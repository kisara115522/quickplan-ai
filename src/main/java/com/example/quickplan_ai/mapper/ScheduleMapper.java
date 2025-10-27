package com.example.quickplan_ai.mapper;

import com.example.quickplan_ai.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 日程 Mapper接口
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 插入日程
     */
    @Insert("INSERT INTO schedule(id, user_id, title, location, date, time, description, is_deleted) " +
            "VALUES(#{id}, #{userId}, #{title}, #{location}, #{date}, #{time}, #{description}, #{isDeleted})")
    int insert(Schedule schedule);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM schedule WHERE id = #{id} AND is_deleted = 0")
    Schedule selectById(String id);

    /**
     * 根据用户ID查询所有日程
     */
    @Select("SELECT * FROM schedule WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY date ASC, time ASC")
    List<Schedule> selectByUserId(String userId);

    /**
     * 查询用户指定日期范围内的日程
     */
    @Select("SELECT * FROM schedule WHERE user_id = #{userId} " +
            "AND date BETWEEN #{startDate} AND #{endDate} " +
            "AND is_deleted = 0 ORDER BY date ASC, time ASC")
    List<Schedule> selectByUserIdAndDateRange(@Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询用户指定日期的日程
     */
    @Select("SELECT * FROM schedule WHERE user_id = #{userId} AND date = #{date} " +
            "AND is_deleted = 0 ORDER BY time ASC")
    List<Schedule> selectByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 更新日程
     */
    @Update("UPDATE schedule SET title = #{title}, location = #{location}, " +
            "date = #{date}, time = #{time}, description = #{description} WHERE id = #{id}")
    int updateById(Schedule schedule);

    /**
     * 逻辑删除
     */
    @Update("UPDATE schedule SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(String id);

    /**
     * 统计用户日程数量
     */
    @Select("SELECT COUNT(*) FROM schedule WHERE user_id = #{userId} AND is_deleted = 0")
    long countByUserId(String userId);
}
