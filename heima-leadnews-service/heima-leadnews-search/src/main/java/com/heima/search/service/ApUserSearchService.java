package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface ApUserSearchService {

    public void insert(String keywords, Integer userId);

    public ResponseResult findUserSearch();

    public ResponseResult delUserSearch(HistorySearchDto dto);
}
