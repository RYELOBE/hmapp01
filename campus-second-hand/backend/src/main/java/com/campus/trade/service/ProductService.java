package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.PageDTO;
import com.campus.trade.dto.ProductPublishDTO;
import com.campus.trade.dto.ProductQueryDTO;
import com.campus.trade.dto.ProductUpdateDTO;
import com.campus.trade.entity.Product;
import com.campus.trade.vo.ProductVO;
import com.campus.trade.vo.Result;

import java.util.List;

public interface ProductService extends BaseService<Product> {
    Result<Long> publishProduct(Long userId, ProductPublishDTO dto);
    Result<IPage<ProductVO>> getProductPage(ProductQueryDTO dto);
    Result<ProductVO> getProductDetail(Long id);
    Result<Boolean> updateProduct(Long id, Long userId, ProductUpdateDTO dto);
    Result<Boolean> deleteProduct(Long id, Long userId);
    Result<List<ProductVO>> getRecommendProducts(Long userId);
    Result<IPage<ProductVO>> searchProducts(String keyword, PageDTO pageDTO);
    Result<Boolean> incrementViewCount(Long id);
    Result<List<ProductVO>> getUserProducts(Long userId);
}
