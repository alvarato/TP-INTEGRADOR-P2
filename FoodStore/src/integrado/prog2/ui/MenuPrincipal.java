package integrado.prog2.ui;

import integrado.prog2.config.ConexionDB;
import integrado.prog2.service.CategoriaService;
import integrado.prog2.service.PedidoService;
import integrado.prog2.service.ProductoService;
import integrado.prog2.service.UsuarioService;

public class MenuPrincipal extends Menu {

    private final MenuCategorias menuCategorias;
    private final MenuProductos menuProductos;
    private final MenuUsuarios menuUsuarios;
    private final MenuPedidos menuPedidos;

    public MenuPrincipal(Consola c,
                         CategoriaService categoriaService,
                         ProductoService productoService,
                         UsuarioService usuarioService,
                         PedidoService pedidoService) {
        super(c);
        this.menuCategorias = new MenuCategorias(c, categoriaService);
        this.menuProductos = new MenuProductos(c, productoService, categoriaService);
        this.menuUsuarios = new MenuUsuarios(c, usuarioService);
        this.menuPedidos = new MenuPedidos(c, pedidoService, menuUsuarios, menuProductos);
    }

    public void iniciar() {
        try {
            int op;
            do {
                c.titulo("FOOD STORE - MENÚ PRINCIPAL");
                c.info("1. Gestión de categorías   [BASE DE DATOS]");
                c.info("2. Gestión de productos    [BASE DE DATOS]");
                c.info("3. Gestión de usuarios     [MEMORIA]");
                c.info("4. Gestión de pedidos      [MEMORIA]");
                c.info("5. Ver configuración de conexión");
                c.info("0. Salir");
                op = c.opcion("Opción: ", 0, 5);
                switch (op) {
                    case 1 -> menuCategorias.mostrar();
                    case 2 -> menuProductos.mostrar();
                    case 3 -> menuUsuarios.mostrar();
                    case 4 -> menuPedidos.mostrar();
                    case 5 -> ejecutar(this::verConfiguracion);
                    default -> { }
                }
            } while (op != 0);
            c.info("¡Hasta luego!");
        } catch (Consola.EntradaCerradaException e) {
            c.info("\nEntrada cerrada. Fin del programa.");
        }
    }

    private void verConfiguracion() {
        c.titulo("CONFIGURACIÓN DE CONEXIÓN");
        c.info(ConexionDB.getConfiguracionVisible());
    }
}
