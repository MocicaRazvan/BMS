package com.mocicarazvan.websocketservice.dtos.post;

import com.mocicarazvan.websocketservice.dtos.generic.ApproveResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
//@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostResponse extends ApproveResponse {

}
