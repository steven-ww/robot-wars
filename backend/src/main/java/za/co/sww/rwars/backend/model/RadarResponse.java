package za.co.sww.rwars.backend.model;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the response from a radar scan.
 */
@Schema(description = "Response from a radar scan, detailing detections")
public class RadarResponse {

    @Schema(description = "Type of detection in a radar scan response")
    public enum DetectionType {
        WALL,
        ROBOT
    }

    @Schema(description = "Range of the radar scan", example = "5")
    private int range;

    @Schema(description = "List of detections from the radar scan")
    private List<Detection> detections;

    public RadarResponse(int range, List<Detection> detections) {
        this.range = range;
        this.detections = detections;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public List<Detection> getDetections() {
        return detections;
    }

    public void setDetections(List<Detection> detections) {
        this.detections = detections;
    }

   @Schema(description = "Detection details in a radar scan")
   public static class Detection {
        @Schema(description = "X coordinate of the detection", example = "10")
        private int x;
        
        @Schema(description = "Y coordinate of the detection", example = "15")
        private int y;
        
        @Schema(description = "Type of detection", implementation = DetectionType.class)
        private DetectionType type;
        
        @Schema(description = "Additional details about the detection", example = "Wall ahead")
        private String details;

        public Detection(int x, int y, DetectionType type, String details) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.details = details;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public DetectionType getType() {
            return type;
        }

        public void setType(DetectionType type) {
            this.type = type;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }
}
