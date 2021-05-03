package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

/**
 * @author achao
 * @create 2020/8/14
 */
public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse setSaleStatus(Integer productId,Integer status);

    ServerResponse manageProductDetail(Integer productId);

    ServerResponse getProductList(int pageNum, int pageSize);

    ServerResponse searchProduct(String productName, Integer productId, int pageNum, int pageSize);
}
