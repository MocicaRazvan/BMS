package com.mocicarazvan.commentservice.dtos.post;


import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "The post response dto")
public class PostResponse extends ApproveDto {
    @Schema(description = "The tags contained in the post.")
    private List<String> tags;
}
