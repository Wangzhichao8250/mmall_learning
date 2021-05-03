package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台产品操作service层
 *
 * @author achao
 * @create 2020/8/14
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 更新、添加产品
     *
     * @param product
     * @return
     */
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            //判断有没有子图,子图不为空的话就取第一个做为主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId() != null) {
                int productUpdate = productMapper.updateByPrimaryKey(product);
                if (productUpdate > 0) {
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                } else {
                    return ServerResponse.createByErrorMessage("更新产品失败");
                }
            } else {
                int productInsert = productMapper.insert(product);
                if (productInsert > 0) {
                    return ServerResponse.createBySuccessMessage("新增产品成功");
                } else {
                    return ServerResponse.createByErrorMessage("新增产品失败");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");
        }
    }

    /**
     * 修改产品销售状态
     *
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        if (productId == null && status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }

    /**
     * 后台获取商品详情
     *
     * @param productId
     * @return
     */
    public ServerResponse manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或已删除");
        }
        ProductDetailVo productVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo vo = new ProductDetailVo();
        vo.setId(product.getId());
        vo.setSubtitle(product.getSubtitle());
        vo.setPrice(product.getPrice());
        vo.setMainImage(product.getMainImage());
        vo.setSubImage(product.getSubImages());
        vo.setCategoryId(product.getCategoryId());
        vo.setDetail(product.getDetail());
        vo.setName(product.getName());
        vo.setStatus(product.getStatus());
        vo.setStock(product.getStock());

        //添加图片url前缀
        vo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(vo.getCategoryId());
        if (category == null) {
            vo.setParentCategoryId(0);      //默认根节点
        } else {
            vo.setParentCategoryId(category.getParentId());
        }
        vo.setCrateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        vo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return vo;
    }

    /**
     * list查询
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse getProductList(int pageNum, int pageSize) {
        //startPage 开始页
        PageHelper.startPage(pageNum, pageSize);
        //填充自己的sql查询逻辑
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> listVo = new ArrayList<>();
        for (Product productItem : productList) {
            listVo.add(assembleProductListVo(productItem));
        }
        //pageHelper-收尾
        PageInfo pageResult = new PageInfo();
        pageResult.setList(listVo);
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo listVo = new ProductListVo();
        listVo.setId(product.getId());
        listVo.setName(product.getName());
        listVo.setCategoryId(product.getCategoryId());
        listVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        listVo.setMainImage(product.getMainImage());
        listVo.setPrice(product.getPrice());
        listVo.setSubtitle(product.getSubtitle());
        listVo.setStatus(product.getStatus());
        return listVo;
    }

    /**
     * 根据name或者id查询
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();   //拼接模糊查询
        }
        List<Product> productSearchList = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVo = new ArrayList<>();
        for (Product items :productSearchList){
            productListVo.add(assembleProductListVo(items));
        }
        //pageHelper-收尾
        PageInfo pageResult = new PageInfo();
        pageResult.setList(productListVo);
        return ServerResponse.createBySuccess(pageResult);
    }
}
