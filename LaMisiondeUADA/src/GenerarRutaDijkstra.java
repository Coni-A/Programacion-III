
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GenerarRutaDijkstra {

    private Grafo grafo;
    private ValidarMovimiento mover;
    private Dijkstra dijkstra;

    public GenerarRutaDijkstra(Grafo grafo, ValidarMovimiento mover) {
        this.grafo = grafo;
        this.mover = mover;
        this.dijkstra = new Dijkstra(grafo);
    }

    public Estado generarRuta() {

        // 1. Crear el estado inicial
        Estado estado = new Estado(
                grafo.getInicio(),
                0,
                100
        );

        // 2. Mientras queden lugares obligatorios
        while (!todosObligatoriosVisitados(estado)) {

            ArrayList<CandidatoDijkstra> candidatos =
                    new ArrayList<>();

            // 3. Buscar caminos hacia cada lugar pendiente
            for (Lugar lugar : grafo.getLugares().values()) {

                if (lugar.esObligatorio()
                        && !estado.getVisitados().contains(lugar)) {

                    TramoDijkstra tramo = dijkstra.buscar(
                            estado.getActual(),
                            lugar,
                            estado.getVisitados()
                    );

                    if (tramo != null) {
                        candidatos.add(
                                new CandidatoDijkstra(lugar, tramo)
                        );
                    }
                }
            }

            // Si no hay candidatos, no se puede continuar
            if (candidatos.isEmpty()) {
                return null;
            }

            // 4. Ordenar por tiempo del tramo, de menor a mayor
            Collections.sort(
                    candidatos,
                    new Comparator<CandidatoDijkstra>() {
                        @Override
                        public int compare(
                                CandidatoDijkstra c1,
                                CandidatoDijkstra c2
                        ) {
                            return Double.compare(
                                    c1.getTramo().getTiempo(),
                                    c2.getTramo().getTiempo()
                            );
                        }
                    }
            );

            boolean avanzo = false;

            // 5. Intentar los candidatos en orden
            for (CandidatoDijkstra candidato : candidatos) {

                Estado nuevoEstado = simularTramo(
                        estado,
                        candidato.getTramo()
                );

                if (nuevoEstado != null) {
                    estado = nuevoEstado;
                    avanzo = true;
                    break;
                }
            }

            // Ningún candidato pudo recorrerse válidamente
            if (!avanzo) {
                return null;
            }
        }

        // 6. Buscar el camino de regreso al inicio
        TramoDijkstra regreso = dijkstra.buscar(
                estado.getActual(),
                grafo.getInicio(),
                estado.getVisitados()
        );

        if (regreso == null) {
            return null;
        }

        // 7. Simular el regreso
        estado = simularTramo(estado, regreso);

        if (estado == null) {
            return null;
        }

        // 8. Comprobar que sea una solución completa
        if (mover.esSolucionCompleta(estado)) {
            return estado;
        }

        return null;
    }

    private Estado simularTramo(
            Estado estadoActual,
            TramoDijkstra tramo
    ) {

        Estado estado = estadoActual;

        for (PasoDijkstra paso : tramo.getPasos()) {

            Estado nuevoEstado = mover.intentarAvanzar(
                    estado,
                    paso.getConexion(),
                    paso.getModo()
            );

            if (nuevoEstado == null) {
                return null;
            }

            estado = nuevoEstado;
        }

        return estado;
    }

    private boolean todosObligatoriosVisitados(Estado estado) {

        for (Lugar lugar : grafo.getLugares().values()) {

            if (lugar.esObligatorio()
                    && !estado.getVisitados().contains(lugar)) {

                return false;
            }
        }

        return true;
    }
}
