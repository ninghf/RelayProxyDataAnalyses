package com.butel.project.relay.controller;

import com.alibaba.fastjson.JSONObject;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.dto.BaseReqDto;
import com.butel.project.relay.dto.Summary;
import com.butel.project.relay.job.Job;
import com.butel.project.relay.job.JobDispatcher;
import com.butel.project.relay.meeting.MeetingAnalysesData;
import com.butel.project.relay.service.IAnalysesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/1
 * @description TODO
 */
@RestController
@Slf4j
public class StatDataController {

    @Autowired
    private IAnalysesService service;
    @Autowired
    private JobDispatcher dispatcher;

    @RequestMapping(method = RequestMethod.POST, path = "/summary")
    @CrossOrigin()
    public List<Summary> getSummaryData(@RequestBody JSONObject json) {
        StopWatch watch = new StopWatch();
        watch.start("解析请求数据");
        BaseReqDto req = new BaseReqDto();
        req.decode(json.getJSONObject("data"));
        watch.stop();
        watch.start("计算");
//        Summary summary = new Summary();
//        MeetingAnalysesData analysesData = service.generateAnalysesData(req.getKey(), req.getStartTime(), req.getEndTime(), req.getTransTime(), req.getSuperSocketId());
//        if (Objects.isNull(analysesData))
//            return summary;
//        if (analysesData.isEmpty())
//            return summary;
        List<Job> jobs = this.dispatcher.dispatcher(req.getStartTime(), req.getEndTime(), req.getTransTime(), StatObjType.super_socket, req.getSuperSocketId());
        watch.stop();
        watch.start("包装响应数据");
        List<Summary> summaries = Summary.toSummarys(jobs);
//        summary.toSummary(analysesData);
        watch.stop();
//        summary.setTime(watch.getTotalTimeMillis());
        log.info("耗时打印{}", watch.prettyPrint());
//        return summary;
        return summaries;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/detail")
    @CrossOrigin()
    public Summary getDetailData(@RequestBody JSONObject json) {
        StopWatch watch = new StopWatch();
        watch.start("解析请求数据");
        BaseReqDto req = new BaseReqDto();
        req.decode(json.getJSONObject("data"));
        watch.stop();
        watch.start("计算");
        Summary summary = new Summary();
        MeetingAnalysesData analysesData = service.generateAnalysesData(req.getKey(), req.getStartTime(), req.getEndTime(), req.getTransTime(),  StatObjType.super_socket, req.getSuperSocketId());
        if (Objects.isNull(analysesData))
            return summary;
        if (analysesData.isEmpty())
            return summary;
        watch.stop();
        watch.start("包装响应数据");
        summary.toDetail(analysesData);
        watch.stop();
        summary.setTime(watch.getTotalTimeMillis());
        log.info("耗时打印{}", watch.prettyPrint());
        return summary;
    }
}
