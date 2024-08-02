package com.mocicarazvan.orderservice.dtos.notifications;

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
    private String senderEmail;
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
