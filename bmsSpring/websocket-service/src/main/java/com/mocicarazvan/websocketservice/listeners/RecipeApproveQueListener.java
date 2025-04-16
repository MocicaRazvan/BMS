package com.mocicarazvan.websocketservice.listeners;


import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationBody;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.models.Recipe;
import com.mocicarazvan.websocketservice.service.ApproveRecipeNotificationService;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class RecipeApproveQueListener
        extends ApproveQueListener<Recipe, RecipeResponse, ApproveRecipeNotificationBody, ApproveRecipeNotificationResponse> {


    public RecipeApproveQueListener(ApproveRecipeNotificationService approveRecipeNotificationService) {
        super(approveRecipeNotificationService);
    }

    @RabbitListener(queues = "#{@environment['recipe.queue.name']}")
    public void listen(@Valid @Payload ApproveRecipeNotificationBody message) {
        super.listen(message);
    }

}
