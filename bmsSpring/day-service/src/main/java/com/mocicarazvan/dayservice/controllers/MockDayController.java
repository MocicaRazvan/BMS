package com.mocicarazvan.dayservice.controllers;

import com.mocicarazvan.dayservice.dtos.day.DayResponseWithMeals;
import com.mocicarazvan.templatemodule.controllers.MockItemController;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/days/mock")
public class MockDayController extends MockItemController<DayResponseWithMeals> {
    public MockDayController(MockItemService<DayResponseWithMeals> mockItemService, RequestsUtils requestsUtils) {
        super(mockItemService, requestsUtils);
    }
}
