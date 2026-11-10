import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/** Lee un mapa externo de UADE. Admite las secciones en cualquier orden. */
public class Archivos {

    // Lee el archivo, primero crea los lugares y despues carga las conexiones y restricciones.
    public static Grafo cargar(String ruta) throws IOException {
        Path path = Path.of(ruta).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new IOException("No existe el archivo: " + path +
                    "\nIndica la ruta como primer argumento o coloca Mapa.txt en la carpeta del proyecto.");
        }

        // Guardamos las lineas para procesarlas sin depender de su orden en el TXT.
        ArrayList<String[]> registros = new ArrayList<>();
        ArrayList<Integer> lineas = new ArrayList<>();
        int numeroLinea = 0;
        for (String linea : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            numeroLinea++;
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) continue;
            String[] campos = linea.split(";", -1);
            for (int i = 0; i < campos.length; i++) campos[i] = campos[i].trim();
            registros.add(campos);
            lineas.add(numeroLinea);
        }

        Grafo grafo = new Grafo();
        // Primera pasada: crear todos los lugares aunque las ARISTAS aparezcan antes.
        for (int i = 0; i < registros.size(); i++) {
            String[] d = registros.get(i);
            if (!d[0].equals("LUGAR")) continue;
            try {
                exigirCampos(d, 7);
                if (d[1].isEmpty() || grafo.buscarLugar(d[1]) != null)
                    throw new IllegalArgumentException("Nombre de lugar vacio o repetido: " + d[1]);
                grafo.agregarLugar(new Lugar(d[1], Integer.parseInt(d[2]),
                        Integer.parseInt(d[3]), Integer.parseInt(d[4]),
                        indicador(d[5]), indicador(d[6])));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Mapa, linea " + lineas.get(i) + ": " + e.getMessage(), e);
            }
        }
        // Segunda pasada: aristas dirigidas, ventanas e inicio.
        for (int i = 0; i < registros.size(); i++) {
            String[] d = registros.get(i);
            try {
                switch (d[0]) {
                    case "LUGAR": break;
                    case "ARISTA": {
                        exigirCampos(d, 5);
                        Lugar origen = buscarObligatorio(grafo, d[1]);
                        Lugar destino = buscarObligatorio(grafo, d[2]);
                        int segundos = Integer.parseInt(d[3]);
                        if (segundos <= 0) throw new IllegalArgumentException("El tiempo de arista debe ser positivo");
                        if (d[4].isEmpty()) throw new IllegalArgumentException("La arista no tiene movimientos");
                        for (char modo : d[4].toCharArray()) {
                            if ("CSP".indexOf(modo) < 0) throw new IllegalArgumentException("Modo no valido: " + modo);
                        }
                        grafo.agregarConexion(new Conexion(origen, destino, segundos, d[4]));
                        break;
                    }
                    case "VENTANA": {
                        exigirCampos(d, 4);
                        Lugar lugar = buscarObligatorio(grafo, d[1]);
                        int desde = Integer.parseInt(d[2]);
                        int hasta = Integer.parseInt(d[3]);
                        if (desde < 0 || hasta < desde) throw new IllegalArgumentException("Ventana de tiempo invalida");
                        grafo.agregarVentana(new Ventana(lugar, desde, hasta));
                        break;
                    }
                    case "INICIO": {
                        exigirCampos(d, 2);
                        if (grafo.getInicio() != null) throw new IllegalArgumentException("Hay mas de un INICIO");
                        grafo.setInicio(buscarObligatorio(grafo, d[1]));
                        break;
                    }
                    default: throw new IllegalArgumentException("Tipo de registro desconocido: " + d[0]);
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Mapa, linea " + lineas.get(i) + ": " + e.getMessage(), e);
            }
        }
        if (grafo.getInicio() == null) throw new IllegalArgumentException("El mapa no contiene INICIO");
        if (grafo.getInicio().esProhibido()) throw new IllegalArgumentException("INICIO no puede ser prohibido");
        for (Lugar lugar : grafo.getLugares().values()) {
            if (lugar.esProhibido() && lugar.esObligatorio())
                throw new IllegalArgumentException("Lugar obligatorio y prohibido a la vez: " + lugar.getNombre());
        }
        return grafo;
    }

    // Comprueba que cada registro tenga la cantidad de datos esperada.
    private static void exigirCampos(String[] datos, int cantidad) {
        if (datos.length != cantidad) throw new IllegalArgumentException(
                "Se esperaban " + cantidad + " campos y llegaron " + datos.length);
    }

    // Convierte los indicadores 0 y 1 del TXT a verdadero o falso.
    private static boolean indicador(String dato) {
        if (!dato.equals("0") && !dato.equals("1"))
            throw new IllegalArgumentException("El indicador debe ser 0 o 1: " + dato);
        return dato.equals("1");
    }

    // Busca un lugar por nombre y avisa si no existe en el mapa.
    private static Lugar buscarObligatorio(Grafo grafo, String nombre) {
        Lugar lugar = grafo.buscarLugar(nombre);
        if (lugar == null) throw new IllegalArgumentException("No existe el lugar: " + nombre);
        return lugar;
    }
}

