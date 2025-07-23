package com.javac2.framework.JavaC2Framework.Repository;

import com.javac2.framework.JavaC2Framework.Model.Command;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandRepo extends MongoRepository<Command ,String> {
    List<Command> findByAgentId(String agentId);
    List<Command> findByAgentIdAndStatus(String agentId, String status);
}

