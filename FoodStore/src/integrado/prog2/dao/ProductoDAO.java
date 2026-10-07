package integrado.prog2.dao;

import integrado.prog2.config.ConexionDB;
import integrado.prog2.entities.Producto;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.PersistenciaException;
import integrado.prog2.exception.StockInvalidoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Acceso a la tabla producto. categoria_id NO se mapea en la entidad:
 * viaja como parametro en insert y reasignarCategoria.
 */
public class ProductoDAO implements GenericDAO<Producto> {

    private static final String COLUMNAS =
            "id, nombre, precio, descripcion, stock, imagen, disponible, eliminado, created_at";

    private static final String INSERT =
            "INSERT INTO producto (nombre, precio, descripcion, stock, imagen, disponible, eliminado, created_at, categoria_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, FALSE, ?, ?)";
    private static final String SELECT_BY_ID =
            "SELECT " + COLUMNAS + " FROM producto WHERE id = ? AND eliminado = FALSE";
    private static final String SELECT_ACTIVOS =
            "SELECT " + COLUMNAS + " FROM producto WHERE eliminado = FALSE ORDER BY nombre";
    private static final String SELECT_POR_CATEGORIA =
            "SELECT " + COLUMNAS + " FROM producto WHERE categoria_id = ? AND eliminado = FALSE ORDER BY nombre";
    private static final String UPDATE =
            "UPDATE producto SET nombre = ?, precio = ?, descripcion = ?, stock = ?, imagen = ?, disponible = ? "
                    + "WHERE id = ? AND eliminado = FALSE";
    private static final String REASIGNAR =
            "UPDATE producto SET categoria_id = ? WHERE id = ? AND eliminado = FALSE";
    private static final String SOFT_DELETE =
            "UPDATE producto SET eliminado = TRUE WHERE id = ? AND eliminado = FALSE";
    private static final String CONTAR_ACTIVOS_CATEGORIA =
            "SELECT COUNT(*) FROM producto WHERE categoria_id = ? AND eliminado = FALSE";

    // Descuento condicional: solo actualiza si alcanza el stock (evita condiciones de carrera).
    private static final String DESCONTAR =
            "UPDATE producto SET stock = stock - ? WHERE id = ? AND eliminado = FALSE AND stock >= ?";
    // La reposicion no filtra por eliminado: si el producto se dio de baja despues de la venta, el stock igual vuelve.
    private static final String REPONER =
            "UPDATE producto SET stock = stock + ? WHERE id = ?";
    private static final String STOCK_ACTUAL =
            "SELECT stock FROM producto WHERE id = ? AND eliminado = FALSE";

