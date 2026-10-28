package com.studycrew.studyBoard.ai;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile({"local", "dev"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiSearchController {
    private final AiSearchService aiSearchService;

    @PostMapping("/search")
    public ApiResponse<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        return ApiResponse.onSuccess(new SearchResponse(aiSearchService.search(request.message())));
    }

    public record SearchRequest(@NotBlank @Size(max = 1000) String message) {}
    public record SearchResponse(String answer) {}
}
