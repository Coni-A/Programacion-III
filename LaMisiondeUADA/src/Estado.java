import java.util.ArrayList;
import java.util.HashSet;

// Guarda como se encuentra UADA en un punto de la exploracion.
public class Estado {

    private Lugar actual;
    private double tiempo;
    private double bateria;
    // Los visitados evitan repetir estaciones y el camino conserva cada movimiento.
    private HashSet<Lugar> visitados;
    private ArrayList<Paso> camino;

    // Constructor del estado inicial, con la estacion actual, tiempo y bateria.
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

    // Marca una estacion como utilizada para que no se vuelva a recorrer.
    public void agregarVisitado(Lugar lugar) {
        visitados.add(lugar);
    }

    // Agrega al historial el movimiento realizado.
    public void agregarPaso(Paso paso) {
        camino.add(paso);
    }
}
