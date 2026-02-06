package com.qzh.backend.tools;

import com.qzh.backend.tools.vo.Forecast;
import com.qzh.backend.tools.vo.WeatherResponse;
import com.qzh.backend.tools.vo.WeatherResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 百度天气API请求工具类
 */
@Component
public class WeatherApiClient {
    private static final Logger logger = LoggerFactory.getLogger(WeatherApiClient.class);

    private final String weatherApiUrl;

    private final String baiduAk;

    private final RestTemplate restTemplate;

    public WeatherApiClient(
            @Value("${baidu.weather.base-url:https://api.map.baidu.com/weather/v1/}") String weatherApiUrl,
            @Value("${baidu.weather.ak:}") String baiduAk,
            @Value("${baidu.weather.connect-timeout-ms:5000}") int connectTimeout,
            @Value("${baidu.weather.read-timeout-ms:5000}") int readTimeout
    ) {
        this.weatherApiUrl = weatherApiUrl;
        this.baiduAk = baiduAk;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 根据区县ID获取未来几天天气预报
     * @param districtId 区县ID（如：222405 对应龙井市）
     * @return 未来几天预报列表
     * @throws Exception 异常（网络错误、API响应错误等）
     */
    public List<Forecast> getForecastsByDistrictId(String districtId) throws Exception {
        // 参数校验
        if (StringUtils.isBlank(districtId)) {
            throw new IllegalArgumentException("区县ID不能为空");
        }
        if (StringUtils.isBlank(baiduAk)) {
            throw new IllegalStateException("百度地图AK未配置");
        }
        // 构建请求URL（拼接参数）
        String requestUrl = String.format("%s?district_id=%s&data_type=all&ak=%s",
                weatherApiUrl, districtId, baiduAk);
        logger.info("调用百度天气API，districtId：{}", districtId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            // 发送GET请求并接收响应
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    requestUrl, HttpMethod.GET, httpEntity, String.class);
            // 解析响应结果
            String responseBody = responseEntity.getBody();
            logger.info("百度天气API响应：{}", responseBody);
            if (StringUtils.isBlank(responseBody)) {
                throw new Exception("API响应为空");
            }
            WeatherResponse weatherResponse = com.alibaba.fastjson2.JSON.parseObject(responseBody, WeatherResponse.class);
            if (weatherResponse == null || weatherResponse.getStatus() != 0) {
                throw new Exception(String.format("API调用失败，状态码：%d，消息：%s",
                        weatherResponse != null ? weatherResponse.getStatus() : -1,
                        weatherResponse != null ? weatherResponse.getMessage() : "响应解析失败"));
            }
            WeatherResult result = weatherResponse.getResult();
            if (result == null || result.getForecasts() == null || result.getForecasts().isEmpty()) {
                logger.warn("未获取到未来几天预报数据");
                return List.of(); // 返回空列表，避免空指针
            }
            return result.getForecasts();

        } catch (RestClientException e) {
            logger.error("百度天气API请求异常", e);
            throw new Exception("网络请求失败：" + e.getMessage());
        } catch (Exception e) {
            logger.error("获取天气预报异常", e);
            throw e; // 向上抛出异常，由调用方处理
        }
    }
}
