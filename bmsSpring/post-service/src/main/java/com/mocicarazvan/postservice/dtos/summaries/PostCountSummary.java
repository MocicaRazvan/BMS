package com.mocicarazvan.postservice.dtos.summaries;

import com.mocicarazvan.postservice.models.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCountSummary extends Post {
    private Long viewCount;
    private int rank;
}
