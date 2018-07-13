package com.butel.project.relay.service;

import com.butel.project.relay.analyses.Axis;
import com.butel.project.relay.analyses.Packet;
import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;

import java.util.HashMap;
import java.util.Set;

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
     * @param packets
     * @param startTime
     * @param endTime
     * @param superSocketID
     * @param statObjType
     * @param statDataType
     */
    void decode(HashMap <Long, Packet> packets, long startTime, long endTime,
                String superSocketID, StatObjType statObjType, StatDataType statDataType, Axis axis);

    /**
     * 解析MongoDB存储的Protobuf数据结构
     * @param packets
     * @param paths
     * @param startTime
     * @param endTime
     * @param statObjType
     * @param statDataType
     */
    void decode(HashMap <Long, Packet> packets, Set<String> paths, long startTime, long endTime,
                StatObjType statObjType, StatDataType statDataType, Axis axis);
}
