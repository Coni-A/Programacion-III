public class Paso {

    private Lugar origen;
    private Lugar destino;
    private char modo;
    private double tiempoTramo;
    private double consumo;
    private double tiempoAcumulado;
    private double bateriaRestante;

    public Paso(Lugar origen, Lugar destino, char modo,
                double tiempoTramo, double consumo,
                double tiempoAcumulado, double bateriaRestante) {

        this.origen = origen;
        this.destino = destino;
        this.modo = modo;
        this.tiempoTramo = tiempoTramo;
        this.consumo = consumo;
        this.tiempoAcumulado = tiempoAcumulado;
        this.bateriaRestante = bateriaRestante;
    }

    public Lugar getOrigen() {
        return origen;
    }

    public Lugar getDestino() {
        return destino;
    }

    public char getModo() {
        return modo;
    }

    public double getTiempoTramo() {
        return tiempoTramo;
    }

    public double getConsumo() {
        return consumo;
    }

    public double getTiempoAcumulado() {
        return tiempoAcumulado;
    }

    public double getBateriaRestante() {
        return bateriaRestante;
    }
}