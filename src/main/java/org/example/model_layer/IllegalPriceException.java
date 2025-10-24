package org.example.model_layer;
import java.lang.Exception;
public class IllegalPriceException extends RuntimeException {

    public IllegalPriceException(String message)
    {
        super(message);
    }
}
