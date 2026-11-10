
import java.util.ArrayList;
import java.util.HashMap;

// Calcula las distancias minimas entre todos los pares de lugares del mapa.
public class DistanciasFloyd {

    private ArrayList<Lugar> lugares;
    private HashMap<String, Integer> indices;
    private double[][] distancias;

    // Crea la matriz de distancias y relaciona cada lugar con su indice.
    public DistanciasFloyd(Grafo grafo) {

        lugares = new ArrayList<>(grafo.getLugares().values());
        indices = new HashMap<>();

        int cantidad = lugares.size();
        // Cada posicion de la matriz representa el costo de ir de un lugar a otro.
        distancias = new double[cantidad][cantidad];

        // Asociamos cada lugar con una posición de la matriz.
        for (int i = 0; i < cantidad; i++) {
            indices.put(lugares.get(i).getNombre(), i);
        }

        calcular(grafo);
    }

    // Ejecuta Floyd para buscar caminos mas cortos pasando por estaciones intermedias.
    private void calcular(Grafo grafo) {

        int cantidad = lugares.size();

        // 1. Inicializar la matriz.
        for (int i = 0; i < cantidad; i++) {
            for (int j = 0; j < cantidad; j++) {

                if (i == j) {
                    distancias[i][j] = 0;
                } else {
                    distancias[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }

        // 2. Cargar las conexiones directas.
        for (Conexion conexion : grafo.getConexiones()) {

            Lugar origen = conexion.getOrigen();
            Lugar destino = conexion.getDestino();

            // Ignorar lugares prohibidos.
            if (origen.esProhibido() || destino.esProhibido()) {
                continue;
            }

            int i = indices.get(origen.getNombre());
            int j = indices.get(destino.getNombre());

            // Buscar el movimiento que permita llegar en menor tiempo.
            String modos = conexion.getModos();

            for (int m = 0; m < modos.length(); m++) {

                char modo = modos.charAt(m);
                double tiempo = calcularTiempo(conexion, modo);

                if (tiempo < distancias[i][j]) {
                    distancias[i][j] = tiempo;
                }
            }
        }

        // 3. Algoritmo de Floyd.
        for (int k = 0; k < cantidad; k++) {

            for (int i = 0; i < cantidad; i++) {

                for (int j = 0; j < cantidad; j++) {

                    if (distancias[i][k] != Double.POSITIVE_INFINITY
                            && distancias[k][j] != Double.POSITIVE_INFINITY) {

                        double nuevoTiempo =
                                distancias[i][k] + distancias[k][j];

                        if (nuevoTiempo < distancias[i][j]) {
                            distancias[i][j] = nuevoTiempo;
                        }
                    }
                }
            }
        }
    }

    // Calcula el tiempo según el movimiento.
    private double calcularTiempo(Conexion conexion, char modo) {

        switch (modo) {
            case 'C':
                return conexion.getSegundos();

            case 'S':
                return conexion.getSegundos() / 2.0;

            case 'P':
                return conexion.getSegundos() * 2.0;

            default:
                return Double.POSITIVE_INFINITY;
        }
    }

    // Devuelve la distancia mínima entre dos lugares.
    public double obtenerDistancia(Lugar origen, Lugar destino) {

        int i = indices.get(origen.getNombre());
        int j = indices.get(destino.getNombre());

        return distancias[i][j];
    }
}

