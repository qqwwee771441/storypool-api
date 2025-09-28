package com.wudc.storypool.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Base", description = "기본 API")
@Controller
public class HomeController {

    @Operation(summary = "Swagger UI 리다이렉트", description = "루트 경로 접속 시 Swagger UI 페이지로 자동 리다이렉트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Swagger UI로 리다이렉트")
    })
    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger/index.html";
    }
}