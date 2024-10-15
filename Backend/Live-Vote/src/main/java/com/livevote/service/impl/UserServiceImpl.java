package com.livevote.service.impl;

import com.livevote.dto.LoginRequest;
import com.livevote.dto.Response;
import com.livevote.entity.User;
import com.livevote.repository.UserRepository;
import com.livevote.service.interfac.UserServiceInterface;
import org.apache.catalina.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserServiceInterface {

    @Autowired
    private UserRepository userRepository;


    @Override
    public Response login(LoginRequest loginRequest) {
        Response response = new Response();
        if (StringUtils.isEmpty(loginRequest.getPassword()) || StringUtils.isEmpty(loginRequest.getUsername())) {
            response.setMessage("Login Request is empty");
            response.setStatusCode(404);
            return response;
        }

        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getHashedPassword())) {
            response.setMessage("Username or password is empty");
            response.setStatusCode(402);

            if (StringUtils.equals(user.getHashedPassword(), loginRequest.getPassword())) {
                response.setMessage("Password is correct, Login Successfully");
                response.setStatusCode(200);
            } else {
                response.setMessage("Password is incorrect, Login Unsuccessfully");
                response.setStatusCode(404);
            }
        } else {
            response.setMessage("Username not found");
            response.setStatusCode(401);
        }
        return response;
    }
}
