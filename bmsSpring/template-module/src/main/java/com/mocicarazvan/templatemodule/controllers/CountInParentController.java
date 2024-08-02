package com.mocicarazvan.templatemodule.controllers;

import com.mocicarazvan.templatemodule.dtos.response.EntityCount;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface CountInParentController {
    @GetMapping(value = "/internal/count/{childId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Mono<ResponseEntity<EntityCount>> getCountInParent(@PathVariable Long childId);
}
