package integrado.prog2.entities;

import integrado.prog2.enums.Rol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Usuario del sistema (vive en memoria). Asociación 1:N unidireccional hacia Pedido:
 * el Usuario conoce sus Pedidos, pero el Pedido no conoce a su Usuario.
 */
public class Usuario extends Base {

    private String nombre;
    private String apellido;
    private String mail;
    private String celular;
    private String contrasenia;
    private Rol rol;
    private final List<Pedido> pedidos = new ArrayList<>();

    public Usuario(String nombre, String apellido, String mail,
                   String celular, String contrasenia, Rol rol) {
        super();
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.celular = celular;
        this.contrasenia = contrasenia;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }

    public void agregarPedido(Pedido pedido) {
        if (pedido != null) {
            pedidos.add(pedido);
        }
    }

    public void quitarPedido(Pedido pedido) {
        pedidos.remove(pedido);
    }

    /** No incluye la contraseña a propósito. */
    @Override
    public String toString() {
        return "Usuario{id=" + getId()
                + ", nombre='" + nombre + '\''
                + ", apellido='" + apellido + '\''
                + ", mail='" + mail + '\''
                + ", rol=" + rol + '}';
    }
}
