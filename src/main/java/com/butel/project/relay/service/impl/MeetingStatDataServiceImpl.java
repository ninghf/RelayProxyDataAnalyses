package com.butel.project.relay.service.impl;

import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.entity.StatDataEntity;
import com.butel.project.relay.entity.StatObjKey;
import com.butel.project.relay.meeting.MeetingIndex;
import com.butel.project.relay.meeting.MeetingOriginalData;
import com.butel.project.relay.meeting.MeetingPacket;
import com.butel.project.relay.repository.StatDataRepository;
import com.butel.project.relay.service.IMeetingStatDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/8/10
 * @description TODO
 */
@Slf4j
@Service
public class MeetingStatDataServiceImpl implements IMeetingStatDataService {

    @Autowired
    private StatDataRepository repository;

    @Override
    public void decode(MeetingOriginalData originalData, MeetingIndex idx) {
        StopWatch watch = new StopWatch();
//        watch.start("DB 操作");
//        List<StatDataEntity> statDataEntities = statDataDao.queryMeetingStatData(meetingId, statObjType);
//        watch.stop();
        watch.start();
        StatObjKey objKey = new StatObjKey();
        objKey.self("60041876", 210);
//        objKey.addAssociate("16906736368878068135", 1);
        objKey.addAssociate("62000405", 211);
        objKey.addAssociate("200", 212);
        List<StatDataEntity> statDataEntities =
                repository.findAllByStatObjKey_SelfAndStatObjKey_AssociatesAndStatObjKey_StatTypeIn(
                        objKey.getSelf(),
                        objKey.getAssociates(),
                        StatDataType.XBOX_send.getType(),
                        StatDataType.XBOX_recv.getType(),
                        StatDataType.XBOX_send_fail.getType());
        log.debug("============================================{}", statDataEntities.size());
        watch.stop();
        watch.start("数据处理");
        // 并行处理
        // 处理发送数据包
        statDataEntities.parallelStream()
                .filter(statDataEntity -> statDataEntity.comparedWithStatDataType(StatDataType.XBOX_send))
                .forEach(statDataEntity -> {
                    statDataEntity.parseStatData();
                    long timestamp = statDataEntity.getTimestamp();
                    // 此处没有验证objType == StatObjType.pLink
                    String associateId = statDataEntity.getStatObjKey().getAssociates().get(0).getObjId();
                    ConcurrentMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
//                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.getPacket(packetId, true);
                            if (Objects.nonNull(packet))
                                packet.updateUserStatSendTime(timestamp, associateId);
                        }

                    });
                });
        // 处理接收数据包
        // 处理发送失败数据包
        watch.stop();
//        if (log.isDebugEnabled())
//            log.debug("[decode]【{}】条数据耗时{}", statDataEntities.size(), watch.prettyPrint());
    }

    @Override
    public void decode(MeetingOriginalData originalData, long sendTime, long endTime, String superSocketId) {
        StatObjKey objKey = new StatObjKey();
        objKey.self(superSocketId, StatObjType.super_socket.getType());
        List<StatDataEntity> statDataEntities =
                repository.findAllByTime_AdjustedTimeBetweenAndStatObjKey_SelfAndStatObjKey_StatTypeIn(
                        sendTime,
                        endTime,
                        objKey.getSelf(),
                        StatDataType.super_socket_send.getType(),
                        StatDataType.super_socket_recv.getType(),
                        StatDataType.super_socket_send_repeat.getType(),
                        StatDataType.super_socket_recv_repeat.getType());
        // 并行处理
        // 处理发送数据包
        // 处理接收数据包
        // 处理重复发送数据包
        // 处理重复接收数据包
        // 处理路径数据包
        if (statDataEntities.size() == 0) return;
        // 处理发送数据包、处理重复发送数据包、收集关联统计对象ID
        statDataEntities.parallelStream()
                .filter(statDataEntity ->
                        statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send) ||
                                statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send_repeat))
                .forEach(statDataEntity -> {
                    statDataEntity.parseStatData();
                    long timestamp = statDataEntity.getTimestamp();
                    // 此处没有验证objType == StatObjType.path
                    String associateId = statDataEntity.getStatObjKey().getAssociates().get(0).getObjId();
                    originalData.collectAssociate(associateId);
                    ConcurrentMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.getPacket(packetId, true);
                            if (Objects.nonNull(packet))
                                packet.updateUserStatSendTime(time, associateId);
                        }
                    });
                });
        // 路径信息处理
        if (originalData.isEmpty()) return;
        List<StatDataEntity> statDataEntities_associate =
                repository.findAllByTime_AdjustedTimeBetweenAndStatObjKey_SelfInAndStatObjKey_StatTypeIn(
                        sendTime,
                        endTime,
                        originalData.toArray(StatObjType.path),
                        StatDataType.super_socket_send.getType(),
                        StatDataType.super_socket_recv.getType(),
                        StatDataType.super_socket_send_repeat.getType(),
                        StatDataType.super_socket_recv_repeat.getType());
        if (statDataEntities_associate.size() == 0) return;
        statDataEntities_associate.parallelStream()
                .filter(statDataEntity ->
                        statDataEntity.comparedWithStatDataType(StatDataType.super_socket_recv) ||
                                statDataEntity.comparedWithStatDataType(StatDataType.super_socket_recv_repeat))
                .forEach(statDataEntity -> {
                    statDataEntity.parseStatData();
                    long timestamp = statDataEntity.getTimestamp();
                    String associateId = statDataEntity.getStatObjKey().getSelf().getObjId();
                    ConcurrentMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.getPacket(packetId, false);
                            if (Objects.nonNull(packet))
                                packet.updateNetStat_ProxyRecvTime(associateId, time);
                        }
                    });
                });
        statDataEntities_associate.parallelStream()
                .filter(statDataEntity ->
                        statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send) ||
                                statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send_repeat))
                .forEach(statDataEntity -> {
                    statDataEntity.parseStatData();
                    long timestamp = statDataEntity.getTimestamp();
                    String associateId = statDataEntity.getStatObjKey().getSelf().getObjId();
                    ConcurrentMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.getPacket(packetId, false);
                            if (Objects.nonNull(packet))
                                packet.updateNetStat_ProxySendTime(associateId, time);
                        }
                    });
                });
        // 处理接收数据包、处理重复接收数据包
        statDataEntities.parallelStream()
                .filter(statDataEntity ->
                        statDataEntity.comparedWithStatDataType(StatDataType.super_socket_recv) ||
                                statDataEntity.comparedWithStatDataType(StatDataType.super_socket_recv_repeat))
                .forEach(statDataEntity -> {
                    statDataEntity.parseStatData();
                    long timestamp = statDataEntity.getTimestamp();
                    // 此处没有验证objType == StatObjType.path
                    String associateId = statDataEntity.getStatObjKey().getAssociates().get(0).getObjId();
                    originalData.collectAssociate(associateId);
                    ConcurrentMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.getPacket(packetId, false);
                            if (Objects.nonNull(packet))
                                packet.updateUserStatRecvTime(time, associateId);
                        }
                    });
                });

    }
}
