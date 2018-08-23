package com.butel.project.relay.spark;

import com.butel.project.relay.meeting.MeetingAnalysesData;
import com.butel.project.relay.meeting.MeetingOriginalData;
import com.butel.project.relay.meeting.MeetingPacket;
import lombok.extern.slf4j.Slf4j;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class JavaSparkAPI {

//    @Autowired
//    private JavaSparkContext context;
//
//    public void processOriginalData(MeetingOriginalData originalData, final long checkTransTime, MeetingAnalysesData analysesData) {
//        List<MeetingPacket> meetingPacketList = originalData.generateSequentialPackets();
//        analysesData.setMeetingPacketList(meetingPacketList);
//        // 判断是否有符合条件的原始数据
//        if (Objects.isNull(meetingPacketList) || meetingPacketList.isEmpty())
//            return;
//        // 端到端概要信息
//        // 网络特性 实际的发包总数
//        JavaRDD<MeetingPacket> dataset = context.parallelize(meetingPacketList);
////        dataset.reduce().
////        sendTotal = meetingPacketList.parallelStream().collect(Collectors.summarizingLong(MeetingPacket::sendCount)).getSum();
////        // 网络特性 实际的收包总数
////        recvTotal = meetingPacketList.parallelStream().collect(Collectors.summarizingLong(MeetingPacket::recvCount)).getSum();
//    }
}
