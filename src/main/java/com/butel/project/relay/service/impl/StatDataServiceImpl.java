package com.butel.project.relay.service.impl;

import com.butel.project.relay.analyses.Base;
import com.butel.project.relay.analyses.OriginalData;
import com.butel.project.relay.analyses.Packet;
import com.butel.project.relay.analyses.Proxy;
import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.dao.IStatDataDao;
import com.butel.project.relay.entity.StatDataEntity;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoDataValue;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoDataValue.ProtoBitmapValue;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoStatObjKey;
import com.butel.project.relay.protobuf.SDNMessage.ProtoStatItemValue.ProtoStatObjKey.ProtoStatObj;
import com.butel.project.relay.service.IStatDataService;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;
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
public class StatDataServiceImpl implements IStatDataService {

    @Autowired
    private IStatDataDao statDataDao;


    @Override
    public void encode() {

    }

    @Override
    public void decode(OriginalData originalData, long startTime, long endTime, StatObjType statObjType, String... objIds) {
        StopWatch watch = new StopWatch();
        watch.start("DB 操作");
        List <StatDataEntity> statDataEntities = statDataDao.queryStatData(startTime, endTime, statObjType, objIds);
        watch.stop();
        watch.start("数据处理");
        statDataEntities.stream().forEach(statDataEntity -> parseStatData(originalData, statDataEntity));
        watch.stop();
        if (log.isDebugEnabled())
            log.debug("[decode]【{}】条数据耗时{}", statDataEntities.size(), watch.prettyPrint());
    }

    public boolean isRepeat(StatDataEntity statDataEntity) {
        if (statDataEntity.getStatType() == StatDataType.super_socket_send_repeat.getType()
                || statDataEntity.getStatType() == StatDataType.super_socket_recv_repeat.getType())
            return true;
        return false;
    }

    public boolean isSuperSocket(StatDataEntity statDataEntity) {
        return statDataEntity.getObjType() == StatObjType.super_socket.getType();
    }

    public boolean isSender(StatDataEntity statDataEntity) {
        return statDataEntity.getStatType() == StatDataType.super_socket_send.getType() ||
                statDataEntity.getStatType() == StatDataType.super_socket_send_repeat.getType();
    }

    public boolean isRecver(StatDataEntity statDataEntity) {
        return statDataEntity.getStatType() == StatDataType.super_socket_recv.getType() ||
                statDataEntity.getStatType() == StatDataType.super_socket_recv_repeat.getType();
    }

    public boolean isProxy(StatDataEntity statDataEntity) {
        return statDataEntity.getObjType() == StatObjType.path.getType();
    }

    /**
     * 解析protobuf(StatDataEntity)->ProtoStatItemValue
     * @param originalData
     * @param statDataEntity
     */
    public void parseStatData(OriginalData originalData, StatDataEntity statDataEntity) {
        if (Objects.isNull(statDataEntity)) return;
        byte[] data = statDataEntity.getItemValue().getData();
        if (data.length == 0) return;
        try {
            ProtoStatItemValue statItemValue = ProtoStatItemValue.parseFrom(data);
            if (Objects.nonNull(statItemValue))
                parseDataValue(originalData, statItemValue, statDataEntity);
        } catch (InvalidProtocolBufferException e) {
            log.error("解析【统计数据包】异常：", e);
        }
    }

    /**
     * 解析 DataValue
     * @param originalData
     * @param statItemValue
     * @param statDataEntity
     */
    public void parseDataValue(OriginalData originalData, ProtoStatItemValue statItemValue, StatDataEntity statDataEntity) {
        if (Objects.isNull(statItemValue)) return;
        List <ProtoDataValue> dataValuesList = statItemValue.getDataValuesList();
        ProtoStatObjKey objKey = statItemValue.getObjKey();
        dataValuesList.parallelStream().filter(dataValue -> Objects.nonNull(dataValue)).forEach(dataValue -> parsePacket(originalData, objKey, dataValue, statDataEntity, statItemValue.getTimestamp(),
                statItemValue.getSubStatInterval()));
    }

    public static final int unit = 64;
    public static final long bit = 1;

