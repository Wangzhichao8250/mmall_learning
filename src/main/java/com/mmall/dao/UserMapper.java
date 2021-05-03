package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /**
     * 判断用户名存不存在
     * @param username
     * @return
     */
    int checkUsername(String username);

    int checkEmail(String email);

    /**
     * 检查
     * @param username
     * @param password
     * @return
     */
    User selectLogin(@Param("username") String username, @Param("password") String password);

    /**
     * 忘记密码 根据账号查找获取密码问题
     * @param username
     * @return
     */
    String selectQuestionByUsername(String username);


    /**
     * 校验回答的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username, @Param("newPassword") String newPassword);

    int checkPassword(@Param("password") String password, @Param("userId") Integer userId);

    int checkEmailByUserId(@Param("email") String email,@Param("userId") Integer userId);
}