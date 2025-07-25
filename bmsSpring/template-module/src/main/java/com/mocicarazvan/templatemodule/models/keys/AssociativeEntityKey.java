package com.mocicarazvan.templatemodule.models.keys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AssociativeEntityKey {
    private Long masterId;

    private Long childId;
}
