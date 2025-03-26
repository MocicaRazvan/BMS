package com.mocicarazvan.postservice.dtos.summaries;

import com.mocicarazvan.postservice.dtos.PostResponse;
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
public class PostCountSummaryResponse extends PostResponse {
    private Long viewCount;
    private int rank;

    public static PostCountSummaryResponse fromResponse(PostResponse postResponse, Long viewCount, int rank) {
        return PostCountSummaryResponse.builder()
                .id(postResponse.getId())
                .createdAt(postResponse.getCreatedAt())
                .updatedAt(postResponse.getUpdatedAt()).userId(postResponse.getUserId())
                .body(postResponse.getBody()).title(postResponse.getTitle()).userDislikes(postResponse.getUserDislikes()).userLikes(postResponse.getUserLikes())
                .images(postResponse.getImages()).approved(postResponse.isApproved()).tags(postResponse.getTags())
                .viewCount(viewCount).rank(rank).build();
    }
}
