package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.ocr.OcrUitil;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Override
    @Async
    public void autoScanWmNews(Integer id) {
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);
            Boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
//            if(!isSensitive) return;

//            Boolean isTextScan = handleTextScan((String) textAndImages.get("content"), wmNews);
//            if(!isTextScan) return;
//
//            Boolean isImageScan = handleImageScan((List<String>) textAndImages.get("image"), wmNews);
//            if(!isImageScan) return;

            // tess4j因为mac不兼容，换成了baidu ocr
//            Boolean isOcrPass = handleImageOcr((List<String>) textAndImages.get("image"), wmNews);
//            // 又因为minio局域网，ocr访问不到，又想测试一下静态生成html，暂时关了
//            if(!isOcrPass) return;

            ResponseResult responseResult = saveAppArticle(wmNews);
//            if(!responseResult.getCode().equals(200)) {
//                throw new RuntimeException("WmNewsAutoScanServiceImpl-审核，保存app端相关文章数据失败");
//            }
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews, (short) 9, "审核成功");
        }
    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    private Boolean handleSensitiveScan(String content, WmNews wmNews) {
        Boolean flag = true;
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        SensitiveWordUtil.initMap(sensitiveList);
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size() > 0) {
            updateWmNews(wmNews, (short) 2, "当前文章存在违规内容" + map);
            flag = false;
        }
        return flag;
    }

    @Autowired
    private IArticleClient iArticleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmUserMapper wmUserMapper;

    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(wmNews, articleDto);
        articleDto.setLayout(wmNews.getType());
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel != null) {
            articleDto.setChannelName(wmChannel.getName());
        }
        articleDto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser != null) {
            articleDto.setAuthorName(wmUser.getName());
        }
        if(wmNews.getArticleId() != null) {
            articleDto.setId(wmNews.getArticleId());
        }
        articleDto.setCreatedTime(new Date());
        ResponseResult responseResult = iArticleClient.saveArticle(articleDto);
        return responseResult;
    }

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private GreenImageScan greenImageScan;

    private Boolean handleImageScan(List<String> images, WmNews wmNews) {
        Boolean flag = true;
        if(images == null || images.size() == 0) {
            return flag;
        }
        images = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> imageList = new ArrayList<>();
        for(String image : images) {
            byte[] bytes = fileStorageService.downLoadFile(image);
            imageList.add(bytes);
        }
        try {
            Map map = greenImageScan.imageScan(imageList);
            if(map != null) {
                if(map.get("suggestion").equals("block")) {
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "文章存在违规内容");
                }
                if(map.get("suggestion").equals("review")) {
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "文章存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    @Autowired
    private GreenTextScan greenTextScan;

    private Boolean handleTextScan(String content, WmNews wmNews) {
        Boolean flag = true;
        if((wmNews.getTitle() + content).length() == 0) {
            return flag;
        }
        try {
            Map map = greenTextScan.greeTextScan(wmNews.getTitle() + content);
            if(map != null) {
                if(map.get("suggestion").equals("block")) {
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "文章存在违规内容");
                }
                if(map.get("suggestion").equals("review")) {
                    flag = false;
                    updateWmNews(wmNews, (short) 3, "文章存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();

        if(StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for(Map map : maps) {
                if(map.get("type").equals("text")) {
                    stringBuilder.append(map.get("value"));
                }
                if(map.get("type").equals("image")) {
                    images.add((String) map.get("value"));
                }
            }
        }

        if(StringUtils.isNotBlank(wmNews.getImages())) {
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder.toString());
        resultMap.put("image", images);

        return resultMap;
    }

    @Autowired
    private OcrUitil ocrUitil;

    private Boolean handleImageOcr(List<String> images, WmNews wmNews) {
        Boolean flag = true;
        if(images == null || images.size() == 0) {
            return flag;
        }
        images = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> imageList = new ArrayList<>();
        for(String image : images) {
            String str = ocrUitil.doOcr(image);
            Boolean isSensitive = handleSensitiveScan(str, wmNews);
            if(!isSensitive) {
                return isSensitive;
            }
        }
        return flag;
    }
}
