package com.qzh.backend.constants.Interface;

public interface WarehouseInterfaceConstant {

    // 新增仓库（POST /warehouse/add）
    String WAREHOUSE_ADD_POST = "POST:/warehouse/add";

    // 根据ID查询仓库（GET /warehouse/get/{id}）
    String WAREHOUSE_DETAIL_GET = "GET:/warehouse/get/:id";

    // 更新仓库信息（PUT /warehouse/update）
    String WAREHOUSE_UPDATE_PUT = "PUT:/warehouse/update";

    // 删除仓库（DELETE /warehouse/delete/{id}）
    String WAREHOUSE_DELETE_DELETE = "DELETE:/warehouse/delete/:id";

    // 分页查询仓库列表（GET /warehouse/list）
    String WAREHOUSE_LIST_GET = "GET:/warehouse/list";

    // 分页查询仓库列表（兼容旧接口）（GET /warehouse/page）
    String WAREHOUSE_PAGE_GET = "GET:/warehouse/page";
}