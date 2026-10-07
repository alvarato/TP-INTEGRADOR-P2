package integrado.prog2.repository;

import integrado.prog2.entities.Usuario;
import integrado.prog2.exception.EntidadNoEncontradaException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistencia en memoria de Usuario. Genera ids y hace baja logica (eliminado = true).
 * No valida reglas de negocio: la unicidad del mail la decide el service con existeMail.
 */
public class UsuarioRepository {

    private final Map<Long, Usuario> usuarios = new LinkedHashMap<>();
    private long siguienteId = 1;

    /** Si el usuario no tiene id, le asigna uno nuevo. Luego lo guarda (o lo reemplaza si ya existia). */
    public Usuario guardar(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(siguienteId++);
        }
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    public Optional<Usuario> buscarPorId(Long id) {
        Usuario u = usuarios.get(id);
        return (u != null && !u.isEliminado()) ? Optional.of(u) : Optional.empty();
    }

    public Optional<Usuario> buscarPorMail(String mail) {
        return usuarios.values().stream()
                .filter(u -> !u.isEliminado() && mismoMail(u.getMail(), mail))
                .findFirst();
    }

    public List<Usuario> listarActivos() {
        List<Usuario> activos = new ArrayList<>();
        for (Usuario u : usuarios.values()) {
            if (!u.isEliminado()) {
                activos.add(u);
            }
        }
        return activos;
    }

    /**
     * Baja logica. Lanza EntidadNoEncontradaException si no existe o ya estaba dado de baja.
     */
    public void eliminar(Long id) {
        Usuario u = usuarios.get(id);
        if (u == null || u.isEliminado()) {
            throw new EntidadNoEncontradaException("No existe el usuario con id " + id + ".");
        }
        u.eliminar();
    }

    /**
     * Indica si hay un usuario activo con ese mail (sin distinguir mayusculas).
     * @param excluirId id a ignorar al editar el propio usuario; null al crear.
     */
    public boolean existeMail(String mail, Long excluirId) {
        return usuarios.values().stream()
                .anyMatch(u -> !u.isEliminado()
                        && mismoMail(u.getMail(), mail)
                        && (excluirId == null || !excluirId.equals(u.getId())));
    }

    private boolean mismoMail(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }
}
