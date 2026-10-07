package integrado.prog2.service;

import integrado.prog2.dao.ProductoDAO;
import integrado.prog2.entities.DetallePedido;
import integrado.prog2.entities.Pedido;
import integrado.prog2.entities.Producto;
import integrado.prog2.entities.Usuario;
import integrado.prog2.enums.Estado;
import integrado.prog2.enums.FormaPago;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.OperacionNoPermitidaException;
import integrado.prog2.exception.StockInvalidoException;
import integrado.prog2.exception.ValidacionException;
import integrado.prog2.repository.PedidoRepository;
import integrado.prog2.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reglas de pedidos. El stock vive en la base (ProductoDAO) y los pedidos en memoria (repositorios).
 * Solo un pedido CONFIRMADO tiene stock descontado, por eso solo ese caso repone al cancelar o eliminar.
 */
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoDAO productoDAO;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                         ProductoDAO productoDAO) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoDAO = productoDAO;
    }

    public Pedido crear(Long idUsuario, FormaPago formaPago) {
        if (formaPago == null) {
            throw new ValidacionException("Debe indicar la forma de pago.");
        }
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el usuario con id " + idUsuario + "."));
        Pedido pedido = pedidoRepository.guardar(new Pedido(formaPago));
        usuario.agregarPedido(pedido);
        return pedido;
    }

    public List<Pedido> listar() {
        return pedidoRepository.listarActivos();
    }

    public List<Pedido> listarPorUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el usuario con id " + idUsuario + "."));
        List<Pedido> activos = new ArrayList<>();
        for (Pedido p : usuario.getPedidos()) {
            if (!p.isEliminado()) {
                activos.add(p);
            }
        }
        return activos;
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el pedido con id " + id + "."));
    }

    /**
     * Agrega una linea (o suma cantidad si el producto ya esta en el pedido). La verificacion de stock
     * aca es previa; la definitiva e atomica ocurre al confirmar.
     */
    public Pedido agregarProducto(Long idPedido, Long idProducto, int cantidad) {
        Pedido pedido = buscarPorId(idPedido);
        exigirPendiente(pedido);
        Validaciones.cantidad(cantidad);
        Producto producto = productoDAO.buscarPorId(idProducto)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el producto con id " + idProducto + "."));
        if (!Boolean.TRUE.equals(producto.getDisponible())) {
            throw new OperacionNoPermitidaException("El producto '" + producto.getNombre() + "' no esta disponible.");
        }
        DetallePedido existente = pedido.findeDetallePedidoByProducto(producto);
        int yaPedido = existente == null ? 0 : existente.getCantidad();
        if (yaPedido + cantidad > producto.getStock()) {
            throw new StockInvalidoException("Stock insuficiente para '" + producto.getNombre()
                    + "': disponible " + producto.getStock() + ", solicitado " + (yaPedido + cantidad) + ".");
        }
        pedido.addDetallePedido(cantidad, producto.getPrecio(), producto);
        return pedidoRepository.guardar(pedido);
    }

    public Pedido quitarProducto(Long idPedido, Long idProducto) {
        Pedido pedido = buscarPorId(idPedido);
        exigirPendiente(pedido);
        for (DetallePedido d : pedido.getDetalles()) {
            if (!d.isEliminado() && idProducto.equals(d.getProducto().getId())) {
                pedido.deleteDetallePedidoByProducto(d.getProducto());
                return pedido;
            }
        }
        throw new EntidadNoEncontradaException("El pedido no tiene una linea con el producto " + idProducto + ".");
    }

    /** Descuenta el stock de todas las lineas en una unica transaccion y pasa el pedido a CONFIRMADO. */
    public Pedido confirmar(Long idPedido) {
        Pedido pedido = buscarPorId(idPedido);
        exigirPendiente(pedido);
        Map<Long, Integer> cantidades = cantidadesPorProducto(pedido);
        if (cantidades.isEmpty()) {
            throw new OperacionNoPermitidaException("No se puede confirmar un pedido sin productos.");
        }
        productoDAO.descontarStock(cantidades);
        pedido.calcularTotal();
        pedido.setEstado(Estado.CONFIRMADO);
        return pedido;
    }

    /** Pasa el pedido a CANCELADO. Si estaba CONFIRMADO, repone el stock. */
    public Pedido cancelar(Long idPedido) {
        Pedido pedido = buscarPorId(idPedido);
        if (pedido.getEstado() == Estado.CANCELADO) {
            throw new OperacionNoPermitidaException("El pedido ya esta cancelado.");
        }
        reponerSiCorresponde(pedido);
        pedido.setEstado(Estado.CANCELADO);
        return pedido;
    }

    /** Baja logica del pedido y sus detalles. Si estaba CONFIRMADO, repone el stock. */
    public void eliminar(Long idPedido) {
        Pedido pedido = buscarPorId(idPedido);
        reponerSiCorresponde(pedido);
        pedidoRepository.eliminar(idPedido);
    }

    // ------------------------------------------------------------------ helpers

    private void reponerSiCorresponde(Pedido pedido) {
        if (pedido.getEstado() == Estado.CONFIRMADO) {
            productoDAO.reponerStock(cantidadesPorProducto(pedido));
        }
    }

    private Map<Long, Integer> cantidadesPorProducto(Pedido pedido) {
        Map<Long, Integer> cantidades = new HashMap<>();
        for (DetallePedido d : pedido.getDetalles()) {
            if (!d.isEliminado()) {
                cantidades.merge(d.getProducto().getId(), d.getCantidad(), Integer::sum);
            }
        }
        return cantidades;
    }

    private void exigirPendiente(Pedido pedido) {
        if (pedido.getEstado() != Estado.PENDIENTE) {
            throw new OperacionNoPermitidaException("Solo se pueden modificar pedidos en estado PENDIENTE.");
        }
    }
}
