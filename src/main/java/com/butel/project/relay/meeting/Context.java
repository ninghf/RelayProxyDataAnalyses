package com.butel.project.relay.meeting;

import lombok.Data;
import lombok.NonNull;

@Data
public class Context<T> {

    @NonNull protected T id;
    protected long sendTime = -1;
    protected long recvTime = -1;

    // 数值有效性校验
    private boolean isValid(long value) {
        if (value != -1)
            return true;
        return false;
    }

    protected boolean isValidSend() {
        return isValid(sendTime);
    }

    protected boolean isValidRecv() {
        return isValid(recvTime);
    }

    protected boolean isValid() {
        return isValidSend() && isValidRecv();
    }

}
