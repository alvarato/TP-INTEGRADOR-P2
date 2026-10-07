package integrado.prog2.exception;

/** El registro buscado no existe o esta dado de baja. */
public class EntidadNoEncontradaException extends AppException {

    public EntidadNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
