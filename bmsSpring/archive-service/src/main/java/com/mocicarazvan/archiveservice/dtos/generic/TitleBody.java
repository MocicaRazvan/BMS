package com.mocicarazvan.archiveservice.dtos.generic;


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
public abstract class TitleBody extends ManyToOneUser {
    private String body;

    @Override
    public String toString() {
        return "TitleBody{" +
                "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "body='" + body + '\'' +
                ", title='" + title + '\'' +
                ", userLikes=" + userLikes +
                ", userDislikes=" + userDislikes +
                '}';
    }

    private String title;

    private List<Long> userLikes;
    private List<Long> userDislikes;
}
