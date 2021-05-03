package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台产品
 *
 * @author achao
 * @create 2020/8/14
 */
@RestController
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 添加或更新产品
     *
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    public ServerResponse productSave(HttpSession session, Product product) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            //进行添加或更新的业务逻辑
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }

    /**
     * 修改产品销售状态
     *
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }

    /**
     * 获取产品详情
     *
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }

    /**
     * list查询产品
     *
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }

    /**
     * 根据name或者id查询
     *
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }

    /**
     * todo 上传图片操作
     *
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("upload.do")
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_File", required = false) MultipartFile file, HttpServletRequest request) {
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录");
        }
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upLoad(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);
        } else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }

    /**
     * todo 富文本图片上传
     * @param session
     * @param file
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("richText_img_upload.do")
    public Map richTextImgUpload(HttpSession session, @RequestParam(value = "upload_File", required = false) MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response) {
        Map map = Maps.newHashMap();
        User sessionUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (sessionUser == null) {
            map.put("success", false);
            map.put("msg", "请登录管理员");
            return map;
        }
        if (iUserService.checkAdminRole(sessionUser).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upLoad(file, path);
            if (StringUtils.isBlank(targetFileName)) {
                map.put("success", false);
                map.put("msg", "上传失败");
                return map;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            map.put("success", true);
            map.put("msg", "上传成功");
            map.put("file_path", url);

            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");   //响应给前端的格式

            return map;
        } else {
            map.put("success", false);
            map.put("msg", "无权限操作");
            return map;
        }
    }

}
