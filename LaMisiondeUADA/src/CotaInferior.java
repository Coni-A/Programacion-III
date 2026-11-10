
import java.util.ArrayList;
import java.util.HashSet;

public class CotaInferior {

    private Grafo grafo;
    private DistanciasFloyd floyd;
    private HashSet<Lugar> obligatorios;

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

    public double calcularCI(Estado estado) {

        // 1. Obtener los lugares obligatorios pendientes.
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
