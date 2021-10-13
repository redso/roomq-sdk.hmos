package hk.noq.roomq;

public class HTTPRequestException extends Exception {
    private int statusCode;

    public HTTPRequestException(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
