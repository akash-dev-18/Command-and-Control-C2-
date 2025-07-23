package com.javac2.framework.JavaC2Framework.Repository;

import com.javac2.framework.JavaC2Framework.Model.Screenshot;
import com.javac2.framework.JavaC2Framework.Service.ScreenshotService;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenshotRepo extends MongoRepository<Screenshot,String> {
List<Screenshot>findByAgentId(String agentId);
}
