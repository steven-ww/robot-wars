package za.co.sww.rwars.backend.api;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Standard HTTP error response payload.
 */
@RegisterForReflection
@Schema(description = "Error response containing error message")
public record HttpError(
        @Schema(description = "Error message describing what went wrong", example = "Invalid request")
        String message
) {
    public HttpError() {
        this(null);
    }
}

