package com.mocicarazvan.postservice.controllers;


import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.templatemodule.controllers.MockItemController;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/mock")
public class MockPostController extends MockItemController<PostResponse> {

    public MockPostController(MockItemService<PostResponse> mockItemService, RequestsUtils requestsUtils) {
        super(mockItemService, requestsUtils);
    }
}
