import java.util.ArrayList;
import java.util.HashMap;

public class Grafo {

    private HashMap<String, Lugar> lugares;
    private ArrayList<Conexion> conexiones;
    private ArrayList<Ventana> ventanas;
    private Lugar inicio;

    public Grafo() {

        lugares = new HashMap<>();
        conexiones = new ArrayList<>();
        ventanas = new ArrayList<>();
        inicio = null;
    }

    public void agregarLugar(Lugar lugar) {
        lugares.put(lugar.getNombre(), lugar);
    }

    public void agregarConexion(Conexion conexion) {
        conexiones.add(conexion);
    }

    public void agregarVentana(Ventana ventana) {
        ventanas.add(ventana);
    }

    public Lugar buscarLugar(String nombre) {
        return lugares.get(nombre);
    }

    public void setInicio(Lugar inicio) {
        this.inicio = inicio;
    }

    public Lugar getInicio() {
        return inicio;
    }

    public ArrayList<Conexion> getConexiones() {
        return conexiones;
    }

    public ArrayList<Ventana> getVentanas() {
        return ventanas;
    }

    public HashMap<String, Lugar> getLugares() {
        return lugares;
    }
}