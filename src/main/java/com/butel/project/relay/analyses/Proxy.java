package com.butel.project.relay.analyses;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/16
 * @description TODO
 */
@Slf4j
@Getter
@Setter
@ToString
public class Proxy extends Base {

    private String id;
    private long sendTime = -1;
    private long recvTime = -1;
    private long transTime = -1;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proxy proxy = (Proxy) o;
        return Objects.equals(id, proxy.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
