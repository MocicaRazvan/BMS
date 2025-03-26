package com.mocicarazvan.postservice.models.keys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostViewCountKey {
    private Long postId;
    private Long accessDate;
}
