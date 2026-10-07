package integrado.prog2.service;

import integrado.prog2.dao.CategoriaDAO;
import integrado.prog2.dao.ProductoDAO;
import integrado.prog2.entities.Categoria;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.NombreDuplicadoException;
import integrado.prog2.exception.OperacionNoPermitidaException;

import java.util.List;

public class CategoriaService {

    private final CategoriaDAO categoriaDAO;
    private final ProductoDAO productoDAO;

    public CategoriaService(CategoriaDAO categoriaDAO, ProductoDAO productoDAO) {
        this.categoriaDAO = categoriaDAO;
        this.productoDAO = productoDAO;
    }

    public Categoria crear(String nombre, String descripcion) {
        String n = Validaciones.texto(nombre, "nombre");
        validarNombreLibre(n, null);
        Categoria categoria = new Categoria(n, descripcion == null ? "" : descripcion.trim());
        categoriaDAO.insert(categoria);
        return categoria;
    }

    public List<Categoria> listar() {
        List<Categoria> categorias = categoriaDAO.listarActivos();
        categorias.forEach(this::cargarProductos);
        return categorias;
    }

    public Categoria buscarPorId(Long id) {
        Categoria categoria = categoriaDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la categoria con id " + id + "."));
        cargarProductos(categoria);
        return categoria;
    }

    public Categoria modificar(Long id, String nombre, String descripcion) {
        String n = Validaciones.texto(nombre, "nombre");
        Categoria categoria = buscarPorId(id);
        validarNombreLibre(n, id);
        categoria.setNombre(n);
        categoria.setDescripcion(descripcion == null ? "" : descripcion.trim());
        categoriaDAO.actualizar(categoria);
        return categoria;
    }

    /** Baja logica. No se permite si la categoria tiene productos activos. */
    public void eliminar(Long id) {
        buscarPorId(id);
        int activos = productoDAO.contarActivosPorCategoria(id);
        if (activos > 0) {
            throw new OperacionNoPermitidaException(
                    "No se puede dar de baja la categoria: tiene " + activos + " producto(s) activo(s).");
        }
        categoriaDAO.eliminar(id);
    }

    private void validarNombreLibre(String nombre, Long excluirId) {
        if (categoriaDAO.existeNombreActivo(nombre, excluirId)) {
            throw new NombreDuplicadoException("Ya existe una categoria activa llamada '" + nombre + "'.");
        }
    }

    private void cargarProductos(Categoria categoria) {
        categoria.setProductos(productoDAO.listarPorCategoria(categoria.getId()));
    }
}
