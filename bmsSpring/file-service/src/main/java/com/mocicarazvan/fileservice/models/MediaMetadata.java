package com.mocicarazvan.fileservice.models;

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
public class MediaMetadata extends Auditable {
    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String mediaId;
}
