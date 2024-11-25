package com.mocicarazvan.websocketservice.models;


import com.mocicarazvan.websocketservice.models.generic.IdGenerated;
import com.mocicarazvan.websocketservice.utils.Transformable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class ChatRoom extends IdGenerated implements Transformable<ChatRoom> {

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "chat_room_users",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            indexes = {
                    @Index(name = "idx_chat_room_id_chat_room", columnList = "chat_room_id"),
                    @Index(name = "idx_user_id_chat_room", columnList = "user_id")
            }
    )
    private List<ConversationUser> users;


    @Override
    public String toString() {
        return "ChatRoom{" +
                "users=" + users.stream().map(ConversationUser::getEmail).toList() +
                ", id=" + super.getId() +
                '}';
    }
}
