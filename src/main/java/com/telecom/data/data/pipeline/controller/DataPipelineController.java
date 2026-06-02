package com.telecom.data.data.pipeline.controller;

import com.telecom.data.data.pipeline.service.impl.DataPipelineServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pipeline")
public class DataPipelineController {
    private final DataPipelineServiceImpl dataPipelineService;

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String runPipeline(@RequestParam String filePath) throws Exception {
        dataPipelineService.processCsvFile(filePath);
        return "Pipeline successfully completed!";
    }
}
