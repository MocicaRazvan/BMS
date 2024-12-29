package com.mocicarazvan.archiveservice.dtos;

import com.mocicarazvan.archiveservice.dtos.generic.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Meal extends ManyToOneUser {
    private List<Long> recipes;
    private Long dayId;
    private String period;

    @Override
    public String toString() {
        return "Meal{" + "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "recipes=" + recipes +
                ", dayId=" + dayId +
                ", period='" + period + '\'' +
                '}';
    }
}
