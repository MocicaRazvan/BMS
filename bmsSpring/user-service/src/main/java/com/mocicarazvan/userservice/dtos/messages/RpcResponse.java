package com.mocicarazvan.userservice.dtos.messages;

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

    public static <T> RpcResponse<T> success(T data) {
        return RpcResponse.<T>builder().data(data).build();
    }

    public static <T> RpcResponse<T> error(Throwable error) {
        return RpcResponse.<T>builder().error(error).build();
    }
}
