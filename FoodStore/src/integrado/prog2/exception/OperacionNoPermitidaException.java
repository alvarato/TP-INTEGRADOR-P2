package integrado.prog2.exception;

/** La regla de negocio impide la operacion (por ejemplo, baja de una categoria con productos activos). */
public class OperacionNoPermitidaException extends AppException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
