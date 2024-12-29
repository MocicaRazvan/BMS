package com.mocicarazvan.archiveservice.dtos;


import com.mocicarazvan.archiveservice.dtos.enums.DayType;
import com.mocicarazvan.archiveservice.dtos.generic.TitleBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class Day extends TitleBody {
    private DayType type;

    @Override
    public String toString() {
        return "Day{" +
                "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "body='" + getBody() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", userLikes=" + getUserLikes() +
                ", userDislikes=" + getUserDislikes() +
                "type=" + type +
                '}';
    }
}
