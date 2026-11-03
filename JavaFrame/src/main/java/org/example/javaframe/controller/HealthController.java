package org.example.javaframe.controller;


import org.example.javaframe.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;

@Tag(name = "健康检查")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Operation(summary = "查看服务是否存活")
    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.success(Map.of("status", "UP"));
    }


    
}