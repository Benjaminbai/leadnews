package com.heima.common.ocr;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class OcrUitil {
    public static final String APP_ID = "115878614";
    public static final String API_KEY = "GhezhZiDXeG1DAtcXLWrEzwI";
    public static final String SECRET_KEY = "hd0skDuGalt94Gcj1nlhJX1OBBCX7P15";

    public String doOcr(String imagePath) {
        // 初始化一个AipOcr
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

//        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
//
//        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
//
//        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
//        // 也可以直接通过jvm启动参数设置此环境变量
//        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");

        // 调用接口
        ArrayList<String> arrayList = new ArrayList<>();
        String path = imagePath;
        // 这块二有问题，因为minio没有部署到公网，baidu ocr 访问不了图片
        JSONObject res = client.basicGeneralUrl(path, new HashMap<String, String>());
        JSONArray words_result = res.getJSONArray("words_result");
        System.out.println(res.toString(2));
        if(res != null) {
            for (Object word : words_result) {
                JSONObject jsonObject = (JSONObject) word;
                String words = (String) jsonObject.get("words");
                arrayList.add(words);
            }
        }else {
            System.out.println("识别失败");
        }
        return arrayList.stream().collect(Collectors.joining("-"));
    }
}
