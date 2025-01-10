package az.atlacademy.libraryadp.exception;

public class AuthorNotFoundException extends RuntimeException
{
    public AuthorNotFoundException(String message)
    {
        super(message);
    }    
}
