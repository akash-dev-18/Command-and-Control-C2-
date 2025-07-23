package com.javac2.framework.JavaC2Framework.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javac2.framework.JavaC2Framework.Model.FileRecord;
import com.javac2.framework.JavaC2Framework.Repository.FileRecordRepo;

@RestController
@RequestMapping("/files")
public class FileController {
    private static final String UPLOAD_DIR = "uploaded-files";

    @Autowired
    private FileRecordRepo fileRecordRepo;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("agentId") String agentId) throws IOException {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("No file uploaded");
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();
        String fileId = UUID.randomUUID().toString();
        String filename = fileId + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR, filename);
        Files.write(path, file.getBytes());
        FileRecord rec = new FileRecord();
        rec.setFileId(fileId);
        rec.setAgentId(agentId);
        rec.setOriginalFilename(file.getOriginalFilename());
        rec.setStoragePath(path.toString());
        rec.setUploadTime(new Date());
        rec.setSize(file.getSize());
        fileRecordRepo.save(rec);
        return ResponseEntity.ok(fileId);
    }

    @GetMapping("/agent/{agentId}")
    public List<FileRecord> listFiles(@PathVariable String agentId) {
        return fileRecordRepo.findByAgentId(agentId);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) throws IOException {
        Optional<FileRecord> recOpt = fileRecordRepo.findById(fileId);
        if (recOpt.isEmpty()) return ResponseEntity.notFound().build();
        FileRecord rec = recOpt.get();
        File file = new File(rec.getStoragePath());
        if (!file.exists()) return ResponseEntity.notFound().build();
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + rec.getOriginalFilename() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
} 