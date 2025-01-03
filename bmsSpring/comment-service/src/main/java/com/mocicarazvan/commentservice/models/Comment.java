package com.mocicarazvan.commentservice.models;

import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.templatemodule.models.TitleBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "comment")
public class Comment extends TitleBody implements Cloneable {
    @Column("reference_id")
    private Long referenceId;
    @Column("reference_type")
    private CommentReferenceType referenceType;

    @Override
    public Comment clone() {
        return (Comment) super.clone();

    }
}
