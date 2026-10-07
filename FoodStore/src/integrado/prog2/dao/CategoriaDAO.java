package integrado.prog2.dao;

import integrado.prog2.config.ConexionDB;
import integrado.prog2.entities.Categoria;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO implements GenericDAO<Categoria> {

    private static final String INSERT =
            "INSERT INTO categoria (nombre, descripcion, eliminado, created_at) VALUES (?, ?, FALSE, ?)";
    private static final String SELECT_BY_ID =
            "SELECT id, nombre, descripcion, eliminado, created_at FROM categoria WHERE id = ? AND eliminado = FALSE";
    private static final String SELECT_ACTIVAS =
            "SELECT id, nombre, descripcion, eliminado, created_at FROM categoria WHERE eliminado = FALSE ORDER BY nombre";
    private static final String UPDATE =
            "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id = ? AND eliminado = FALSE";
    private static final String SOFT_DELETE =
            "UPDATE categoria SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
    private static final String EXISTE_NOMBRE =
            "SELECT COUNT(*) FROM categoria WHERE LOWER(nombre) = LOWER(?) AND eliminado = FALSE";
    private static final String EXISTE_NOMBRE_EXCLUYENDO =
            EXISTE_NOMBRE + " AND id <> ?";

    /** Inserta la categoria y le asigna el id generado. */
    public void insert(Categoria categoria) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime creada = categoria.getCreatedAt() != null ? categoria.getCreatedAt() : LocalDateTime.now();
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setTimestamp(3, Timestamp.valueOf(creada));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    categoria.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la categoria.", e);
        }
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar la categoria.", e);
        }
    }

    @Override
    public List<Categoria> listarActivos() {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ACTIVAS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(mapear(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo listar las categorias.", e);
        }
    }

    @Override
    public void actualizar(Categoria categoria) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setLong(3, categoria.getId());
            if (ps.executeUpdate() == 0) {
                throw new EntidadNoEncontradaException("No existe la categoria con id " + categoria.getId() + ".");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar la categoria.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SOFT_DELETE)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                throw new EntidadNoEncontradaException("No existe la categoria con id " + id + ".");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo dar de baja la categoria.", e);
        }
    }

    /**
     * Unicidad del nombre entre categorias activas, validada con SQL (sin distinguir mayusculas).
     * @param excluirId id a ignorar al editar la propia categoria; null al crear.
     */
    public boolean existeNombreActivo(String nombre, Long excluirId) {
        String sql = excluirId == null ? EXISTE_NOMBRE : EXISTE_NOMBRE_EXCLUYENDO;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            if (excluirId != null) {
                ps.setLong(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo validar el nombre de la categoria.", e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria(rs.getString("nombre"), rs.getString("descripcion"));
        c.setId(rs.getLong("id"));
        c.setEliminado(rs.getBoolean("eliminado"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    }
}
