package com.javac2.framework.JavaC2Framework.Service;

import com.javac2.framework.JavaC2Framework.Model.Result;
import com.javac2.framework.JavaC2Framework.Repository.ResultRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ResultService {

    @Autowired
    public ResultRepo resultRepo;

    //get result by resultId
    public Optional<Result> getResultByResultId(String resultId){
        return resultRepo.findById(resultId);
    }

    //get result by agentId
    public List<Result> getResultByAgentId(String agentId){
        return resultRepo.findByAgentId(agentId);
    }

    //get result by commandId
    public List<Result> getResultByCommandId(String commandId){
        return resultRepo.findByCommandId(commandId);
    }

    //save result
    public String saveResult(String agentId,String commandId,String resultText){
        Result result = new Result();
        result.setAgentId(agentId);
        result.setCommandId(commandId);
        result.setDate(new Date());
        result.setResult(resultText);
        resultRepo.save(result);
        return "RESULT SAVED";
    }
    //delete all result
    public String deleteAllResult(){
        resultRepo.deleteAll();
        return "DELETED ALL RESULT";
    }

    //delete result by resultId
    public String deleteByResultId(String resultId){
        resultRepo.deleteById(resultId);
        return "DELETED RESULT";
    }

}
