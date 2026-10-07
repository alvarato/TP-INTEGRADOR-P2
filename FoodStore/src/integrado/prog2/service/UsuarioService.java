package integrado.prog2.service;

import integrado.prog2.entities.Usuario;
import integrado.prog2.enums.Rol;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.MailDuplicadoException;
import integrado.prog2.exception.ValidacionException;
import integrado.prog2.repository.UsuarioRepository;

import java.util.List;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrar(String nombre, String apellido, String mail, String celular,
                             String contrasenia, Rol rol) {
        String n = Validaciones.texto(nombre, "nombre");
        String a = Validaciones.texto(apellido, "apellido");
        String m = Validaciones.texto(mail, "mail");
        String c = Validaciones.texto(celular, "celular");
        String p = Validaciones.texto(contrasenia, "contrasenia");
        Validaciones.mail(m);
        exigirRol(rol);
        validarMailLibre(m, null);
        return usuarioRepository.guardar(new Usuario(n, a, m, c, p, rol));
    }

    public List<Usuario> listar() {
        return usuarioRepository.listarActivos();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el usuario con id " + id + "."));
    }

    /** Si la contrasenia llega vacia se conserva la actual. */
    public Usuario modificar(Long id, String nombre, String apellido, String mail, String celular,
                             String contrasenia, Rol rol) {
        String n = Validaciones.texto(nombre, "nombre");
        String a = Validaciones.texto(apellido, "apellido");
        String m = Validaciones.texto(mail, "mail");
        String c = Validaciones.texto(celular, "celular");
        Validaciones.mail(m);
        exigirRol(rol);
        Usuario usuario = buscarPorId(id);
        validarMailLibre(m, id);
        usuario.setNombre(n);
        usuario.setApellido(a);
        usuario.setMail(m);
        usuario.setCelular(c);
        if (contrasenia != null && !contrasenia.isBlank()) {
            usuario.setContrasenia(contrasenia.trim());
        }
        usuario.setRol(rol);
        return usuarioRepository.guardar(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.eliminar(id);
    }

    private void validarMailLibre(String mail, Long excluirId) {
        if (usuarioRepository.existeMail(mail, excluirId)) {
            throw new MailDuplicadoException("Ya existe un usuario con el mail " + mail + ".");
        }
    }

    private void exigirRol(Rol rol) {
        if (rol == null) {
            throw new ValidacionException("Debe indicar un rol.");
        }
    }
}
