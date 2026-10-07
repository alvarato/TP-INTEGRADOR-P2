package integrado.prog2.service;

import integrado.prog2.dao.CategoriaDAO;
import integrado.prog2.dao.ProductoDAO;
import integrado.prog2.entities.Producto;
import integrado.prog2.exception.EntidadNoEncontradaException;

import java.util.List;

public class ProductoService {

    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;

    public ProductoService(ProductoDAO productoDAO, CategoriaDAO categoriaDAO) {
        this.productoDAO = productoDAO;
        this.categoriaDAO = categoriaDAO;
    }

    public Producto crear(String nombre, Double precio, String descripcion, int stock,
                          String imagen, Boolean disponible, Long idCategoria) {
        String n = Validaciones.texto(nombre, "nombre");
        Validaciones.precio(precio);
        Validaciones.stock(stock);
        exigirCategoria(idCategoria);
        Producto producto = new Producto(n, precio, descripcion == null ? "" : descripcion.trim(),
                stock, imagen, disponible);
        productoDAO.insert(producto, idCategoria);
        return producto;
    }

    public List<Producto> listar() {
        return productoDAO.listarActivos();
    }

    public List<Producto> listarPorCategoria(Long idCategoria) {
        exigirCategoria(idCategoria);
        return productoDAO.listarPorCategoria(idCategoria);
    }

    public Producto buscarPorId(Long id) {
        return productoDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el producto con id " + id + "."));
    }

    public Producto modificar(Long id, String nombre, Double precio, String descripcion, int stock,
                              String imagen, Boolean disponible) {
        String n = Validaciones.texto(nombre, "nombre");
        Validaciones.precio(precio);
        Validaciones.stock(stock);
        Producto producto = buscarPorId(id);
        producto.setNombre(n);
        producto.setPrecio(precio);
        producto.setDescripcion(descripcion == null ? "" : descripcion.trim());
        producto.setStock(stock);
        producto.setImagen(imagen);
        producto.setDisponible(disponible);
        productoDAO.actualizar(producto);
        return producto;
    }

    public void reasignarCategoria(Long idProducto, Long idCategoria) {
        buscarPorId(idProducto);
        exigirCategoria(idCategoria);
        productoDAO.reasignarCategoria(idProducto, idCategoria);
    }

    public void eliminar(Long id) {
        buscarPorId(id);
        productoDAO.eliminar(id);
    }

    private void exigirCategoria(Long idCategoria) {
        if (idCategoria == null || categoriaDAO.buscarPorId(idCategoria).isEmpty()) {
            throw new EntidadNoEncontradaException("No existe la categoria con id " + idCategoria + ".");
        }
    }
}
