package integrado.prog2.ui;

import integrado.prog2.entities.Categoria;
import integrado.prog2.service.CategoriaService;

import java.util.List;

public class MenuCategorias extends Menu {

    private final CategoriaService service;

    public MenuCategorias(Consola c, CategoriaService service) {
        super(c);
        this.service = service;
    }

    public void mostrar() {
        int op;
        do {
            c.titulo("CATEGORÍAS [BASE DE DATOS]");
            c.info("1. Crear categoría");
            c.info("2. Listar categorías");
            c.info("3. Ver categoría con sus productos");
            c.info("4. Modificar categoría");
            c.info("5. Dar de baja categoría");
            c.info("0. Volver");
            op = c.opcion("Opción: ", 0, 5);
            switch (op) {
                case 1 -> ejecutar(this::crear);
                case 2 -> ejecutar(this::listar);
                case 3 -> ejecutar(this::ver);
                case 4 -> ejecutar(this::modificar);
                case 5 -> ejecutar(this::eliminar);
                default -> { }
            }
        } while (op != 0);
    }

    /** Lista reutilizable por otros menús (ej: al elegir categoría de un producto). */
    void imprimirListado() {
        List<Categoria> lista = service.listar();
        if (lista.isEmpty()) {
            c.info("No hay categorías cargadas.");
            return;
        }
        c.info(String.format("%-5s %-25s %-40s", "ID", "NOMBRE", "DESCRIPCIÓN"));
        for (Categoria cat : lista) {
            c.info(String.format("%-5d %-25s %-40s",
                    cat.getId(), cat.getNombre(), nvl(cat.getDescripcion())));
        }
    }

    private void crear() {
        String nombre = c.texto("Nombre: ");
        String descripcion = c.texto("Descripción: ");
        Categoria cat = service.crear(nombre, descripcion);
        c.ok("Categoría creada con ID " + cat.getId() + ".");
    }

    private void listar() {
        imprimirListado();
    }

    private void ver() {
        Categoria cat = service.buscarPorId(c.id("ID de la categoría: "));
        c.info("ID: " + cat.getId());
        c.info("Nombre: " + cat.getNombre());
        c.info("Descripción: " + nvl(cat.getDescripcion()));
        if (cat.getProductos().isEmpty()) {
            c.info("Sin productos activos.");
            return;
        }
        c.info("Productos:");
        cat.getProductos().forEach(p ->
                c.info(String.format("  - [%d] %s ($%.2f, stock %d)",
                        p.getId(), p.getNombre(), p.getPrecio(), p.getStock())));
    }

    private void modificar() {
        Categoria actual = service.buscarPorId(c.id("ID de la categoría: "));
        String nombre = c.textoConDefecto("Nombre", actual.getNombre());
        String descripcion = c.textoConDefecto("Descripción", actual.getDescripcion());
        service.modificar(actual.getId(), nombre, descripcion);
        c.ok("Categoría modificada.");
    }

    private void eliminar() {
        Long id = c.id("ID de la categoría: ");
        if (c.siNo("¿Confirmás la baja de la categoría " + id + "?")) {
            service.eliminar(id);
            c.ok("Categoría dada de baja.");
        } else {
            c.info("Operación cancelada.");
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
