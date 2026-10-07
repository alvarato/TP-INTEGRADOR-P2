package integrado.prog2.service;

import integrado.prog2.exception.StockInvalidoException;
import integrado.prog2.exception.ValidacionException;

import java.util.regex.Pattern;

/** Validaciones de entrada compartidas por los services. Todas lanzan excepciones propias. */
final class Validaciones {

    private static final Pattern MAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validaciones() { }

    /** Exige texto no vacio y lo devuelve sin espacios en los extremos. */
    static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo '" + campo + "' no puede estar vacio.");
        }
        return valor.trim();
    }

    static void precio(Double precio) {
        if (precio == null || precio.isNaN()) {
            throw new ValidacionException("El precio es obligatorio.");
        }
        if (precio < 0) {
            throw new ValidacionException("El precio no puede ser negativo.");
        }
    }

    static void stock(int stock) {
        if (stock < 0) {
            throw new StockInvalidoException("El stock no puede ser negativo.");
        }
    }

    static void cantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor a cero.");
        }
    }

    static void mail(String mail) {
        if (!MAIL.matcher(mail).matches()) {
            throw new ValidacionException("El mail ingresado no tiene un formato valido.");
        }
    }
}
