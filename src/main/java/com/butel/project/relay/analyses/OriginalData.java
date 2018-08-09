package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/16
 * @description TODO
 */
@Slf4j
@Getter
@Setter
@ToString
public class OriginalData {

    private long startTime;
    private long endTime;

    private ConcurrentMap<String, Integer> paths;// 0:初始化时使用的path;1:重复发包时使用的path
    private ConcurrentMap<Long, Packet> packets;

    public OriginalData(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void addPath(String pathId, boolean repeat) {
        if (Objects.isNull(paths))
            paths = new ConcurrentHashMap <>();
        if (paths.containsKey(pathId)) {
            paths.put(pathId, repeat ? 1 : 0);
        } else {
            paths.put(pathId, 1);
        }
    }

    public String[] getPaths() {
        String[] paths = new String[this.paths.size()];
        this.paths.keySet().toArray(paths);
        return paths;
    }

    public Packet getPacket(long packetId, boolean repeat, long timestamp) {
        if (Objects.isNull(packets))
            packets = new ConcurrentHashMap <>();
        Packet packet;
        if (!packets.containsKey(packetId)) {
            packet = new Packet();
            packet.setPacketID(packetId);
            packet.setTimestamp(timestamp);
            packets.put(packetId, packet);
        } else {
            packet = packets.get(packetId);
        }
        if (repeat) {
            Packet repeatP = new Packet();
            repeatP.setPacketID(packetId);
            repeatP.setTimestamp(timestamp);
            packet.addRepeat(repeatP);
            return repeatP;
        }
        return packet;
    }

    public List<Packet> generateSequentialPackets() {
        // 筛选待分析的数据包
        List<Packet> packetList = packets.entrySet().stream().map(Map.Entry::getValue)
                .filter(packet -> packet.getTimestamp() > this.startTime && packet.getTimestamp() < this.endTime)
                .filter(Packet::isOnceSend)
                .sorted()
                .collect(Collectors.toList());
        if (packetList.isEmpty())
            return null;
        return packetList;
    }
}
