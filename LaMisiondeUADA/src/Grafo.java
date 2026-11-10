import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

// Contiene los lugares y conexiones del mapa, las ventanas y el lugar inicial.
public class Grafo {
    private LinkedHashMap<String, Lugar> lugares = new LinkedHashMap<>();
    private ArrayList<Conexion> conexiones = new ArrayList<>();
    private ArrayList<Ventana> ventanas = new ArrayList<>();
    // Las salientes permiten consultar rapidamente hacia donde se puede avanzar.
    private HashMap<Lugar, ArrayList<Conexion>> salientes = new HashMap<>();
    private Lugar inicio;

    // Incorpora la estacion y deja preparada su lista de salidas.
    public void agregarLugar(Lugar lugar) {
        lugares.put(lugar.getNombre(), lugar);
        salientes.put(lugar, new ArrayList<>());
    }

    // Guarda la arista y la agrega a las salidas de su origen.
    public void agregarConexion(Conexion conexion) {
        conexiones.add(conexion);
        salientes.get(conexion.getOrigen()).add(conexion);
    }

    // Registra un intervalo de cierre para una estacion.
    public void agregarVentana(Ventana ventana) { ventanas.add(ventana); }
    public Lugar buscarLugar(String nombre) { return lugares.get(nombre); }
    public void setInicio(Lugar inicio) { this.inicio = inicio; }
    public Lugar getInicio() { return inicio; }
    public ArrayList<Conexion> getConexiones() { return conexiones; }
    // Devuelve solo las conexiones que salen de la estacion indicada.
    public ArrayList<Conexion> getConexionesDesde(Lugar lugar) { return salientes.get(lugar); }
    public ArrayList<Ventana> getVentanas() { return ventanas; }
    public LinkedHashMap<String, Lugar> getLugares() { return lugares; }
}
