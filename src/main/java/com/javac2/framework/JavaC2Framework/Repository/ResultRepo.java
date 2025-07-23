package com.javac2.framework.JavaC2Framework.Repository;

import com.javac2.framework.JavaC2Framework.Model.Result;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepo extends MongoRepository<Result,String>{
     List<Result>findByAgentId(String agentId);
     List<Result>findByCommandId(String commandId);
}
