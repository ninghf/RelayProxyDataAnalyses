package com.butel.project.relay.service.impl;

import com.butel.project.relay.analyses.Axis;
import com.butel.project.relay.analyses.AnalysesData;
import com.butel.project.relay.analyses.Packet;
import com.butel.project.relay.constant.StatDataType;
import com.butel.project.relay.constant.StatObjType;
import com.butel.project.relay.dto.DelayedReq;
import com.butel.project.relay.dto.LossReq;
import com.butel.project.relay.service.IAnalysesService;
import com.butel.project.relay.service.IStatDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
public class AnalysesServiceImpl implements IAnalysesService {

    public static final int bound = 1000;

    @Autowired
    private IStatDataService statDataService;

    @Override
    @Cacheable(value = "analyses", key = "#reqId")
    public AnalysesData generateDelayed(long reqId, DelayedReq req) {
        AnalysesData analysesData = new AnalysesData();
        final Axis axis = new Axis(req.getSuperSocketId());
        List <Packet> vPackets = getDelayedPackets(
                req.getStartTime(),
                req.getEndTime(),
                req.getTransTime(),
                req.getSuperSocketId(), req.isDetail(), axis);
        axis.generateAxis();
        analysesData.setId(req.getReqId());
        analysesData.setAxis(axis);
        analysesData.setPackets(vPackets);
        return analysesData;
    }

    @Override
    @Cacheable(value = "analyses", key = "#reqId")
    public AnalysesData generateLoss(long reqId, LossReq req) {
        AnalysesData analysesData = new AnalysesData();
        final Axis axis = new Axis(req.getSuperSocketId());
        List <Packet> vPackets = getLossPackets(
                req.getStartTime(),
                req.getEndTime(),
                req.getPacketIds(),
                req.getLimit(),
                req.getSuperSocketId(), req.isDetail(), axis);
        axis.generateAxis();
        analysesData.setId(req.getReqId());
        analysesData.setAxis(axis);
        analysesData.setPackets(vPackets);
        return analysesData;
    }

    @Override
    public List<Packet> getDelayedPackets(long startTime, long endTime, long transTime,
                                          String superSocketID, boolean isDetail, Axis axis) {
        if (isDetail) {
            // TODO
            List <Packet> packetsFromCToA = getPacketsFromCToA(startTime, endTime, transTime,null, 0, superSocketID, axis);
            List <Packet> packetsFromAToR = getPacketsFromAToR(startTime, endTime, transTime,null, 0, superSocketID, axis);
            return mergePackets(packetsFromCToA, packetsFromAToR);
        }
        return getPacketsFromCToR(startTime, endTime, transTime, null, 0, superSocketID, axis);
    }

    @Override
    public List <Packet> getLossPackets(long startTime, long endTime, List <Long> packetIds, int limit,
                                        String superSocketID, boolean isDetail, Axis axis) {
        if (isDetail) {
            // TODO
            List <Packet> packetsFromCToA = getPacketsFromCToA(startTime, endTime, 0, packetIds, limit, superSocketID, axis);
            List <Packet> packetsFromAToR = getPacketsFromAToR(startTime, endTime, 0, packetIds, limit, superSocketID, axis);
            return mergePackets(packetsFromCToA, packetsFromAToR);
        }
        return getPacketsFromCToR(startTime, endTime, 0, packetIds, limit, superSocketID, axis);
    }

    /**
     * 合并两段的数据
     * @param packetsFromCToA
     * @param packetsFromAToR
     * @return
     */
    public List<Packet> mergePackets(List <Packet> packetsFromCToA, List <Packet> packetsFromAToR) {
        List <Packet> vPackets = new LinkedList <>();
        HashMap<Long, Packet[]> merge = new HashMap <>();
        if (Objects.nonNull(packetsFromCToA)) {
            repeatPackets(merge, 0, packetsFromCToA);
        }
        if (Objects.nonNull(packetsFromAToR)) {
            repeatPackets(merge, 1, packetsFromAToR);
        }
        for(Map.Entry <Long, Packet[]> entry : merge.entrySet()) {
            Packet[] packets = entry.getValue();
            Packet packet = packets[0];
            Packet packet_ = packets[1];
            // TODO 缺少一半数据如何处理
            if (Objects.isNull(packet)) {
                packet_.selfCopy();
                vPackets.add(packet_);
                continue;
            }
            if (Objects.isNull(packet_)) {
                vPackets.add(packet);
                continue;
            }
            if (Objects.nonNull(packet) && Objects.nonNull(packet_)) {
                packet.copy(packet_);
                vPackets.add(packet);
            }
        }

        return vPackets;
    }

