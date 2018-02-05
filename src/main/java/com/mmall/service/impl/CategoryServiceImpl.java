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

import java.util.List;
import java.util.Set;

/**
 * @author fan
 * @date 2018/1/27 17:44
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加品类
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse addCategory(String categoryName,Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.creatByErrorMessage("添加品类参数错误");
        }

        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true); //表名这个分类是可用的
       int rowCount =  categoryMapper.insertSelective(category);
       if (rowCount > 0) {
           return ServerResponse.creatBySuccessMessage("添加品类成功");
       }
       return ServerResponse.creatBySuccessMessage("添加品类失败");
    }

    /**
     * 修改品类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.creatByErrorMessage("修改品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setId(categoryId);
       int rowCount =  categoryMapper.updateByPrimaryKeySelective(category);
       if (rowCount > 0) {
           return ServerResponse.creatBySuccessMessage("更新品类名称成功");
       }
       return ServerResponse.creatByErrorMessage("修改品类名称失败");
    }

    /**
     * 通过parentId查找该分类信息
     * @param parentId
     * @return
     *  重写Category的equals和hashCode方法进行排重
     */
    public ServerResponse<List<Category>> getChildrenParalleCategory(Integer parentId) {
        List<Category> categoryList = categoryMapper.getChildrenParalleCategoryByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.creatBySuccess(categoryList);
    }

    /**
     * 递归查询本节的的id及其孩子节点的Id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        //初始化set
       Set<Category> categorySet =  Sets.newHashSet();
        findChildrenCategory(categorySet,categoryId);
     //此时经过递归方法，categorySet里面已经被填充category对象了
        List<Integer> categoryIdList =  Lists.newArrayList();
       if (categoryId != null) {
           for(Category categoryItem : categorySet) {
                categoryIdList.add(categoryItem.getId());
           }
       }
       return ServerResponse.creatBySuccess(categoryIdList);
    }

    //递归算法，算出子节点
    private Set<Category> findChildrenCategory(Set<Category> categorySet,Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        //查找子节点，递归算法一定要有一个退出的条件
       List<Category> CategoryList =  categoryMapper.getChildrenParalleCategoryByParentId(categoryId);
         for(Category categoryItem : CategoryList) {
             findChildrenCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }


}
