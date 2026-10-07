package integrado.prog2.ui;

import integrado.prog2.entities.DetallePedido;
import integrado.prog2.entities.Pedido;
import integrado.prog2.enums.FormaPago;
import integrado.prog2.service.PedidoService;

import java.util.List;

public class MenuPedidos extends Menu {

    private final PedidoService service;
    private final MenuUsuarios menuUsuarios;
    private final MenuProductos menuProductos;

    public MenuPedidos(Consola c, PedidoService service,
                       MenuUsuarios menuUsuarios, MenuProductos menuProductos) {
        super(c);
        this.service = service;
        this.menuUsuarios = menuUsuarios;
        this.menuProductos = menuProductos;
    }

    public void mostrar() {
        int op;
        do {
            c.titulo("PEDIDOS [MEMORIA]");
            c.info("1. Crear pedido");
            c.info("2. Listar pedidos");
            c.info("3. Listar pedidos de un usuario");
            c.info("4. Ver detalle de un pedido");
            c.info("5. Agregar producto a un pedido");
            c.info("6. Quitar producto de un pedido");
            c.info("7. Confirmar pedido (descuenta stock en [BASE DE DATOS])");
            c.info("8. Cancelar pedido (repone stock en [BASE DE DATOS])");
            c.info("9. Eliminar pedido (repone stock si estaba confirmado)");
            c.info("0. Volver");
            op = c.opcion("Opción: ", 0, 9);
            switch (op) {
                case 1 -> ejecutar(this::crear);
                case 2 -> ejecutar(this::listar);
                case 3 -> ejecutar(this::listarPorUsuario);
                case 4 -> ejecutar(this::ver);
                case 5 -> ejecutar(this::agregarProducto);
                case 6 -> ejecutar(this::quitarProducto);
                case 7 -> ejecutar(this::confirmar);
                case 8 -> ejecutar(this::cancelar);
                case 9 -> ejecutar(this::eliminar);
                default -> { }
            }
        } while (op != 0);
    }

    private void crear() {
        menuUsuarios.imprimirListado();
        Long idUsuario = c.id("ID del usuario: ");
        FormaPago forma = c.elegir("Forma de pago:", FormaPago.values());
        Pedido p = service.crear(idUsuario, forma);
        c.ok("Pedido creado con ID " + p.getId() + " (estado " + p.getEstado() + ").");
        if (c.siNo("¿Querés agregar productos ahora?")) {
            cargarProductos(p.getId());
        }
    }

    /** Agrega líneas hasta que el usuario decida terminar. Los errores no cortan el ciclo. */
    private void cargarProductos(Long idPedido) {
        do {
            ejecutar(() -> {
                menuProductos.imprimirListado();
                Long idProducto = c.id("ID del producto: ");
                int cantidad = c.entero("Cantidad: ");
                service.agregarProducto(idPedido, idProducto, cantidad);
                c.ok("Producto agregado al pedido.");
            });
        } while (c.siNo("¿Agregar otro producto?"));
        ejecutar(() -> imprimirPedido(service.buscarPorId(idPedido)));
    }

    private void listar() {
        imprimirResumen(service.listar());
    }

    private void listarPorUsuario() {
        menuUsuarios.imprimirListado();
        Long idUsuario = c.id("ID del usuario: ");
        imprimirResumen(service.listarPorUsuario(idUsuario));
    }

    private void ver() {
        imprimirPedido(service.buscarPorId(c.id("ID del pedido: ")));
    }

    private void agregarProducto() {
        Long idPedido = c.id("ID del pedido: ");
        cargarProductos(idPedido);
    }

    private void quitarProducto() {
        Long idPedido = c.id("ID del pedido: ");
        imprimirPedido(service.buscarPorId(idPedido));
        Long idProducto = c.id("ID del producto a quitar: ");
        service.quitarProducto(idPedido, idProducto);
        c.ok("Producto quitado del pedido.");
    }

    private void confirmar() {
        Long id = c.id("ID del pedido: ");
        if (c.siNo("Se descontará el stock de cada producto. ¿Confirmás el pedido " + id + "?")) {
            service.confirmar(id);
            c.ok("Pedido confirmado y stock descontado.");
        } else {
            c.info("Operación cancelada.");
        }
    }

    private void cancelar() {
        Long id = c.id("ID del pedido: ");
        if (c.siNo("¿Confirmás la cancelación del pedido " + id + "?")) {
            service.cancelar(id);
            c.ok("Pedido cancelado.");
        } else {
            c.info("Operación cancelada.");
        }
    }

    private void eliminar() {
        Long id = c.id("ID del pedido: ");
        if (c.siNo("¿Confirmás la baja del pedido " + id + "?")) {
            service.eliminar(id);
            c.ok("Pedido dado de baja.");
        } else {
            c.info("Operación cancelada.");
        }
    }

    private void imprimirResumen(List<Pedido> lista) {
        if (lista.isEmpty()) {
            c.info("No hay pedidos para mostrar.");
            return;
        }
        c.info(String.format("%-5s %-12s %-12s %-15s %12s", "ID", "FECHA", "ESTADO", "FORMA DE PAGO", "TOTAL"));
        for (Pedido p : lista) {
            c.info(String.format("%-5d %-12s %-12s %-15s %12.2f",
                    p.getId(), p.getFecha(), p.getEstado(), p.getFormaPago(), p.getTotal()));
        }
    }

    private void imprimirPedido(Pedido p) {
        c.info("Pedido #" + p.getId() + " | Fecha: " + p.getFecha()
                + " | Estado: " + p.getEstado() + " | Pago: " + p.getFormaPago());
        c.info(String.format("%-5s %-25s %8s %12s", "ID P.", "PRODUCTO", "CANT.", "SUBTOTAL"));
        boolean hayLineas = false;
        for (DetallePedido d : p.getDetalles()) {
            if (d.isEliminado()) {
                continue;
            }
            hayLineas = true;
            c.info(String.format("%-5d %-25s %8d %12.2f",
                    d.getProducto().getId(), d.getProducto().getNombre(),
                    d.getCantidad(), d.getSubtotal()));
        }
        if (!hayLineas) {
            c.info("(sin productos)");
        }
        c.info(String.format("TOTAL: $%.2f", p.getTotal()));
    }
}
