package com.javac2.framework.JavaC2Framework.Controller;

import com.javac2.framework.JavaC2Framework.Model.Command;
import com.javac2.framework.JavaC2Framework.Service.CommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/commands")
public class CommandController {

    @Autowired
    private CommandService commandService;

    @GetMapping("/agents/{agentId}")
    public ResponseEntity<List<Command>> getAllCommands(@PathVariable String agentId){
        return new ResponseEntity<>(commandService.getByAgentId(agentId), HttpStatus.OK);
    }

//    @GetMapping("/agents/{agentId}")
//    public ResponseEntity<?> getLastCommand(@PathVariable String agentId) {
//        List<Command> commands = commandService.getByAgentId(agentId);
//        if (commands.isEmpty()) return ResponseEntity.noContent().build();
//
//        Command last = commands.getLast();
//        return ResponseEntity.ok(Map.of(
//                "command", last.getCommand(),
//                "commandId", last.getCommandId()
//        ));
//    }


    @GetMapping("/{commandId}")
    public ResponseEntity<Command> getByCommandId(@PathVariable String commandId){
        return ResponseEntity.of(commandService.getById(commandId));
    }

    @GetMapping("/agents/{agentId}/pending")
    public ResponseEntity<List<Command>> getPendingCommands(@PathVariable String agentId){
        return new ResponseEntity<>(commandService.getPendingCommandsByAgentId(agentId), HttpStatus.OK);
    }

//    @PostMapping("/assign/{agentId}/{commandText}")
//    public ResponseEntity<String> assignCommand(@PathVariable String agentId,@PathVariable String commandText){
//return new ResponseEntity<>(commandService.assignCommand(agentId, commandText),HttpStatus.CREATED);
//    }

    @PostMapping("/assign")  // ✅ THIS LINE IS VERY IMPORTANT
    public ResponseEntity<String> assignCommand(@RequestBody Map<String, String> payload) {
        String agentId = payload.get("agentId");
        String command = payload.get("command");
        return new ResponseEntity<>(commandService.assignCommand(agentId, command), HttpStatus.CREATED);
    }

    @PutMapping("/assign/{commandId}/{status}")
    public ResponseEntity<String> updateStatus(@PathVariable String commandId,@PathVariable String status){
        return new ResponseEntity<>(commandService.updateCommand(commandId,status),HttpStatus.ACCEPTED);
    }



    @DeleteMapping("/{commandId}")
    public ResponseEntity<String> deleteByCommandId(@PathVariable String commandId){
        return new ResponseEntity<>(commandService.deleteCommandById(commandId),HttpStatus.OK);
    }

    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllCommand(){
        return new ResponseEntity<>(commandService.deleteAllCommand(),HttpStatus.OK);
    }

}
