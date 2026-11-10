import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Guarda en archivo el arbol de exploracion sin llenar la consola. */
public class RegistroLog implements AutoCloseable {
    private final BufferedWriter salida;

    // Abre el archivo de salida. Si no se indica ruta, el log queda desactivado.
    public RegistroLog(Path ruta) throws IOException {
        salida = ruta == null ? null : Files.newBufferedWriter(ruta, StandardCharsets.UTF_8);
        escribir("LA MISION DE UADA - LOG DEL BACKTRACKING");
        escribir("Cada sangria representa un nivel del arbol; cada estado cuenta una vez.\n");
    }

    // Agrega una linea al archivo cuando el registro esta habilitado.
    public void escribir(String linea) {
        if (salida == null) return;
        try {
            salida.write(linea);
            salida.newLine();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo escribir el log", e);
        }
    }

    // Registra un estado con su profundidad, tiempo, bateria y cotas.
    public void evento(String tipo, Estado estado, int profundidad, long analizados,
                       double ci, double cs, String detalle) {
        if (salida == null) return;
        String sangria = "  ".repeat(Math.max(0, profundidad));
        escribir(String.format(Locale.US,
                "%s%s | actual=%s | tiempo=%.2f s | bateria=%.4f | CI=%s | CS=%s | nodos=%d%s",
                sangria, tipo, estado.getActual().getNombre(), estado.getTiempo(), estado.getBateria(),
                numero(ci), numero(cs), analizados, detalle.isEmpty() ? "" : " | " + detalle));
    }

    // Muestra infinito como INF para que el log sea mas facil de leer.
    private String numero(double n) {
        return Double.isInfinite(n) ? "INF" : String.format(Locale.US, "%.2f", n);
    }

    @Override
    // Cierra el archivo al terminar la ejecucion.
    public void close() throws IOException { if (salida != null) salida.close(); }
}
