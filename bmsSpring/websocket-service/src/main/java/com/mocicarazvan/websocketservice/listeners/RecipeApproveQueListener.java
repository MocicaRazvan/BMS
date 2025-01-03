package com.mocicarazvan.websocketservice.listeners;


import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationBody;
import com.mocicarazvan.websocketservice.dtos.recipe.ApproveRecipeNotificationResponse;
import com.mocicarazvan.websocketservice.dtos.recipe.RecipeResponse;
import com.mocicarazvan.websocketservice.models.Recipe;
import com.mocicarazvan.websocketservice.service.ApproveRecipeNotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component

public class RecipeApproveQueListener
        extends ApproveQueListener<Recipe, RecipeResponse, ApproveRecipeNotificationBody, ApproveRecipeNotificationResponse> {


    public RecipeApproveQueListener(ApproveRecipeNotificationService approveRecipeNotificationService) {
        super(approveRecipeNotificationService);
    }

    @RabbitListener(queues = "#{@environment['recipe.queue.name']}", executor = "scheduledExecutorService")
    public void listen(ApproveRecipeNotificationBody message) {
        super.listen(message);
    }

}
