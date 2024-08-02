package com.mocicarazvan.commentservice.convertors;

import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.templatemodule.convertors.BaseWritingConverter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class CommentReferenceTypeWritingConvertor extends BaseWritingConverter<CommentReferenceType> {
    public CommentReferenceTypeWritingConvertor() {
        super(CommentReferenceType.class);
    }
}
