package integrado.prog2.config;

import integrado.prog2.exception.PersistenciaException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Punto unico de acceso a la conexion JDBC.
 * Lee driver, url, usuario y password de src/META-INF/persistence.xml con el parser DOM del JDK.
 * No contiene SQL: el diagnostico usa DatabaseMetaData.
 */
public final class ConexionDB {

    private static final String RUTA_ARCHIVO = "src/META-INF/persistence.xml";
    private static final String RECURSO = "META-INF/persistence.xml";
    private static final String[] TABLAS_REQUERIDAS = {"categoria", "producto"};

    private record Configuracion(String driver, String url, String usuario, String password) { }

    /** Resultado del diagnostico de arranque. El mensaje es apto para mostrar al usuario. */
    public record Diagnostico(boolean ok, String mensaje) { }

    private static Configuracion configuracion;

    private ConexionDB() { }

    /** Abre una conexion nueva. Quien la use debe cerrarla (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        Configuracion c = obtenerConfiguracion();
        return DriverManager.getConnection(c.url(), c.usuario(), c.password());
    }

    /**
     * Diagnostico de conexion para ejecutar al arrancar: configuracion, driver, conexion y existencia
     * de las tablas categoria y producto. Nunca lanza excepciones ni expone detalles tecnicos.
     */
    public static Diagnostico diagnosticar() {
        try {
            obtenerConfiguracion();
        } catch (PersistenciaException e) {
            return new Diagnostico(false, e.getMessage());
        }
        try (Connection con = getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            String motor = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
            StringBuilder faltantes = new StringBuilder();
            for (String tabla : TABLAS_REQUERIDAS) {
                if (!existeTabla(meta, tabla)) {
                    faltantes.append(faltantes.length() == 0 ? "" : ", ").append(tabla);
                }
            }
            if (faltantes.length() > 0) {
                return new Diagnostico(false, "Conexion exitosa con " + motor
                        + ", pero faltan tablas: " + faltantes + ". Ejecute el script de la carpeta db/.");
            }
            return new Diagnostico(true, "Conexion exitosa con " + motor + ". Tablas verificadas.");
        } catch (SQLException e) {
            return new Diagnostico(false, "No se pudo conectar a la base de datos. "
                    + "Verifique que el motor este en ejecucion y los datos de src/META-INF/persistence.xml.");
        }
    }

    /** Texto para la opcion "Ver configuracion de conexion". La contrasenia se enmascara. */
    public static String getConfiguracionVisible() {
        Configuracion c = obtenerConfiguracion();
        return "Driver  : " + c.driver() + System.lineSeparator()
                + "URL     : " + c.url() + System.lineSeparator()
                + "Usuario : " + (c.usuario().isEmpty() ? "(vacio)" : c.usuario()) + System.lineSeparator()
                + "Password: " + (c.password().isEmpty() ? "(vacia)" : "********");
    }

    // ------------------------------------------------------------------ carga de configuracion

    private static synchronized Configuracion obtenerConfiguracion() {
        if (configuracion == null) {
            configuracion = cargar();
        }
        return configuracion;
    }

    private static Configuracion cargar() {
        Map<String, String> props = leerPropiedades();
        String driver = props.get("driver");
        String url = props.get("url");
        if (driver == null || driver.isBlank() || url == null || url.isBlank()) {
            throw new PersistenciaException(
                    "El archivo persistence.xml debe definir las propiedades de driver y url de conexion.");
        }
        try {
            Class.forName(driver.trim());
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException(
                    "No se encontro el driver JDBC " + driver.trim() + ". Verifique que el .jar este en lib/.", e);
        }
        return new Configuracion(driver.trim(), url.trim(),
                props.getOrDefault("user", "").trim(), props.getOrDefault("password", ""));
    }

    /**
     * Parsea persistence.xml con DOM. Cada <property name="...jdbc.url" value="..."/> se guarda
     * usando como clave lo que sigue al ultimo punto (url, driver, user, password), asi funcionan
     * tanto los nombres jakarta.persistence.jdbc.* como javax.persistence.jdbc.*.
     */
    private static Map<String, String> leerPropiedades() {
        try (InputStream in = abrirArchivo()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document doc = factory.newDocumentBuilder().parse(in);

            Map<String, String> props = new HashMap<>();
            NodeList nodos = doc.getElementsByTagName("property");
            for (int i = 0; i < nodos.getLength(); i++) {
                Element p = (Element) nodos.item(i);
                String nombre = p.getAttribute("name");
                if (!nombre.isEmpty()) {
                    props.put(nombre.substring(nombre.lastIndexOf('.') + 1), p.getAttribute("value"));
                }
            }
            return props;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo persistence.xml. Verifique su formato.", e);
        }
    }

    private static InputStream abrirArchivo() throws IOException {
        File archivo = new File(RUTA_ARCHIVO);
        if (archivo.isFile()) {
            return new FileInputStream(archivo);
        }
        InputStream recurso = ConexionDB.class.getClassLoader().getResourceAsStream(RECURSO);
        if (recurso == null) {
            throw new PersistenciaException("No se encontro " + RUTA_ARCHIVO
                    + ". Ejecute la aplicacion desde la carpeta raiz del proyecto.");
        }
        return recurso;
    }

    private static boolean existeTabla(DatabaseMetaData meta, String tabla) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, "%", new String[] {"TABLE"})) {
            while (rs.next()) {
                if (tabla.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
