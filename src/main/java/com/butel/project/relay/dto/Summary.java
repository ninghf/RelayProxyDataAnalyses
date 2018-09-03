package com.butel.project.relay.dto;

import com.butel.project.relay.job.Job;
import com.butel.project.relay.meeting.MeetingAnalysesData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/17
 * @description TODO
 */
@Slf4j
@ToString
@Getter
@Setter
public class Summary extends BaseRespDto {

    private Date startTime;
    private Date endTime;
    private long max;
    private long min;
    private List<Table> tables;
    private List<Option> options;
    private List<MeetingAnalysesData.Slice> zoom;

    public void toSummary(MeetingAnalysesData analysesData) {
        max = analysesData.getMax();
        min = analysesData.getMin();
        if (Objects.isNull(tables))
            tables = new LinkedList <>();
        Table table = new Table();
        List<Item> items = new LinkedList <>();
        items.add(new Item("端到端").builder(
                analysesData.getSendTotal(), analysesData.getRecvTotal(),
                analysesData.getNonRepeatSendTotal(), analysesData.getNonRepeatRecvTotal(),
                analysesData.getLossRate(), analysesData.getSendRate(), analysesData.getFecLossRate(), analysesData.getFecRate(),
                analysesData.getRepeatSpendRate(), analysesData.getRepeatWasteRate()));
        table.setItems(items);
        tables.add(table);
        List <MeetingAnalysesData.Agent> agents = analysesData.getAgents();
        if (Objects.nonNull(agents)) {
            int agentIdx = 1;
            for (int i = 0; i < agents.size(); i++) {
                MeetingAnalysesData.Agent agent = agents.get(i);
                items.add(new Item("Client->Agent" + agentIdx).builder(agent.getSendToAgentCount(), agent.getRecvFromClientCount(), agent.getAssociateId()));
                items.add(new Item("Agent" + agentIdx + "中转").builder(agent.getRecvFromClientCount(), agent.getSendToRelayCount(), agent.getAssociateId()));
                items.add(new Item("Agent" + agentIdx + "->Relay").builder(agent.getSendToRelayCount(), agent.getRecvFromAgentCount(), agent.getAssociateId()));
                agentIdx++;
            }
        }
    }

    public void toDetail(MeetingAnalysesData analysesData) {
        max = analysesData.getMax();
        min = analysesData.getMin();
        if (Objects.nonNull(analysesData.getTransTimeDistributionDetail())) {
            zoom = analysesData.getTransTimeDistributionDetail();
            Collections.sort(zoom);
        }
        if (Objects.isNull(tables))
            tables = new LinkedList <>();
        Table table = new Table();
        List<Item> items = new LinkedList <>();
        items.add(new Item("端到端").builder(
                analysesData.getSendTotal(), analysesData.getRecvTotal(),
                analysesData.getNonRepeatSendTotal(), analysesData.getNonRepeatRecvTotal(),
                analysesData.getLossRate(), analysesData.getSendRate(), analysesData.getFecLossRate(), analysesData.getFecRate(),
                analysesData.getRepeatSpendRate(), analysesData.getRepeatWasteRate()));
        table.setItems(items);
        tables.add(table);

        if (Objects.isNull(options))
            options = new LinkedList <>();
        // 数据包落点追踪
        options.add(Option.createOption(analysesData.getMeetingPacketList(), analysesData.getAgents()));
        options.add(Option.createOption("端到端延时分布", "ms", analysesData.getTransTimeDistribution()));
        options.add(Option.createOption("首次延时分布", "ms", analysesData.getOnceTransTimeDistribution()));
        options.add(Option.createOption("重复延时分布", "ms", analysesData.getRepeatTransTimeDistribution()));
        options.add(Option.createOption("成功发送次数分布", "次数", analysesData.getSendSuccessDistribution()));
        options.add(Option.createOption("失败发送次数分布", "次数", analysesData.getSendFailureDistribution()));
        options.add(Option.createOption("重复成功发送次数分布", "次数", analysesData.getRepeatSuccessDistribution()));
        options.add(Option.createOption("重复失败发送次数分布", "次数", analysesData.getRepeatFailureDistribution()));
        List <MeetingAnalysesData.Agent> agents = analysesData.getAgents();
        if (Objects.nonNull(agents)) {
            int agentIdx = 1;
            for (int i = 0; i < agents.size(); i++) {
                MeetingAnalysesData.Agent agent = agents.get(i);
                items.add(new Item("Client->Agent" + agentIdx).builder(agent.getSendToAgentCount(), agent.getRecvFromClientCount(), agent.getAssociateId()));
                items.add(new Item("Agent" + agentIdx + "中转").builder(agent.getRecvFromClientCount(), agent.getSendToRelayCount(), agent.getAssociateId()));
                items.add(new Item("Agent" + agentIdx + "->Relay").builder(agent.getSendToRelayCount(), agent.getRecvFromAgentCount(), agent.getAssociateId()));
                options.add(Option.createOption("Agent" + agentIdx + "延时分布", "associateId:" + agent.getAssociateId(), "ms", agent.getTransTimeOnAgent()));
                agentIdx++;
            }
        }
    }

    public static List<Summary> toSummarys(List<Job> jobs) {
        List<Summary> summaries = new LinkedList<>();
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            if (Objects.isNull(job.getAnalysesData()))
                continue;
            if (job.getAnalysesData().isEmpty())
                continue;
            Summary summary = new Summary();
            summaries.add(summary);
            summary.setStartTime(new Date(job.getStartTime()));
            summary.setEndTime(new Date(job.getEndTime()));
            summary.toSummary(job.getAnalysesData());
        }
        return summaries;
    }
}
