package com.butel.project.relay.meeting;

import com.google.common.math.LongMath;
import com.google.common.primitives.Doubles;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/8/10
 * @description TODO
 */
@Slf4j
@ToString
@Getter
@Setter
public class MeetingAnalysesData {

    private List<MeetingPacket> meetingPacketList;

    private long max;
    private long min;
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

    private List<Slice> transTimeDistributionDetail;

    private Map <Long, Long> transTimeDistribution;
    private Map <Long, Long> onceTransTimeDistribution;
    @Deprecated
    private Map <Long, Long> repeatTransTimeDistribution;
    private Map <Long, Long> sendSuccessDistribution;
    private Map <Long, Long> sendFailureDistribution;
    private Map <Long, Long> repeatSuccessDistribution;
    private Map <Long, Long> repeatFailureDistribution;

    public void processOriginalData(MeetingOriginalData originalData, final long checkTransTime) {
        meetingPacketList = originalData.generateSequentialPackets();
        if (Objects.isNull(meetingPacketList) || meetingPacketList.isEmpty())
            return;
        // 端到端概要信息
        // 网络特性 实际的发包总数
        sendTotal = meetingPacketList.parallelStream().collect(Collectors.summarizingLong(MeetingPacket::sendCount)).getSum();
        // 网络特性 实际的收包总数
        recvTotal = meetingPacketList.parallelStream().collect(Collectors.summarizingLong(MeetingPacket::recvCount)).getSum();
        // 业务发包总数（即不重复数据包发送总数）
        nonRepeatSendTotal = meetingPacketList.parallelStream().filter(MeetingPacket::isValidPointSend).count();
        // 业务收包总数（即不重复数据包接收总数）
        nonRepeatRecvTotal = meetingPacketList.parallelStream().filter(MeetingPacket::isValidPointRecv).count();
        // 网络特性 原始丢包率=（1 – 实际收包/实际发包）*100
        lossRate = recvTotal > 0 && sendTotal > 0 ? percentage_1(recvTotal, sendTotal) : 0;
        // 发送丢包率=（1 - 业务发包/应发包）*100
        max = meetingPacketList.parallelStream().max(MeetingPacket::compareTo).get().getUserStat().getPacketId();
        min = meetingPacketList.parallelStream().min(MeetingPacket::compareTo).get().getUserStat().getPacketId();
        long perfectSendTotal = max - min + 1;
        sendRate = nonRepeatSendTotal > 0 && perfectSendTotal > 0 ? percentage_1(nonRepeatSendTotal, perfectSendTotal) : 0;
        // 纠错后丢包率=（1 - 业务收包（在超时时间范围内）/业务发包）*100
        long validNonRepeatRecvTotal = meetingPacketList.parallelStream()
                .filter(packet -> packet.isValidPointRecv(checkTransTime, min)).count();
        fecLossRate = validNonRepeatRecvTotal > 0 && nonRepeatSendTotal > 0 ?
                percentage_1(validNonRepeatRecvTotal, nonRepeatSendTotal) : 0;
        // 纠错效率=修复的数据包/问题数据包；
        long repeatSendCount = meetingPacketList.parallelStream()
                .filter(MeetingPacket::isRepeatSend).count();
        // 修复的数据包
        long fecValidNonRepeatRecvTotal = meetingPacketList.parallelStream()
                .filter(MeetingPacket::isRepeatSend)
                .filter(packet -> packet.isValidPointRecv(checkTransTime, min)).count();
        fecRate = fecValidNonRepeatRecvTotal > 0 && repeatSendCount > 0 ? percentage(fecValidNonRepeatRecvTotal, repeatSendCount) : 0;
        // 重传成本率：重发个数/修复包个数；
        long repeatSendCountTotal = meetingPacketList.parallelStream()
                .collect(Collectors.summarizingLong(MeetingPacket::repeatSendCount)).getSum();
        repeatSpendRate = repeatSendCountTotal > 0 && fecValidNonRepeatRecvTotal > 0 ? percentage(repeatSendCountTotal, fecValidNonRepeatRecvTotal) : 0;
        // 重传浪费率： 收到的重复包/修改包个数；
        long repeatRecvCountTotal = meetingPacketList.parallelStream()
                .collect(Collectors.summarizingLong(MeetingPacket::repeatRecvCount)).getSum();
        repeatWasteRate = repeatRecvCountTotal > 0 && fecValidNonRepeatRecvTotal > 0 ?
                percentage (repeatRecvCountTotal, fecValidNonRepeatRecvTotal) : 0;

        // 用户质量特性 延时分布 抖动分布（抖动 = 延时 - 最小延时）
        long minTransTime = meetingPacketList.parallelStream()
                .filter(MeetingPacket::isValidPointRecv)// 排除没有接收到的数据包
                .min(Comparator.comparingLong(MeetingPacket::minTransTime)).get().minTransTime();
        // 抖动 = 延时 - 最小延时
        transTimeDistribution = meetingPacketList.parallelStream()
                .filter(MeetingPacket::isValidPointRecv)// 排除没有接收到的数据包
                .collect(Collectors.groupingBy((packet) -> packet.getUserStat().transTime(minTransTime), Collectors.counting()));
        // 网络特性 首次发包成功 延时分布 抖动分布
        onceTransTimeDistribution = meetingPacketList.parallelStream()
                .filter(packet -> !packet.isRepeatSend())// 排除重复发送包
                .filter(MeetingPacket::isValidPointRecv)
                .collect(Collectors.groupingBy((packet) -> packet.getUserStat().transTime(minTransTime), Collectors.counting()));
        // 网络特性 重复发包成功 延时分布

        // 网络特性 用户成功收到, 数据包发送次数
        sendSuccessDistribution = meetingPacketList.parallelStream()
                .filter(MeetingPacket::isValidPointRecv)
                .collect(Collectors.groupingBy(MeetingPacket::sendCount, Collectors.counting()));

        // 网络特性 用户未收到, 数据包发送次数
        sendFailureDistribution = meetingPacketList.parallelStream()
                .filter(packet -> !packet.isValidPointRecv())
                .collect(Collectors.groupingBy(MeetingPacket::sendCount, Collectors.counting()));

        // 网络特性 用户成功收到, 数据包是重复发送, 数据包发送次数
        repeatSuccessDistribution = meetingPacketList.parallelStream()
                .filter(MeetingPacket::isRepeatSend)
                .filter(MeetingPacket::isValidPointRecv)
                .collect(Collectors.groupingBy(MeetingPacket::sendCount, Collectors.counting()));
        // 网络特性 用户未收到, 数据包是重复发送, 数据包发送次数
        repeatFailureDistribution = meetingPacketList.parallelStream()
                .filter(MeetingPacket::isRepeatSend)
                .filter(packet -> !packet.isValidPointRecv())
                .collect(Collectors.groupingBy(MeetingPacket::sendCount, Collectors.counting()));

        // 分段概要信息
        // Client发送给指定Agent的数据包总数
        // Agent接到的数据包总数
        // Agent发送的数据包总数
        // Relay从指定Agent接收的数据包总数
        // 指定Agent传输数据包耗时分布
        LinkedList<NetStat> netStats = meetingPacketList.parallelStream()
                .collect(() -> new LinkedList <>(), (list, packet) -> list.addAll(packet.getNetStats()), (list1, list2) -> list1.addAll(list2));
        // 不同的中转分别统计
        originalData.associatesStream().parallel().forEach(associatesId -> {
            List<NetStat> netStats_agent = netStats.parallelStream()
                    .filter(netStat -> netStat.isSendToAgent(associatesId))
                    .collect(Collectors.toList());
            // Client发送给指定Agent的数据包总数
            // Agent接到的数据包总数
            // Agent发送的数据包总数
            // Relay从指定Agent接收的数据包总数
            // 指定Agent传输数据包耗时分布
            Map <Long, Long> transTimeOnAgent = netStats_agent.parallelStream()
                    .filter(NetStat::isValidPointRecv)// 排除没有成功到达对端的数据包
                    .collect(Collectors.groupingBy(netStat -> netStat.transTime(minTransTime), Collectors.counting()));
            if (Objects.isNull(agents))
                agents = new LinkedList <>();
            Agent agent = new Agent(associatesId,
                    netStats_agent.size(),
                    netStats_agent.parallelStream().filter(NetStat::isValidProxyRecv).count(),
                    netStats_agent.parallelStream().filter(NetStat::isValidProxySend).count(),
                    netStats_agent.parallelStream().filter(NetStat::isValidPointRecv).count(),
                    transTimeOnAgent);
            agents.add(agent);
            log.debug("Client发送【{}】个数据包到Agent【{}】", agent.getSendToAgentCount(), agent.getAssociateId());
            log.debug("Agent【{}】接收到【{}】个数据包", agent.getAssociateId(), agent.getRecvFromClientCount());
            log.debug("Agent【{}】发送【{}】个数据包到Relay", agent.getAssociateId(), agent.getSendToRelayCount());
            log.debug("Relay从Agent【{}】接收到【{}】个数据包", agent.getAssociateId(), agent.getRecvFromAgentCount());
        });

        if (Objects.isNull(transTimeDistributionDetail))
            transTimeDistributionDetail = new LinkedList<>();
        // 数据包按照延时分组 数据包成功接收到正常、未成功接收设置延时 10000ms
        meetingPacketList.stream()
                .collect(Collectors.groupingBy((packet) -> packet.getUserStat()._transTime(minTransTime), Collectors.toList()))
                .forEach((transTime, packets) -> {
                    List<String> slices = new LinkedList <>();
                    // 获取数据包连续片段
                    for (int i = 0; i < packets.size(); i++) {
                        MeetingPacket packet = packets.get(i);
                        packet.setExpectIdx(i);
                        packet.setDiff((int)(packet.getUserStat().getPacketId() - i));
                    }
                    packets.stream()
                            .collect(Collectors.groupingBy(MeetingPacket::getDiff, Collectors.toList()))
                            .entrySet().stream().forEach(entry -> {
                        List <MeetingPacket> list = entry.getValue();
                        StringBuilder slice = new StringBuilder();
                        slice.append(list.get(0).getUserStat().getPacketId());// 分组中最小值
                        if (list.size() > 1) {
                            slice.append("-");
                            slice.append(list.get(list.size() - 1).getUserStat().getPacketId());// 最大值
                        }
                        slices.add(slice.toString());
                    });
                    // 排序
                    List <String> slicesToSorted = slices.stream().sorted((o1, o2) -> {
                        long packet1 = Long.parseLong(o1.indexOf("-") > 0 ? o1.substring(0, o1.indexOf("-")) : o1);
                        long packet2 = Long.parseLong(o2.indexOf("-") > 0 ? o2.substring(0, o2.indexOf("-")) : o2);
                        return Long.compare(packet1, packet2);
                    }).collect(Collectors.toList());
                    transTimeDistributionDetail.add(new Slice("<=" + transTime, transTime, slicesToSorted));
                });
    }

