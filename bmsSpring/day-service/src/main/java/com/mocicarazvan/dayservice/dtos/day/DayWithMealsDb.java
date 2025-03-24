package com.mocicarazvan.dayservice.dtos.day;

import com.mocicarazvan.dayservice.enums.DayType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("day")
public class DayWithMealsDb {
    private DayType type;
    private String body;
    private Long userId;
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> userLikes;
    private List<Long> userDislikes;
    private String title;
    private String meals;

}
