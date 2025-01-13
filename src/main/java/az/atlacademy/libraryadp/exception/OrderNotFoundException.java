package az.atlacademy.libraryadp.exception;

public class OrderNotFoundException extends RuntimeException
{
    public OrderNotFoundException(String message)
    {
        super(message);
    }    
}
