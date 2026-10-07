package integrado.prog2.exception;

/**
 * Base de todas las excepciones propias de la aplicacion.
 * Es no verificada (RuntimeException) para no ensuciar las firmas;
 * la capa ui captura AppException y muestra solo getMessage().
 */
public class AppException extends RuntimeException {

    public AppException(String mensaje) {
        super(mensaje);
    }

    public AppException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
