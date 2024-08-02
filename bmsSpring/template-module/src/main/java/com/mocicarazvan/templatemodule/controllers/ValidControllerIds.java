package com.mocicarazvan.templatemodule.controllers;

import com.mocicarazvan.templatemodule.dtos.generic.WithUserDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ValidControllerIds<R extends WithUserDto> {

    @GetMapping(value = "/internal/validIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Mono<ResponseEntity<Void>> validIds(@RequestParam List<Long> ids);

    @GetMapping(value = "/internal/getByIds", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    Flux<R> getByIds(@RequestParam List<Long> ids);
}
