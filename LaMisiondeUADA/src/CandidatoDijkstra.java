// Guarda un lugar obligatorio y el tramo propuesto por Dijkstra para llegar hasta el.
public class CandidatoDijkstra {
    private Lugar destino;
    private TramoDijkstra tramo;

    // Guarda juntos el destino y el camino que se obtuvo para llegar.
    public CandidatoDijkstra(Lugar destino, TramoDijkstra tramo) {
        this.destino = destino;
        this.tramo = tramo;
    }

    public Lugar getDestino() {
        return destino;
    }

    public TramoDijkstra getTramo() {
        return tramo;
    }
}

