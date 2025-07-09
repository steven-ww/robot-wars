package za.co.sww.rwars.backend.model;

import java.util.List;

/**
 * Represents the response from a radar scan.
 */
public class RadarResponse {

    public enum DetectionType {
        WALL,
        ROBOT
    }

    private int range;
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

    public static class Detection {
        private int x;
        private int y;
        private DetectionType type;
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
