package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {

    ServerResponse<User> Login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String str,String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken);

    ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user);

    ServerResponse<User> updateInformation(User userSession,User user);

    ServerResponse<User> getInformation(Integer userId);

    ServerResponse checkAdminRole(User user);
}
