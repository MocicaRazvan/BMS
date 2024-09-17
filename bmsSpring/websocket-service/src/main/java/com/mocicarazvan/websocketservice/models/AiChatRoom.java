package com.mocicarazvan.websocketservice.models;

import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class AiChatRoom extends IdGenerated implements Transformable<AiChatRoom> {

    @OneToOne(fetch = FetchType.EAGER)
    private ConversationUser user;
}
