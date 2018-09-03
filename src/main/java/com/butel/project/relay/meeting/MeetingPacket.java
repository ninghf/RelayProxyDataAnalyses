package com.butel.project.relay.meeting;

import com.butel.project.relay.constant.StatDataType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/8/14
 * @description TODO
 */
@Slf4j
@ToString
@Getter
public class MeetingPacket implements Comparable<MeetingPacket> {

    // 用户感受（丢包、延时）
    private final UserStat userStat;
    // 网络质量（有数据包发送就创建一个NetStat）
    private List<NetStat> netStats = new LinkedList<>();
    // 第一次发送的统计时间戳, 目的是划分统计边界
    private long timestamp;
    // 数据包是否连续
    @Setter
    private int expectIdx;
    @Setter
    private int diff;
    // 统计类型
    private StatDataType statType;

    public MeetingPacket(long packetId, StatDataType statType) {
        this.userStat = new UserStat(packetId);
        this.statType = statType;
    }

    /**
     * 1) 有数据包发送就创建一个NetStat
     * 2) 更新用户体验的发送时间
     * @param sendTime
     * @param associateId
     */
    public void updateUserStatSendTime(long sendTime, String associateId, long timestamp) {
        addNetStat(associateId, sendTime);
        userStat.updateSendTime(sendTime);
        if (timestamp > 0) {
            if (this.timestamp == 0) this.timestamp = timestamp;
            if (timestamp < this.timestamp)
                this.timestamp = timestamp;
        }
    }

    public long sendTime() {
        return userStat.getSendTime();
    }

    public List<String> associateIds() {
        List<String> associateIds = new LinkedList<>();
        netStats.stream().forEach(netStat -> associateIds.add(netStat.associateId()));
        return associateIds;
    }

    public void merge(MeetingPacket packet) {
        packet.associateIds().stream().forEach(associateId -> updateUserStatSendTime(packet.sendTime(), associateId, packet.getTimestamp()));
        if (statType == packet.statType)
            log.debug("重复汇报首次发包：{}", this);
    }

    /**
     * 1) 更新用户体验的接收时间
     * 2) 找到对应的proxy的NetStat更新其packet的接收时间
     * 3) 如果找到多个NetStat根据【Proxy的发送时间】最近值更新其packet的接收时间
     * @param recvTime
     */
    public void updateUserStatRecvTime(long recvTime, String associateId) {
        List<NetStat> subsetStats = netStats.parallelStream()
                .filter(netStat -> netStat.isExist(associateId))
                .collect(Collectors.toList());
        if (subsetStats.isEmpty())
            return;
        else if (subsetStats.size() == 1) {
            subsetStats.get(0).updatePacketRecvTime(recvTime);
        } else {// 取Proxy的发送时间与recvTime差值最小
            subsetStats.parallelStream()
                    .min((o1, o2) -> {
                        long diff = o1.computeDiffProxySendTime(recvTime) - o2.computeDiffProxySendTime(recvTime);
                        if (diff > 0)
                            return 1;
                        else if (diff < 0)
                            return -1;
                        return 0;
                    }).ifPresent(netStat -> netStat.updatePacketRecvTime(recvTime));
        }
        userStat.updateRecvTime(recvTime);
    }


    public void addNetStat(String associateId, long sendTime) {
        NetStat netStat = new NetStat(userStat.getPacketId(), associateId);
        netStat.updatePacketSendTime(sendTime);
        netStats.add(netStat);
    }

    /**
     * 更新NetStat_Proxy的发送时间
     * 1) 存在一个相同proxy, 直接更新其发送时间
     * 2) 存在多个相同proxy, 找到NetStat_Proxy的发送时间与sendTime的最小差值
     * @param associateId
     * @param sendTime
     */
    public void updateNetStat_ProxySendTime(String associateId, long sendTime) {
        List<NetStat> subsetStats = netStats.parallelStream()
                .filter(netStat -> netStat.isExist(associateId))
                .collect(Collectors.toList());
        if (subsetStats.isEmpty())
            return;
        else if (subsetStats.size() == 1) {
            subsetStats.get(0).updateProxySendTime(sendTime);
        } else {// 取Proxy的发送时间与recvTime差值最小
            subsetStats.parallelStream()
                    .min((o1, o2) -> {
                        long diff = o1.computeDiffProxySendTime(sendTime) - o2.computeDiffProxySendTime(sendTime);
                        if (diff > 0)
                            return 1;
                        else if (diff < 0)
                            return -1;
                        return 0;
                    }).ifPresent(netStat -> netStat.updateProxySendTime(sendTime));
        }
    }

