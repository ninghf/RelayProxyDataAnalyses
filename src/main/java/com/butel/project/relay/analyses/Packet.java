package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
@Setter
@ToString
public class Packet implements Comparable<Packet> {

    private long packetID;
    private long sendTime;
    private long recvTime;
    private long transTime;
    private long timestampSend;
    private long timestampRecv;
    private int idxSend;
    private int idxRecv;
    private String sendSuperSocketID;
    private String recvSuperSocketID;
    private Set<String> paths;

    private int yAxisSend;
    private int yAxisRecv;

    private long sendTime_;
    private long recvTime_;
    private long transTime_;
    private int yAxisSend_;
    private int yAxisRecv_;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return packetID == packet.packetID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetID);
    }

    @Override
    public int compareTo(Packet o) {
        if (o.getPacketID() > this.getPacketID()) return -1;
        if (o.getPacketID() < this.getPacketID()) return 1;
        return 0;
    }

    public boolean isVaild() {
        if (sendTime > 0 && recvTime > 0) {
            transTime = recvTime - sendTime;
            return true;
        }
        return false;
    }

    public void addPathID(String pathID) {
        if (Objects.isNull(paths))
            paths = new HashSet <>();
        paths.add(pathID);
    }

    public List<long[]> toLongArray(boolean isDetail, Axis axis) {
        List<long[]> line = new LinkedList <>();
        if (isDetail) {
            long[] sendPoint = new long[9];
            sendPoint[0] = sendTime;
            sendPoint[1] = axis.getYAxis(yAxisSend);
            sendPoint[2] = packetID;
            sendPoint[3] = sendTime;
            sendPoint[4] = recvTime;
            sendPoint[5] = transTime;
            sendPoint[6] = sendTime_;
            sendPoint[7] = recvTime_;
            sendPoint[8] = transTime_;
            line.add(sendPoint);
            long[] recvPoint = new long[9];
            recvPoint[0] = recvTime;
            recvPoint[1] = axis.getYAxis(yAxisRecv);
            recvPoint[2] = packetID;
            recvPoint[3] = sendTime;
            recvPoint[4] = recvTime;
            recvPoint[5] = transTime;
            recvPoint[6] = sendTime_;
            recvPoint[7] = recvTime_;
            recvPoint[8] = transTime_;
            line.add(recvPoint);

            long[] sendPoint_ = new long[9];
            sendPoint_[0] = sendTime_;
            sendPoint_[1] = axis.getYAxis(yAxisSend_);
            sendPoint_[2] = packetID;
            sendPoint_[3] = sendTime;
            sendPoint_[4] = recvTime;
            sendPoint_[5] = transTime;
            sendPoint_[6] = sendTime_;
            sendPoint_[7] = recvTime_;
            sendPoint_[8] = transTime_;
            line.add(sendPoint_);
            long[] recvPoint_ = new long[9];
            recvPoint_[0] = recvTime_;
            recvPoint_[1] = axis.getYAxis(yAxisRecv_);
            recvPoint_[2] = packetID;
            recvPoint_[3] = sendTime;
            recvPoint_[4] = recvTime;
            recvPoint_[5] = transTime;
            recvPoint_[6] = sendTime_;
            recvPoint_[7] = recvTime_;
            recvPoint_[8] = transTime_;
            line.add(recvPoint_);
        } else {
            long[] sendPoint = new long[6];
            sendPoint[0] = sendTime;
            sendPoint[1] = axis.getYAxis(yAxisSend);
            sendPoint[2] = packetID;
            sendPoint[3] = sendTime;
            sendPoint[4] = recvTime;
            sendPoint[5] = transTime;
            line.add(sendPoint);
            long[] recvPoint = new long[6];
            recvPoint[0] = recvTime;
            recvPoint[1] = axis.getYAxis(yAxisRecv);
            recvPoint[2] = packetID;
            recvPoint[3] = sendTime;
            recvPoint[4] = recvTime;
            recvPoint[5] = transTime;
            line.add(recvPoint);
        }
        return line;
    }

    public void copy(Packet packet_) {
        this.setSendTime_(packet_.getSendTime());
        this.setRecvTime_(packet_.getRecvTime());
        this.setTransTime_(packet_.getTransTime());
        this.setYAxisSend_(packet_.getYAxisSend());
        this.setYAxisRecv_(packet_.getYAxisRecv());
    }

    public void selfCopy() {
        sendTime_ = sendTime;
        recvTime_ = recvTime;
        transTime_ = transTime;
        yAxisSend_ = yAxisSend;
        yAxisRecv_ = yAxisRecv;
        sendTime = 0;
        recvTime = 0;
        transTime = 0;
        yAxisSend = 0;
        yAxisRecv = 0;
    }
}