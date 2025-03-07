package com.auracoda.dbspringload.Workloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class AJAXMessages {

    final List<String> activityMessages = new ArrayList<>();

    public void AddMessage(String myMessage) {
        activityMessages.add("{\"success\": true, \"message\": \""
                + Base64.getEncoder().encodeToString(myMessage.getBytes()) + "\"}");
    };

    public void AddError(String myMessage) {
        activityMessages.add("{\"success\": false, \"message\": \""
                + Base64.getEncoder().encodeToString(myMessage.getBytes()) + "\"}");
    }

    String GenerateResponseJSON() {
        final StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(String.join(",", activityMessages));
        sb.append(']');
        return sb.toString();
    };
}
