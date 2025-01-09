package az.atlacademy.libraryadp.exception;

public class CategoryNotFoundException extends RuntimeException
{
    public CategoryNotFoundException(String message)
    {
        super(message);
    }
}