    /**
     * 重复数据 C to A 放 0， A to R 放 1；
     * @param merge
     * @param idx
     * @param packets
     */
    public void repeatPackets(HashMap<Long, Packet[]> merge, int idx, List <Packet> packets) {
        for (int i = 0; i < packets.size(); i++) {
            Packet packet = packets.get(i);
            Packet[] packetArr = merge.get(packet.getPacketID());
            if (Objects.isNull(packetArr)) {
                packetArr = new Packet[2];
            }
            packetArr[idx] = packet;
            merge.put(packet.getPacketID(), packetArr);
        }
    }

    /**
     * Client->Relay
     * @param startTime
     * @param endTime
     * @param transTime
     * @param packetIds
     * @param limit
     * @param superSocketID
     * @param axis
     * @return
     */
    public List<Packet> getPacketsFromCToR(long startTime, long endTime, long transTime, List <Long> packetIds, int limit,
                                                  String superSocketID, Axis axis) {
        HashMap <Long, Packet> packets = new HashMap <>();
        statDataService.decode(packets, startTime, endTime, superSocketID,
                StatObjType.super_socket, StatDataType.super_socket_send, axis);
        statDataService.decode(packets, startTime - bound, endTime + bound,
                superSocketID, StatObjType.super_socket, StatDataType.super_socket_recv, axis);
        if (transTime > 0 && limit == 0) {
            return processDelay(packets, transTime);
        } else {
            return processLoss(packets, packetIds, limit);
        }
    }

    /**
     * Client->Agent
     * @param startTime
     * @param endTime
     * @param transTime
     * @param packetIds
     * @param limit
     * @param superSocketID
     * @param axis
     * @return
     */
    public List<Packet> getPacketsFromCToA(long startTime, long endTime, long transTime, List <Long> packetIds, int limit,
                                                  String superSocketID, Axis axis) {
        HashMap <Long, Packet> packets = new HashMap <>();
        statDataService.decode(packets, startTime, endTime,
                superSocketID, StatObjType.super_socket, StatDataType.super_socket_send, axis);
        // 获取所有的Path
        HashSet <String> paths = extractPaths(packets);

        statDataService.decode(packets, paths, startTime - bound, endTime + bound, StatObjType.path,
                StatDataType.super_socket_recv, axis);
        if (transTime > 0 && limit == 0) {
            return processDelay(packets, transTime);
        } else {
            return processLoss(packets, packetIds, limit);
        }
    }

    /**
     * Agent->Relay
     * @param startTime
     * @param endTime
     * @param transTime
     * @param packetIds
     * @param limit
     * @param superSocketID
     * @param axis
     * @return
     */
    public List<Packet> getPacketsFromAToR(long startTime, long endTime, long transTime, List <Long> packetIds, int limit,
                                                  String superSocketID, Axis axis) {
        HashMap<Long, Packet> packets = new HashMap <>();
        statDataService.decode(packets, startTime, endTime, superSocketID, StatObjType.super_socket, StatDataType.super_socket_recv, axis);
        HashSet<String> paths = extractPaths(packets);
        statDataService.decode(packets, paths, startTime - bound, endTime + bound,
                StatObjType.path, StatDataType.super_socket_send, axis);
        if (transTime > 0 && limit == 0) {
            return processDelay(packets, transTime);
        } else {
            return processLoss(packets, packetIds, limit);
        }
    }

