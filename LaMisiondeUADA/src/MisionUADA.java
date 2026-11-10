
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;

/**
 * Backtracking ITERATIVO para los tres mejores recorridos de ESTACIONES distintas.
 * Para una misma secuencia de estaciones conserva solamente el mejor conjunto de modos.
 * Floyd + Prim suministran una cota inferior optimista.
 * Dijkstra aporta una primera ruta valida cuando la encuentra.
 * BFS descarta rutas sin conectividad para completar la mision.
 */
public class MisionUADA {
    private final Grafo grafo;
    private final ValidarMovimiento mover;
    private final CotaInferior cota;
    private final ConectividadBFS conectividad;
    private final RegistroLog log;
    // Resultados completos ordenados; se guardan como maximo tres recorridos distintos.
    private final ArrayList<Estado> mejoresRutas = new ArrayList<>();
    private Estado mejorParcial;
    // Mientras no haya tres soluciones, no hay un tiempo limite para podar.
    private double cs = Double.POSITIVE_INFINITY;
    private long nodosAnalizados;
    private long podasCota;
    private long podasImposibilidad;
    private long movimientosInvalidos;
    private long retrocesos;
    private long tiempoTotalNanos;

    // Inicializa los auxiliares que usa la busqueda.
    public MisionUADA(Grafo grafo, RegistroLog log) {
        this.grafo = grafo;
        this.log = log;
        this.mover = new ValidarMovimiento(grafo);
        this.cota = new CotaInferior(grafo, new DistanciasFloyd(grafo));
        this.conectividad = new ConectividadBFS(grafo);
    }

    // Una alternativa es una conexion y un movimiento que podemos probar.
    private static class Alternativa {
        Conexion conexion;
        char modo;
        Estado siguiente;
        double ci;
        Alternativa(Conexion conexion, char modo, Estado siguiente, double ci) {
            this.conexion = conexion;
            this.modo = modo;
            this.siguiente = siguiente;
            this.ci = ci;
        }
    }

    // Cada marco recuerda el estado y que opciones le faltan al volver de una rama.
    private static class Marco {
        Estado estado;
        boolean evaluado;
        ArrayList<Alternativa> opciones;
        int proxima;
        Marco(Estado estado) { this.estado = estado; }
    }

