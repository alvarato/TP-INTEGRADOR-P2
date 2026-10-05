package integrado.prog2.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Categoría de productos. Mantiene la agregación 1:N hacia Producto.
 * La relación es unidireccional: Categoría conoce sus Productos, pero Producto no conoce su Categoría.
 */
public class Categoria extends Base {

    private String nombre;
    private String descripcion;
    private final List<Producto> productos = new ArrayList<>();

    public Categoria(String nombre, String descripcion) {
        super();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public void agregarProducto(Producto producto) {
        if (producto != null) {
            productos.add(producto);
        }
    }

    public void setProductos(List<Producto> nuevos) {
        productos.clear();
        if (nuevos != null) {
            productos.addAll(nuevos);
        }
    }

    @Override
    public String toString() {
        return "Categoria{id=" + getId()
                + ", nombre='" + nombre + '\''
                + ", descripcion='" + descripcion + '\''
                + ", productos=" + productos.size() + '}';
    }
}
