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
public class IdNameResponse extends BaseErrorResponse {
    private String name;
    private Long id;

    public IdNameResponse withBase(BaseErrorResponse baseErrorResponse, String name, Long id) {
        return IdNameResponse.builder()
                .error(baseErrorResponse.getError())
                .message(baseErrorResponse.getMessage())
                .path(baseErrorResponse.getPath())
                .status(baseErrorResponse.getStatus())
                .timestamp(baseErrorResponse.getTimestamp())
                .name(name)
                .id(id)
                .build();

    }
}