    // Inicia la busqueda y recorre el arbol de posibilidades usando una pila.
    // Cada marco conserva las alternativas pendientes para poder retroceder sin recursion.
    public void resolver() {
        long comienzo = System.nanoTime();
        Estado inicial = new Estado(grafo.getInicio(), 0, 100);

        // Dijkstra intenta conseguir una primera ruta; si falla, la busqueda sigue igual.
        GenerarRutaDijkstra generador = new GenerarRutaDijkstra(grafo, mover);
        Estado propuesta = generador.generarRuta();
        if (propuesta != null && mover.esSolucionCompleta(propuesta)) {
            agregarSolucion(propuesta);
            log.evento("DIJKSTRA RUTA INICIAL VALIDA", propuesta, 0, 0, Double.NaN, cs,
                    "tiempo=" + formato(propuesta.getTiempo()));
        } else {
            log.escribir("DIJKSTRA: no encontro ruta completa; Backtracking continua igualmente.");
        }

        // La pila reemplaza las llamadas recursivas del Backtracking.
        ArrayDeque<Marco> pila = new ArrayDeque<>();
        pila.push(new Marco(inicial));
        while (!pila.isEmpty()) {
            Marco marco = pila.peek();
            Estado nodo = marco.estado;
            int nivel = pila.size() - 1;

            // Cada estado se cuenta una sola vez, aunque volvamos a el al retroceder.
            if (!marco.evaluado) {
                marco.evaluado = true;
                nodosAnalizados++;
                // Si cerramos una ruta valida, intentamos incorporarla a las tres mejores.
                if (mover.esSolucionCompleta(nodo)) {
                    agregarSolucion(nodo);
                    log.evento("SOLUCION COMPLETA", nodo, nivel, nodosAnalizados, nodo.getTiempo(), cs,
                            "rutas_encontradas=" + mejoresRutas.size());
                    pila.pop();
                    continue;
                }

                actualizarMejorParcial(nodo);
                double ci = cota.calcularCI(nodo);
                log.evento("NODO ANALIZADO", nodo, nivel, nodosAnalizados, ci, cs,
                        "obligatorios=" + contarObligatorios(nodo));

                // Esta rama ya no puede superar a la tercera mejor ruta conocida.
                if (ci > cs + 1e-9) {
                    podasCota++;
                    log.evento("PODA POR COTA", nodo, nivel, nodosAnalizados, ci, cs, "CI > tiempo tercera ruta");
                    pila.pop();
                    continue;
                }

                // Si no hay soluciones completas, guardamos candidatos a mejor parcial.
                if (!mejoresRutas.isEmpty() && conectividad.noEsPosibleCompletar(nodo)) {
                    podasImposibilidad++;
                    log.evento("PODA IMPOSIBILIDAD", nodo, nivel, nodosAnalizados, ci, cs,
                            "no se pueden completar los obligatorios y regresar");
                    pila.pop();
                    continue;
                }

                marco.opciones = buscarSiguientesOpciones(nodo, nivel);
            }

            Alternativa siguiente = null;
            // Probamos la siguiente opcion del marco sin repetir las anteriores.
            while (marco.proxima < marco.opciones.size()) {
                Alternativa candidata = marco.opciones.get(marco.proxima++);
                if (candidata.ci > cs + 1e-9 ||
                        (!mejoresRutas.isEmpty() && Double.isInfinite(candidata.ci))) {
                    podasCota++;
                    log.evento("PODA OPCION POR COTA", nodo, nivel, nodosAnalizados, candidata.ci, cs,
                            "destino=" + candidata.conexion.getDestino().getNombre() + " modo=" + candidata.modo);
                    continue;
                }
                siguiente = candidata;
                break;
            }

            // Si no quedan movimientos utiles, volvemos al estado anterior.
            if (siguiente == null) {
                retrocesos++;
                log.evento("RETROCESO", nodo, nivel, nodosAnalizados, Double.NaN, cs, "sin alternativas pendientes");
                pila.pop();
            } else {
                log.evento("AVANZAR", siguiente.siguiente, nivel + 1, nodosAnalizados,
                        siguiente.ci, cs, "modo=" + siguiente.modo + " desde=" + nodo.getActual().getNombre());
                pila.push(new Marco(siguiente.siguiente));
            }
        }
        tiempoTotalNanos = System.nanoTime() - comienzo;
        log.escribir("\nRESUMEN FINAL: rutas=" + mejoresRutas.size() + ", estados_analizados=" + nodosAnalizados
                + ", podas_cota=" + podasCota + ", podas_imposibilidad=" + podasImposibilidad
                + ", movimientos_invalidos=" + movimientosInvalidos + ", retrocesos=" + retrocesos);
    }

    /** Se obtienen los vecinos validos y se ordenan por menor CI (y mas bateria en empate). */
    private ArrayList<Alternativa> buscarSiguientesOpciones(Estado nodo, int nivel) {
        ArrayList<Alternativa> alternativas = new ArrayList<>();
        for (Conexion conexion : grafo.getConexionesDesde(nodo.getActual())) {
            for (int i = 0; i < conexion.getModos().length(); i++) {
                char modo = conexion.getModos().charAt(i);
                Estado siguiente = mover.intentarAvanzar(nodo, conexion, modo);
                if (siguiente == null) {
                    movimientosInvalidos++;
                    log.evento("MOVIMIENTO INVALIDO", nodo, nivel, nodosAnalizados, Double.NaN, cs,
                            "intento=" + conexion.getDestino().getNombre() + " modo=" + modo);
                    continue;
                }
                double ci = cota.calcularCI(siguiente);
                alternativas.add(new Alternativa(conexion, modo, siguiente, ci));
            }
        }
        alternativas.sort(Comparator.comparingDouble((Alternativa a) -> a.ci)
                .thenComparing((Alternativa a) -> -a.siguiente.getBateria()));
        return alternativas;
    }

