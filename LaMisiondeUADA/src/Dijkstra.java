import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

// Busca el tramo mas corto entre dos estaciones. No valida bateria ni ventanas.
public class Dijkstra {

    private Grafo grafo;

    // Recibe el mapa sobre el que va a buscar caminos.
    public Dijkstra(Grafo grafo) {
        this.grafo = grafo;
    }

    // Busca un tramo de tiempo minimo entre origen y destino.
    // Los caminos propuestos se validan despues, antes de usarlos en una ruta.
    public TramoDijkstra buscar(Lugar origen, Lugar destino,
                                HashSet<Lugar> visitadosRuta) {

        // Guardamos los tiempos minimos conocidos y como se llego a cada lugar.
        HashMap<Lugar, Double> distancias = new HashMap<>();
        HashMap<Lugar, Lugar> anteriores = new HashMap<>();
        HashMap<Lugar, Conexion> conexionesAnteriores = new HashMap<>();
        HashMap<Lugar, Character> movimientos = new HashMap<>();

        HashSet<Lugar> fijados = new HashSet<>();

        // 1. Inicializar las distancias.
        for (Lugar lugar : grafo.getLugares().values()) {
            distancias.put(lugar, Double.POSITIVE_INFINITY);
            anteriores.put(lugar, null);
            conexionesAnteriores.put(lugar, null);
            movimientos.put(lugar, null);
        }

        distancias.put(origen, 0.0);

        // 2. Buscar iterativamente el lugar con menor distancia.
        while (fijados.size() < grafo.getLugares().size()) {

            Lugar actual = null;
            double menor = Double.POSITIVE_INFINITY;

            // Elegimos el lugar pendiente con la menor distancia conocida.
            for (Lugar lugar : grafo.getLugares().values()) {

                if (!fijados.contains(lugar)
                        && distancias.get(lugar) < menor) {

                    menor = distancias.get(lugar);
                    actual = lugar;
                }
            }

            if (actual == null) {
                break;
            }

            if (actual == destino) {
                break;
            }

            fijados.add(actual);

            // 3. Revisar las conexiones que salen del lugar actual.
            for (Conexion conexion : grafo.getConexiones()) {

                if (conexion.getOrigen() != actual) {
                    continue;
                }

                Lugar siguiente = conexion.getDestino();

                // No pasar por lugares prohibidos ni fijados.
                if (siguiente.esProhibido()
                        || fijados.contains(siguiente)) {
                    continue;
                }

                // No volver a lugares ya visitados.
                if (visitadosRuta.contains(siguiente)
                        && siguiente != destino) {
                    continue;
                }

                // No regresar al inicio antes de tiempo.
                if (siguiente == grafo.getInicio()
                        && destino != grafo.getInicio()) {
                    continue;
                }

                // 4. Elegir el movimiento más rápido.
                char mejorModo = '\0';
                double costo = Double.POSITIVE_INFINITY;

                String modos = conexion.getModos();

                for (int i = 0; i < modos.length(); i++) {

                    char modo = modos.charAt(i);
                    double tiempo = calcularTiempo(conexion, modo);

                    if (tiempo < costo) {
                        costo = tiempo;
                        mejorModo = modo;
                    }
                }

                if (mejorModo == '\0') {
                    continue;
                }

                // 5. Relajar la distancia.
                double nuevaDistancia = distancias.get(actual) + costo;

                if (nuevaDistancia < distancias.get(siguiente)) {

                    distancias.put(siguiente, nuevaDistancia);
                    anteriores.put(siguiente, actual);
                    conexionesAnteriores.put(siguiente, conexion);
                    movimientos.put(siguiente, mejorModo);
                }
            }
        }

        // 6. Comprobar si se encontró un camino.
        if (distancias.get(destino) == Double.POSITIVE_INFINITY) {
            return null;
        }

        // 7. Reconstruir el camino desde el destino al origen.
        ArrayList<PasoDijkstra> pasos = new ArrayList<>();

        Lugar actual = destino;

        while (actual != origen) {

            Lugar previo = anteriores.get(actual);
            Conexion conexion = conexionesAnteriores.get(actual);

            if (previo == null || conexion == null) {
                return null;
            }

            char modo = movimientos.get(actual);

            pasos.add(new PasoDijkstra(conexion, modo));

            actual = previo;
        }

        // Invertir para obtener el orden origen -> destino.
        // Reconstruimos hacia atras, por eso al final invertimos los pasos.
        Collections.reverse(pasos);

        return new TramoDijkstra(pasos, distancias.get(destino));
    }

    // Obtiene el costo de cada modo para comparar las conexiones.
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
}

