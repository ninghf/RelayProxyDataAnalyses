package com.butel.project.relay.meeting;

import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.entity.StatObjKey;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/8/10
 * @description TODO
 */
@RequiredArgsConstructor
public class MeetingOriginalData {

    @NonNull private long startTime;
    @NonNull private long endTime;

    // 关联统计对象
    private ConcurrentSkipListSet<String> associateIds;
    // 数据包
    private ConcurrentMap<Long, MeetingPacket> packets;

    public void collectAssociate(String associateId) {
        if (Objects.isNull(associateIds))
            associateIds = new ConcurrentSkipListSet<>();
        associateIds.add(associateId);
    }

    public List<StatObjKey.ObjKey> toArray(StatObjType objType) {
        List<StatObjKey.ObjKey> selfs = new LinkedList<>();
        associateIds.stream().forEach(associateId -> {
            StatObjKey objKey = new StatObjKey();
            objKey.self(associateId, objType.getType());
            selfs.add(objKey.getSelf());
        });
        return selfs;
    }

    public MeetingPacket getPacket(long packetId, boolean create) {
        if (Objects.isNull(packets))
            packets = new ConcurrentHashMap<>();
        MeetingPacket packet = null;
        if (!packets.containsKey(packetId)) {
            if (create) {
                packet = new MeetingPacket(packetId);
                packets.put(packetId, packet);
            }
        } else {
            packet = packets.get(packetId);
        }
        return packet;
    }

    public List<MeetingPacket> generateSequentialPackets() {
        if (Objects.isNull(packets))
            return null;
        if (Objects.nonNull(packets) && packets.isEmpty())
            return null;
        // 筛选待分析的数据包
        List<MeetingPacket> packetList = packets.entrySet().stream().map(Map.Entry::getValue)
                .filter(packet -> packet.getUserStat().getSendTime() > this.startTime && packet.getUserStat().getSendTime() < this.endTime)
                .sorted()
                .collect(Collectors.toList());
        if (packetList.isEmpty())
            return null;
        return packetList;
    }

    public Stream<String> associatesStream() {
        String[] associateIds = new String[this.associateIds.size()];
        this.associateIds.toArray(associateIds);
        return Stream.of(associateIds);
    }

    public boolean isEmpty() {
        if (Objects.isNull(associateIds)) return true;
        return associateIds.isEmpty();
    }
}
