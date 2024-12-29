package com.mocicarazvan.archiveservice.dtos;

import com.mocicarazvan.archiveservice.dtos.enums.DietType;
import com.mocicarazvan.archiveservice.dtos.generic.Approve;
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
public class Recipe extends Approve {
    private List<String> videos;

    private DietType type;

    @Override
    public String toString() {
        return "Recipe{" + "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "body='" + getBody() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", userLikes=" + getUserLikes() +
                ", userDislikes=" + getUserDislikes() +
                "images=" + getImages() +
                "approved=" + isApproved() +
                "videos=" + videos +
                ", type=" + type +
                '}';
    }
}
