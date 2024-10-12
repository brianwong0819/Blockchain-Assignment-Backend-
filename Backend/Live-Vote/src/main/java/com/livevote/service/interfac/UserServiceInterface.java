package com.livevote.service.interfac;

import com.livevote.dto.LoginRequest;
import com.livevote.dto.Response;

public interface UserServiceInterface {
    Response login(LoginRequest loginRequest);
}
