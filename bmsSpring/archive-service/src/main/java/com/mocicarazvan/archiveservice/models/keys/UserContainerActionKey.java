package com.mocicarazvan.archiveservice.models.keys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserContainerActionKey {
    private String actionId;
    private String userId;
}
