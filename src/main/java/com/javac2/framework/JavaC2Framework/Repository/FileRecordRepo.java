package com.javac2.framework.JavaC2Framework.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.javac2.framework.JavaC2Framework.Model.FileRecord;

public interface FileRecordRepo extends MongoRepository<FileRecord, String> {
    List<FileRecord> findByAgentId(String agentId);
} 