    /**
     * 解析bitmap并封装packet
     * @param originalData
     * @param objKey
     * @param dataValue
     * @param statDataEntity
     * @param timestamp
     * @param subStatInterval
     */
    public void parsePacket(OriginalData originalData, ProtoStatObjKey objKey, ProtoDataValue dataValue, StatDataEntity statDataEntity,
                            long timestamp, int subStatInterval) {
        if (Objects.isNull(dataValue)) return;
        List <ProtoBitmapValue> bitmapValuesList = dataValue.getBitmapValuesList();
        bitmapValuesList.parallelStream().forEach(bitmapValue -> {
            long index = bitmapValue.getIndex();
            long bitMap = bitmapValue.getBitMap();
            for (int j = 0; j < unit; j++) {
                if ((bitMap & (bit << j)) != 0) {
                    long packetID = index * unit + j;
                    // 如何是路径信息数据，只能取出原始数据包，不能判断是否是重复发包
                    Packet packet = originalData.getPacket(packetID, isRepeat(statDataEntity), statDataEntity.getTimestamp());
                    if (Objects.nonNull(packet))
                        complementPacket(originalData, objKey, dataValue, statDataEntity, timestamp, subStatInterval, packet);
                }
            }
        });
    }

    /**
     * 赋值superSocketID和收集Paths
     * @param originalData
     * @param objKey
     * @param dataValue
     * @param statDataEntity
     * @param timestamp
     * @param subStatInterval
     * @param packet
     */
    public void complementPacket(OriginalData originalData, ProtoStatObjKey objKey, ProtoDataValue dataValue, StatDataEntity statDataEntity, long timestamp,
                                 int subStatInterval, Packet packet) {
        if (isSuperSocket(statDataEntity)) {
            packet.setSuperSocketID(statDataEntity.getObjID());
            if (objKey.getAssociatesCount() == 0) return;
            List <ProtoStatObj> associatesList = objKey.getAssociatesList();
            associatesList.parallelStream().filter(statObj -> statObj.getObjType() == StatObjType.path.getType())
                    .forEach(statObj -> {
                        String pathId = statObj.getObjId();
                        if (isSender(statDataEntity))
                            packet.setSenderPathId(pathId);
                        if (isRecver(statDataEntity))
                            packet.setRecverPathId(pathId);
                        originalData.addPath(pathId, isRepeat(statDataEntity));// 收集Paths
                    });
            complementTime(packet, dataValue, statDataEntity, timestamp, subStatInterval);
        } else if (isProxy(statDataEntity)) {
            String pathId = statDataEntity.getObjID();
            Proxy proxy = new Proxy();
            proxy.setId(pathId);
            proxy.setTimestamp(statDataEntity.getTimestamp());
            complementTime(proxy, dataValue, statDataEntity, timestamp, subStatInterval);
            packet.addProxy(proxy);
        }
    }

    /**
     * 完成packet的发送时间(sendTime)和接收时间(recvTime)
     * @param base
     * @param dataValue
     * @param statDataEntity
     * @param timestamp
     * @param subStatInterval
     */
    public void complementTime(Base base, ProtoDataValue dataValue, StatDataEntity statDataEntity, long timestamp,
                               int subStatInterval) {
        long time = timestamp + subStatInterval * dataValue.getIndex();
        if (isSender(statDataEntity)) {
            if (base instanceof Packet) {
                Packet packet = (Packet) base;
                packet.setSendTime(time);
            } else if (base instanceof Proxy) {
                Proxy proxy = (Proxy) base;
                proxy.setSendTime(time);
            }
            base.setTimestampSend(timestamp);
            base.setIdxSend(dataValue.getIndex());
        } else if (isRecver(statDataEntity)) {
            if (base instanceof Packet) {
                Packet packet = (Packet) base;
                packet.setRecvTime(time);
            } else if (base instanceof Proxy) {
                Proxy proxy = (Proxy) base;
                proxy.setRecvTime(time);
            }
            base.setTimestampRecv(timestamp);
            base.setIdxRecv(dataValue.getIndex());
        }
    }
}
