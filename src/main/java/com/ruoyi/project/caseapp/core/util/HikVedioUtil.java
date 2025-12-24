package com.ruoyi.project.caseapp.core.util;


import com.alibaba.fastjson.JSONObject;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;

/**
 * 海康视频截取工具类
 * @author renjingkai
 * @date 2022年11月4日
 */
public class HikVedioUtil {

    /**
     * 平台认证信息读取
     */
    private String ip;

    private String port;

    private String appKey;

    private String appSecret;

    public void fz(String ip, String port, String appKey, String appSecret){
        this.ip = ip;
        this.port = port;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    /**
     * 海康平台的调用
     * @param url 需要实现功能的接口
     * @param jsonObject 传递的参数
     * @return 接口返回值
     */
    public String vedioCut(String url, JSONObject jsonObject) {
        /**
         * 第一步：设置平台参数，主要是平台的IP、端口、秘钥key和secret
         */
        ArtemisConfig artemisConfig = new ArtemisConfig();
        artemisConfig.setHost(ip+":"+port);
        artemisConfig.setAppKey(appKey);
        artemisConfig.setAppSecret(appSecret);

        /**
         * 第二步：设置接口的上下文
         */
        final String ARTEMIS_PATH = "/artemis";

        /**
         * 第三步：设置接口的url地址（需要什么功能，就使用什么地址）
         */
        final String previewURLsApi = ARTEMIS_PATH + url;

        Map<String,String> path = new HashMap<String,String>(2){
            {
                put("https://",previewURLsApi);
            }
        };

        /**
         * 第四步：设置参数提交方式
         */
        String contentType = "application/json";

        /**
         * 第五步：组装请求参数(需要传递给监控平台的参数)
         */
        String body = jsonObject.toJSONString();

        /**
         * 第六步：调用接口
         */
        String result = null;
        try {
             result = ArtemisHttpUtil.doPostStringArtemis(artemisConfig,path,body,null,null,contentType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 时间转为ISO8601
     * @param timestamp 需要转为ISO的时间
     * @return ISO格式的时间
     */
    public static String getISO8601TimestampFromDateStr(Date timestamp){
        // 直接使用Instant进行转换，避免多次转换带来的精度损失
        return timestamp.toInstant()
                .atOffset(ZoneOffset.of("+08:00"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }


    /**
     * 将字符串转为JSON并取某个值
     * @param result 需要转为JSON的字符串
     * @param key 取值的key
     * @return 根据key取到的值
     */
    public String getContentByJson(String result,String key){
        //将字符串转换成json
        JSONObject jsonObject = JSONObject.parseObject(result);
        //取出data里的数据
        String code = jsonObject.getString(key);

        if (StringUtils.isEmpty(code)){
            // 如果为空，说明没有找到，去data找
            JSONObject codeobject = JSONObject.parseObject(jsonObject.getString("data"));
            String string = codeobject.getString(key);
            return string;
        }

        return code;
    }

    public static void main(String[] args) {
        String is = getISO8601TimestampFromDateStr(new Date());
        System.out.println(is);
    }

    /*
    public static void main(String[] args) {
        *//**
         * 这是返回的数据示例
         *//*
        String result = "{\n" +
                "    \"code\": \"0\",\n" +
                "    \"msg\": \"success\",\n" +
                "    \"data\": {\n" +
                "        \"list\": [\n" +
                "            {\n" +
                "                \"lockType\": 1,\n" +
                "                \"beginTime\": \"2018-08-07T14:44:04.923+08:00\",\n" +
                "                \"endTime\": \"2018-08-07T14:54:18.183+08:00\",\n" +
                "                \"size\": 66479332\n" +
                "            }\n" +
                "        ],\n" +
                "        \"uuid\": \"e33421g1109046a79b6280bafb6fa5a7\",\n" +
                "        \"url\": \"rtsp://10.0.0.10:6304/EUrl/Dib1ErK\"\n" +
                "    }\n" +
                "}";

        String url = getContentByJson(result, "url");
        System.out.println(url);
    }
    */

}
