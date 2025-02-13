package com.mocicarazvan.cartservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class UserCartBody {
    private List<Long> planIds;
}
