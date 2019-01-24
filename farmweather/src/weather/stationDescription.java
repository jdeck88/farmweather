package weather;

public class stationDescription {
    private String code;
    private String description;

    public stationDescription(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }

}
