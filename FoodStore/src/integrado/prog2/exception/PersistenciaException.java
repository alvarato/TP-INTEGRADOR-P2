package integrado.prog2.exception;

/**
 * Error de acceso a la base de datos o a su configuracion.
 * Se crea unicamente en dao y config: envuelve la SQLException original como causa
 * (para depuracion) pero el mensaje es siempre apto para el usuario.
 */
public class PersistenciaException extends AppException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
