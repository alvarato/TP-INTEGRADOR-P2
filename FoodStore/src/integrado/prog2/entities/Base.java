package integrado.prog2.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clase abstracta de la que heredan todas las entidades del dominio.
 * Concentra los atributos comunes: id, baja lógica y fecha de creación.
 */
public abstract class Base {

    private Long id;
    private boolean eliminado;
    private LocalDateTime createdAt;

    protected Base() {
        this.eliminado = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Baja lógica: marca la entidad como eliminada sin quitarla de ningún almacenamiento. */
    public void eliminar() {
        this.eliminado = true;
    }

    /** Dos entidades son iguales si son de la misma clase y tienen el mismo id (no nulo). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base other = (Base) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : Objects.hash(getClass().getName(), id);
    }
}
