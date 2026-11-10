import java.util.ArrayList;
import java.util.HashSet;

// Calcula la cota inferior con Prim usando las distancias preparadas por Floyd.
public class CotaInferior {

    private Grafo grafo;
    private DistanciasFloyd floyd;
    private HashSet<Lugar> obligatorios;

    // Prepara el conjunto de lugares obligatorios para usarlo en cada estado.
    public CotaInferior(Grafo grafo, DistanciasFloyd floyd) {

        this.grafo = grafo;
        this.floyd = floyd;

        obligatorios = new HashSet<>();

        // Identificar los lugares obligatorios.
        for (Lugar lugar : grafo.getLugares().values()) {
            if (lugar.esObligatorio()) {
                obligatorios.add(lugar);
            }
        }
    }

    // Aplica Prim sobre el lugar actual, los obligatorios pendientes y el inicio.
    // El resultado es una estimacion optimista y no un recorrido real.
    public double calcularCI(Estado estado) {

        // 1. Obtener los lugares obligatorios pendientes.
        // Solo nos interesan los objetivos que todavia faltan visitar.
        HashSet<Lugar> pendientes = new HashSet<>(obligatorios);
        pendientes.removeAll(estado.getVisitados());

        // 2. Obtener los lugares necesarios.
        HashSet<Lugar> necesarios = new HashSet<>(pendientes);
        necesarios.add(estado.getActual());
        necesarios.add(grafo.getInicio());

        if (necesarios.size() == 1) {
            return estado.getTiempo();
        }

        // 3. Inicializar Prim.
        // Prim empieza en la posicion actual y va conectando los lugares necesarios.
        ArrayList<Lugar> conectados = new ArrayList<>();
        conectados.add(estado.getActual());

        double costoARM = 0;

        // 4. Buscar las conexiones de menor costo.
        while (conectados.size() < necesarios.size()) {

            double menorCosto = Double.POSITIVE_INFINITY;
            Lugar siguienteLugar = null;

            for (Lugar a : conectados) {

                for (Lugar b : necesarios) {

                    if (conectados.contains(b)) {
                        continue;
                    }

                    // Se ignora la dirección para estimar la cota.
                    double ida = floyd.obtenerDistancia(a, b);
                    double vuelta = floyd.obtenerDistancia(b, a);

                    // Para esta estimacion se usa el menor sentido, aunque el grafo real sea dirigido.
                    double costo = Math.min(ida, vuelta);

                    if (costo < menorCosto) {
                        menorCosto = costo;
                        siguienteLugar = b;
                    }
                }
            }

            // No se pudo conectar otro lugar.
            if (siguienteLugar == null) {
                return Double.POSITIVE_INFINITY;
            }

            conectados.add(siguienteLugar);
            costoARM = costoARM + menorCosto;
        }

        // 5. Sumar el tiempo ya recorrido.
        return estado.getTiempo() + costoARM;
    }
}

