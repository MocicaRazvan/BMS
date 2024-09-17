package com.mocicarazvan.planservice.models;

import com.mocicarazvan.planservice.enums.DietType;
import com.mocicarazvan.planservice.enums.ObjectiveType;
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
@Table("plan")
public class Plan extends Approve {
    private double price;

    private List<Long> days;

    private DietType type;

    private boolean display;

    private ObjectiveType objective;
}
