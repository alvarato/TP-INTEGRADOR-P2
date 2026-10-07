package integrado.prog2.exception;

/** Ya existe una categoria activa con ese nombre. */
public class NombreDuplicadoException extends AppException {

    public NombreDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
