import java.util.ArrayDeque;
import java.util.HashSet;

/** BFS iterativo: poda de ramas sin conectividad posible. */
public class ConectividadBFS {
    private final Grafo grafo;

    public ConectividadBFS(Grafo grafo) { this.grafo = grafo; }

    // Recorre con una cola los lugares alcanzables sin reutilizar estaciones bloqueadas.
    public boolean existeCamino(Lugar origen, Lugar destino, HashSet<Lugar> visitadosRuta) {
        ArrayDeque<Lugar> cola = new ArrayDeque<>();
        HashSet<Lugar> explorados = new HashSet<>();
        cola.add(origen);
        explorados.add(origen);

        // Vamos recorriendo las estaciones que se pueden alcanzar desde el origen.
        while (!cola.isEmpty()) {
            Lugar actual = cola.remove();
            if (actual == destino) return true;
            for (Conexion conexion : grafo.getConexionesDesde(actual)) {
                Lugar siguiente = conexion.getDestino();
                if (siguiente.esProhibido() || conexion.getModos().isEmpty()) continue;
                // Solo admitimos el retorno al origen de la mision si es el destino final.
                if (siguiente == grafo.getInicio() && destino != grafo.getInicio()) continue;
                if (visitadosRuta.contains(siguiente) && siguiente != destino) continue;
                if (explorados.add(siguiente)) cola.add(siguiente);
            }
        }
        return false;
    }

    // Verifica si falta conectividad hacia algun obligatorio o hacia el inicio.
    public boolean noEsPosibleCompletar(Estado estado) {
        for (Lugar pendiente : grafo.getLugares().values()) {
            if (!pendiente.esObligatorio() || estado.getVisitados().contains(pendiente)) continue;
            if (!existeCamino(estado.getActual(), pendiente, estado.getVisitados())) return true;
            if (!existeCamino(pendiente, grafo.getInicio(), estado.getVisitados())) return true;
        }
        return !existeCamino(estado.getActual(), grafo.getInicio(), estado.getVisitados());
    }
}

