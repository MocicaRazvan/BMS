package com.mocicarazvan.templatemodule.dtos.response;

import com.mocicarazvan.templatemodule.dtos.generic.IdGenerateDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MonthlyEntityGroup<M> {
    private M entity;
    private int month;
    private int year;
}
