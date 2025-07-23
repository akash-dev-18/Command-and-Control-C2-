package com.javac2.framework.JavaC2Framework.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "commands")
public class Command {

    @Id
    private String commandId;

    private String agentId;
    private String command;
    private String status;
    private Date date;


    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Command{" +
                "agentId='" + agentId + '\'' +
                ", commandId='" + commandId + '\'' +
                ", command='" + command + '\'' +
                ", status='" + status + '\'' +
                ", timeStamp='" + date + '\'' +
                '}';
    }
}
