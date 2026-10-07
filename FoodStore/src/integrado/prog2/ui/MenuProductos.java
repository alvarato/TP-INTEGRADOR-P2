package integrado.prog2.ui;

import integrado.prog2.entities.Producto;
import integrado.prog2.service.CategoriaService;
import integrado.prog2.service.ProductoService;

import java.util.List;

public class MenuProductos extends Menu {

    private final ProductoService service;
    private final MenuCategorias menuCategorias;

    public MenuProductos(Consola c, ProductoService service, CategoriaService categoriaService) {
        super(c);
        this.service = service;
        this.menuCategorias = new MenuCategorias(c, categoriaService);
    }

    public void mostrar() {
        int op;
        do {
            c.titulo("PRODUCTOS [BASE DE DATOS]");
            c.info("1. Crear producto");
            c.info("2. Listar productos");
            c.info("3. Listar productos por categoría");
            c.info("4. Ver producto");
            c.info("5. Modificar producto");
            c.info("6. Reasignar categoría");
            c.info("7. Dar de baja producto");
            c.info("0. Volver");
            op = c.opcion("Opción: ", 0, 7);
            switch (op) {
                case 1 -> ejecutar(this::crear);
                case 2 -> ejecutar(this::listar);
                case 3 -> ejecutar(this::listarPorCategoria);
                case 4 -> ejecutar(this::ver);
                case 5 -> ejecutar(this::modificar);
                case 6 -> ejecutar(this::reasignar);
                case 7 -> ejecutar(this::eliminar);
                default -> { }
            }
        } while (op != 0);
    }

    /** Lista reutilizable por el menú de pedidos. */
    void imprimirListado(List<Producto> lista) {
        if (lista.isEmpty()) {
            c.info("No hay productos para mostrar.");
            return;
        }
        c.info(String.format("%-5s %-25s %10s %7s %-10s", "ID", "NOMBRE", "PRECIO", "STOCK", "DISPONIBLE"));
        for (Producto p : lista) {
            c.info(String.format("%-5d %-25s %10.2f %7d %-10s",
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock(),
                    Boolean.TRUE.equals(p.getDisponible()) ? "Sí" : "No"));
        }
    }

    void imprimirListado() {
        imprimirListado(service.listar());
    }

    private void crear() {
        String nombre = c.texto("Nombre: ");
        Double precio = c.decimal("Precio: ");
        String descripcion = c.texto("Descripción: ");
        int stock = c.entero("Stock: ");
        String imagen = c.texto("Imagen (URL o nombre de archivo): ");
        boolean disponible = c.siNo("¿Disponible?");
        c.info("Categorías disponibles:");
        menuCategorias.imprimirListado();
        Long idCategoria = c.id("ID de la categoría: ");
        Producto p = service.crear(nombre, precio, descripcion, stock, imagen, disponible, idCategoria);
        c.ok("Producto creado con ID " + p.getId() + ".");
    }

    private void listar() {
        imprimirListado();
    }

    private void listarPorCategoria() {
        menuCategorias.imprimirListado();
        Long idCategoria = c.id("ID de la categoría: ");
        imprimirListado(service.listarPorCategoria(idCategoria));
    }

    private void ver() {
        Producto p = service.buscarPorId(c.id("ID del producto: "));
        c.info("ID: " + p.getId());
        c.info("Nombre: " + p.getNombre());
        c.info(String.format("Precio: $%.2f", p.getPrecio()));
        c.info("Descripción: " + nvl(p.getDescripcion()));
        c.info("Stock: " + p.getStock());
        c.info("Imagen: " + nvl(p.getImagen()));
        c.info("Disponible: " + (Boolean.TRUE.equals(p.getDisponible()) ? "Sí" : "No"));
    }

    private void modificar() {
        Producto a = service.buscarPorId(c.id("ID del producto: "));
        String nombre = c.textoConDefecto("Nombre", a.getNombre());
        Double precio = c.decimalConDefecto("Precio", a.getPrecio());
        String descripcion = c.textoConDefecto("Descripción", a.getDescripcion());
        int stock = c.enteroConDefecto("Stock", a.getStock());
        String imagen = c.textoConDefecto("Imagen", a.getImagen());
        boolean disponible = c.siNoConDefecto("¿Disponible?", Boolean.TRUE.equals(a.getDisponible()));
        service.modificar(a.getId(), nombre, precio, descripcion, stock, imagen, disponible);
        c.ok("Producto modificado.");
    }

    private void reasignar() {
        Long idProducto = c.id("ID del producto: ");
        menuCategorias.imprimirListado();
        Long idCategoria = c.id("ID de la nueva categoría: ");
        service.reasignarCategoria(idProducto, idCategoria);
        c.ok("Categoría reasignada.");
    }

    private void eliminar() {
        Long id = c.id("ID del producto: ");
        if (c.siNo("¿Confirmás la baja del producto " + id + "?")) {
            service.eliminar(id);
            c.ok("Producto dado de baja.");
        } else {
            c.info("Operación cancelada.");
        }
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
