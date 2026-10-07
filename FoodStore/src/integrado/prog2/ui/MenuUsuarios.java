package integrado.prog2.ui;

import integrado.prog2.entities.Usuario;
import integrado.prog2.enums.Rol;
import integrado.prog2.service.UsuarioService;

import java.util.List;

public class MenuUsuarios extends Menu {

    private final UsuarioService service;

    public MenuUsuarios(Consola c, UsuarioService service) {
        super(c);
        this.service = service;
    }

    public void mostrar() {
        int op;
        do {
            c.titulo("USUARIOS [MEMORIA]");
            c.info("1. Registrar usuario");
            c.info("2. Listar usuarios");
            c.info("3. Ver usuario");
            c.info("4. Modificar usuario");
            c.info("5. Dar de baja usuario");
            c.info("0. Volver");
            op = c.opcion("Opción: ", 0, 5);
            switch (op) {
                case 1 -> ejecutar(this::registrar);
                case 2 -> ejecutar(this::listar);
                case 3 -> ejecutar(this::ver);
                case 4 -> ejecutar(this::modificar);
                case 5 -> ejecutar(this::eliminar);
                default -> { }
            }
        } while (op != 0);
    }

    /** Lista reutilizable por el menú de pedidos. */
    void imprimirListado() {
        List<Usuario> lista = service.listar();
        if (lista.isEmpty()) {
            c.info("No hay usuarios registrados.");
            return;
        }
        c.info(String.format("%-5s %-15s %-15s %-28s %-10s", "ID", "NOMBRE", "APELLIDO", "MAIL", "ROL"));
        for (Usuario u : lista) {
            c.info(String.format("%-5d %-15s %-15s %-28s %-10s",
                    u.getId(), u.getNombre(), u.getApellido(), u.getMail(), u.getRol()));
        }
    }

    private void registrar() {
        String nombre = c.texto("Nombre: ");
        String apellido = c.texto("Apellido: ");
        String mail = c.texto("Mail: ");
        String celular = c.texto("Celular: ");
        String contrasenia = c.texto("Contraseña: ");
        Rol rol = c.elegir("Rol:", Rol.values());
        Usuario u = service.registrar(nombre, apellido, mail, celular, contrasenia, rol);
        c.ok("Usuario registrado con ID " + u.getId() + ".");
    }

    private void listar() {
        imprimirListado();
    }

    private void ver() {
        Usuario u = service.buscarPorId(c.id("ID del usuario: "));
        c.info("ID: " + u.getId());
        c.info("Nombre: " + u.getNombre() + " " + u.getApellido());
        c.info("Mail: " + u.getMail());
        c.info("Celular: " + u.getCelular());
        c.info("Rol: " + u.getRol());
    }

    private void modificar() {
        Usuario a = service.buscarPorId(c.id("ID del usuario: "));
        String nombre = c.textoConDefecto("Nombre", a.getNombre());
        String apellido = c.textoConDefecto("Apellido", a.getApellido());
        String mail = c.textoConDefecto("Mail", a.getMail());
        String celular = c.textoConDefecto("Celular", a.getCelular());
        String contrasenia = c.texto("Nueva contraseña (Enter para conservar la actual): ");
        Rol rol = a.getRol();
        if (c.siNo("¿Cambiar el rol? (actual: " + a.getRol() + ")")) {
            rol = c.elegir("Rol:", Rol.values());
        }
        service.modificar(a.getId(), nombre, apellido, mail, celular, contrasenia, rol);
        c.ok("Usuario modificado.");
    }

    private void eliminar() {
        Long id = c.id("ID del usuario: ");
        if (c.siNo("¿Confirmás la baja del usuario " + id + "?")) {
            service.eliminar(id);
            c.ok("Usuario dado de baja.");
        } else {
            c.info("Operación cancelada.");
        }
    }
}
