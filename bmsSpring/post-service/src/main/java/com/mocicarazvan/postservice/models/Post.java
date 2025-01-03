package com.mocicarazvan.postservice.models;

import com.mocicarazvan.templatemodule.models.Approve;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("post")
public class Post extends Approve implements Cloneable {
    public List<String> tags;

    @Override
    public Post clone() {
        Post clone = (Post) super.clone();
        clone.setTags(List.copyOf(tags));
        return clone;
    }
}
