package com.mocicarazvan.planservice.dtos.dayClient.collect;

import com.mocicarazvan.planservice.dtos.dayClient.DayResponse;
import com.mocicarazvan.templatemodule.utils.Transformable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class FullDayResponse extends DayResponse implements Transformable<FullDayResponse> {
    private List<FullMealResponse> meals;

    public static FullDayResponse fromDayResponse(DayResponse dayResponse) {
        return FullDayResponse.builder()
                .type(dayResponse.getType())
                .body(dayResponse.getBody())
                .title(dayResponse.getTitle())
                .userDislikes(dayResponse.getUserDislikes())
                .userLikes(dayResponse.getUserLikes())
                .userId(dayResponse.getUserId())
                .id(dayResponse.getId())
                .createdAt(dayResponse.getCreatedAt())
                .updatedAt(dayResponse.getUpdatedAt())
                .build();
    }
}
