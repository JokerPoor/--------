package com.qzh.backend.controller;

import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.tools.WeatherApiClient;
import com.qzh.backend.tools.vo.Forecast;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherApiClient weatherApiClient;

    @GetMapping
    public BaseResponse<List<Forecast>> getWeather(@RequestParam(defaultValue = "110105") String districtId) {
        try {
            List<Forecast> forecasts = weatherApiClient.getForecastsByDistrictId(districtId);
            return ResultUtils.success(forecasts);
        } catch (Exception e) {
            // 如果获取失败，不影响主流程，返回空列表或错误信息
            return ResultUtils.success(List.of());
        }
    }
}
