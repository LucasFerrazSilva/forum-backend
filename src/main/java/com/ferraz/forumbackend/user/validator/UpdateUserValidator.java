package com.ferraz.forumbackend.user.validator;

import com.ferraz.forumbackend.user.dto.UpdateUserDTO;

public interface UpdateUserValidator {
    void validate(String username, UpdateUserDTO updateUserDTO);
}
