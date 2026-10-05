package integrado.prog2.entities;

/**
 * Línea de un Pedido. Asociación N:1 unidireccional hacia Producto
 * (el Producto no conoce los detalles en los que aparece).
 */
public class DetallePedido extends Base {

    private int cantidad;
    private Double subtotal;
    private final Producto producto;

    public DetallePedido(int cantidad, Double subtotal, Producto producto) {
        super();
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Producto getProducto() {
        return producto;
    }

    @Override
    public String toString() {
        return "DetallePedido{id=" + getId()
                + ", producto='" + (producto != null ? producto.getNombre() : "-") + '\''
                + ", cantidad=" + cantidad
                + ", subtotal=" + subtotal + '}';
    }
}
