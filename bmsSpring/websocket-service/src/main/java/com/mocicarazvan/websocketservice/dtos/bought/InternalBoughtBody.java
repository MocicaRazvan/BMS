package com.mocicarazvan.websocketservice.dtos.bought;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InternalBoughtBody {
    @NotBlank
    private String senderEmail;
    @NotEmpty
    private List<InnerPlanResponse> plans;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static final class InnerPlanResponse {
        private String id;
        private String title;
    }
}
