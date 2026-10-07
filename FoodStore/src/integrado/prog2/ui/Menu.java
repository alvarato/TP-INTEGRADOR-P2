package integrado.prog2.ui;

import integrado.prog2.exception.AppException;

/**
 * Base de los submenús. Centraliza el manejo de errores: la UI nunca muestra
 * stack traces, solo el mensaje de las excepciones propias.
 */
abstract class Menu {

    protected final Consola c;

    protected Menu(Consola c) {
        this.c = c;
    }

    protected void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (Consola.EntradaCerradaException e) {
            throw e;
        } catch (AppException e) {
            c.error(e.getMessage());
        } catch (RuntimeException e) {
            c.error("Ocurrió un error inesperado. Intentá nuevamente.");
        }
    }
}
