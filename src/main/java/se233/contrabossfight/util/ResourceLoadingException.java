package se233.contrabossfight.util;

public class ResourceLoadingException extends Exception {

    public ResourceLoadingException() {
        super();
    }

    public ResourceLoadingException(String message) {
        super(message);
    }

    public ResourceLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceLoadingException(Throwable cause) {
        super(cause);
    }
}