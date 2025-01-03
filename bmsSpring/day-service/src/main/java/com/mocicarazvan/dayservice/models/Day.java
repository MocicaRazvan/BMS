package com.mocicarazvan.dayservice.models;

import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.templatemodule.models.TitleBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("day")
public class Day extends TitleBody implements Cloneable {
    private DayType type;

    @Override
    public String toString() {
        return "Day{" +
                "id=" + getId() +
                ", userId='" + getUserId() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", type=" + type +
                ", body='" + getBody() + '\'' +
                ", userLikes=" + getUserLikes() +
                ", userDislikes=" + getUserDislikes() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +

                '}';
    }

    @Override
    public Day clone() {
        return (Day) super.clone();
    }
}
