package com.mocicarazvan.commentservice.convertors;

import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.templatemodule.convertors.BaseReadingConverter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class CommentReferenceTypeReadingConvertor extends BaseReadingConverter<CommentReferenceType> {
    public CommentReferenceTypeReadingConvertor() {
        super(CommentReferenceType.class);
    }
}