    /**
     * 获取所有的Path
     * @param packets
     * @return
     */
    public HashSet<String> extractPaths(HashMap<Long, Packet> packets) {
        HashSet<String> paths = new HashSet <>();
        Iterator <Map.Entry <Long, Packet>> it = packets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry <Long, Packet> entry = it.next();
            Packet packet = entry.getValue();
            if (Objects.nonNull(packet.getPaths()))
                paths.addAll(packet.getPaths());
        }
        return paths;
    }

    /**
     * 对指定时间范围内找到的所有数据包进行分析
     * @param packets
     */
    public List<Packet> processDelay(HashMap<Long, Packet> packets, long transTime) {
        if (packets.isEmpty()) return null;
        Iterator <Map.Entry <Long, Packet>> it = packets.entrySet().iterator();
        HashMap<Long, TreeSet<Packet>> vaildPackets = new HashMap <>();
        while (it.hasNext()) {
            Map.Entry <Long, Packet> next = it.next();
            Packet packet = next.getValue();
            if (packet.isVaild()) {
                TreeSet <Packet> treeSet = vaildPackets.get(packet.getTransTime());
                if (Objects.isNull(treeSet))
                    treeSet = new TreeSet <>();
                treeSet.add(packet);
                vaildPackets.put(packet.getTransTime(), treeSet);
            } else {
                it.remove();
            }
        }
        List<Packet> vPackets = new LinkedList <>();
        if (vaildPackets.isEmpty()) return vPackets;
        Set <Long> transSet = vaildPackets.keySet();
        Long[] rtts = new Long[transSet.size()];
        transSet.toArray(rtts);
        Arrays.sort(rtts);
        log.debug("rtt分布:{}", transSet);
        // 打印所有找到的有效数据包
//        for (int i = 0; i < rtts.length; i++) {
//            TreeSet <Packet> treeSet = vaildPackets.get(rtts[i]);
//            Iterator <Packet> iterator = treeSet.iterator();
//            while (iterator.hasNext())
//                log.debug("数据包:{}", iterator.next());
//        }
        long minTransTime = rtts[0];
        int idx = -1;
        for (int i = 0; i < rtts.length; i++) {
            if (rtts[i] - minTransTime >= transTime) {
                idx = i;
                break;
            }
        }
        if (idx > 0) {
            for (int i = idx; i < rtts.length; i++) {
                TreeSet <Packet> treeSet = vaildPackets.get(rtts[i]);
                Iterator <Packet> iterator = treeSet.iterator();
                while (iterator.hasNext()) {
                    vPackets.add(iterator.next());
                }
            }
        }
        return vPackets;
    }

    public List<Packet> processLoss(HashMap<Long, Packet> packets, List <Long> packetIds, int limit) {
        // 获取需要展示的数据包序列
        if (Objects.isNull(packetIds) || packetIds.isEmpty() || packets.isEmpty())
            return null;
        Long[] packetIdsArr = new Long[packetIds.size()];
        packetIds.toArray(packetIdsArr);
        Arrays.sort(packetIdsArr);
        long min = packetIdsArr[0];
        long max = packetIdsArr[packetIdsArr.length - 1];
        if (max > min) {
            for (long i = min + 1; i < max; i++) {
                packetIds.add(i);
            }
        }
        int part = (int)(max - min);
        if (part > limit)
            throw new IndexOutOfBoundsException("输入的数据包ID范围大于要返回的数据包限制");
        boolean bound = false;
        for (int i = 1; i < limit - part; i++) {
            long packetId = 0;
            if (i%2 == 0 && !bound) {
                min -= 1;
                packetId = min - 1;
                if (packetId == 0)
                    bound = true;
            } else {
                max += 1;
                packetId = max + 1;
            }
            packetIds.add(packetId);
        }

        Iterator <Map.Entry <Long, Packet>> it = packets.entrySet().iterator();
        List<Packet> vPackets = new LinkedList <>();
        while (it.hasNext()) {
            Map.Entry <Long, Packet> next = it.next();
            Packet packet = next.getValue();
            if (packet.isVaild() && packetIds.contains(packet.getPacketID())) {// 数据包有效并且在需要查找的范围内
                vPackets.add(packet);
            } else {
                it.remove();
            }
        }
        return vPackets;
    }
}
