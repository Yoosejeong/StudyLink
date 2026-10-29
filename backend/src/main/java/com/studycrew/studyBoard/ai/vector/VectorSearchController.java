package com.studycrew.studyBoard.ai.vector;

import com.studycrew.studyBoard.ai.vector.StudyPostVectorService.IndexResult;
import com.studycrew.studyBoard.apiPayload.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/vector")
@ConditionalOnProperty(prefix = "app.vector", name = "enabled", havingValue = "true")
public class VectorSearchController {

    private final StudyPostVectorService studyPostVectorService;

    // 전체 모집글을 Gemini로 임베딩하므로 필요할 때만 수동 호출
    @PostMapping("/index")
    public ApiResponse<IndexResult> index() {
        return ApiResponse.onSuccess(studyPostVectorService.indexAll());
    }

    @GetMapping("/search")
    public ApiResponse<List<VectorSearchResult>> search(
            @RequestParam @NotBlank @Size(max = 1000) String query,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int size) {
        return ApiResponse.onSuccess(studyPostVectorService.search(query, size));
    }
}
