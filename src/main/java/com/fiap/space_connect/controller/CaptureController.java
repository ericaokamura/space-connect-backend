package com.fiap.space_connect.controller;

import com.fiap.space_connect.model.dto.CaptureEstimateDTO;
import com.fiap.space_connect.service.CaptureEstimatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("capture")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class CaptureController {

    private final CaptureEstimatorService estimatorService;

    public CaptureController(CaptureEstimatorService estimatorService) {
        this.estimatorService = estimatorService;
    }

    @GetMapping("/estimate")
    public ResponseEntity<CaptureEstimateDTO> estimate(
            @RequestParam String noradId) {
        try {
            CaptureEstimateDTO result = estimatorService.estimate(
                    noradId
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Estimation failed for {}: {}", noradId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estimate/batch")
    public ResponseEntity<List<CaptureEstimateDTO>> estimateBatch(
            @RequestBody List<String> requests) {
        List<CaptureEstimateDTO> results = requests.stream()
                .map(r -> {
                    try {
                        return estimatorService.estimate(r);
                    } catch (Exception e) {
                        log.warn("Skipping {}: {}", r, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }
}
