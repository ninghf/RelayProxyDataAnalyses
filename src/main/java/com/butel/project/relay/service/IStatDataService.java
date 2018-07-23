package com.butel.project.relay.service;

import com.butel.project.relay.analyses.OriginalData;
import com.butel.project.relay.constant.StatObjType;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
public interface IStatDataService {

    void encode();

    /**
     * 解析MongoDB存储的Protobuf数据结构
     * @param originalData
     * @param startTime
     * @param endTime
     * @param statObjType
     * @param objIds
     */
    void decode(OriginalData originalData, long startTime, long endTime, StatObjType statObjType, String... objIds);
}
