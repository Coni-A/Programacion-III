
import java.util.ArrayList;

// Guarda una secuencia de pasos propuesta por Dijkstra y su tiempo estimado.
public class TramoDijkstra {

    private ArrayList<PasoDijkstra> pasos;
    private double tiempo;

    // Recibe los pasos propuestos y el tiempo total estimado.
    public TramoDijkstra(ArrayList<PasoDijkstra> pasos, double tiempo) {
        this.pasos = pasos;
        this.tiempo = tiempo;
    }

    public ArrayList<PasoDijkstra> getPasos() {
        return pasos;
    }

    public double getTiempo() {
        return tiempo;
    }
}

