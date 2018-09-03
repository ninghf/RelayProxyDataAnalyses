package com.butel.project.relay.service.impl;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.meeting.MeetingAnalysesData;
import com.butel.project.relay.meeting.MeetingOriginalData;
import com.butel.project.relay.service.IAnalysesService;
import com.butel.project.relay.service.IMeetingStatDataService;
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
    private IMeetingStatDataService meetingStatDataService;

    @Override
//    @Cacheable(value = "analyses")
    public MeetingAnalysesData generateAnalysesData(long key, long startTime, long endTime, int transTime, StatObjType objType, String objId) {
        MeetingOriginalData originalData = generateOriginalData(startTime, endTime, objType, objId);
        if (Objects.isNull(originalData))
            return null;
        MeetingAnalysesData analysesData = new MeetingAnalysesData();
        analysesData.processOriginalData(originalData, transTime, startTime, endTime);
//        if (log.isDebugEnabled())
//            log.debug("生成分析数据：{}", analysesData);
        return analysesData;
    }

    @Override
    @Cacheable(value = "original")
    public MeetingOriginalData generateOriginalData(long startTime, long endTime, StatObjType objType, String objId) {
        MeetingOriginalData originalData = new MeetingOriginalData(startTime, endTime);
        StopWatch watch = new StopWatch();
        watch.start("数据库查询、生成统计数据");
        meetingStatDataService.decode(originalData, startTime, endTime, bound, objType, objId);
        watch.stop();
        if (log.isDebugEnabled())
            log.debug("生成原始数据耗时：{}", watch.prettyPrint());
        return originalData;
    }

    @Override
    public void generateOriginalData(String meetingId) {
//        MeetingOriginalData originalData = new MeetingOriginalData(0, 1);
//        meetingStatDataService.decode(originalData, new MeetingIndex("", "", "", ""));
//        MeetingAnalysesData analysesData = new MeetingAnalysesData();
//        analysesData.processOriginalData(originalData, 300);
//        log.debug("{}", analysesData);
    }

}
