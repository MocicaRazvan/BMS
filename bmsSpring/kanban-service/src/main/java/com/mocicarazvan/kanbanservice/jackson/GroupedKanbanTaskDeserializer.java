package com.mocicarazvan.kanbanservice.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.kanbanservice.dtos.tasks.KanbanTaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class GroupedKanbanTaskDeserializer extends JsonDeserializer<Map<Long, List<KanbanTaskResponse>>> {
    private final ObjectMapper objectMapper;

    @Override
    public Map<Long, List<KanbanTaskResponse>> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {


        TypeReference<Map<Long, List<KanbanTaskResponse>>> typeRef = new TypeReference<>() {
        };
        return objectMapper.readValue(p, typeRef);

    }
}
