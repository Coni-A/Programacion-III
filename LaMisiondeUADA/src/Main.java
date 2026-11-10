
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Punto de entrada. Se ejecuta desde IntelliJ o desde consola.
 *
 * Ejemplo: java -cp out Prueba_Main Mapa.txt
 *          java -cp out Prueba_Main Mapa.txt --sin-log
 */
public class Main {

    // Lee el mapa, ejecuta Backtracking y muestra el resultado en consola.
    public static void main(String[] args) {
        try {
            if (args.length > 0 && (args[0].equals("--ayuda") || args[0].equals("--help"))) {
                System.out.println("Uso: java -cp out Prueba_Main [ruta_mapa.txt] [--sin-log]");
                return;
            }
            Path mapa = obtenerRutaMapa(args);
            boolean crearLog = true;
            for (String arg : args) if (arg.equals("--sin-log")) crearLog = false;

            // El mapa viene del TXT externo, no esta escrito dentro de las clases.
            Grafo grafo = Archivos.cargar(mapa.toString());
            long obligatorios = grafo.getLugares().values().stream().filter(Lugar::esObligatorio).count();
            long prohibidos = grafo.getLugares().values().stream().filter(Lugar::esProhibido).count();
            System.out.println("LA MISION DE UADA");
            System.out.println("Mapa externo: " + mapa.toAbsolutePath());
            System.out.printf("Estaciones: %d | Conexiones: %d | Obligatorias: %d | Prohibidas: %d | Ventanas: %d%n",
                    grafo.getLugares().size(), grafo.getConexiones().size(), obligatorios,
                    prohibidos, grafo.getVentanas().size());
            System.out.println("Inicio: " + grafo.getInicio().getNombre());

            Path rutaLog = Path.of("log_ejecucion.txt").toAbsolutePath().normalize();
            try (RegistroLog registro = new RegistroLog(crearLog ? rutaLog : null)) {
                MisionUADA mision = new MisionUADA(grafo, registro);
                mision.resolver();
                ArrayList<Estado> resultados = mision.getMejoresRutas();
                if (resultados.isEmpty()) {
                    System.out.println("\nNO HAY RECORRIDOS COMPLETOS VALIDOS.");
                    Estado parcial = mision.getMejorParcial();
                    if (parcial == null) parcial = new Estado(grafo.getInicio(), 0, 100);
                    System.out.println("Se devuelve el mejor recorrido parcial:");
                    mostrarRuta(parcial, 0);
                } else {
                    System.out.printf("%nSE ENCONTRARON %d MEJORES RECORRIDOS DE ESTACIONES DISTINTAS:%n", resultados.size());
                    for (int i = 0; i < resultados.size(); i++) mostrarRuta(resultados.get(i), i + 1);
                }
                System.out.printf(Locale.US,
                        "%nRESUMEN DE BACKTRACKING%nNodos analizados: %d%nPodas por cota: %d%n" +
                                "Podas por imposibilidad: %d%nMovimientos invalidos: %d%nRetrocesos: %d%n" +
                                "Tiempo de ejecucion: %.3f ms%n",
                        mision.getNodosAnalizados(), mision.getPodasCota(),
                        mision.getPodasImposibilidad(), mision.getMovimientosInvalidos(),
                        mision.getRetrocesos(), mision.getDuracionMs());
                if (crearLog) System.out.println("Log del arbol de exploracion: " + rutaLog.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println("Ejemplo: java -cp out Prueba_Main LaMisiondeUADA/Mapa.txt");
            System.exit(1);
        }
    }

    // Acepta una ruta pasada como argumento o busca el mapa en carpetas habituales.
    private static Path obtenerRutaMapa(String[] args) {
        if (args.length > 0 && !args[0].startsWith("--")) return Path.of(args[0]).toAbsolutePath().normalize();
        // Alternativas para ejecutar desde la carpeta LaMisiondeUADA o desde la raiz del ZIP.
        String[] ubicaciones = {"Mapa.txt", "LaMisiondeUADA/Mapa.txt",
                "Programacion-III-main/LaMisiondeUADA/Mapa.txt", "../Mapa.txt"};
        for (String ubicacion : ubicaciones) {
            Path ruta = Path.of(ubicacion).toAbsolutePath().normalize();
            if (Files.isRegularFile(ruta)) return ruta;
        }
        throw new IllegalArgumentException("No se encontro Mapa.txt. Pasalo como argumento de ejecucion.");
    }

    // Imprime el camino y los datos de cada movimiento para verificar la solucion.
    private static void mostrarRuta(Estado estado, int numero) {
        System.out.println("\n" + (numero == 0 ? "RECORRIDO PARCIAL" : "RUTA " + numero));
        System.out.printf(Locale.US, "Tiempo total: %.2f s | Bateria final: %.4f | Movimientos: %d%n",
                estado.getTiempo(), estado.getBateria(), estado.getCamino().size());
        System.out.print("Camino: " + (estado.getCamino().isEmpty() ? estado.getActual().getNombre() : estado.getCamino().get(0).getOrigen().getNombre()));
        for (Paso paso : estado.getCamino()) System.out.print(" -> " + paso.getDestino().getNombre());
        System.out.println();
        for (Paso paso : estado.getCamino()) {
            System.out.printf(Locale.US,
                    "  %-20s -> %-20s | %s | tramo=%6.2f s | consumo=%6.4f | acumulado=%7.2f s | bateria=%7.4f%n",
                    paso.getOrigen().getNombre(), paso.getDestino().getNombre(), nombreModo(paso.getModo()),
                    paso.getTiempoTramo(), paso.getConsumo(), paso.getTiempoAcumulado(), paso.getBateriaRestante());
        }
    }

    // Cambia las letras C, S y P por nombres legibles.
    private static String nombreModo(char modo) {
        if (modo == 'C') return "Caminando";
        if (modo == 'S') return "Saltando";
        return "Patas arriba";
    }
}

