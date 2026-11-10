
// Representa un movimiento sugerido por Dijkstra, antes de comprobar si es valido.
public class PasoDijkstra {

    private Conexion conexion;
    private char modo;

    // Guarda una conexion y el movimiento elegido para atravesarla.
    public PasoDijkstra(Conexion conexion, char modo) {
        this.conexion = conexion;
        this.modo = modo;
    }

    public Conexion getConexion() {
        return conexion;
    }

    public char getModo() {
        return modo;
    }
}