    // Guarda solo una version por secuencia de estaciones y conserva las tres mejores.
    private void agregarSolucion(Estado candidata) {
        if (!mover.esSolucionCompleta(candidata)) return;
        // Identificamos la ruta solo por sus estaciones, no por los movimientos elegidos.
        String firma = firmaRecorrido(candidata);
        for (int i = 0; i < mejoresRutas.size(); i++) {
            if (firmaRecorrido(mejoresRutas.get(i)).equals(firma)) {
                Estado anterior = mejoresRutas.get(i);
                if (candidata.getTiempo() < anterior.getTiempo() - 1e-9 ||
                        (Math.abs(candidata.getTiempo() - anterior.getTiempo()) < 1e-9 &&
                                candidata.getBateria() > anterior.getBateria())) {
                    mejoresRutas.set(i, new Estado(candidata));
                }
                // Reordenar tambien cuando se mejora una ruta ya guardada.
                ordenarMejoresRutas();
                actualizarCS();
                return;
            }
        }
        mejoresRutas.add(new Estado(candidata));
        ordenarMejoresRutas();
        while (mejoresRutas.size() > 3) mejoresRutas.remove(mejoresRutas.size() - 1);
        actualizarCS();
    }

    // Ordena por menor tiempo y, si empatan, por mayor bateria restante.
    private void ordenarMejoresRutas() {
        mejoresRutas.sort(Comparator.comparingDouble(Estado::getTiempo)
                .thenComparing(Comparator.comparingDouble(Estado::getBateria).reversed())
                .thenComparing(this::firmaRecorrido));
    }

    // La cota superior para podar se fija solo cuando ya hay tres rutas distintas.
    private void actualizarCS() {
        cs = mejoresRutas.size() == 3 ? mejoresRutas.get(2).getTiempo() : Double.POSITIVE_INFINITY;
    }

    /**
     * La identidad de un recorrido depende UNICAMENTE de sus estaciones.
     * Cambiar de caminar a saltar (o viceversa) NO crea una ruta nueva.
     * Los modos se usan para conservar la version mas rapida (y, en empate,
     * la que termina con mayor bateria) de esas mismas estaciones.
     */
    private String firmaRecorrido(Estado estado) {
        StringBuilder firma = new StringBuilder(grafo.getInicio().getNombre());
        for (Paso paso : estado.getCamino()) {
            firma.append(" -> ").append(paso.getDestino().getNombre());
        }
        return firma.toString();
    }

    // Si no hay solucion completa, conserva el mejor avance alcanzado.
    private void actualizarMejorParcial(Estado candidato) {
        if (candidato.getCamino().isEmpty()) return;
        if (mejorParcial == null) { mejorParcial = new Estado(candidato); return; }
        int actual = contarObligatorios(candidato);
        int mejor = contarObligatorios(mejorParcial);
        double distActual = distanciaRecta(candidato.getActual(), grafo.getInicio());
        double distMejor = distanciaRecta(mejorParcial.getActual(), grafo.getInicio());
        if (actual > mejor || (actual == mejor && (distActual < distMejor - 1e-9 ||
                (Math.abs(distActual - distMejor) < 1e-9 && candidato.getTiempo() < mejorParcial.getTiempo())))) {
            mejorParcial = new Estado(candidato);
        }
    }

    // Cuenta los objetivos obligatorios que ya aparecen entre los visitados.
    private int contarObligatorios(Estado estado) {
        int cantidad = 0;
        for (Lugar lugar : grafo.getLugares().values())
            if (lugar.esObligatorio() && estado.getVisitados().contains(lugar)) cantidad++;
        return cantidad;
    }

    // Calcula la distancia geometrica al inicio para comparar rutas parciales.
    private double distanciaRecta(Lugar a, Lugar b) {
        return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
    }

    // Devuelve una lista nueva con los resultados, sin exponer la lista interna.
    public ArrayList<Estado> getMejoresRutas() { return new ArrayList<>(mejoresRutas); }
    public Estado getMejorParcial() { return mejorParcial; }
    public long getNodosAnalizados() { return nodosAnalizados; }
    public long getPodasCota() { return podasCota; }
    public long getPodasImposibilidad() { return podasImposibilidad; }
    public long getMovimientosInvalidos() { return movimientosInvalidos; }
    public long getRetrocesos() { return retrocesos; }
    // Convierte el tiempo de ejecucion de nanosegundos a milisegundos.
    public double getDuracionMs() { return tiempoTotalNanos / 1_000_000.0; }
    public double getCS() { return cs; }
    // Muestra los segundos con dos decimales en el log.
    private String formato(double numero) { return String.format(Locale.US, "%.2f", numero); }
}

