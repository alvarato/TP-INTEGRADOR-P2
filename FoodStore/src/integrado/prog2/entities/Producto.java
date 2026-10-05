package integrado.prog2.entities;

/**
 * Producto del catálogo.
 * NO tiene referencia a su Categoría (relación unidireccional): el id de la categoría
 * viaja como parámetro en el DAO y se guarda en la columna categoria_id.
 */
public class Producto extends Base {

    private String nombre;
    private Double precio;
    private String descripcion;
    private int stock;
    private String imagen;
    private Boolean disponible;

    public Producto(String nombre, Double precio, String descripcion,
                    int stock, String imagen, Boolean disponible) {
        super();
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.stock = stock;
        this.imagen = imagen;
        this.disponible = disponible;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Producto{id=" + getId()
                + ", nombre='" + nombre + '\''
                + ", precio=" + precio
                + ", stock=" + stock
                + ", disponible=" + disponible + '}';
    }
}
