package com.livevote.service.impl;

import com.livevote.dto.LoginRequest;
import com.livevote.dto.Response;
import com.livevote.entity.User;
import com.livevote.repository.UserRepository;
import com.livevote.service.interfac.UserServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import static com.livevote.utils.Utility.*;

@Service
public class UserServiceImpl implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;


    @Override
    public Response login(LoginRequest loginRequest) {
        Response response = new Response();
        if (StringUtils.isEmpty(loginRequest.getPassword()) || StringUtils.isEmpty(loginRequest.getUsername())) {
            response.setMessage("Login Request is empty");
            response.setStatusCode(STATUS_INVALID_REQUEST);
            return response;
        }

        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getHashedPassword())) {
            response.setMessage("Username or password is empty");
            response.setStatusCode(STATUS_INVALID_REQUEST);
        }
        if (StringUtils.equals(user.getHashedPassword(), loginRequest.getPassword()) &&
                StringUtils.equals(user.getUsername(), loginRequest.getUsername())){
            response.setMessage("Password is correct, Login Successfully");
            response.setStatusCode(STATUS_SUCCESSFUL);
        } else {
            response.setMessage("Password is incorrect, Login Unsuccessfully");
            response.setStatusCode(STATUS_NOT_FOUND);
        }
        return response;
    }
}
