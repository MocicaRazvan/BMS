package com.mocicarazvan.websocketservice.listeners;

import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationBody;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.enums.ApprovedNotificationType;
import com.mocicarazvan.websocketservice.models.Recipe;
import com.mocicarazvan.websocketservice.service.ApproveRecipeNotificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {RecipeApproveQueListener.class})
class RecipeApproveQueListenerTest extends BaseApproveListenerTest<Recipe, RecipeResponse, ApproveRecipeNotificationBody, ApproveRecipeNotificationResponse> {

    @MockBean
    ApproveRecipeNotificationService approveRecipeNotificationService;

    @Value("${recipe.queue.name}")
    private String recipeQueueName;

    protected RecipeApproveQueListenerTest() {
        super(ApproveRecipeNotificationBody.class);
    }

    @Override
    protected ApproveRecipeNotificationBody createBody() {
        return ApproveRecipeNotificationBody.builder()
                .senderEmail("sender@example.com")
                .receiverEmail("receiver@example.com")
                .type(ApprovedNotificationType.APPROVED)
                .referenceId(1L)
                .content("content")
                .extraLink("extraLink")
                .build();
    }

    @Override
    protected ApproveRecipeNotificationBody createInvalidBody() {
        return ApproveRecipeNotificationBody.builder()
                .senderEmail("sender@example.com")
                .receiverEmail("receiver@example.com")
                .type(ApprovedNotificationType.APPROVED)
                .content("content")
                .extraLink("extraLink")
                .build();
    }

    @Override
    protected ApproveRecipeNotificationResponse createResponse() {
        return new ApproveRecipeNotificationResponse();
    }

    @PostConstruct
    void init() {
        setQueueName(recipeQueueName);
        setServiceTemplate(approveRecipeNotificationService);
    }
}