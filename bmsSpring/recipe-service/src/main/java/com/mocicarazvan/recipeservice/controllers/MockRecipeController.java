package com.mocicarazvan.recipeservice.controllers;


import com.mocicarazvan.recipeservice.dtos.RecipeResponse;
import com.mocicarazvan.templatemodule.controllers.MockItemController;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.RequestsUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipes/mock")
public class MockRecipeController extends MockItemController<RecipeResponse> {
    public MockRecipeController(MockItemService<RecipeResponse> mockItemService, RequestsUtils requestsUtils) {
        super(mockItemService, requestsUtils);
    }
}
