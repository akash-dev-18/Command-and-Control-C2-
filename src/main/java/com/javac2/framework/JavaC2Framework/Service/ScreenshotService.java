package com.javac2.framework.JavaC2Framework.Service;

import com.javac2.framework.JavaC2Framework.Model.Screenshot;
import com.javac2.framework.JavaC2Framework.Repository.ScreenshotRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScreenshotService {

    @Autowired
    private ScreenshotRepo screenshotRepo;


    public List<Screenshot> getAllScreenshots() {
        return screenshotRepo.findAll();
    }

    public List<Screenshot> getAllScreenshotsByAgentId(String agentId) {
        return screenshotRepo.findByAgentId(agentId);
    }


//    public String insertScreenshot(String agentId,String screenshotData){
//        Screenshot screenshot= new Screenshot();
//        screenshot.setAgentId(agentId);
//        screenshot.setScreenshotData(screenshotData);
//        screenshot.setTime(new Date());
//        screenshotRepo.save(screenshot);
//        return "SAVED SUCCESSFULLY";
//    }


    public String insertScreenshot(String agentId, String screenshotData) {
        Screenshot screenshot = new Screenshot();
        screenshot.setScreenshotId(UUID.randomUUID().toString()); // add this line
        screenshot.setAgentId(agentId);
        screenshot.setScreenshotData(screenshotData);
        screenshot.setTime(new Date());
        screenshotRepo.save(screenshot);
        return "SAVED SUCCESSFULLY";
    }



    public String deleteAll(){
        screenshotRepo.deleteAll();
        return "DELETED";
    }

    public String deleteByScreenshotId(String screenshotId){
        screenshotRepo.deleteById(screenshotId);
        return "DELETED";
    }
//    public String insertScreenshot(Screenshot screenshot){
//        screenshotRepo.save(screenshot);
//        return "SAVED";
//    }

   }
