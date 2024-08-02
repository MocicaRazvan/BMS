package com.mocicarazvan.templatemodule.dtos.errors;


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
public class SubEntityOwnerResponse extends BaseErrorResponse {
    private Long expectedUserId;
    private Long receivedUserId;
    private Long entityId;

    public SubEntityOwnerResponse withBase(BaseErrorResponse baseErrorResponse, Long expectedUserId, Long receivedUserId, Long entityId) {
        return SubEntityOwnerResponse.builder()
                .error(baseErrorResponse.getError())
                .message(baseErrorResponse.getMessage())
                .path(baseErrorResponse.getPath())
                .status(baseErrorResponse.getStatus())
                .timestamp(baseErrorResponse.getTimestamp())
                .expectedUserId(expectedUserId)
                .receivedUserId(receivedUserId)
                .entityId(entityId)
                .build();
    }
}
