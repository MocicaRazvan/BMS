package com.mocicarazvan.websocketservice.repositories;

import com.mocicarazvan.websocketservice.models.Plan;
import com.mocicarazvan.websocketservice.repositories.generic.ApproveRepository;

import java.util.Optional;

public interface PlanRepository extends ApproveRepository<Plan> {

    Optional<Plan> findByAppIdAndApprovedTrue(Long appId);
}
