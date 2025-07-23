package com.javac2.framework.JavaC2Framework.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "results")
public class Result {

    @Id
    private String resultId;

    private String agentId;
    private String commandId;
    private String result;
    private Date date;



    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Result{" +
                "agentId='" + agentId + '\'' +
                ", resultId='" + resultId + '\'' +
                ", commandId='" + commandId + '\'' +
                ", result='" + result + '\'' +
                ", date=" + date +
                '}';
    }
}
