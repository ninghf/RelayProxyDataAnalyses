package com.butel.project.relay.meeting;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class NetStat {

    private final PacketContext packet;
    private final ProxyContext proxy;
    private boolean sendFail;

    public NetStat(long packetId, String associateId) {
        this.packet = new PacketContext(packetId);
        this.proxy = new ProxyContext(associateId);
    }

    public void updatePacketSendTime(long sendTime) {
        packet.setSendTime(sendTime);
    }

    public void updatePacketRecvTime(long recvTime) {
        packet.setRecvTime(recvTime);
    }

    public void updateProxySendTime(long sendTime) {
        proxy.setSendTime(sendTime);
    }

    public void updateProxyRecvTime(long recvTime) {
        proxy.setRecvTime(recvTime);
    }

    public boolean isExist(String associateId) {
        return Objects.equals(proxy.id, associateId);
    }

    public long computeDiffProxySendTime(long recvTime) {
        return recvTime - proxy.getSendTime();
    }

    public long computeDiffPacketSendTime(long recvTime) {
        return recvTime - packet.getSendTime();
    }

    /**
     * point（Client） 成功发送
     * @return
     */
    public boolean isValidPointSend() {
        return packet.isValidSend();
    }

    /**
     * point（Relay） 成功接收
     * @return
     */
    public boolean isValidPointRecv() {
        return packet.isValidRecv();
    }

    /**
     * Agent 中转发送
     * @return
     */
    public boolean isValidProxySend() {
        return proxy.isValidSend();
    }

    /**
     * Agent 中转接收
     * @return
     */
    public boolean isValidProxyRecv() {
        return proxy.isValidRecv();
    }

    /**
     * 是否是发送到指定Agent的数据包
     * @param associatesId
     * @return
     */
    public boolean isSendToAgent(String associatesId) {
        return Objects.equals(associatesId, proxy.getId());
    }

    public long transTime() {
//        return packet.getRecvTime() - packet.getSendTime();
        // 页面显示数据量大无法显示, 故 50ms 一个区间划分
        long transTime = packet.getRecvTime() - packet.getSendTime();

        return (transTime/50 + 1) * 50;
    }

    // 为详细落点提示信息
    private Map<String, String> extras;

    public Map<String, String> getExtras(boolean repeat) {
        if (Objects.isNull(extras)) {
            extras = new HashMap<>();
            extras.put("packetId", Long.toString(packet.getId()));
            extras.put("repeat", Boolean.toString(repeat));
            extras.put("associatesId", proxy.getId());
        }
        return extras;
    }
}
