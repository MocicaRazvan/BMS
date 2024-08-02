package com.mocicarazvan.kanbanservice.convertors;


import com.mocicarazvan.kanbanservice.enums.KanbanTaskType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class KanbanTaskTypeReadingConvertor extends BaseReadingConverter<KanbanTaskType> {
    public KanbanTaskTypeReadingConvertor() {
        super(KanbanTaskType.class);
    }
}
