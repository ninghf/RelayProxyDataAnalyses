package com.butel.project.relay.meeting;

import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.entity.StatObjKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
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
@NoArgsConstructor
@Data
public class MeetingOriginalData {

    @NonNull private long startTime;
    @NonNull private long endTime;

    // 关联统计对象
    private Set<String> associateIds;
    // 数据包
    private HashMap<Long, MeetingPacket> packets;

    public MeetingPacket pack(long packetId, boolean create, StatDataType statType, HashMap<Long, MeetingPacket> packets) {
        MeetingPacket packet = null;
        if (!packets.containsKey(packetId)) {
            if (create) {
                packet = new MeetingPacket(packetId, statType);
                packets.put(packetId, packet);
            }
        } else {
            packet = packets.get(packetId);
        }
        return packet;
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

    public List<MeetingPacket> generateSequentialPackets(long startTime, long endTime) {
        if (Objects.isNull(packets))
            return null;
        if (Objects.nonNull(packets) && packets.isEmpty())
            return null;
        // 筛选待分析的数据包
        List<MeetingPacket> packetList = packets.entrySet().stream().map(Map.Entry::getValue)
//                .filter(packet -> packet.getTimestamp() <= startTime || packet.getTimestamp() >= endTime)
                .filter(packet -> packet.getTimestamp() > startTime && packet.getTimestamp() < endTime)
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

    public void merge(MeetingOriginalData originalData) {
        // 合并开始时间、结束时间
        if (startTime > originalData.startTime)
            startTime = originalData.startTime;
        if (endTime < originalData.endTime)
            endTime = originalData.endTime;
        if (originalData.isEmpty())
            return;
        // 合并关联ID
        if (isEmpty()) {
            associateIds = originalData.associateIds;
            packets = originalData.packets;
        } else {
            associateIds.addAll(originalData.associateIds);
            // 合并数据包
            originalData.packets.entrySet().stream().forEach(entry -> {
                long packetId = entry.getKey();
                MeetingPacket packet = entry.getValue();
                if (packets.containsKey(packetId))
                    packets.get(packetId).merge(packet);
                else
                    packets.put(packetId, packet);
            });
        }
    }
}
