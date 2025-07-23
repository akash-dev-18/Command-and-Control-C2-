package com.javac2.framework.JavaC2Framework.Repository;

import com.javac2.framework.JavaC2Framework.Model.Agent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepo extends MongoRepository<Agent ,String> {

}