    public boolean isEmpty() {
        if (Objects.nonNull(meetingPacketList) && !meetingPacketList.isEmpty())
            return false;
        return true;
    }

    /**
     * p/q
     * @param p
     * @param q
     * @return
     */
    public static double divide(long p, long q) {
        return (double) p/q;
    }

    /**
     * p/q * 100
     * @param p
     * @param q
     * @return
     */
    public static double percentage(long p, long q) {
        return keepFourDecimalPlaces(divide(p, q) * 100, 2);
    }

    /**
     * (1 - p/q) * 100
     * @param p
     * @param q
     * @return
     */
    public static double percentage_1(long p, long q) {
        return keepFourDecimalPlaces((1 - divide(p, q)) * 100, 2);
    }

    public static void main(String[] args) {

        System.out.println(percentage(1, 3));
        System.out.println(percentage_1(1, 3));
    }

    /**
     * 保留[newScale]位小数,不进行四舍五入
     * @param d
     * @param newScale
     * @return
     */
    public static double keepFourDecimalPlaces(double d, int newScale) {
        BigDecimal decimal = new BigDecimal(d);
        double scaleValue = decimal.setScale(newScale, BigDecimal.ROUND_FLOOR).doubleValue();
        return scaleValue;
    }

    @Data
    @AllArgsConstructor
    public class Agent {
        String associateId;
        long sendToAgentCount;
        long recvFromClientCount;
        long sendToRelayCount;
        long recvFromAgentCount;
        Map<Long, Long> transTimeOnAgent;
    }
    @Data
    @AllArgsConstructor
    public class Slice implements Comparable<Slice> {
        String range;
        long val;
        List<String> slices;

        @Override
        public int compareTo(Slice o) {
            if (o.val > this.val)
                return 1;
            else if (o.val < this.val)
                return -1;
            return 0;
        }
    }
}
