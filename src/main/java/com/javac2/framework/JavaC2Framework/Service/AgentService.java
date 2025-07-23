package com.javac2.framework.JavaC2Framework.Service;

import com.javac2.framework.JavaC2Framework.Model.Agent;
import com.javac2.framework.JavaC2Framework.Repository.AgentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgentService {

    @Autowired
    private AgentRepo agentRepo;

    //listing all agents
    public List<Agent> getAllAgents(){
        return agentRepo.findAll();
    }

    //finding by id
    public Optional<Agent> getAgentById(String agentId){
       return agentRepo.findById(agentId);
    }

    //adding agents
    public String addAgent(Agent agent){
        agentRepo.save(agent);
        return "AGENT ADDED";
    }

    //updating agent
    public String updateAgent(Agent agent,String agentId){
        Agent exsAgent=agentRepo.findById(agentId).orElseThrow(()->new RuntimeException("agent now found"+agentId));
        exsAgent.setAgentIp(agent.getAgentIp());
        exsAgent.setAgentOs(agent.getAgentOs());
        exsAgent.setAgentStatus(agent.getAgentStatus());
        agentRepo.save(exsAgent);
        return "UPDATED SUCCESSFULLY";
    }

    public String deleteAgentById(String agentId) {
        agentRepo.deleteById(agentId);
        return "DELETED";
    }

    public String deleteAllAgent() {
        agentRepo.deleteAll();
        return "ALL AGENT DELETED";
    }
}
