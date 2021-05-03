package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 后台品类操作Service层
 *
 * @author achao
 * @create 2020/8/13
 */
@Service
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加品类
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category cate = new Category();
        cate.setName(categoryName);
        cate.setParentId(parentId);
        cate.setStatus(true);
        int insertCount = categoryMapper.insert(cate);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    /**
     * 更新品类名字
     *
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category cate = new Category();
        cate.setId(categoryId);
        cate.setName(categoryName);

        int updateCount = categoryMapper.updateByPrimaryKeySelective(cate);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    /**
     * 根据parent_id查询子分类
     *
     * @param parentId
     * @return
     */
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer parentId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询当前节点id,及子节点下的id
     *
     * @param categoryId
     * @return
     */
    public ServerResponse selectCategoryAndChildrenCategoryById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategory(categorySet,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId!=null){
            for(Category categoryIdItem : categorySet){
                categoryIdList.add(categoryIdItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    /**
     * 递归算法，算出子节点
     *
     * @return
     */
    private Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId) {
        //先根据id查询出当前的节点信息,然后在查该id是不是父节点(及下面还有子节点)
        Category cate = categoryMapper.selectByPrimaryKey(categoryId);
        if (cate != null) {
            categorySet.add(cate);
        }
        //查找子节点，递归算法要有一个退出条件
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem : categoryList) {
            findChildrenCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
