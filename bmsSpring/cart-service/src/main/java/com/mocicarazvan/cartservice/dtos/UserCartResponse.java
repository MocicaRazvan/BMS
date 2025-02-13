package com.mocicarazvan.cartservice.dtos;

import com.mocicarazvan.cartservice.dtos.clients.PlanResponse;
import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class UserCartResponse extends WithUserDto {
    private List<PlanResponse> plans;
}
