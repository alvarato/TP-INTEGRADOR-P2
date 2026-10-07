package integrado.prog2.exception;

/** Dato de entrada invalido: precio o stock negativo, cantidad menor o igual a cero, campo vacio, formato incorrecto. */
public class ValidacionException extends AppException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
