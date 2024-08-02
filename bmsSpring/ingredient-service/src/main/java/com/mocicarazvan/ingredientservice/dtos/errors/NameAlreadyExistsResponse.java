package com.mocicarazvan.ingredientservice.dtos.errors;


import com.mocicarazvan.ingredientservice.exceptions.NameAlreadyExists;
import com.mocicarazvan.templatemodule.dtos.errors.BaseErrorResponse;
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
public class NameAlreadyExistsResponse extends BaseErrorResponse {
    private String name;

    public NameAlreadyExistsResponse withBase(BaseErrorResponse baseErrorResponse, NameAlreadyExists e) {
        return NameAlreadyExistsResponse.builder()
                .error(baseErrorResponse.getError())
                .message(baseErrorResponse.getMessage())
                .path(baseErrorResponse.getPath())
                .status(baseErrorResponse.getStatus())
                .timestamp(baseErrorResponse.getTimestamp())
                .name(e.getName())
                .build();
    }
}
