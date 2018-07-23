package com.butel.project.relay.service.impl;

import com.butel.project.relay.analyses.AnalysesData;
import com.butel.project.relay.analyses.OriginalData;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.service.IAnalysesService;
import com.butel.project.relay.service.IStatDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Objects;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
@Slf4j
@Service
public class AnalysesServiceImpl implements IAnalysesService {

    public static final int bound = 60 * 1000;// 边界

    @Autowired
    private IStatDataService statDataService;

    @Override
//    @Cacheable(value = "analyses")
    public AnalysesData generateAnalysesData(long key, long startTime, long endTime, String superSocketId) {
        OriginalData originalData = generateOriginalData(startTime, endTime, superSocketId);
        if (Objects.isNull(originalData))
            return null;
        AnalysesData analysesData = new AnalysesData();
        analysesData.processOriginalData(originalData);
        if (log.isDebugEnabled())
            log.debug("生成分析数据：{}", analysesData);
        return analysesData;
    }

    @Override
    @Cacheable(value = "original")
    public OriginalData generateOriginalData(long startTime, long endTime, String superSocketId) {
        OriginalData originalData = new OriginalData(startTime, endTime);
        // 扩大时间范围
        startTime -= bound;
        endTime += bound;
        StopWatch watch = new StopWatch();
        watch.start("端到端");
        statDataService.decode(originalData, startTime, endTime, StatObjType.super_socket, superSocketId);
        watch.stop();
        watch.start("端到Proxy, Proxy到端");
        // 查询proxy节点信息
        if (Objects.isNull(originalData.getPackets()))
            return null;
        statDataService.decode(originalData, startTime, endTime, StatObjType.path, originalData.getPaths());
        watch.stop();
        if (log.isDebugEnabled())
            log.debug("生成原始数据耗时：{}", watch.prettyPrint());
        return originalData;
    }

}
