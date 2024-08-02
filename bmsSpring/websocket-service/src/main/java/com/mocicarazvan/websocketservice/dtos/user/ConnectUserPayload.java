package com.mocicarazvan.websocketservice.dtos.user;


import com.mocicarazvan.websocketservice.enums.ConnectedStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ConnectUserPayload {
    private ConnectedStatus connectedStatus;
}
