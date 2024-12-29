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
public abstract class TitleBodyImages extends TitleBody {
    private List<String> images;

    @Override
    public String toString() {
        return "TitleBodyImages{" +
                "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "body='" + getBody() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", userLikes=" + getUserLikes() +
                ", userDislikes=" + getUserDislikes() +
                "images=" + images +
                '}';
    }
}
