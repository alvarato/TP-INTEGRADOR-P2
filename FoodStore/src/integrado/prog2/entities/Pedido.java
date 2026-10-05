package integrado.prog2.entities;

import integrado.prog2.enums.Estado;
import integrado.prog2.enums.FormaPago;
import integrado.prog2.interfaces.Calculable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Pedido. Composición 1:N con DetallePedido.
 * NO tiene referencia al Usuario (relación unidireccional: Usuario conoce sus Pedidos).
 */
public class Pedido extends Base implements Calculable {

    private LocalDate fecha;
    private Estado estado;
    private Double total;
    private FormaPago formaPago;
    private final List<DetallePedido> detalles = new ArrayList<>();

    public Pedido(FormaPago formaPago) {
        super();
        this.fecha = LocalDate.now();
        this.estado = Estado.PENDIENTE;
        this.total = 0.0;
        this.formaPago = formaPago;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Double getTotal() {
        return total;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    /**
     * Agrega una línea al pedido. Si el producto ya tiene una línea activa, suma la cantidad.
     * El subtotal de la línea se calcula como cantidad * precioUnitario y luego se recalcula el total.
     */
    public void addDetallePedido(int cantidad, Double precioUnitario, Producto producto) {
        Objects.requireNonNull(producto, "El producto no puede ser nulo");
        Objects.requireNonNull(precioUnitario, "El precio unitario no puede ser nulo");

        DetallePedido existente = findeDetallePedidoByProducto(producto);
        if (existente != null) {
            int nuevaCantidad = existente.getCantidad() + cantidad;
            existente.setCantidad(nuevaCantidad);
            existente.setSubtotal(nuevaCantidad * precioUnitario);
        } else {
            detalles.add(new DetallePedido(cantidad, cantidad * precioUnitario, producto));
        }
        calcularTotal();
    }

    /** Devuelve la línea activa (no eliminada) del producto, o null si no existe. */
    public DetallePedido findeDetallePedidoByProducto(Producto producto) {
        for (DetallePedido d : detalles) {
            if (!d.isEliminado() && d.getProducto().equals(producto)) {
                return d;
            }
        }
        return null;
    }

    /** Baja lógica de la línea correspondiente al producto y recálculo del total. */
    public void deleteDetallePedidoByProducto(Producto producto) {
        DetallePedido d = findeDetallePedidoByProducto(producto);
        if (d != null) {
            d.eliminar();
            calcularTotal();
        }
    }

    /** Suma los subtotales de las líneas activas y lo guarda en total. */
    @Override
    public void calcularTotal() {
        double suma = 0.0;
        for (DetallePedido d : detalles) {
            if (!d.isEliminado()) {
                suma += d.getSubtotal();
            }
        }
        this.total = suma;
    }

    /** Baja lógica del pedido y, por composición, de todos sus detalles. */
    @Override
    public void eliminar() {
        super.eliminar();
        for (DetallePedido d : detalles) {
            d.eliminar();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pedido{id=").append(getId())
          .append(", fecha=").append(fecha)
          .append(", estado=").append(estado)
          .append(", formaPago=").append(formaPago)
          .append(", total=").append(total).append('}');
        for (DetallePedido d : detalles) {
            if (!d.isEliminado()) {
                sb.append("\n    - ").append(d);
            }
        }
        return sb.toString();
    }
}
