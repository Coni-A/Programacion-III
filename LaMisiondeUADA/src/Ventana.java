// Representa el intervalo de tiempo en que un lugar permanece cerrado.
public class Ventana {

    private Lugar lugar;
    private int desde;
    private int hasta;

    // Guarda la estacion y los limites del cierre.
    public Ventana(Lugar lugar, int desde, int hasta) {

        this.lugar = lugar;
        this.desde = desde;
        this.hasta = hasta;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public int getDesde() {
        return desde;
    }

    public int getHasta() {
        return hasta;
    }

    // Los dos extremos del intervalo se consideran cerrados.
    public boolean estaCerrado(int tiempo) {
        return tiempo >= desde && tiempo <= hasta;
    }
}