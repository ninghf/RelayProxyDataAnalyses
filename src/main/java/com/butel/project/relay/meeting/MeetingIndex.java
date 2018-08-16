package com.butel.project.relay.meeting;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/8/10
 * @description TODO
 */
@Slf4j
@Data
@AllArgsConstructor
public class MeetingIndex {

    private String meetingId;
    private String userId;
    private String pLinkId;
    private String sourceId;
}
