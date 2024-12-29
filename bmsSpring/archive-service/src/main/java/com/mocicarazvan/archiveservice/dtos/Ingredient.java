package com.mocicarazvan.archiveservice.dtos;


import com.mocicarazvan.archiveservice.dtos.enums.DietType;
import com.mocicarazvan.archiveservice.dtos.generic.ManyToOneUser;
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

public class Ingredient extends ManyToOneUser {

    private String name;

    private DietType type;

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + getId() +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                "userId=" + getUserId() +
                "name='" + name + '\'' +
                ", type=" + type +
                ", display=" + display +
                '}';
    }

    private boolean display;
}
