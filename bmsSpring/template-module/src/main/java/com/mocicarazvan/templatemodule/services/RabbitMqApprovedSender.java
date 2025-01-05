package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.dtos.UserDto;
import com.mocicarazvan.templatemodule.dtos.generic.ApproveDto;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;

public interface RabbitMqApprovedSender<T extends ApproveDto> {
    void sendMessage(boolean approved, ResponseWithUserDto<T> model, UserDto authUser);
}
