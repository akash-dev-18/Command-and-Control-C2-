package com.javac2.framework.JavaC2Framework.Controller;

import com.javac2.framework.JavaC2Framework.Model.Result;
import com.javac2.framework.JavaC2Framework.Service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import com.javac2.framework.JavaC2Framework.Controller.EventWebSocketController;

@RestController
@RequestMapping("/results")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @Autowired
    private EventWebSocketController wsController;

    //get by resultId
    @GetMapping("/{resultId}")
    public ResponseEntity<Result> getByResultId(@PathVariable String resultId){
        return ResponseEntity.of(resultService.getResultByResultId(resultId));
    }

    //getByAgentId
    @GetMapping("/agents/{agentId}")
    public ResponseEntity<List<Result>> getByAgentId(@PathVariable String agentId){
        return new ResponseEntity<>(resultService.getResultByAgentId(agentId), HttpStatus.OK);
    }

    //getByCommandId
    @GetMapping("/commands/{commandId}")
    public ResponseEntity<List<Result>> getByCommandId(@PathVariable String commandId){
        return new ResponseEntity<>(resultService.getResultByCommandId(commandId), HttpStatus.OK);
    }

    //save result
    @PostMapping("/save")
    public ResponseEntity<String> saveResult(@RequestParam String agentId,@RequestParam String commandId,@RequestParam String resultText){
        String res = resultService.saveResult(agentId, commandId, resultText);
        wsController.broadcast("result", agentId);
        return new ResponseEntity<>(res,HttpStatus.CREATED);
    }

    //delete by resultId
    @DeleteMapping("/{resultId}")
    public ResponseEntity<String> deleteByResultId(@PathVariable String resultId ){
        return new ResponseEntity<>(resultService.deleteByResultId(resultId),HttpStatus.OK);
    }

    //delete all results
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAll(){
        return new ResponseEntity<>(resultService.deleteAllResult(),HttpStatus.OK);
    }


}
