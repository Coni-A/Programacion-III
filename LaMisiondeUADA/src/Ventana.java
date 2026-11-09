public class Ventana {

    private Lugar lugar;
    private int desde;
    private int hasta;

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

    public boolean estaCerrado(int tiempo) {
        return tiempo >= desde && tiempo <= hasta;
    }
}