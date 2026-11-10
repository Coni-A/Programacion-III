import java.util.ArrayList;
import java.util.HashSet;

public class Estado {

    private Lugar actual;
    private double tiempo;
    private double bateria;
    private HashSet<Lugar> visitados;
    private ArrayList<Paso> camino;

    public Estado(Lugar actual, double tiempo, double bateria) {

        this.actual = actual;
        this.tiempo = tiempo;
        this.bateria = bateria;

        visitados = new HashSet<>();
        camino = new ArrayList<>();

        visitados.add(actual);
    }

    // Constructor para copiar un estado existente
    public Estado(Estado otro) {

        this.actual = otro.actual;
        this.tiempo = otro.tiempo;
        this.bateria = otro.bateria;

        this.visitados = new HashSet<>(otro.visitados);
        this.camino = new ArrayList<>(otro.camino);
    }

    public Lugar getActual() {
        return actual;
    }

    public double getTiempo() {
        return tiempo;
    }

    public double getBateria() {
        return bateria;
    }

    public HashSet<Lugar> getVisitados() {
        return visitados;
    }

    public ArrayList<Paso> getCamino() {
        return camino;
    }

    public void setActual(Lugar actual) {
        this.actual = actual;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }

    public void setBateria(double bateria) {
        this.bateria = bateria;
    }

    public void agregarVisitado(Lugar lugar) {
        visitados.add(lugar);
    }

    public void agregarPaso(Paso paso) {
        camino.add(paso);
    }
}