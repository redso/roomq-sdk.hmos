package hk.noq.roomq.network;

public class APIError {
    String code;
    String message;

    public APIError(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
