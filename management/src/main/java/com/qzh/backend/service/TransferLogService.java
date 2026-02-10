package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.transfer.TransferLogQueryDTO;
import com.qzh.backend.model.entity.TransferLog;
import com.qzh.backend.model.vo.TransferLogVO;
import jakarta.servlet.http.HttpServletRequest;

public interface TransferLogService extends IService<TransferLog> {

    Page<TransferLogVO> listTransferLogs(TransferLogQueryDTO queryDTO, HttpServletRequest request);
}