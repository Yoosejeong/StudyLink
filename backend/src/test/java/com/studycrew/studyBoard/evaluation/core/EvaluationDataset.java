package com.studycrew.studyBoard.evaluation.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

public final class EvaluationDataset {

    public static final String DEFAULT_PATH = "evaluation/search-queries.json";

    private EvaluationDataset() {}

    public static List<EvalQuery> load(String classpath) {
        try (InputStream in = new ClassPathResource(classpath).getInputStream()) {
            return new ObjectMapper().readValue(in, new TypeReference<>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("평가 데이터셋을 읽을 수 없습니다: " + classpath, e);
        }
    }
}
