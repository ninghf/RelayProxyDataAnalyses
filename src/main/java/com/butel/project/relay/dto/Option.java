package com.butel.project.relay.dto;

import com.butel.project.relay.analyses.AnalysesData;
import com.butel.project.relay.analyses.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/2
 * @description Echarts option
 */
@Slf4j
@Getter
@Setter
public class Option {

    private static final String sender = "sender";
    private static final String recver = "recver";
    private static final String delimiter = "-";

    private String title;
    private String subtext;
    private List<Series> series;

    public static Option createOption(List <Packet> packetList, List <AnalysesData.Agent> agents) {
        Map <String, Integer> axis = generateAxis(agents);
        Option option = new Option();
        option.setTitle("数据落点追踪");
        option.setSubtext("包序号从【0】到【10000】");
        List<Series> seriesList = new LinkedList<>();
        option.setSeries(seriesList);
        packetList.stream().forEach(packet -> {
            // 根据Agent划线：每根线Agent唯一
            // 正常数据包（首次发送，成功接收，无重复发送数据包）
            if (packet.nonRepeat()) {
                Series series = new Series("line");
                if (packet.isOnceSend()) {
                    String senderPathId = packet.getSenderPathId();
                    // Client -> Agent
                    series.addLineData((int)packet.getSendTime(), axis.get(sender), packet.getExtras(false));
                    long recvTimeByPathId = packet.getAgentRecvTimeByPathId(senderPathId, packet.getSendTime(), packet);
                    if (recvTimeByPathId != -1)
                        series.addLineData((int)recvTimeByPathId, axis.get(recver + delimiter + senderPathId), packet.getExtras(false));
                }
                if (packet.isOnceRecv()) {
                    // Agent -> Relay
                    String recverPathId = packet.getRecverPathId();
                    long sendTimeByPathId = packet.getAgentSendTimeByPathId(recverPathId, packet.getRecvTime(), packet);
                    if (sendTimeByPathId != -1)
                        series.addLineData((int)sendTimeByPathId, axis.get(sender + delimiter + recverPathId), packet.getExtras(false));
                    series.addLineData((int)packet.getRecvTime(), axis.get(recver), packet.getExtras(false));
                }
                seriesList.add(series);

            } else {// 重复发包
                // 首次发包路径
                if (packet.isOnceSend()) {
                    Series series = new Series("line");
                    completeSeries(packet, packet.getExtras(false), axis, series, packet);
                    seriesList.add(series);
                }
                // 重复发包路径
                packet.getRepeats().stream().filter(Packet::isOnceSend)
                        .forEach(repeat -> {

                            Series series = new Series("line");
                            completeSeries(repeat, repeat.getExtras(true), axis, series, packet);
                            seriesList.add(series);
                        });

            }
        });
        return option;
    }

    public static void completeSeries(Packet packet, Map <String, String> extras, Map <String, Integer> axis, Series series, Packet oncePacket) {
        String pathId = packet.getSenderPathId();
        // Client -> Agent
        series.addLineData((int)packet.getSendTime(), axis.get(sender), extras);
        long agentRecvTime = packet.getAgentRecvTimeByPathId(pathId, packet.getSendTime(), oncePacket);
        if (agentRecvTime != -1) {
            series.addLineData((int)agentRecvTime, axis.get(recver + delimiter + pathId), extras);
            // Agent -> Agent（可以看做 发->收）
            long agentSendTime = packet.getAgentSendTimeByPathId(pathId, agentRecvTime, oncePacket);
            if (agentSendTime != -1) {
                series.addLineData((int)agentSendTime, axis.get(sender + delimiter + pathId), extras);
                long recvTime = packet.getValidRecvTimeByAgent(pathId, agentSendTime, oncePacket);
                if (recvTime != -1) {
                    series.addLineData((int)recvTime, axis.get(recver), extras);
                }
            }
        }
    }

    public static Map <String, Integer> generateAxis(List <AnalysesData.Agent> agents) {
        Map <String, Integer> axis = new HashMap <>();
        int yAxis = 5;
        axis.put(sender, yAxis);
        axis.put(recver, (agents.size() + 1) * 10);
        for (int i = 0; i < agents.size(); i++) {
            AnalysesData.Agent agent = agents.get(i);
            axis.put(recver + delimiter + agent.getPathId(), yAxis += 5);
            axis.put(sender + delimiter + agent.getPathId(), yAxis += 5);
        }
        return axis;
    }

    public static Option createOption(String title, Map <Long, Long> distribution) {
        return createOption(title, "", distribution);
    }

    public static Option createOption(String title, String subtext, Map <Long, Long> distribution) {
        if (Objects.isNull(distribution))
            return null;
        Option option = new Option();
        option.setTitle(title);
        option.setSubtext(subtext);
        List<Series> seriesList = new LinkedList<>();
        option.setSeries(seriesList);
        Series series = new Series("pie");
        seriesList.add(series);
        distribution.entrySet().stream()
                .forEach(entry -> series.addPieData(entry.getValue().intValue(), entry.getKey().toString()));
        return option;
    }
}
