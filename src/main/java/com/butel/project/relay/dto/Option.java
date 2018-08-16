package com.butel.project.relay.dto;

import com.butel.project.relay.meeting.*;
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

    public static Option createOption(List <MeetingPacket> packetList, List <MeetingAnalysesData.Agent> agents) {
        Map <String, Integer> axis = generateAxis(agents);
        Option option = new Option();
        option.setTitle("数据落点追踪");
        option.setSubtext("包序号从【0】到【10000】");
        List<Series> seriesList = new LinkedList<>();
        option.setSeries(seriesList);
        packetList.stream().forEach(meetingPacket -> {
            List<NetStat> netStats = meetingPacket.getNetStats();
            Collections.sort(netStats, (o1, o2) -> {
                long early = o1.getPacket().getSendTime() - o2.getPacket().getSendTime();
                if (early > 0)
                    return 1;
                else if (early < 0)
                    return -1;
                return 0;
            });
            for (int i = 0; i < netStats.size(); i++) {
                Series series = new Series("line");
                NetStat netStat = netStats.get(i);
                boolean repeat = i == 0 ? false : true;
                PacketContext packet = netStat.getPacket();
                ProxyContext proxy = netStat.getProxy();
                // Client -> Agent
                series.addLineData(packet.getSendTime(), axis.get(sender), netStat.getExtras(repeat));
                if (netStat.isValidProxyRecv()) {
                    series.addLineData(proxy.getRecvTime(), axis.get(recver + delimiter + proxy.getId()), netStat.getExtras(repeat));
                    if (netStat.isValidProxySend())
                        series.addLineData(proxy.getSendTime(), axis.get(sender + delimiter + proxy.getId()), netStat.getExtras(repeat));
                        if (netStat.isValidPointRecv())
                            series.addLineData(packet.getRecvTime(), axis.get(recver), netStat.getExtras(repeat));
                }
                // Agent -> Relay
                seriesList.add(series);
            }
        });
        return option;
    }

    public static Map <String, Integer> generateAxis(List <MeetingAnalysesData.Agent> agents) {
        Map <String, Integer> axis = new HashMap <>();
        int yAxis = 5;
        axis.put(sender, yAxis);
        axis.put(recver, (agents.size() + 1) * 10);
        for (int i = 0; i < agents.size(); i++) {
            MeetingAnalysesData.Agent agent = agents.get(i);
            axis.put(recver + delimiter + agent.getAssociateId(), yAxis += 5);
            axis.put(sender + delimiter + agent.getAssociateId(), yAxis += 5);
        }
        return axis;
    }

    public static Option createOption(String title, String unit, Map <Long, Long> distribution) {
        return createOption(title, "", unit, distribution);
    }

    public static Option createOption(String title, String subtext, String unit, Map <Long, Long> distribution) {
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
                .forEach(entry -> series.addPieData(entry.getValue().longValue(), entry.getKey().toString()  + unit));
        return option;
    }
}
