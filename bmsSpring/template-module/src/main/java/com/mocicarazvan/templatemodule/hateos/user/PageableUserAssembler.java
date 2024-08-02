package com.mocicarazvan.templatemodule.hateos.user;


import com.mocicarazvan.templatemodule.controllers.UserController;
import com.mocicarazvan.templatemodule.dtos.UserDto;

public class PageableUserAssembler extends PageableResponseAssembler<UserDto, UserDtoAssembler> {
    public PageableUserAssembler(UserDtoAssembler itemAssembler, Class<? extends UserController> userController) {
        super(itemAssembler, userController);
    }
}
