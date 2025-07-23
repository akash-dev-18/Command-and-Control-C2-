package com.javac2.framework.JavaC2Framework.Controller;

import com.javac2.framework.JavaC2Framework.Model.Agent;
import com.javac2.framework.JavaC2Framework.Service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import com.javac2.framework.JavaC2Framework.Controller.EventWebSocketController;

@RestController
@RequestMapping("/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private EventWebSocketController wsController;

    @GetMapping("")
    public ResponseEntity<List<Agent>> getAll(){
        return new ResponseEntity<>(agentService.getAllAgents(), HttpStatus.OK);
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<Optional<Agent>> getById(@PathVariable String agentId){
        return new ResponseEntity<>(agentService.getAgentById(agentId),HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<String> addAgent(@RequestBody Agent agent){
        String res = agentService.addAgent(agent);
        wsController.broadcast("agent_status", agent.getAgentId());
        return new ResponseEntity<>(res,HttpStatus.CREATED);
    }

    @PutMapping("/{agentId}")
    public ResponseEntity<String> updateAgent(@RequestBody Agent agent,@PathVariable String agentId){
        String res = agentService.updateAgent(agent,agentId);
        wsController.broadcast("agent_status", agentId);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }

    @DeleteMapping("/{agentId}")
    public ResponseEntity<String> deleteAgent(@PathVariable String agentId){
        String res = agentService.deleteAgentById(agentId);
        wsController.broadcast("agent_status", agentId);
        return new ResponseEntity<>(res,HttpStatus.OK);
    }

    @DeleteMapping("/all")
    public ResponseEntity<String>deleteAll(){
        return new ResponseEntity<>(agentService.deleteAllAgent(),HttpStatus.OK);
    }


}
