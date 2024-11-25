package com.mocicarazvan.dayservice.dtos.day;

import com.mocicarazvan.dayservice.enums.DayType;
import com.mocicarazvan.templatemodule.dtos.generic.TitleBodyUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class DayResponse extends TitleBodyUserDto {
    private DayType type;

    @Override
    public String toString() {
        return "DayResponse{" +
                "type=" + type +
                ", id=" + this.getId() +
                ", title='" + this.getTitle() + '\'' +
                ", body='" + getBody() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
