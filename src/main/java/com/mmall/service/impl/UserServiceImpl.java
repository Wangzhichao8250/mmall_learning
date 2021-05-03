package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import sun.org.mozilla.javascript.internal.Token;
import java.util.UUID;


@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> Login(String username, String password) {
        int count = userMapper.checkUsername(username);
        if (count == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //MD5加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //重新把密码设置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    /**
     * 注册
     *
     * @param user
     * @return
     */
    public ServerResponse<String> register(User user) {
        //对用户名判断存在与否
        ServerResponse<String> validUsername = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validUsername.isSuccess()) {   //为false表示存在
            return validUsername;
        }
        //对email判断存在与否
        ServerResponse<String> validEmail = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validEmail.isSuccess()) {      //为false表示存在
            return validEmail;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //开始进行MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //进行DB中的添加
        int insertCount = userMapper.insert(user);
        if (insertCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }


    /**
     * 通用校验方法，校验用户名或者邮箱存不存在
     *
     * @param str
     * @param type
     * @return
     */
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type) && (Const.USERNAME.equals(type) || Const.EMAIL.equals(type))) {
            if (Const.USERNAME.equals(type)) {
                int count = userMapper.checkUsername(str);
                if (count > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int countEmail = userMapper.checkEmail(str);
                if (countEmail > 0) {
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
            return ServerResponse.createBySuccessMessage("校验成功");
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    /**
     * 根据用户名操作忘记密码功能，获得密码问题
     *
     * @param username
     * @return
     */
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> validUsername = this.checkValid(username, Const.USERNAME);
        if (validUsername.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccessMessage(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    /**
     * 校验回答的问题是否正确,正确的话创建uuid，存入本地guava缓存，利用本地guava开始倒计时
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int checkCount = userMapper.checkAnswer(username, question, answer);
        if (checkCount > 0) {
            //问题及问题答案是该用户的，并且回答正确
            String forgetToken = UUID.randomUUID().toString();
            //存入本地guava缓存，利用本地guava开始倒计时
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 更改新密码，确认token是否有效
     *
     * @param username
     * @param newPassword
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        ServerResponse<String> validUsername = this.checkValid(username, Const.USERNAME);
        if (validUsername.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        //对比下穿进来的token和guava缓存中的token是否一致
        if (StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createBySuccessMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");

    }

    /**
     * 登录状态下，更新密码
     *
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //防止横向越权，要校验一下用户旧密码
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            //需要再次把密码置空，与session保持一致,不对啊 返回String 用user搞毛线？--？
//            user.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    /**
     * 更新个人信息
     *
     * @param user
     * @return
     */
    public ServerResponse<User> updateInformation(User userSession, User user) {
        //username不能进行更新
        //email要进行校验 新的email是否已经被其他用户占用
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), userSession.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已存在,请更换email在进行更新");
        }
        userSession.setEmail(user.getEmail());
        userSession.setPhone(user.getPhone());
        userSession.setQuestion(user.getQuestion());
        userSession.setAnswer(user.getAnswer());
        userSession.setPassword(null);


        int updateCount = userMapper.updateByPrimaryKeySelective(userSession);
        if (updateCount > 0) {
            userSession.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccess("更新个人信息成功", userSession);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    /**
     * 根据id返回个人信息
     *
     * @param userId
     * @return
     */
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    //backEnd -- category

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