    /**
     * 更新NetStat_Proxy的接收时间
     * 1) 存在一个相同proxy, 直接更新其接收时间
     * 2) 存在多个相同proxy, 找到NetStat_Packet的发送时间与recvTime的最小差值
     * @param associateId
     * @param recvTime
     */
    public void updateNetStat_ProxyRecvTime(String associateId, long recvTime) {
        List<NetStat> subsetStats = netStats.parallelStream()
                .filter(netStat -> netStat.isExist(associateId))
                .collect(Collectors.toList());
        if (subsetStats.isEmpty())
            return;
        else if (subsetStats.size() == 1) {
            subsetStats.get(0).updateProxyRecvTime(recvTime);
        } else {// 取Proxy的发送时间与recvTime差值最小
            subsetStats.parallelStream()
                    .min((o1, o2) -> {
                        long diff = o1.computeDiffPacketSendTime(recvTime) - o2.computeDiffPacketSendTime(recvTime);
                        if (diff > 0)
                            return 1;
                        else if (diff < 0)
                            return -1;
                        return 0;
                    }).ifPresent(netStat -> netStat.updateProxyRecvTime(recvTime));
        }
    }

    @Override
    public int compareTo(MeetingPacket o) {
        if (o.getUserStat().getPacketId() > this.getUserStat().getPacketId()) return -1;
        if (o.getUserStat().getPacketId() < this.getUserStat().getPacketId()) return 1;
        return 0;
    }

    /**
     * 用户质量特性
     * 用户发送数据包
     * @return
     */
    public boolean isValidPointSend() {
        return userStat.isValidSend();
    }
    /**
     * 用户质量特性
     * 用户接收数据包
     * @return
     */
    public boolean isValidPointRecv() {
        return userStat.isValidRecv();
    }

    /**
     * 用户质量特性
     * 在超时时间范围内接收到数据包
     * @param checkTransTime
     * @return
     */
    public boolean isValidPointRecv(long checkTransTime, long min) {
        if (isValidPointRecv()) {
            long transTime = userStat.transTime(min);
            if (checkTransTime >= transTime) return true;
        }
        return false;
    }

    /**
     * 网络特性
     * 数据包发送次数
     * @return
     */
    public long sendCount() {
        return netStats.parallelStream().filter(NetStat::isValidPointSend).count();
    }

    /**
     * 网络特性
     * 数据包接收次数
     * @return
     */
    public long recvCount() {
        return netStats.parallelStream().filter(NetStat::isValidPointRecv).count();
    }

    /**
     * 网络特性
     * 重复发送
     * @return
     */
    public boolean isRepeatSend() {
        return sendCount() > 1 ? true : false;
    }

    /**
     * 网络特性
     * 重复接收
     * @return
     */
    public boolean isRepeatRecv() {
        return recvCount() > 1 ? true : false;
    }

    /**
     * 网络特性
     * 重复发送次数
     * @return
     */
    public long repeatSendCount() {
        return sendCount() - 1;
    }

    /**
     * 网络特性
     * 重复接收次数
     * @return
     */
    public long repeatRecvCount() {
        return recvCount() - 1;
    }

    /**
     * 网络特性
     * 数据包是否经过指定Agent到达对端
     * @param associatesId
     * @return
     */
    public boolean isSendToAgent(String associatesId) {
        return netStats.parallelStream().anyMatch(netStat -> netStat.isSendToAgent(associatesId));
    }

    public long minTransTime() {
        return userStat.minTransTime();
    }

}
