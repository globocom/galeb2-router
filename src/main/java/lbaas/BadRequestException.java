package lbaas;

public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 4297206544278009367L;
    
    public BadRequestException() {
        super("Bad Request");
    }

}