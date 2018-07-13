package com.butel.project.relay.controller;

import com.alibaba.fastjson.JSONObject;
import com.butel.project.relay.analyses.AnalysesData;
import com.butel.project.relay.analyses.Packet;
import com.butel.project.relay.dto.DelayedDataDto;
import com.butel.project.relay.dto.DelayedReq;
import com.butel.project.relay.dto.LossReq;
import com.butel.project.relay.service.IAnalysesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @RequestMapping(method = RequestMethod.POST, path = "/delayed")
    @CrossOrigin()
    public DelayedDataDto getDelayedData(@RequestBody JSONObject json) {
        StopWatch watch = new StopWatch();
        watch.start("解析请求数据");
        DelayedReq req = new DelayedReq();
        req.decode(json);
        watch.stop();
        watch.start("计算");
        AnalysesData analysesData = service.generateDelayed(req.getReqId(), req);
        List <Packet> vPackets = analysesData.getPackets();
        watch.stop();
        watch.start("包装响应数据");
        DelayedDataDto delayedDataDto = DelayedDataDto.createInstance(vPackets, analysesData.getAxis(), req.isDetail(), req.getLimit(), req.getCurrentPage());
        watch.stop();
        log.info("总数据包数:{}个,{},耗时打印{}", Objects.nonNull(vPackets) ? vPackets.size() : 0, analysesData.getAxis(), watch.prettyPrint());
        return delayedDataDto;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/loss")
    @CrossOrigin()
    public DelayedDataDto getLossData(@RequestBody JSONObject json) {
        LossReq req = new LossReq();
        req.decode(json);
        AnalysesData analysesData = service.generateLoss(req.getReqId(), req);
        List <Packet> vPackets = analysesData.getPackets();
        log.info("总数据包数:{}个,{}", Objects.nonNull(vPackets) ? vPackets.size() : 0, analysesData.getAxis());
        return DelayedDataDto.createInstance(vPackets, analysesData.getAxis(), req.isDetail(), req.getLimit(), req.getCurrentPage());
    }
}
