package com.heima.xxljob.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloJob {

    @XxlJob("demoJobHandler")
    public void hellojob() {
        System.out.println("任务执行了");
    }

    @XxlJob("shardingJobHandler")
    public void sharding() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<Integer> list = getList();
        for (Integer o : list) {
            if(o % shardTotal == shardIndex) {
                System.out.println("当前分片" + shardIndex + "执行了， 执行项为" + o);
            }
        }
    }

    public List<Integer> getList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }
        return list;
    }
}
