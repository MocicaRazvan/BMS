package com.mocicarazvan.kanbanservice.convertors;


import com.mocicarazvan.kanbanservice.enums.KanbanTaskType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class KanbanTaskTypeWritingConvertor extends BaseWritingConverter<KanbanTaskType> {
    public KanbanTaskTypeWritingConvertor() {
        super(KanbanTaskType.class);
    }
}
