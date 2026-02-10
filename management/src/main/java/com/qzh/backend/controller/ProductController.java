package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.ProductAddDTO;
import com.qzh.backend.model.dto.product.ProductQueryDTO;
import com.qzh.backend.model.dto.product.ProductUpdateDTO;
import com.qzh.backend.model.entity.Product;
import com.qzh.backend.service.ProductService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.qzh.backend.constants.Interface.ProductInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.PRODUCT_MODULE;

@RestController
@RequestMapping("product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 新增商品
     */
    @PostMapping
    @LogInfoRecord(SystemModule = PRODUCT_MODULE + ":" + PRODUCT_ADD_POST)
    public BaseResponse<Long> addProduct(@Valid @RequestBody ProductAddDTO productAddDTO, HttpServletRequest request) {
        Long productId = productService.addProduct(productAddDTO,request);
        return ResultUtils.success(productId);
    }

    /**
     * 更新商品信息
     */
    @PutMapping
    @LogInfoRecord(SystemModule = PRODUCT_MODULE + ":" + PRODUCT_UPDATE_PUT)
    public BaseResponse<Void> updateProduct(@Valid @RequestBody ProductUpdateDTO productUpdateDTO,HttpServletRequest request) {
        boolean result = productService.updateProduct(productUpdateDTO,request);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR,"商品信息更新失败");
        return ResultUtils.success(null);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    @LogInfoRecord(SystemModule = PRODUCT_MODULE + ":" + PRODUCT_DELETE_DELETE)
    public BaseResponse<Void> deleteProduct(@PathVariable Long id) {
        boolean result = productService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "商品删除失败");
        return ResultUtils.success(null);
    }

    /**
     * 根据ID查询商品详情
     */
    @GetMapping("/{id}")
    public BaseResponse<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return ResultUtils.success(product);
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<Product>> listProducts(ProductQueryDTO productQueryDTO) {
        Page<Product> productPage = productService.listProductsByPage(productQueryDTO);
        return ResultUtils.success(productPage);
    }

    /**
     * 分页查询自有商品列表
     */
    @GetMapping("/list/own")
    public BaseResponse<Page<Product>> listProductsByOwn(ProductQueryDTO productQueryDTO , HttpServletRequest request) {
        Page<Product> productPage = productService.listProductsByPageOwn(productQueryDTO, request);
        return ResultUtils.success(productPage);
    }

}