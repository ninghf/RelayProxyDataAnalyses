package com.butel.project.relay.analyses;

import lombok.ToString;

import java.util.HashMap;

/**
 * @author ninghf
 * @version 1.0.0.1
 * @copyright www.butel.com
 * @createtime 2018/7/5
 * @description TODO
 */
@ToString
public class Axis {

    private int yAxis = 5;

    private static final String sendPoint = "[send]-";
    private static final String recvPoint = "[recv]-";

    private int roleId;
    private HashMap<String, Integer> roles = new HashMap <>();

    public Axis (String superSocketID) {
        superSocketID = "[" + superSocketID + "]";
        roles.put(sendPoint + superSocketID, roleId++);
        roles.put(recvPoint + superSocketID, roleId++);
    }

    private int[] axis = null;
    public void generateAxis() {
        axis = new int[roles.size()];
        axis[0] = yAxis;
        axis[1] = yAxis * roles.size();
        for (int i = 2; i < axis.length; i++) {
            axis[i] = yAxis += 5;
        }
    }

    public int getYAxis(int roleId) {
        return axis[roleId];
    }

    public int getRoleId(String id) {
        if (roles.containsKey(id)) {
            return roles.get(id);
        }
        createRole(id);
        return roles.get(id);
    }

    private void createRole(String id) {
        String superSocketID = id.substring(id.indexOf("-") + 1);
        roles.put(recvPoint + superSocketID, roleId++);
        roles.put(sendPoint + superSocketID, roleId++);
    }

    public HashMap<String,Integer> getAxis() {
        return (HashMap <String, Integer>) roles.clone();
    }
}
