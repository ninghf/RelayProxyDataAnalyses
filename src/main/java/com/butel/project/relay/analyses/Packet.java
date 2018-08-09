package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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

    private String senderPathId;
    private String recverPathId;
    private Set<Proxy> proxies;
    private List<Packet> repeats;

    private int expectIdx;
    private int diff;

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

    public void addProxy(Proxy proxy) {
        if (Objects.isNull(proxies))
            proxies = new HashSet <>();
        proxies.add(proxy);
    }

    public void addRepeat(Packet packet) {
        if (Objects.isNull(repeats))
            repeats = new LinkedList <>();
        repeats.add(packet);
    }

    // 为详细落点提示信息
    private Map<String, String> extras;

    public Map<String, String> getExtras(boolean repeat) {
        if (Objects.isNull(extras)) {
            extras = new HashMap <>();
            extras.put("packetId", Long.toString(packetID));
            extras.put("repeat", Boolean.toString(repeat));
            extras.put("pathId", senderPathId);
        }
        return extras;
    }

    //==================================================================================================================

    // 数据包重复性校验
    public boolean isRepeat() {
        if (Objects.nonNull(repeats) && !repeats.isEmpty()) return true;
        return false;
    }

    public boolean nonRepeat() {
        return !isRepeat();
    }

    /**
     * 1）验证是否是首次发送；
     * 2）验证重复包是否是有效发送
     * @return
     */
    public boolean isOnceSend() {
        if (isValid(sendTime)) return true;
        return false;
    }

    public boolean isOnceRecv() {
        if (isValid(recvTime)) return true;
        return false;
    }

    public boolean isRepeatSend() {
        if (isRepeat()) {
            return repeats.parallelStream().anyMatch(packet -> isValid(packet.getSendTime()));
        }
        return false;
    }

    public boolean isRepeatRecv() {
        if (isRepeat()) {
            return repeats.parallelStream().anyMatch(packet -> isValid(packet.getRecvTime()));
        }
        return false;
    }

    // 有效性的判断
    public boolean isValidPacket() {
        if (isOnceSend() && isOnceRecv()) return true;
        return false;
    }

    public boolean isInvalidPackt() {
        return !isValidPacket();
    }

    // 在超时时间范围内接收到数据包
    public boolean isValidRecv(long checkTransTime) {
        long transTime = getTransTime();
        if (isValid(transTime)) {
            if (checkTransTime >= transTime) return true;
        }
        return false;
    }

    // 端到端收发包统计: 发送、接收存在就统计
    public long sendCount() {
        long count = 0;
        if (isOnceSend()) count += 1;
        if (isRepeat())
            count += repeatSendCount();
        return count;
    }

    public long recvCount() {
        int count = 0;
        if (isOnceRecv()) count += 1;
        if (isRepeat())
            count += repeatRecvCount();
        return count;
    }

    //端到端重复包发送、接收次数统计
    public long repeatSendCount() {
        long count = 0;
        if (isRepeat()) {
            count = repeats.stream().filter(packet -> isValid(packet.getSendTime())).count();
        }
        return count;
    }

    public long repeatRecvCount() {
        long count = 0;
        if (isRepeat()) {
            count = repeats.stream().filter(packet -> isValid(packet.getRecvTime())).count();
        }
        return count;
    }

    // 传输时间
    public long getValidSendTime() {
        if (isRepeat()) {
            Optional <Packet> optional = repeats.stream().filter(packet -> isValid(packet.getSendTime()))
                    .min(Comparator.comparingLong(Packet::getSendTime));
            if (optional.isPresent()) {
                return optional.get().getSendTime();
            }
        }
        return -1;
    }

    public long getValidRecvTime() {
        if (isRepeat()) {
            Optional <Packet> optional = repeats.stream().filter(packet -> isValid(packet.getRecvTime()))
                    .min(Comparator.comparingLong(Packet::getRecvTime));
            if (optional.isPresent()) {
                return optional.get().getRecvTime();
            }
        }
        return -1;
    }

    public long getRecvTimeDifference(long sendTime, long recvTime) {
        return Math.abs(recvTime - sendTime);
    }

    // 指定Agent 接收时间
    public long getValidRecvTimeByAgent(String pathId, long sendTime, Packet oncePacket) {
        long recvTime = -1;
        Optional <Packet> optionalRecver = oncePacket.getRepeats().stream()
                .filter(packet -> pathId.equals(packet.getRecverPathId()))
                .filter(packet -> isValid(packet.getRecvTime()))
                .min((o1, o2) -> {
                    if (o1.getRecvTimeDifference(sendTime, o1.getRecvTime()) > o2.getRecvTimeDifference(sendTime, o2.getRecvTime()))
                        return 1;
                    else if (o1.getRecvTimeDifference(sendTime, o1.getRecvTime()) < o2.getRecvTimeDifference(sendTime, o2.getRecvTime()))
                        return -1;
                    return 0;
                });
        if (pathId.equals(oncePacket.getRecverPathId())) {
            if (optionalRecver.isPresent()) {
                if (isValid(oncePacket.getRecvTime())) {
                    return getRecvTimeDifference(sendTime, oncePacket.getRecvTime()) > getRecvTimeDifference(sendTime, optionalRecver.get().getRecvTime())
                            ? optionalRecver.get().getRecvTime() : oncePacket.getRecvTime();
                } else {
                    recvTime = optionalRecver.get().getRecvTime();
                }
            } else recvTime = oncePacket.getRecvTime();
        } else {
            if (optionalRecver.isPresent())
                recvTime = optionalRecver.get().getRecvTime();
        }
        return recvTime;
    }

    // 求指定Agent传输耗时
    public long getTransTimeByPathId(String pathId) {
        if (pathId.equals(senderPathId) && pathId.equals(recverPathId)) {// 首次发送|接收
            return getOnceTransTime();
        } else {
            long recvTime = -1;
            Optional <Packet> optionalRecver = repeats.stream()
                    .filter(packet -> pathId.equals(packet.getRecverPathId()))
                    .filter(packet -> isValid(packet.getRecvTime()))
                    .min(Comparator.comparingLong(Packet::getRecvTime));
            if (pathId.equals(recverPathId)) {
                if (optionalRecver.isPresent())
                    recvTime = optionalRecver.get().getRecvTime() > this.recvTime ? this.recvTime : optionalRecver.get().getRecvTime();
                else recvTime = this.recvTime;
            } else {
                if (optionalRecver.isPresent())
                    recvTime = optionalRecver.get().getRecvTime();
            }
            long sendTime = -1;
            Optional <Packet> optionalSender = repeats.stream()
                    .filter(packet -> pathId.equals(packet.getSenderPathId()))
                    .filter(packet -> isValid(packet.getSendTime()))
                    .min(Comparator.comparingLong(Packet::getSendTime));
            if (pathId.equals(senderPathId)) {
                if (optionalSender.isPresent())
                    sendTime = optionalSender.get().getSendTime() > this.sendTime ? this.sendTime : optionalSender.get().getRecvTime();
                else sendTime = this.sendTime;
            } else {
                if (optionalSender.isPresent())
                    sendTime = optionalSender.get().getSendTime();
            }
            return recvTime - sendTime;
        }
    }
    // Agent选择规则：时间最近
    // 根据pathID获取指定Agent的接收和发送时间
    public long getAgentRecvTimeByPathId(String pathId, long sendTime, Packet oncePacket) {
        long recvTime = -1;
        if (Objects.isNull(oncePacket.getProxies()) || StringUtils.isEmpty(pathId))
            return recvTime;
        Optional <Proxy> min = oncePacket.getProxies().parallelStream()
                .filter(proxy -> pathId.equals(proxy.getId()))
                .filter(Proxy::isValidRecv)
                .min((o1, o2) -> {
                    if (o1.getRecvTimeDifference(sendTime) - o2.getRecvTimeDifference(sendTime) > 0)
                        return 1;
                    else if (o1.getRecvTimeDifference(sendTime) - o2.getRecvTimeDifference(sendTime) < 0)
                        return -1;
                    return 0;
                });
        if (min.isPresent())
            recvTime = min.get().getRecvTime();
        return recvTime;
    }

    public long getAgentSendTimeByPathId(String pathId, long recvTime, Packet oncePacket) {
        long sendTime = -1;
        if (Objects.isNull(oncePacket.getProxies()) || StringUtils.isEmpty(pathId))
            return sendTime;
        Optional <Proxy> min = oncePacket.getProxies().parallelStream()
                .filter(proxy -> pathId.equals(proxy.getId()))
                .filter(Proxy::isValidSend)
                .min((o1, o2) -> {
                    if (o1.getSendTimeDifference(recvTime) - o2.getSendTimeDifference(recvTime) > 0)
                        return 1;
                    else if (o1.getSendTimeDifference(recvTime) - o2.getSendTimeDifference(recvTime) < 0)
                        return -1;
                    return 0;
                });
        if (min.isPresent())
            sendTime = min.get().getSendTime();
        return sendTime;
    }

    public long getTransTime() {
        long transTime = getOnceTransTime();
        if (isValid(transTime))
            return transTime;
        transTime = getRepeatTransTime();
        if (isValid(transTime))
            return transTime;
        return transTime;
    }

    // 首次发送传输时间
    public long getOnceTransTime() {
        if (isOnceSend() && isOnceRecv())
            transTime = recvTime - sendTime;
        return transTime;
    }

    // 重复发送传输时间
    public long getRepeatTransTime() {
        if (!isOnceSend() && isOnceRecv()) {
            long sendTime = getValidSendTime();
            if (isValid(sendTime))
                transTime = recvTime - sendTime;
        } else if (isOnceSend() && !isOnceRecv()) {
            long recvTime = getValidRecvTime();
            if (isValid(recvTime))
                transTime = recvTime - sendTime;
        } else {
            long sendTime = getValidSendTime();
            long recvTime = getValidRecvTime();
            if (isValid(sendTime) && isValid(recvTime))
                transTime = recvTime - sendTime;
        }
        return transTime;
    }

    public long getSendCountBySuccess() {// 接收端成功收到数据包
        long count = 0;
        if (isOnceRecv()) {
            count = sendCount();
        }
        return count;
    }

    public long getSendCountByFailure() {
        long count = 0;
        if (!isOnceRecv()) {
            count = sendCount();
        }

        return count;
    }

    //========================================路径详细处理=========================================

    //每个路径的接包统计，发包统计，路径是否是重复发包路径



    // 指定Agent有效发包
    public boolean isValidProxyOfSenderById(final String pathId) {
        if (Objects.isNull(proxies)) return false;
        return proxies.parallelStream()
                .anyMatch(proxy -> proxy.getId().equals(pathId) && isValid(proxy.getSendTime()));
    }

    // 指定Agent有效收包
    public boolean isValidProxyOfRecverById(final String pathId) {
        if (Objects.isNull(proxies)) return false;
        return proxies.parallelStream()
                .anyMatch(proxy -> proxy.getId().equals(pathId) && isValid(proxy.getRecvTime()));
    }

    // 是否给指定Agent发送数据包
    public boolean isSendToAgent(String pathId) {
        if (pathId.equals(senderPathId)) return true;
        if (isRepeat()) {
            return repeats.parallelStream().anyMatch(packet -> pathId.equals(packet.getSenderPathId()));
        }
        return false;
    }

    public boolean isRecvFromAgent(String pathId) {
        if (pathId.equals(recverPathId)) return true;
        if (isRepeat()) {
            return repeats.parallelStream().anyMatch(packet -> pathId.equals(packet.getRecverPathId()));
        }
        return false;
    }

    // 有效的Agent接收
    public boolean isValidProxyRecv() {
        if (Objects.isNull(proxies)) return false;
        return proxies.parallelStream()
                .anyMatch(proxy -> proxy.getId().equals(senderPathId) && isValid(proxy.getRecvTime()));
    }

    // 有效的Agent发送
    public boolean isValidProxySend() {
        if (Objects.isNull(proxies)) return false;
        return proxies.parallelStream()
                .anyMatch(proxy -> proxy.getId().equals(recverPathId) && isValid(proxy.getSendTime()));
    }

    // 将路径信息复制到每个重复包
    public void copyProxiesToRepeats() {
        if (isRepeat())
            repeats.parallelStream().forEach(packet -> packet.setProxies(proxies));
    }

    // 重复有效的Proxy接收
    // 重复有效的Proxy发送
}