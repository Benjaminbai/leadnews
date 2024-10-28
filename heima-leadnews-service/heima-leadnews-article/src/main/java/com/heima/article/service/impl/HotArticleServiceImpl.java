package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HotArticleServiceImpl implements HotArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private IWemediaClient wemediaClient;

    @Override
    public void computedHotArticle() {
        Date date = DateTime.now().minusDays(5).toDate();
        List<ApArticle> listByLast5Days = apArticleMapper.findListByLast5Days(date);
        List<HotArticleVo> hotArticleVoList = computedScore(listByLast5Days);
        cacheTagToRedis(hotArticleVoList);
    }

    @Autowired
    private CacheService cacheService;

    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        ResponseResult responseResult = wemediaClient.getChannels();
        if(responseResult.getCode() == 200) {
            String channelJson = JSON.toJSONString(responseResult.getData());
            List<WmChannel> wmChannels = JSON.parseArray(channelJson, WmChannel.class);
            if(wmChannels != null && wmChannels.size() > 0) {
                for (WmChannel wmChannel : wmChannels) {
                    List<HotArticleVo> hotArticleVos = hotArticleVoList.stream().filter(x -> x.getChannelId().equals(wmChannel.getId())).collect(Collectors.toList());
                    hotArticleVos = hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
                    if(hotArticleVos.size() > 30) {
                        hotArticleVos = hotArticleVos.subList(0, 30);
                    }
                    cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + wmChannel.getId(), JSON.toJSONString(hotArticleVos));
                }
            }
        }

        hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
        if(hotArticleVoList.size() > 30) {
            hotArticleVoList = hotArticleVoList.subList(0, 30);
        }
        cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG, JSON.toJSONString(hotArticleVoList));
    }

    private List<HotArticleVo> computedScore(List<ApArticle> listByLast5Days) {
        ArrayList<HotArticleVo> hotArticleVos = new ArrayList<>();

        if(listByLast5Days != null && listByLast5Days.size() > 0) {
            for (ApArticle listByLast5Day : listByLast5Days) {
                HotArticleVo hotArticleVo = new HotArticleVo();
                BeanUtils.copyProperties(listByLast5Day, hotArticleVo);
                Integer score = scoreHandle(hotArticleVo);
                hotArticleVo.setScore(score);
                hotArticleVos.add(hotArticleVo);
            }
        }
        return hotArticleVos;
    }

    private Integer scoreHandle(HotArticleVo hotArticleVo) {
        Integer score = 0;
        if(hotArticleVo.getLikes() != null) {
            score += hotArticleVo.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }if(hotArticleVo.getViews() != null) {
            score += hotArticleVo.getViews();
        }if(hotArticleVo.getComment() != null) {
            score += hotArticleVo.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }if(hotArticleVo.getCollection() != null) {
            score += hotArticleVo.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        return score;
    }
}
