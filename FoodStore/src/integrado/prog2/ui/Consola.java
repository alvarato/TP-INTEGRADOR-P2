package integrado.prog2.ui;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Entrada y salida por consola. Todas las lecturas validan el formato y
 * repiten la pregunta hasta recibir un valor correcto.
 */
public final class Consola {

    /** Se lanza si la entrada estándar se cierra (Ctrl+D / fin de archivo). */
    public static final class EntradaCerradaException extends RuntimeException {
        EntradaCerradaException() {
            super("Entrada cerrada");
        }
    }

    private final Scanner in = new Scanner(System.in);

    // ---------- salida ----------

    public void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    public void info(String texto) {
        System.out.println(texto);
    }

    public void ok(String texto) {
        System.out.println("[OK] " + texto);
    }

    public void error(String texto) {
        System.out.println("[ERROR] " + texto);
    }

    // ---------- entrada ----------

    public String texto(String prompt) {
        System.out.print(prompt);
        try {
            return in.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaCerradaException();
        }
    }

    /** Enter conserva el valor actual. */
    public String textoConDefecto(String prompt, String actual) {
        String s = texto(prompt + " [" + (actual == null ? "" : actual) + "]: ");
        return s.isEmpty() ? actual : s;
    }

    public int opcion(String prompt, int min, int max) {
        while (true) {
            String s = texto(prompt);
            try {
                int n = Integer.parseInt(s);
                if (n >= min && n <= max) {
                    return n;
                }
            } catch (NumberFormatException ignorada) {
                // se repite la pregunta
            }
            error("Ingresá un número entre " + min + " y " + max + ".");
        }
    }

    public int entero(String prompt) {
        while (true) {
            String s = texto(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                error("Ingresá un número entero válido.");
            }
        }
    }

    public int enteroConDefecto(String prompt, int actual) {
        while (true) {
            String s = texto(prompt + " [" + actual + "]: ");
            if (s.isEmpty()) {
                return actual;
            }
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                error("Ingresá un número entero válido.");
            }
        }
    }

    public Long id(String prompt) {
        while (true) {
            String s = texto(prompt);
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                error("Ingresá un ID numérico válido.");
            }
        }
    }

    public Double decimal(String prompt) {
        while (true) {
            String s = texto(prompt).replace(',', '.');
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                error("Ingresá un número válido (ej: 1500.50).");
            }
        }
    }

    public Double decimalConDefecto(String prompt, Double actual) {
        while (true) {
            String s = texto(prompt + " [" + actual + "]: ").replace(',', '.');
            if (s.isEmpty()) {
                return actual;
            }
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                error("Ingresá un número válido (ej: 1500.50).");
            }
        }
    }

    public boolean siNo(String prompt) {
        while (true) {
            String s = texto(prompt + " (s/n): ").toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sí")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
            error("Respondé 's' o 'n'.");
        }
    }

    public boolean siNoConDefecto(String prompt, boolean actual) {
        while (true) {
            String s = texto(prompt + " (s/n) [" + (actual ? "s" : "n") + "]: ").toLowerCase();
            if (s.isEmpty()) {
                return actual;
            }
            if (s.equals("s") || s.equals("si") || s.equals("sí")) {
                return true;
            }
            if (s.equals("n") || s.equals("no")) {
                return false;
            }
            error("Respondé 's' o 'n'.");
        }
    }

    /** Muestra los valores del enum numerados y devuelve el elegido. */
    public <E extends Enum<E>> E elegir(String prompt, E[] valores) {
        System.out.println(prompt);
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ". " + valores[i]);
        }
        int n = opcion("Opción: ", 1, valores.length);
        return valores[n - 1];
    }

    public void pausa() {
        texto("Presioná Enter para continuar...");
    }
}
