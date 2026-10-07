package integrado.prog2.exception;

/** Ya existe un usuario con ese mail. */
public class MailDuplicadoException extends AppException {

    public MailDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
