package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/17
 * @description TODO
 */
@Slf4j
@Getter
@Setter
@ToString
public class AnalysesData {

    private List<Packet> packetList;

    private long sendTotal;
    private long recvTotal;
    private long nonRepeatSendTotal;
    private long nonRepeatRecvTotal;
    private double lossRate;
    private double sendRate;
    private double fecLossRate;
    private double fecRate;
    private double repeatSpendRate;
    private double repeatWasteRate;
    private List<Agent> agents;

    private Map<Long, List<String>> transTimeDistributionDetail;

    private Map <Long, Long> transTimeDistribution;
    private Map <Long, Long> onceTransTimeDistribution;
    private Map <Long, Long> repeatTransTimeDistribution;
    private Map <Long, Long> sendSuccessDistribution;
    private Map <Long, Long> sendFailureDistribution;
    private Map <Long, Long> repeatSuccessDistribution;
    private Map <Long, Long> repeatFailureDistribution;

    public void processOriginalData(OriginalData originalData, final long checkTransTime) {
        packetList = originalData.generateSequentialPackets();
        if (Objects.isNull(packetList) || packetList.isEmpty())
            return;
        // 端到端概要信息
        // 实际的发包总数
        sendTotal = packetList.parallelStream()
                .collect(Collectors.summarizingLong(Packet::sendCount)).getSum();
        // 实际的收包总数
        recvTotal = packetList.parallelStream()
                .collect(Collectors.summarizingLong(Packet::recvCount)).getSum();
        // 业务发包总数（即不重复数据包发送总数）
        nonRepeatSendTotal = packetList.parallelStream()
                .filter(Packet::isOnceSend).count();
        // 业务收包总数（即不重复数据包接收总数）
        nonRepeatRecvTotal = packetList.parallelStream()
                .filter(Packet::isOnceRecv).count();
        // 原始丢包率=（1 – 实际收包/实际发包）*100
        lossRate = keepFourDecimalPlaces(1 - (double)recvTotal/sendTotal) * 100;
        // 发送丢包率=（1 - 业务发包/应发包）*100
        long max = packetList.parallelStream().max(Packet::compareTo).get().getPacketID();
        long min = packetList.parallelStream().min(Packet::compareTo).get().getPacketID();
        long perfectSendTotal = max - min + 1;
        sendRate = keepFourDecimalPlaces(1 - (double)nonRepeatSendTotal/perfectSendTotal) * 100;
        // 纠错后丢包率=（1 - 业务收包（在超时时间范围内）/业务发包）*100
        long validNonRepeatRecvTotal = packetList.parallelStream()
                .filter(packet -> packet.isValidRecv(checkTransTime)).count();
        fecLossRate = keepFourDecimalPlaces(1 - (double)validNonRepeatRecvTotal/nonRepeatSendTotal) * 100;
        // 纠错效率=修复的数据包/问题数据包；
        long repeatSendCount = packetList.parallelStream()
                .filter(Packet::isRepeatSend).count();
        // 修复的数据包
        long fecValidNonRepeatRecvTotal = packetList.parallelStream()
                .filter(Packet::isRepeatSend)
                .filter(packet -> packet.isValidRecv(checkTransTime)).count();
        fecRate = keepFourDecimalPlaces((double)fecValidNonRepeatRecvTotal/repeatSendCount) * 100;
        // 重传成本率：重发个数/修复包个数；
        long repeatSendCountTotal = packetList.parallelStream()
                .collect(Collectors.summarizingLong(Packet::repeatSendCount)).getSum();
        repeatSpendRate = keepFourDecimalPlaces((double) repeatSendCountTotal/fecValidNonRepeatRecvTotal) * 100;
        // 重传浪费率： 收到的重复包/修改包个数；
        long repeatRecvCountTotal = packetList.parallelStream()
                .collect(Collectors.summarizingLong(Packet::repeatRecvCount)).getSum();
        repeatWasteRate = keepFourDecimalPlaces((double) repeatRecvCountTotal/fecValidNonRepeatRecvTotal) * 100;

        // 分段概要信息
        Stream.of(originalData.getPaths()).parallel().forEach(pathId -> {
            // Client发送给指定Agent的数据包总数
            LinkedList<Packet> sendToAgentList = packetList.parallelStream()
                    .filter(packet -> packet.isSendToAgent(pathId))
                    .collect(() -> new LinkedList <>(), (list, packet) -> {
                        if (pathId.equals(packet.getSenderPathId()))
                            list.add(packet);
                        if (packet.isRepeat()) {
                            packet.getRepeats().parallelStream().forEach(repeat -> {
                                if (pathId.equals(repeat.getSenderPathId()))
                                    list.add(repeat);
                            });
                        }
                    }, (list1, list2) -> list1.addAll(list2));
            LinkedList<Proxy> agentList = packetList.parallelStream()
                    .filter(packet -> Objects.nonNull(packet.getProxies()))
                    .filter(packet -> packet.isSendToAgent(pathId))
                    .collect(() -> new LinkedList <>(), (list, packet) -> {
                        if (Objects.nonNull(packet.getProxies())) {
                            packet.getProxies().stream()
                                    .filter(proxy -> pathId.equals(proxy.getId()))
                                    .forEach(proxy -> list.add(proxy));
                        }
                    }, (list1, list2) -> list1.addAll(list2));
            LinkedList<Packet> recvFromAgentList = packetList.parallelStream()
                    .filter(packet -> packet.isSendToAgent(pathId))
                    .filter(packet -> packet.isRecvFromAgent(pathId))
                    .collect(() -> new LinkedList <>(), (list, packet) -> {
                        if (pathId.equals(packet.getRecverPathId()))
                            list.add(packet);
                        if (packet.isRepeat()) {
                            packet.getRepeats().parallelStream().forEach(repeat -> {
                                if (pathId.equals(repeat.getRecverPathId()))
                                    list.add(repeat);
                            });
                        }
                    }, (list1, list2) -> list1.addAll(list2));
            // Client发送给指定Agent的数据包总数
            // Agent接到的数据包总数
            // Agent发送的数据包总数
            // Relay从指定Agent接收的数据包总数
            // 指定Agent传输数据包耗时分布
            Map <Long, Long> transTimeOnAgent = packetList.parallelStream()
                    .filter(packet -> packet.isSendToAgent(pathId))
                    .filter(packet -> packet.isRecvFromAgent(pathId))
                    .collect(Collectors.groupingBy((packet) -> packet.getTransTimeByPathId(pathId), Collectors.counting()));
            if (Objects.isNull(agents))
                agents = new LinkedList <>();
            agents.add(new Agent(pathId, sendToAgentList.size(), recvFromAgentList.size(),
                    agentList.parallelStream().filter(Proxy::isValidRecv).count(),
                    agentList.parallelStream().filter(Proxy::isValidSend).count(),
                    transTimeOnAgent));
            log.debug("Client发送【{}】个数据包到Agent【{}】", sendToAgentList.size(), pathId);
            log.debug("Agent【{}】接收到【{}】个数据包", pathId, agentList.parallelStream().filter(Proxy::isValidRecv).count());
            log.debug("Agent【{}】发送【{}】个数据包到Relay", pathId, agentList.parallelStream().filter(Proxy::isValidSend).count());
            log.debug("Relay从Agent【{}】接收到【{}】个数据包", pathId, recvFromAgentList.size());
        });

        if (Objects.isNull(transTimeDistributionDetail))
            transTimeDistributionDetail = new HashMap <>();
        // 数据包按照延时分组
        packetList.stream()
                .filter(Packet::isValidPacket)
                .collect(Collectors.groupingBy(Packet::getTransTime, Collectors.toList()))
                .forEach((transTime, packets) -> {
                    List<String> slices = new LinkedList <>();
                    // 获取数据包连续片段
                    for (int i = 0; i < packets.size(); i++) {
                        Packet packet = packets.get(i);
                        packet.setExpectIdx(i);
                        packet.setDiff((int)(packet.getPacketID() - i));
                    }
                    packets.stream()
                            .collect(Collectors.groupingBy(Packet::getDiff, Collectors.toList()))
                            .entrySet().stream().forEach(entry -> {
                        List <Packet> list = entry.getValue();
                        StringBuilder slice = new StringBuilder();
                        slice.append(list.get(0).getPacketID());
                        if (list.size() > 1) {
                            slice.append("-");
                            slice.append(list.get(list.size() - 1).getPacketID());
                        }
                        slices.add(slice.toString());
                    });
                    // 排序
                    List <String> slicesToSorted = slices.stream().sorted((o1, o2) -> {
                        long packet1 = Long.parseLong(o1.indexOf("-") > 0 ? o1.substring(0, o1.indexOf("-")) : o1);
                        long packet2 = Long.parseLong(o2.indexOf("-") > 0 ? o2.substring(0, o2.indexOf("-")) : o2);
                        return Long.compare(packet1, packet2);
                    }).collect(Collectors.toList());
                    transTimeDistributionDetail.put(transTime, slicesToSorted);
                });
        // 延时分布
        transTimeDistribution = packetList.parallelStream()
                .filter(Packet::isValidPacket)
                .collect(Collectors.groupingBy(Packet::getTransTime, Collectors.counting()));

        onceTransTimeDistribution = packetList.parallelStream()
                .filter(Packet::isValidPacket)
                .filter(Packet::nonRepeat)
                .collect(Collectors.groupingBy(Packet::getOnceTransTime, Collectors.counting()));

        repeatTransTimeDistribution = packetList.parallelStream()
                .filter(Packet::isValidPacket)
                .filter(Packet::isRepeat)
                .collect(Collectors.groupingBy(Packet::getRepeatTransTime, Collectors.counting()));
        // 发送次数分布
        sendSuccessDistribution = packetList.parallelStream()
                .filter(Packet::isValidPacket)
                .collect(Collectors.groupingBy(Packet::getSendCountBySuccess, Collectors.counting()));

        sendFailureDistribution = packetList.parallelStream()
                .filter(Packet::isInvalidPackt)
                .collect(Collectors.groupingBy(Packet::getSendCountByFailure, Collectors.counting()));

        repeatSuccessDistribution = packetList.parallelStream()
                .filter(Packet::isValidPacket)
                .filter(Packet::isRepeat)
                .collect(Collectors.groupingBy(Packet::getSendCountBySuccess, Collectors.counting()));

        repeatFailureDistribution = packetList.parallelStream()
                .filter(Packet::isInvalidPackt)
                .filter(Packet::isRepeat)
                .collect(Collectors.groupingBy(Packet::getSendCountByFailure, Collectors.counting()));
    }

    /**
     * 保留四位小数,不进行四舍五入
     * @param d
     * @return
     */
    public static double keepFourDecimalPlaces(double d) {
        BigDecimal decimal = new BigDecimal(d);
        double scaleValue = decimal.setScale(4, BigDecimal.ROUND_FLOOR).doubleValue();
        return scaleValue;

    }

    @Getter
    @Setter
    @ToString
    public class Agent {

        String pathId;
        long sendToAgentCount;
        long recvFromAgentCount;
        long recvFromClientCount;
        long sendToRelayCount;
        Map<Long, Long> transTimeOnAgent;

        public Agent(String pathId, long sendToAgentCount, long recvFromAgentCount, long recvFromClientCount, long sendToRelayCount, Map <Long, Long> transTimeOnAgent) {
            this.pathId = pathId;
            this.sendToAgentCount = sendToAgentCount;
            this.recvFromAgentCount = recvFromAgentCount;
            this.recvFromClientCount = recvFromClientCount;
            this.sendToRelayCount = sendToRelayCount;
            this.transTimeOnAgent = transTimeOnAgent;
        }
    }

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Long.MAX_VALUE);
        System.out.println(keepFourDecimalPlaces((double)1/3));
    }
}
