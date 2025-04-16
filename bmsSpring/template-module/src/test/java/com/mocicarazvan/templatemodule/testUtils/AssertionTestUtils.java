package com.mocicarazvan.templatemodule.testUtils;

import com.mocicarazvan.templatemodule.exceptions.notFound.NotFoundEntity;
import com.mocicarazvan.templatemodule.models.IdGenerated;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertionTestUtils {

    public static final Duration AWAiTILITY_TIMEOUT_SECONDS = Duration.ofSeconds(22);

    public static boolean assertNotFound(Throwable throwable, String modelName, IdGenerated model) {
        if (throwable instanceof NotFoundEntity notFoundEntity) {
            assertEquals(modelName, notFoundEntity.getName());
            assertEquals(model.getId(), notFoundEntity.getId());
            return true;
        } else {
            return false;
        }
    }
}
