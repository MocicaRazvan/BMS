package com.mocicarazvan.websocketservice.dtos.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcResponse<T> {

    private Throwable error;
    private T data;

    public boolean isErrored() {
        return error != null;
    }

    public boolean isSuccessful() {
        return data != null;
    }
}
