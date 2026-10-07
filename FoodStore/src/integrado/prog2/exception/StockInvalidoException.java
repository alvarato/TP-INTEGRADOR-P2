package integrado.prog2.exception;

/** Stock insuficiente o stock invalido (por ejemplo, negativo). */
public class StockInvalidoException extends AppException {

    public StockInvalidoException(String mensaje) {
        super(mensaje);
    }
}
