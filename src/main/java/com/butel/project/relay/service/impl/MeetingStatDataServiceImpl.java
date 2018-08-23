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

import java.util.*;

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
        // 处理接收数据包
        // 处理发送失败数据包
        watch.stop();
//        if (log.isDebugEnabled())
//            log.debug("[decode]【{}】条数据耗时{}", statDataEntities.size(), watch.prettyPrint());
    }

    @Override
    public void decode(MeetingOriginalData originalData, long startTime, long endTime, int bound, String superSocketId) {
        StatObjKey objKey = new StatObjKey();
        objKey.self(superSocketId, StatObjType.super_socket.getType());
        // 扩大检索数据包时间范围
        long from  = startTime - bound;
        long to = endTime + bound;
        List<StatDataEntity> statDataEntities =
                repository.findAllByTime_AdjustedTimeBetweenAndStatObjKey_SelfAndStatObjKey_StatTypeIn(
                        from,
                        to,
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
        HashMap<Long, MeetingPacket> packets = statDataEntities.parallelStream()
                .filter(statDataEntity ->
                        statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send) ||
                                statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send_repeat))
                .collect(HashMap::new, (hashMap, statDataEntity) -> {
                            statDataEntity.parseStatData();
                            long adjustedTime = statDataEntity.getTime().getAdjustedTime();
                            if (adjustedTime < startTime || adjustedTime > endTime)
                                return;
                            // 此处没有验证objType == StatObjType.path
                            String associateId = statDataEntity.getStatObjKey().getAssociates().get(0).getObjId();
                            HashMap<Long, LinkedList<Long>> packetIds = statDataEntity.getPacketIds();
                            packetIds.entrySet().stream().forEach(entry -> {
                                long time = entry.getKey();
                                LinkedList<Long> _packetIds = entry.getValue();
                                for (int i = 0; i < _packetIds.size(); i++) {
                                    long packetId = _packetIds.get(i);
                                    MeetingPacket packet = originalData.pack(packetId, true, hashMap);
                                    if (Objects.nonNull(packet))
                                        packet.updateUserStatSendTime(time, associateId, adjustedTime);
                                }
                            });
                        }, (hashMap, hashMap2) -> hashMap2.entrySet().stream().forEach(entry -> {
                            long packetId = entry.getKey();
                            MeetingPacket packet = entry.getValue();
                            if (hashMap.containsKey(packetId)) {
                                hashMap.get(packetId).merge(packet);
                            }
                            hashMap.put(packetId, packet);
                        })
                );
        originalData.setPackets(packets);
        // 过滤数据经过的Proxy
        HashSet<String> associateIds = statDataEntities.parallelStream()
                .filter(statDataEntity ->
                        statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send) ||
                                statDataEntity.comparedWithStatDataType(StatDataType.super_socket_send_repeat))
                .collect(HashSet::new, (set, statDataEntity) -> {
                    // 此处没有验证objType == StatObjType.path
                    String associateId = statDataEntity.getStatObjKey().getAssociates().get(0).getObjId();
                    set.add(associateId);
                }, (set, set2) -> set.addAll(set2));
        originalData.setAssociateIds(associateIds);
        // 路径信息处理
        if (originalData.isEmpty()) return;
        List<StatDataEntity> statDataEntities_associate =
                repository.findAllByTime_AdjustedTimeBetweenAndStatObjKey_SelfInAndStatObjKey_StatTypeIn(
                        from,
                        to,
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
                    String associateId = statDataEntity.getStatObjKey().getSelf().getObjId();
                    HashMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.pack(packetId, false, packets);
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
                    String associateId = statDataEntity.getStatObjKey().getSelf().getObjId();
                    HashMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.pack(packetId, false, packets);
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
                    // 此处没有验证objType == StatObjType.path
                    String associateId = statDataEntity.getStatObjKey().getAssociates().get(0).getObjId();
                    HashMap <Long, LinkedList <Long>> packetIds = statDataEntity.getPacketIds();
                    packetIds.entrySet().stream().forEach(entry -> {
                        long time = entry.getKey();
                        LinkedList <Long> _packetIds = entry.getValue();
                        for (int i = 0; i < _packetIds.size(); i++) {
                            long packetId = _packetIds.get(i);
                            MeetingPacket packet = originalData.pack(packetId, false, packets);
                            if (Objects.nonNull(packet))
                                packet.updateUserStatRecvTime(time, associateId);
                        }
                    });
                });

    }
}
