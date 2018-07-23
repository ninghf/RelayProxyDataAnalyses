package com.butel.project.relay.controller;

import com.alibaba.fastjson.JSONObject;
import com.butel.project.relay.analyses.AnalysesData;
import com.butel.project.relay.dto.BaseReqDto;
import com.butel.project.relay.dto.Summary;
import com.butel.project.relay.service.IAnalysesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(method = RequestMethod.POST, path = "/summary")
    @CrossOrigin()
    public Summary getSummaryData(@RequestBody JSONObject json) {
        StopWatch watch = new StopWatch();
        watch.start("解析请求数据");
        BaseReqDto req = new BaseReqDto();
        req.decode(json.getJSONObject("data"));
        watch.stop();
        watch.start("计算");
        AnalysesData analysesData = service.generateAnalysesData(req.getKey(), req.getStartTime(), req.getEndTime(), req.getSuperSocketId());
        if (Objects.isNull(analysesData))
            return null;
        watch.stop();
        watch.start("包装响应数据");
        Summary summary = new Summary();
        summary.toSummary(analysesData);
        watch.stop();
        summary.setTime(watch.getTotalTimeMillis());
        log.info("耗时打印{}", watch.prettyPrint());
        return summary;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/detail")
    @CrossOrigin()
    public void getDetailData(@RequestBody JSONObject json) {

    }
}
