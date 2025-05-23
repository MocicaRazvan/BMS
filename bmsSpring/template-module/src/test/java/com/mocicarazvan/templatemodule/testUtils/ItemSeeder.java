package com.mocicarazvan.templatemodule.testUtils;

import com.mocicarazvan.templatemodule.models.AssociativeEntityImpl;
import com.mocicarazvan.templatemodule.models.IdGeneratedImpl;
import com.mocicarazvan.templatemodule.models.ManyToOneUserImpl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ItemSeeder {

    public static List<IdGeneratedImpl> generateIdGenerated() {
        return IntStream.range(0, 10).mapToObj(i -> {
            var createdAt = LocalDateTime.now().minusDays(i * 10L).truncatedTo(ChronoUnit.MICROS);
            var updatedAt = LocalDateTime.now().minusDays(i).truncatedTo(ChronoUnit.MICROS);
            return IdGeneratedImpl.builder()
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }).collect(Collectors.toUnmodifiableList());
    }

    public static List<ManyToOneUserImpl> generateManyToOne() {
        return IntStream.range(0, 10).mapToObj(i -> {
            var createdAt = LocalDateTime.now().minusDays(i * 10L).truncatedTo(ChronoUnit.MICROS);
            var updatedAt = LocalDateTime.now().minusDays(i).truncatedTo(ChronoUnit.MICROS);
            var userId = i % 2 == 0 ? 1L : 2L;
            return ManyToOneUserImpl.builder()
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .userId(userId)
                    .build();
        }).collect(Collectors.toUnmodifiableList());
    }

    public static List<AssociativeEntityImpl> generateAssociativeEntity() {
        return IntStream.range(0, 10).mapToObj(i -> AssociativeEntityImpl.builder()
                .masterId(i % 2 == 0 ? 1L : 2L)
                .childId((long) i)
                .build()).collect(Collectors.toUnmodifiableList());
    }
}
