
import java.util.ArrayList;

public class TramoDijkstra {

    private ArrayList<PasoDijkstra> pasos;
    private double tiempo;

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
