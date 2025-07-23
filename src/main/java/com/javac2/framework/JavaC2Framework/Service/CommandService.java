package com.javac2.framework.JavaC2Framework.Service;

import com.javac2.framework.JavaC2Framework.Model.Command;
import com.javac2.framework.JavaC2Framework.Repository.CommandRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommandService {

    @Autowired
    private CommandRepo commandRepo;

    //Get all commands by agentId
    public List<Command> getByAgentId(String agentId){
        return commandRepo.findByAgentId(agentId);
    }

    //Get command by commandId
    public Optional<Command> getById(String commandId){
        return commandRepo.findById(commandId);
    }

    //Assigning command to output
    public String assignCommand(String agentId,String CommandText){
        Command command = new Command();
        command.setAgentId(agentId);
        command.setCommand(CommandText);
        command.setStatus("PENDING");
        command.setDate(new Date());
        command.setCommandId(UUID.randomUUID().toString());
        commandRepo.save(command);
        return "COMMAND ASSIGNED";
    }

    //Updating Command
    public String updateCommand(String commandId,String status){
        Command exsCommand = commandRepo.findById(commandId).orElseThrow(()-> new RuntimeException("COMMAND NOT FOUND"+commandId));
        exsCommand.setStatus(status);
        commandRepo.save(exsCommand);
        return "UPDATED";
    }

    public List<Command> getPendingCommandsByAgentId(String agentId) {
        return commandRepo.findByAgentIdAndStatus(agentId, "PENDING");
    }

    //Delete command By commandId
    public String deleteCommandById(String commandId){
        commandRepo.deleteById(commandId);
        return "DELETED";
    }

    //Delete all commands
    public String deleteAllCommand(){
        commandRepo.deleteAll();
        return "ALL COMMANDS DELETED";
    }

}
