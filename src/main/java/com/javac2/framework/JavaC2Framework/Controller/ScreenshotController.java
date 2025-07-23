package com.javac2.framework.JavaC2Framework.Controller;

import com.javac2.framework.JavaC2Framework.Model.Screenshot;
import com.javac2.framework.JavaC2Framework.Service.ScreenshotService;
import com.javac2.framework.JavaC2Framework.Controller.EventWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/screenshots")
public class ScreenshotController {

    @Autowired
    private ScreenshotService screenshotService;

    @Autowired
    private EventWebSocketController wsController;

    // Get all screenshots
    @GetMapping("/")
    public ResponseEntity<List<Screenshot>> getAllScreenshots() {
        List<Screenshot> screenshots = screenshotService.getAllScreenshots();
        return ResponseEntity.ok(screenshots);
    }

    // Get all screenshots by agentId
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<Screenshot>> getScreenshotsByAgent(@PathVariable String agentId) {
        List<Screenshot> screenshots = screenshotService.getAllScreenshotsByAgentId(agentId);
        if (screenshots.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
            return ResponseEntity.ok(screenshots);
        }

//        @PostMapping("/upload")
//        public ResponseEntity<String> insertScreenshot(@RequestParam String agentId,@RequestParam String screenshotData) {
//            String result = screenshotService.insertScreenshot(agentId, screenshotData);
//            return ResponseEntity.ok(result);
//        }


        // Insert screenshot (agentId and screenshotData in request body JSON)
    @PostMapping("/upload")
    public ResponseEntity<String> insertScreenshot(@RequestBody Map<String, String> payload){
        String agentId = payload.get("agentId");
        String screenshotData = payload.get("screenshotData");
        String result = screenshotService.insertScreenshot(agentId, screenshotData);
        wsController.broadcast("screenshot", agentId);
        return ResponseEntity.ok(result);
    }


    // Delete all screenshots
    @DeleteMapping("/")
    public ResponseEntity<String> deleteAllScreenshots() {
        String response = screenshotService.deleteAll();
        return ResponseEntity.ok(response);
        }

        // Delete screenshot by screenshotId

    @DeleteMapping("/{screenshotId}")
    public ResponseEntity<String> deleteScreenshotById(@PathVariable String screenshotId) {
        String response = screenshotService.deleteByScreenshotId(screenshotId);
        return ResponseEntity.ok(response);
        }
}
