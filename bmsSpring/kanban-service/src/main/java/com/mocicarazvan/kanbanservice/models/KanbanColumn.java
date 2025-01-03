package com.mocicarazvan.kanbanservice.models;

import com.mocicarazvan.templatemodule.models.ManyToOneUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table("kanban_column")
public class KanbanColumn extends ManyToOneUser implements Cloneable {
    private String title;

    @Column("order_index")
    private int orderIndex;

    @Override
    public KanbanColumn clone() {
        return (KanbanColumn) super.clone();
    }
}
