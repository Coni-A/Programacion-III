// Representa una conexion dirigida, con su tiempo base y movimientos permitidos.
public class Conexion {

    private Lugar origen;
    private Lugar destino;
    private int segundos;
    private String modos;

    // Guarda el sentido de la conexion y los movimientos habilitados.
    public Conexion(Lugar origen, Lugar destino, int segundos, String modos) {

        this.origen = origen;
        this.destino = destino;
        this.segundos = segundos;
        this.modos = modos;
    }

    public Lugar getOrigen() {
        return origen;
    }

    public Lugar getDestino() {
        return destino;
    }

    public int getSegundos() {
        return segundos;
    }

    public String getModos() {
        return modos;
    }

    // Comprueba si la conexion admite el modo indicado.
    public boolean permiteModo(char modo) {
        return modos.indexOf(modo) != -1;
    }
}

