package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.models.generic.ApprovedModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
//@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "chat_plan", indexes = {
        @Index(name = "idx_receiver_approve_plan_model_id", columnList = "receiver_id"),
})

public class Plan extends ApprovedModel {
}
