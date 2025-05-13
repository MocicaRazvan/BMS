package com.mocicarazvan.userservice.dbCallbacks;

import com.mocicarazvan.templatemodule.dbCallbacks.IdGeneratedBeforeSaveCallback;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.utils.EmailNormalizerWrapperHolder;

public class UserCustomBeforeSaveCallback extends IdGeneratedBeforeSaveCallback<UserCustom> {

    @Override
    protected UserCustom handleBeforeSave(UserCustom entity) {
        UserCustom e = super.handleBeforeSave(entity);
        e.setEmail(EmailNormalizerWrapperHolder.EmailNormalizer.normalize(entity.getEmail()));
        return e;
    }
}
