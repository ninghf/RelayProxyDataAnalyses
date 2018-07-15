package com.butel.project.relay.service.impl;

import com.butel.project.relay.analyses.Axis;
import com.butel.project.relay.analyses.Packet;
import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.dao.IStatDataDao;
import com.butel.project.relay.entity.StatDataEntity;
import com.butel.project.relay.protobuf.SDNMessage.*;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoDataValue;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoStatObjKey;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoStatObjKey.ProtoStatObj;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoDataValue.ProtoBitmapValue;
import com.butel.project.relay.service.IStatDataService;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.*;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/6/28
 * @description TODO
 */
@Slf4j
@Service
public class StatDataServiceImpl implements IStatDataService {

    @Autowired
    private IStatDataDao statDataDao;


    @Override
    public void encode() {

    }

    @Override
    public void decode(HashMap <Long, Packet> packets, long startTime, long endTime,
                       String superSocketID, StatObjType statObjType, StatDataType statDataType, Axis axis) {
        StopWatch watch = new StopWatch();
        watch.start("DB 操作");
        List <StatDataEntity> statDataEntities = statDataDao.queryStatData(startTime,
                endTime, superSocketID, statObjType, statDataType);
        watch.stop();
        watch.start("数据处理");
        statDataEntities.stream().forEach(statDataEntity -> parseStatData(packets, statDataEntity, axis));
        watch.stop();
        if (log.isDebugEnabled())
            log.debug("[superSocket]本次查询记录：【{}】条{}", statDataEntities.size(), watch.prettyPrint());
    }

    @Override
    public void decode(HashMap <Long, Packet> packets, Set <String> paths, long startTime, long endTime,
                       StatObjType statObjType, StatDataType statDataType, Axis axis) {
        StopWatch watch = new StopWatch();
        watch.start("DB 操作");
        List <StatDataEntity> statDataEntities = statDataDao.queryStatDataByPaths(startTime,
                endTime, paths, statObjType, statDataType);
        watch.stop();
        watch.start("数据处理");
        statDataEntities.stream().forEach(statDataEntity -> parseStatData(packets, statDataEntity, axis));
        watch.stop();
        if (log.isDebugEnabled())
            log.debug("[path]本次查询记录：【{}】条{}", statDataEntities.size(), watch.prettyPrint());
    }

    /**
     * 解析protobuf(StatDataEntity)->ProtoStatItemValue
     * @param packets
     * @param statDataEntity
     * @param axis
     */
    public void parseStatData(HashMap <Long, Packet> packets, StatDataEntity statDataEntity, Axis axis) {
        if (Objects.isNull(statDataEntity)) return;
        byte[] data = statDataEntity.getItemValue().getData();
        if (data.length == 0) return;
        try {
            ProtoStatItemValue statItemValue = ProtoStatItemValue.parseFrom(data);
            parseDataValue(packets, statItemValue, statDataEntity, axis);

        } catch (InvalidProtocolBufferException e) {
            log.error("解析【统计数据包】异常：", e);
        }
    }

    /**
     * 解析 DataValue
     * @param packets
     * @param statItemValue
     * @param statDataEntity
     * @param axis
     */
    public void parseDataValue(HashMap <Long, Packet> packets, ProtoStatItemValue statItemValue,
                               StatDataEntity statDataEntity, Axis axis) {
        if (Objects.isNull(statItemValue)) return;
        List <ProtoDataValue> dataValuesList = statItemValue.getDataValuesList();
        ProtoStatObjKey objKey = statItemValue.getObjKey();
        dataValuesList.stream().forEach(dataValue -> parsePacket(packets, objKey, dataValue, statDataEntity, statItemValue.getTimestamp(),
                statItemValue.getSubStatInterval(), axis));
    }

    public static final int unit = 64;
    public static final long bit = 1;

    /**
     * 解析bitmap并封装packet
     * @param packets
     * @param objKey
     * @param dataValue
     * @param statDataEntity
     * @param timestamp
     * @param subStatInterval
     * @param axis
     */
    public void parsePacket(HashMap <Long, Packet> packets, ProtoStatObjKey objKey, ProtoDataValue dataValue, StatDataEntity statDataEntity,
                            long timestamp, int subStatInterval, Axis axis) {
        if (Objects.isNull(dataValue)) return;
        List <ProtoBitmapValue> bitmapValuesList = dataValue.getBitmapValuesList();
        bitmapValuesList.stream().forEach(bitmapValue -> {
            long index = bitmapValue.getIndex();
            long bitMap = bitmapValue.getBitMap();
            for (int j = 0; j < unit; j++) {
                if ((bitMap & (bit << j)) != 0) {
                    long packetID = index * unit + j;
                    Packet packet;
                    if (!packets.containsKey(packetID)) {
                        packet = new Packet();
                        packet.setPacketID(packetID);
                        packets.put(packetID, packet);
                    } else {
                        packet = packets.get(packetID);
                    }
                    if (Objects.nonNull(packet))
                        complementPacket(objKey, dataValue, statDataEntity, timestamp, subStatInterval, packet, axis);
                }
            }
        });
    }

    /**
     * 完成packet的发送时间(sendTime)和接收时间(recvTime)
     * @param objKey
     * @param dataValue
     * @param statDataEntity
     * @param timestamp
     * @param subStatInterval
     * @param packet
     * @param axis
     */
    public void complementPacket(ProtoStatObjKey objKey, ProtoDataValue dataValue, StatDataEntity statDataEntity, long timestamp,
                                 int subStatInterval, Packet packet, Axis axis) {
        if (statDataEntity.getStatType() == StatDataType.super_socket_send.getType()) {
            long sendTime = timestamp + subStatInterval * dataValue.getIndex();
            String sendSuperSocketID = statDataEntity.getObjID();
            packet.setSendTime(sendTime);
            packet.setSendSuperSocketID(sendSuperSocketID);
            packet.setTimestampSend(timestamp);
            packet.setIdxSend(dataValue.getIndex());
            packet.setYAxisSend(axis.getRoleId("[send]-[" + sendSuperSocketID + "]"));
        } else if (statDataEntity.getStatType() == StatDataType.super_socket_recv.getType()) {
            long recvTime = timestamp + subStatInterval * dataValue.getIndex();
            String recvSuperSocketID = statDataEntity.getObjID();
            packet.setRecvTime(recvTime);
            packet.setRecvSuperSocketID(recvSuperSocketID);
            packet.setTimestampRecv(timestamp);
            packet.setIdxRecv(dataValue.getIndex());
            packet.setYAxisRecv(axis.getRoleId("[recv]-[" + recvSuperSocketID + "]"));
        }
        complementPath(statDataEntity, objKey, packet);
    }

    /**
     * 完成packet的路径信息(path)
     * @param statDataEntity
     * @param objKey
     * @param packet
     */
    public void complementPath(StatDataEntity statDataEntity, ProtoStatObjKey objKey, Packet packet) {
        if (statDataEntity.getObjType() == StatObjType.super_socket.getType()) {
            if (objKey.getAssociatesCount() == 0) return;
            List <ProtoStatObj> associatesList = objKey.getAssociatesList();
            for (int i = 0; i < associatesList.size(); i++) {
                ProtoStatObj statObj = associatesList.get(i);
                if (statObj.getObjType() == StatObjType.path.getType()) {
                    packet.addPathID(statObj.getObjId());
                }
            }
        } else if (statDataEntity.getObjType() == StatObjType.path.getType()) {
            packet.addPathID(statDataEntity.getObjID());
        }
    }
}
