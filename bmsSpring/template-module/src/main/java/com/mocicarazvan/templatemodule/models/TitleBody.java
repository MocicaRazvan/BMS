package com.mocicarazvan.templatemodule.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class TitleBody extends ManyToOneUser implements Cloneable {
    private String body;
    private String title;

    @Column("user_likes")
    private List<Long> userLikes;
    @Column("user_dislikes")
    private List<Long> userDislikes;

    @Override
    public TitleBody clone() {
        TitleBody clone = (TitleBody) super.clone();
        clone.setUserLikes(new ArrayList<>(Optional.ofNullable(userLikes).orElseGet(ArrayList::new)));
        clone.setUserDislikes(new ArrayList<>(Optional.ofNullable(userDislikes).orElseGet(ArrayList::new)));
        return clone;
    }
}
