package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpSession;


/**
 * 后台品类操作Controller层
 *
 * @author achao
 * @create 2020/8/13
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {


    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加品类节点
     *
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            //增加逻辑处理
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("无权限，需要管理员权限");
        }
    }

    /**
     * 更新品类名字
     *
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category.do")
    @ResponseBody
    public ServerResponse setCategory(HttpSession session, Integer categoryId, String categoryName) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            //更新业务逻辑处理
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        } else {
            return ServerResponse.createByErrorMessage("无权限，需要管理员权限");
        }
    }

    /**
     * 根据parentId查询平级分类
     *
     * @param session
     * @param parentId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            //查询子节点的category信息，并且不递归保持平级
            return iCategoryService.getChildrenParallelCategory(parentId);
        } else {
            return ServerResponse.createByErrorMessage("无权限，需要管理员权限");
        }
    }

    /**
     * 查询当前节点category,并递归子节点下的category
     *
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        //校验是否是管理员
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            //查询当前节点的id，和递归子节点的id
            return iCategoryService.selectCategoryAndChildrenCategoryById(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无权限，需要管理员权限");
        }
    }
}
