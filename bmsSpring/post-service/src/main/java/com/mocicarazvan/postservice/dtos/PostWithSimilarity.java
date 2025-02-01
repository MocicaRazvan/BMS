package com.mocicarazvan.postservice.dtos;

import com.mocicarazvan.postservice.models.Post;
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
public class PostWithSimilarity extends Post {
    private Double similarity;
}
