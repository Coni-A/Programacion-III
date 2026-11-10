
public class PasoDijkstra {

    private Conexion conexion;
    private char modo;

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
