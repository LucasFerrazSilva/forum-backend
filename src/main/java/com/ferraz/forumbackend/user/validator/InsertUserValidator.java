package com.ferraz.forumbackend.user.validator;

import com.ferraz.forumbackend.user.dto.NewUserDTO;

public interface InsertUserValidator {

    void validate(NewUserDTO newUserDTO);

}
