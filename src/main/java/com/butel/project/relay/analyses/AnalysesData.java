package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private long packetTotal;
    private long senderTotal;
    private long recverTotal;
    private long lossSenderTotal;
    private long lossRecverTotal;
    private long fecLossSenderTotal;
    private long fecLossRecverTotal;
    private long repeatSendPacketTotal;
    private long repeatRecvPacketTotal;
    private Map <Long, Long> transTimeDistribution;
    private Map <Long, Long> onceTransTimeDistribution;
    private Map <Long, Long> repeatTransTimeDistribution;
    private Map <Long, Long> repeatSuccessDistribution;
    private Map <Long, Long> repeatFailureDistribution;

    public void processOriginalData(OriginalData originalData) {
        List <Packet> packetList = originalData.generateSequentialPackets();
        if (Objects.isNull(packetList) || packetList.isEmpty())
            return;
        packetTotal = packetList.size();

        senderTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::sendCount)).getSum();
        recverTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::recvCount)).getSum();

        lossSenderTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::sendLossCount)).getSum();

        lossRecverTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::recvLossCount)).getSum();

        fecLossSenderTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::sendLossCountByFec)).getSum();

        fecLossRecverTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::recvLossCountByFec)).getSum();

        repeatSendPacketTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::repeatSendCount)).getSum();

        repeatRecvPacketTotal = packetList.stream()
                .collect(Collectors.summarizingLong(Packet::repeatRecvCount)).getSum();

        transTimeDistribution = packetList.stream()
                .collect(Collectors.groupingBy(Packet::getTransTime, Collectors.counting()));
        onceTransTimeDistribution = packetList.stream()
                .filter(Packet::nonRepeat)
                .collect(Collectors.groupingBy(Packet::getOnceTransTime, Collectors.counting()));
        repeatTransTimeDistribution = packetList.stream()
                .filter(Packet::isRepeat)
                .collect(Collectors.groupingBy(Packet::getRepeatTransTime, Collectors.counting()));

        repeatSuccessDistribution = packetList.stream()
                .filter(Packet::isRepeat)
                .filter(Packet::isVaildPacket)
                .collect(Collectors.groupingBy(Packet::getRepeatSendCountBySuccess, Collectors.counting()));
        repeatFailureDistribution = packetList.stream()
                .filter(Packet::isRepeat)
                .filter(Packet::isInvalidPackt)
                .collect(Collectors.groupingBy(Packet::getRepeatSendCountByFailure, Collectors.counting()));

    //==================================================================================

    }

}
