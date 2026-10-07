package integrado.prog2.repository;

import integrado.prog2.entities.DetallePedido;
import integrado.prog2.entities.Pedido;
import integrado.prog2.exception.EntidadNoEncontradaException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistencia en memoria de Pedido y sus DetallePedido (los detalles no tienen repositorio propio:
 * se guardan y se dan de baja a traves del pedido). Genera ids para ambos y hace baja logica.
 * La reposicion de stock NO se hace aca: eso es del service con ProductoDAO.
 */
public class PedidoRepository {

    private final Map<Long, Pedido> pedidos = new LinkedHashMap<>();
    private long siguienteIdPedido = 1;
    private long siguienteIdDetalle = 1;

    /** Asigna id al pedido y a los detalles que todavia no lo tengan, y lo guarda. */
    public Pedido guardar(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(siguienteIdPedido++);
        }
        for (DetallePedido d : pedido.getDetalles()) {
            if (d.getId() == null) {
                d.setId(siguienteIdDetalle++);
            }
        }
        pedidos.put(pedido.getId(), pedido);
        return pedido;
    }

    public Optional<Pedido> buscarPorId(Long id) {
        Pedido p = pedidos.get(id);
        return (p != null && !p.isEliminado()) ? Optional.of(p) : Optional.empty();
    }

    public List<Pedido> listarActivos() {
        List<Pedido> activos = new ArrayList<>();
        for (Pedido p : pedidos.values()) {
            if (!p.isEliminado()) {
                activos.add(p);
            }
        }
        return activos;
    }

    /**
     * Baja logica del pedido (en cascada sobre sus detalles, via Pedido.eliminar()).
     * Lanza EntidadNoEncontradaException si no existe o ya estaba dado de baja.
     */
    public void eliminar(Long id) {
        Pedido p = pedidos.get(id);
        if (p == null || p.isEliminado()) {
            throw new EntidadNoEncontradaException("No existe el pedido con id " + id + ".");
        }
        p.eliminar();
    }
}
