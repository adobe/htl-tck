package io.sightly.tck.html;

/**
 * This exception is thrown when the {@link HTMLExtractor} expects to find a certain element or set of elements.
 */
public class ElementNotFoundException extends RuntimeException {

    /**
     * Creates an {@code ElementNotFoundException}.
     * @param message the exception's message
     */
    public ElementNotFoundException(String message) {
        super(message);
    }
}
