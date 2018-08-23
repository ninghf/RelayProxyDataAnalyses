package com.butel.project.relay.meeting;

public class UserStat {

    /**
     * userPacket.sendTime 发送时间最小
     * userPacket.recvTime 接收时间最小
     */
    private final PacketContext packet;

    public UserStat(long packetId) {
        this.packet = new PacketContext(packetId);
    }

    public long getPacketId() {
        return packet.getId();
    }

    public long getSendTime() {
        return packet.getSendTime();
    }

    /**
     * sendTime 发送时间最小
     * @param sendTime
     */
    public void updateSendTime(long sendTime) {
        if (packet.isValidSend()) {
            if (sendTime < packet.getSendTime())
                packet.setSendTime(sendTime);
        } else {
            packet.setSendTime(sendTime);
        }
    }

    /**
     * recvTime 接收时间最小
     * @param recvTime
     */
    public void updateRecvTime(long recvTime) {
        if (packet.isValidRecv()) {
            if (recvTime < packet.getRecvTime())
                packet.setRecvTime(recvTime);
        } else {
            packet.setRecvTime(recvTime);
        }
    }

    public boolean isValidSend() {
        return packet.isValidSend();
    }

    public boolean isValidRecv() {
        return packet.isValidRecv();
    }

    public long transTime(long min) {
//        return packet.getRecvTime() - packet.getSendTime();
        // 页面显示数据量大无法显示, 故 50ms 一个区间划分
        long transTime = packet.getRecvTime() - packet.getSendTime();

        return ((transTime - min)/50 + 1) * 50;
    }

    /**
     * 设置未成功接收的数据延时行为是 10000ms
     * @return
     */
    public long _transTime(long min) {
        if (isValidRecv()) {
            long transTime = packet.getRecvTime() - packet.getSendTime();

            return ((transTime - min)/50 + 1) * 50;
        }
        return 10000L;
    }

    public long minTransTime() {
        return packet.getRecvTime() - packet.getSendTime();
    }
}