    /** Inserta el producto asociado a una categoria y le asigna el id generado. */
    public void insert(Producto producto, Long idCategoria) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime creado = producto.getCreatedAt() != null ? producto.getCreatedAt() : LocalDateTime.now();
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setBoolean(6, producto.getDisponible());
            ps.setTimestamp(7, Timestamp.valueOf(creado));
            ps.setLong(8, idCategoria);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    producto.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar el producto.", e);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar el producto.", e);
        }
    }

    @Override
    public List<Producto> listarActivos() {
        return listar(SELECT_ACTIVOS, null, "No se pudo listar los productos.");
    }

    /** Productos activos de una categoria (para poblar Categoria.productos desde el service). */
    public List<Producto> listarPorCategoria(Long idCategoria) {
        return listar(SELECT_POR_CATEGORIA, idCategoria, "No se pudo listar los productos de la categoria.");
    }

    /** Actualiza los datos del producto. No toca la categoria: para eso esta reasignarCategoria. */
    @Override
    public void actualizar(Producto producto) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setBoolean(6, producto.getDisponible());
            ps.setLong(7, producto.getId());
            if (ps.executeUpdate() == 0) {
                throw new EntidadNoEncontradaException("No existe el producto con id " + producto.getId() + ".");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el producto.", e);
        }
    }

    public void reasignarCategoria(Long idProducto, Long idCategoria) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(REASIGNAR)) {
            ps.setLong(1, idCategoria);
            ps.setLong(2, idProducto);
            if (ps.executeUpdate() == 0) {
                throw new EntidadNoEncontradaException("No existe el producto con id " + idProducto + ".");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo reasignar la categoria del producto.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SOFT_DELETE)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                throw new EntidadNoEncontradaException("No existe el producto con id " + id + ".");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo dar de baja el producto.", e);
        }
    }

    /** Regla "no baja de categoria con productos activos": el service decide con este conteo. */
    public int contarActivosPorCategoria(Long idCategoria) {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(CONTAR_ACTIVOS_CATEGORIA)) {
            ps.setLong(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo contar los productos de la categoria.", e);
        }
    }

    // ---------------------------------------------------------------- stock transaccional

    /**
     * Descuenta el stock de todos los productos en UNA transaccion: o se descuentan todos o ninguno.
     * @param cantidadesPorProducto idProducto -> cantidad a descontar (el service ya valido cantidad > 0).
     * @throws StockInvalidoException si algun producto no tiene stock suficiente (se hace rollback).
     * @throws EntidadNoEncontradaException si algun producto no existe o esta dado de baja.
     */
    public void descontarStock(Map<Long, Integer> cantidadesPorProducto) {
        ajustarStock(cantidadesPorProducto, true);
    }

    /** Repone el stock (cancelar o eliminar pedido) en una unica transaccion. */
    public void reponerStock(Map<Long, Integer> cantidadesPorProducto) {
        ajustarStock(cantidadesPorProducto, false);
    }

    private void ajustarStock(Map<Long, Integer> cantidades, boolean descontar) {
        if (cantidades == null || cantidades.isEmpty()) {
            return;
        }
        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);
            // TreeMap: orden fijo por id, evita deadlocks entre pedidos concurrentes.
            for (Map.Entry<Long, Integer> e : new TreeMap<>(cantidades).entrySet()) {
                aplicarAjuste(con, e.getKey(), e.getValue(), descontar);
            }
            con.commit();
        } catch (StockInvalidoException | EntidadNoEncontradaException e) {
            rollbackSilencioso(con);
            throw e;
        } catch (SQLException e) {
            rollbackSilencioso(con);
            throw new PersistenciaException(
                    descontar ? "No se pudo descontar el stock." : "No se pudo reponer el stock.", e);
        } finally {
            cerrarSilencioso(con);
        }
    }

    private void aplicarAjuste(Connection con, Long idProducto, int cantidad, boolean descontar) throws SQLException {
        int filas;
        try (PreparedStatement ps = con.prepareStatement(descontar ? DESCONTAR : REPONER)) {
            ps.setInt(1, cantidad);
            ps.setLong(2, idProducto);
            if (descontar) {
                ps.setInt(3, cantidad);
            }
            filas = ps.executeUpdate();
        }
        if (filas > 0) {
            return;
        }
        if (!descontar) {
            throw new EntidadNoEncontradaException("No existe el producto con id " + idProducto + ".");
        }
        // 0 filas al descontar: o no existe, o no alcanza el stock. Se distingue para dar un mensaje claro.
        try (PreparedStatement ps = con.prepareStatement(STOCK_ACTUAL)) {
            ps.setLong(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new EntidadNoEncontradaException("No existe el producto con id " + idProducto + ".");
                }
                throw new StockInvalidoException("Stock insuficiente para el producto con id " + idProducto
                        + ": disponible " + rs.getInt(1) + ", solicitado " + cantidad + ".");
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    private List<Producto> listar(String sql, Long parametro, String mensajeError) {
        List<Producto> productos = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setLong(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
            }
            return productos;
        } catch (SQLException e) {
            throw new PersistenciaException(mensajeError, e);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto(
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getString("descripcion"),
                rs.getInt("stock"),
                rs.getString("imagen"),
                rs.getBoolean("disponible"));
        p.setId(rs.getLong("id"));
        p.setEliminado(rs.getBoolean("eliminado"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }

    private void rollbackSilencioso(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignorada) {
                // el error original es el que importa; no se expone nada al usuario
            }
        }
    }

    private void cerrarSilencioso(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignorada) {
                // nada que hacer
            }
        }
    }
}
