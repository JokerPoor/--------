package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.transfer.TransferLogQueryDTO;
import com.qzh.backend.model.vo.TransferLogVO;
import com.qzh.backend.service.TransferLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfer/log")
@RequiredArgsConstructor
public class TransferLogController {

    private final TransferLogService transferLogService;

    @GetMapping("/list")
    public BaseResponse<Page<TransferLogVO>> listTransferLogs(TransferLogQueryDTO queryDTO, HttpServletRequest request) {
        Page<TransferLogVO> page = transferLogService.listTransferLogs(queryDTO, request);
        return ResultUtils.success(page);
    }
}
