package az.atlacademy.libraryadp.exception;

public class BookOutOfStockException extends RuntimeException
{
    public BookOutOfStockException(String message)
    {
        super(message);
    }    
}
