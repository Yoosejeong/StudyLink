package com.studycrew.studyBoard.util;

import com.studycrew.studyBoard.config.props.S3Props;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3UrlUtil {
    private final S3Props props;

    public String buildPublicUrl(String key) {
        if (key == null || key.isBlank()) return null;

        return "https://" + props.bucket()
                + ".s3." + props.region()
                + ".amazonaws.com/" + key;
    }
}
