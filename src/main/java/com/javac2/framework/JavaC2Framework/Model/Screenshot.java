package com.javac2.framework.JavaC2Framework.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "screenshots")
public class Screenshot {

    @Id
    private String screenshotId;

    private String agentId;
    private String screenshotData;
    private Date time;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }



    public String getScreenshotData() {
        return screenshotData;
    }

    public void setScreenshotData(String screenshotData) {
        this.screenshotData = screenshotData;
    }

    public String getScreenshotId() {
        return screenshotId;
    }

    public void setScreenshotId(String screenshotId) {
        this.screenshotId = screenshotId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Screenshot{" +
                "agentId='" + agentId + '\'' +
                ", screenshotId='" + screenshotId + '\'' +
                ", screenshotData='" + screenshotData + '\'' +
                ", time=" + time +
                '}';
    }
}
