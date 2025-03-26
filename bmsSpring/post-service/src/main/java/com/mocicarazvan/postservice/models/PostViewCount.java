package com.mocicarazvan.postservice.models;

import com.mocicarazvan.postservice.models.keys.PostViewCountKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("post_view_count")
public class PostViewCount extends PostViewCountKey
        implements Persistable<PostViewCountKey> {

    private Long viewCount;

    @Override
    public PostViewCountKey getId() {
        return PostViewCountKey
                .builder()
                .postId(getPostId())
                .accessDate(getAccessDate())
                .build();
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
