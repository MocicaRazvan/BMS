package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;

public interface RabbitMqApprovedSenderWrapper<T extends ApproveDto> {
    Void sendMessage(boolean approved, ResponseWithUserDto<T> model, UserDto authUser);
}
