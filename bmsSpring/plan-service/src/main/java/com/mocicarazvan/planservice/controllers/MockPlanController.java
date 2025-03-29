package com.mocicarazvan.planservice.controllers;

import com.mocicarazvan.planservice.dtos.PlanResponse;
import com.mocicarazvan.templatemodule.controllers.MockItemController;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plans/mock")
public class MockPlanController extends MockItemController<PlanResponse> {
    public MockPlanController(MockItemService<PlanResponse> mockItemService, RequestsUtils requestsUtils) {
        super(mockItemService, requestsUtils);
    }
}
