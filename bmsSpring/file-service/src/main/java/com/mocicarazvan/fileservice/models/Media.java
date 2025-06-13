package com.mocicarazvan.fileservice.models;


import com.mocicarazvan.fileservice.enums.CustomMediaType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Media extends Auditable {
    @Id
    private String id;
    private String fileName;
    private long fileSize;
    private String fileType;
    @Indexed(unique = true)
    private String gridFsId;
    private String mediaType;
    @Indexed(partialFilter = "{ toBeDeleted: true }")
    @Builder.Default
    private Boolean toBeDeleted = false;


    public String getMediaType() {
        return mediaType != null ? mediaType : CustomMediaType.fromFileName(fileName).getValue();
    }


}
