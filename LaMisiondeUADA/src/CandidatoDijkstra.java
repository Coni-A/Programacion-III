
public class CandidatoDijkstra {
    private Lugar destino;
    private TramoDijkstra tramo;

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
