package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 数据包：连续、首次传输、重复传输、SuperSocket唯一、proxy多个
 */
@Slf4j
@Getter
@Setter
@ToString
public class Packet extends Base implements Comparable<Packet> {

    private long packetID;
    private String superSocketID;
    private long sendTime = -1;
    private long recvTime = -1;
    private long transTime = -1;
    private long timestamp;

    private Map<String, Proxy> proxies;
    private List<Packet> repeats;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return packetID == packet.packetID &&
                Objects.equals(superSocketID, packet.superSocketID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetID, superSocketID);
    }

    @Override
    public int compareTo(Packet o) {
        if (o.getPacketID() > this.getPacketID()) return -1;
        if (o.getPacketID() < this.getPacketID()) return 1;
        return 0;
    }

    public boolean isExistProxy(String pathId) {
        if (Objects.isNull(proxies))
            return false;
        return proxies.containsKey(pathId);
    }

    public void addProxy(String pathId) {
        if (Objects.isNull(proxies))
            proxies = new HashMap <>();
        if (!proxies.containsKey(pathId)) {
            Proxy proxy = new Proxy();
            proxy.setId(pathId);
            proxies.put(proxy.getId(), proxy);
        }
    }

    public Proxy getProxy(String pathId) {
        return proxies.get(pathId);
    }

    public void addRepeat(Packet packet) {
        if (Objects.isNull(repeats))
            repeats = new LinkedList <>();
        repeats.add(packet);
    }

    //==================================================================================================================
    public boolean isVaild(long value) {
        if (value != -1)
            return true;
        return false;
    }

    public boolean isRepeat() {
        if (Objects.nonNull(repeats) && !repeats.isEmpty()) return true;
        return false;
    }

    public boolean isOnceSend() {
        if (isVaild(sendTime)) return true;
        return false;
    }

    public boolean nonRepeat() {
        return !isRepeat();
    }

    // 端到端收发包统计: 发送、接收存在就统计
    public long sendCount() {
        int count = 0;
        if (isVaild(sendTime)) count += 1;
        if (isRepeat())
            count += repeats.stream().filter(packet -> isVaild(packet.getSendTime())).count();
        return count;
    }

    public long recvCount() {
        int count = 0;
        if (isVaild(recvTime)) count += 1;
        if (isRepeat())
            count += repeats.stream().filter(packet -> isVaild(packet.getRecvTime())).count();
        return count;
    }

    // 端到端原始丢包统计: 首次发送、接收失败的
    public long sendLossCount() {
        int count = 0;
        if (!isVaild(sendTime)) count += 1;
        return count;
    }

    public long recvLossCount() {
        int count = 0;
        if (!isVaild(recvTime)) count += 1;
        return count;
    }

    // 首次发送数据丢包，纠错重发后还是接收失败的
    public long sendLossCountByFec() {
        int count = 0;
        if (!isVaild(sendTime) && isRepeat()) {
            if (repeats.stream().filter(packet -> isVaild(packet.getSendTime())).count() == 0) count += 1;
        }
        return count;
    }

    public long recvLossCountByFec() {
        int count = 0;
        if (!isVaild(recvTime) && isRepeat()) {
            if (repeats.stream().filter(packet -> isVaild(packet.getRecvTime())).count() == 0) count += 1;
        }
        return count;
    }

    //端到端重复包个数统计
    public long repeatSendCount() {
        long count = 0;
        if (isRepeat()) {
            if (repeats.stream().filter(packet -> isVaild(packet.getSendTime())).count() > 0)
                count += 1;
        }
        return count;
    }

    public long repeatRecvCount() {
        long count = 0;
        if (isRepeat()) {
            if (repeats.stream().filter(packet -> isVaild(packet.getRecvTime())).count() > 0)
                count += 1;
        }
        return count;
    }

    // 传输时间
    public long getVaildSendTime() {
        if (isRepeat()) {
            Optional <Packet> optional = repeats.stream().filter(packet -> isVaild(packet.getSendTime()))
                    .min(Comparator.comparingLong(Packet::getSendTime));
            if (optional.isPresent()) {
                return optional.get().getSendTime();
            }
        }
        return -1;
    }

    public long getVaildRecvTime() {
        if (isRepeat()) {
            Optional <Packet> optional = repeats.stream().filter(packet -> isVaild(packet.getRecvTime()))
                    .min(Comparator.comparingLong(Packet::getRecvTime));
            if (optional.isPresent()) {
                return optional.get().getRecvTime();
            }
        }
        return -1;
    }

    public long getTransTime() {
        long transTime = getOnceTransTime();
        if (isVaild(transTime))
            return transTime;
        transTime = getRepeatTransTime();
        if (isVaild(transTime))
            return transTime;
        return transTime;
    }

    // 首次发送传输时间
    public long getOnceTransTime() {
        if (isVaild(sendTime) && isVaild(recvTime))
            transTime = recvTime - sendTime;
        return transTime;
    }

    // 重复发送传输时间
    public long getRepeatTransTime() {
        if (!isVaild(sendTime) && isVaild(recvTime)) {
            long sendTime = getVaildSendTime();
            if (isVaild(sendTime))
                transTime = recvTime - sendTime;
        } else if (isVaild(sendTime) && !isVaild(recvTime)) {
            long recvTime = getVaildRecvTime();
            if (isVaild(recvTime))
                transTime = recvTime - sendTime;
        } else {
            long sendTime = getVaildSendTime();
            long recvTime = getVaildRecvTime();
            if (isVaild(sendTime) && isVaild(recvTime))
                transTime = recvTime - sendTime;
        }
        return transTime;
    }

//    private long check;
//
//    public void doCheckSequence(int index) {
//        check = packetID - index;
//    }

    // 发送次数
    public boolean isVaildPacket() {
        if ((isVaild(sendTime) || repeatSendCount() > 0) && (isVaild(recvTime) || repeatRecvCount() > 0)) return true;
        return false;
    }

    public boolean isInvalidPackt() {
        return !isVaildPacket();
    }

    public long getRepeatSendCountBySuccess() {
        long count = 0;
        if (isRepeat() && isVaildPacket()) {
            return repeats.stream().filter(packet -> isVaild(packet.getSendTime())).count();
        }
        return count;
    }

    public long getRepeatSendCountByFailure() {
        long count = 0;
        if (isRepeat() && !isVaildPacket()) {
            return repeats.stream().filter(packet -> isVaild(packet.getSendTime())).count();
        }
        return count;
    }

}