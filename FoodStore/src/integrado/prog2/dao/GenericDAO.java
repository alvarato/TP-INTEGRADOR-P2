package integrado.prog2.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato comun de acceso a datos para las entidades persistidas en base de datos.
 * El insert NO esta aca: cada DAO lo define porque Producto necesita el id de categoria.
 * Todas las consultas devuelven solo registros activos (eliminado = FALSE).
 */
public interface GenericDAO<T> {

    Optional<T> buscarPorId(Long id);

    List<T> listarActivos();

    /** Actualiza los campos propios de la entidad. Lanza EntidadNoEncontradaException si no existe o esta dada de baja. */
    void actualizar(T entidad);

    /** Baja logica: UPDATE ... SET eliminado = TRUE. Nunca DELETE. */
    void eliminar(Long id);
}
