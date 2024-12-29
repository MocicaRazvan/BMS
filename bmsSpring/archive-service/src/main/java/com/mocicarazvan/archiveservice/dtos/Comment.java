package com.mocicarazvan.archiveservice.dtos;


import com.mocicarazvan.archiveservice.dtos.enums.CommentReferenceType;
import com.mocicarazvan.archiveservice.dtos.generic.TitleBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

public class Comment extends TitleBody {
    private Long referenceId;
    private CommentReferenceType referenceType;

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "body='" + getBody() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", userLikes=" + getUserLikes() +
                ", userDislikes=" + getUserDislikes() +
                "referenceId=" + referenceId +
                ", referenceType=" + referenceType +
                '}';
    }
}
