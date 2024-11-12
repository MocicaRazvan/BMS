package com.mocicarazvan.fileservice.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Media {
    @Id
    private String id;
    private String fileName;
    private long fileSize;
    private String fileType;
    @Indexed(unique = true)
    private String gridFsId;
}
