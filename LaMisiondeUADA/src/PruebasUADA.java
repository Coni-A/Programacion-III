import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

/** Comprobaciones funcionales basicas. Ejecutar: java -cp out PruebasUADA [Mapa.txt] */
public class PruebasUADA {
    private static int pruebas = 0;

    // Muestra OK si se cumple una condicion; de lo contrario corta la prueba.
    private static void comprobar(boolean condicion, String descripcion) {
        if (!condicion) throw new AssertionError("FALLO: " + descripcion);
        pruebas++;
        System.out.println("OK - " + descripcion);
    }

    // Compara dos valores decimales dejando un pequeño margen de precision.
    private static void igual(double esperado, double real, String descripcion) {
        comprobar(Math.abs(esperado - real) < 1e-8, descripcion);
    }

    // Ejecuta las pruebas sobre el mapa y sobre casos pequeños armados para probar reglas.
    public static void main(String[] args) throws Exception {
        String archivo = args.length > 0 ? args[0] : Files.isRegularFile(Path.of("Mapa.txt"))
                ? "Mapa.txt" : "LaMisiondeUADA/Mapa.txt";
        Grafo grafo = Archivos.cargar(archivo);
        comprobar(grafo.getLugares().size() == 20, "lectura de 20 lugares");
        comprobar(grafo.getConexiones().size() == 32, "lectura de 32 conexiones dirigidas");
        comprobar(grafo.getVentanas().size() == 3, "lectura de 3 ventanas");
        comprobar(grafo.getInicio().getNombre().equals("Aula 348"), "lugar de inicio");

        ValidarMovimiento validador = new ValidarMovimiento(grafo);
        Estado inicial = new Estado(grafo.getInicio(), 0, 100);
        Conexion a501 = buscar(grafo, "Aula 348", "Aula 501");
        Estado tras501 = validador.intentarAvanzar(inicial, a501, 'S');
        igual(27.5, tras501.getTiempo(), "saltar tarda la mitad");
        igual(100 - 55 / 60.0 * 2.2 + 6 / 5.0, tras501.getBateria(), "recarga decimal de Aula 501");
        igual(40 / 60.0, validador.calcularConsumo(buscar(grafo, "Aula 348", "Pasillo P3"), 'C'),
                "consumo caminando proporcional a segundos");
        igual(40 / 60.0 * .45, validador.calcularConsumo(buscar(grafo, "Aula 348", "Pasillo P3"), 'P'),
                "consumo patas para arriba 45 por ciento");
        comprobar(validador.estaCerrado(grafo.buscarLugar("Starbucks"), 100), "Starbucks cerrado en limite inicial");
        comprobar(validador.estaCerrado(grafo.buscarLugar("Starbucks"), 130), "Starbucks cerrado en limite final");
        comprobar(!validador.estaCerrado(grafo.buscarLugar("Starbucks"), 131), "Starbucks abierto despues del cierre");
        Estado p3 = validador.intentarAvanzar(inicial, buscar(grafo, "Aula 348", "Pasillo P3"), 'C');
        comprobar(validador.intentarAvanzar(p3, buscar(grafo, "Pasillo P3", "Zona obras"), 'C') == null,
                "no ingresar en zona prohibida");
        comprobar(validador.intentarAvanzar(inicial, a501, 'P') == null,
                "rechazar movimiento no permitido");

        // Comprobamos las tres mejores rutas del mapa que se entrega con el trabajo.
        MisionUADA mision;
        try (RegistroLog log = new RegistroLog(null)) {
            mision = new MisionUADA(grafo, log);
            mision.resolver();
        }
        ArrayList<Estado> rutas = mision.getMejoresRutas();
        comprobar(rutas.size() == 3, "se encuentran tres mejores rutas completas");
        HashSet<String> firmas = new HashSet<>();
        igual(417.5, rutas.get(0).getTiempo(), "mejor ruta del mapa ampliado: 417.5 s");
        for (int i = 1; i < rutas.size(); i++) {
            comprobar(rutas.get(i).getTiempo() >= rutas.get(i - 1).getTiempo() - 1e-9,
                    "rutas ordenadas por tiempo total");
        }
        for (Estado ruta : rutas) {
            comprobar(ruta.getTiempo() >= 417.5 - 1e-9,
                    "ninguna ruta es mas rapida que el optimo conocido de 417.5 s");
            comprobar(validador.esSolucionCompleta(ruta), "ruta empieza, visita obligatorios y termina en inicio");
            HashSet<Lugar> visitados = new HashSet<>();
            visitados.add(grafo.getInicio());
            StringBuilder firma = new StringBuilder();
            double ultimaHora = 0;
            for (int i = 0; i < ruta.getCamino().size(); i++) {
                Paso paso = ruta.getCamino().get(i);
                comprobar(paso.getTiempoAcumulado() > ultimaHora, "tiempo creciente en cada movimiento");
                ultimaHora = paso.getTiempoAcumulado();
                comprobar(paso.getBateriaRestante() >= -1e-9, "bateria nunca negativa");
                comprobar(!validador.estaCerrado(paso.getDestino(), ultimaHora), "no entrar en ventana cerrada");
                if (paso.getDestino() != grafo.getInicio()) {
                    comprobar(visitados.add(paso.getDestino()), "no repetir estacion");
                } else {
                    comprobar(i == ruta.getCamino().size() - 1, "regresar al origen solo al terminar");
                }
                firma.append('/').append(paso.getDestino().getNombre());
            }
            comprobar(firmas.add(firma.toString()), "recorrido con secuencia de estaciones diferente");
        }
        comprobar(mision.getNodosAnalizados() > 0, "contar nodos analizados");

        // Caso de contingencia: no existe camino de regreso al inicio.
        Path sinSolucion = Files.createTempFile("uada_imposible_", ".txt");
        try {
            Files.writeString(sinSolucion, "ARISTA;A;B;20;CS\nLUGAR;A;0;0;0;0;0\n" +
                    "LUGAR;B;0;1;0;1;0\nINICIO;A\n");
            Grafo imposible = Archivos.cargar(sinSolucion.toString());
            MisionUADA misionImposible;
            try (RegistroLog log = new RegistroLog(null)) {
                misionImposible = new MisionUADA(imposible, log);
                misionImposible.resolver();
            }
            comprobar(misionImposible.getMejoresRutas().isEmpty(), "sin solucion devuelve cero rutas completas");
            comprobar(misionImposible.getMejorParcial() != null &&
                            misionImposible.getMejorParcial().getActual().getNombre().equals("B"),
                    "devolver el mejor recorrido parcial disponible");
        } finally { Files.deleteIfExists(sinSolucion); }

        // Mini-grafo: mismo recorrido A -> B -> A con cuatro combinaciones
        // de modos; debe contar como UNA sola ruta y guardar la mas rapida.
        Path minigrafo = Files.createTempFile("uada_tres_", ".txt");
        try {
            Files.writeString(minigrafo, "LUGAR;A;0;0;0;0;0\n" +
                    "LUGAR;B;0;1;0;1;0\n" +
                    "LUGAR;C;0;2;0;0;0\n" +
                    "LUGAR;D;0;3;0;0;0\n" +
                    "ARISTA;A;B;10;CS\nARISTA;B;A;10;CS\n" +
                    "ARISTA;A;C;10;CS\nARISTA;C;B;10;CS\n" +
                    "ARISTA;A;D;10;CS\nARISTA;D;B;10;CS\nINICIO;A\n");
            Grafo mini = Archivos.cargar(minigrafo.toString());
            MisionUADA m;
            try (RegistroLog log = new RegistroLog(null)) {
                m = new MisionUADA(mini, log);
                m.resolver();
            }
            ArrayList<Estado> tres = m.getMejoresRutas();
            comprobar(tres.size() == 3, "mapa pequeno: tres secuencias diferentes de estaciones");
            igual(10.0, tres.get(0).getTiempo(), "mapa pequeno: mejor A-B-A (S,S)");
            igual(15.0, tres.get(1).getTiempo(), "mapa pequeno: segundo recorrido mas corto");
            igual(15.0, tres.get(2).getTiempo(), "mapa pequeno: tercer recorrido mas corto");
            HashSet<String> firmasMini = new HashSet<>();
            for (Estado ruta : tres) {
                StringBuilder firma = new StringBuilder("A");
                for (Paso paso : ruta.getCamino()) firma.append('>').append(paso.getDestino().getNombre());
                comprobar(firmasMini.add(firma.toString()), "sin duplicados por cambio de modo");
            }
        } finally { Files.deleteIfExists(minigrafo); }

        // Varios modos, PERO una unica secuencia de estaciones.
        // Antes se devolvian 3 rutas; ahora debe devolverse solo 1.
        Path soloUna = Files.createTempFile("uada_un_recorrido_", ".txt");
        try {
            Files.writeString(soloUna, "LUGAR;A;0;0;0;0;0\n" +
                    "LUGAR;B;0;1;0;1;0\n" +
                    "ARISTA;A;B;10;CS\nARISTA;B;A;10;CS\nINICIO;A\n");
            Grafo unico = Archivos.cargar(soloUna.toString());
            MisionUADA m;
            try (RegistroLog log = new RegistroLog(null)) {
                m = new MisionUADA(unico, log);
                m.resolver();
            }
            comprobar(m.getMejoresRutas().size() == 1,
                    "un unico recorrido, aunque cambien los movimientos");
            igual(10.0, m.getMejoresRutas().get(0).getTiempo(),
                    "se conserva la combinacion de movimientos mas rapida");
        } finally { Files.deleteIfExists(soloUna); }

        // Caso en que Dijkstra usa el modo mas rapido, llega durante un cierre
        // y no logra ruta inicial; Backtracking debe explorar el modo lento.
        Path ventanaDijkstra = Files.createTempFile("uada_cierre_", ".txt");
        try {
            Files.writeString(ventanaDijkstra, "LUGAR;A;0;0;0;0;0\n" +
                    "LUGAR;B;0;1;0;1;0\n" +
                    "ARISTA;A;B;100;CS\nARISTA;B;A;100;C\n" +
                    "VENTANA;B;40;60\nINICIO;A\n");
            Grafo mini = Archivos.cargar(ventanaDijkstra.toString());
            ValidarMovimiento mover = new ValidarMovimiento(mini);
            comprobar(new GenerarRutaDijkstra(mini, mover).generarRuta() == null,
                    "Dijkstra no logra tramo que respete ventana");
            MisionUADA m;
            try (RegistroLog log = new RegistroLog(null)) {
                m = new MisionUADA(mini, log);
                m.resolver();
            }
            comprobar(m.getMejoresRutas().size() == 1, "Backtracking si encuentra ruta valida");
            igual(200.0, m.getMejoresRutas().get(0).getTiempo(),
                    "Backtracking elige caminar al atravesar ventana");
        } finally { Files.deleteIfExists(ventanaDijkstra); }

        System.out.println("\nPRUEBAS SUPERADAS: " + pruebas);
    }

    // Busca una conexion concreta para usarla en las comprobaciones.
    private static Conexion buscar(Grafo grafo, String desde, String hasta) {
        for (Conexion c : grafo.getConexiones()) {
            if (c.getOrigen().getNombre().equals(desde) && c.getDestino().getNombre().equals(hasta)) return c;
        }
        throw new IllegalArgumentException("No hay conexion " + desde + " -> " + hasta);
    }
}

