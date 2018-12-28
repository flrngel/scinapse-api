package io.scinapse.api.error;

public class ImageProcessingFailedException extends RuntimeException {

    public ImageProcessingFailedException(String message) {
        super(message);
    }

}
