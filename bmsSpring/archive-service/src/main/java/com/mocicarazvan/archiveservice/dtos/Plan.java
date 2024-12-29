package com.mocicarazvan.archiveservice.dtos;


import com.mocicarazvan.archiveservice.dtos.enums.DietType;
import com.mocicarazvan.archiveservice.dtos.enums.ObjectiveType;
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
public class Plan extends Approve {
    private double price;

    private List<Long> days;

    private DietType type;

    private boolean display;

    private ObjectiveType objective;

    @Override
    public String toString() {
        return "Plan{" + "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "body='" + getBody() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", userLikes=" + getUserLikes() +
                ", userDislikes=" + getUserDislikes() +
                "images=" + getImages() +
                "approved=" + isApproved() +
                "price=" + price +
                ", days=" + days +
                ", type=" + type +
                ", display=" + display +
                ", objective=" + objective +
                '}';
    }
}
