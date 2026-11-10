import java.io.IOException;

public class Prueba_Main {

    public static void main(String[] args) {

        try {

            Grafo grafo = Archivos.cargar("Mapa.txt");

            ValidarMovimiento mover = new ValidarMovimiento(grafo);

            // Crear el estado inicial
            Estado inicial = new Estado(grafo.getInicio(), 0, 100);

            System.out.println("ESTADO INICIAL");
            System.out.println("Lugar: "
                    + inicial.getActual().getNombre());
            System.out.println("Tiempo: "
                    + inicial.getTiempo());
            System.out.println("Bateria: "
                    + inicial.getBateria());

            Conexion conexionPrueba = null;

            for (Conexion conexion : grafo.getConexiones()) {

                if (conexion.getOrigen() == grafo.getInicio()
                        && conexion.getDestino().getNombre().equals("Pasillo P3")) {

                    conexionPrueba = conexion;
                }
            }

            // Probar el movimiento saltando
            Estado siguiente = mover.intentarAvanzar(
                    inicial,
                    conexionPrueba,
                    'S'
            );

            if (siguiente != null) {

                System.out.println("\nDESPUES DEL MOVIMIENTO");

                System.out.println("Lugar: "
                        + siguiente.getActual().getNombre());

                System.out.println("Tiempo: "
                        + siguiente.getTiempo());

                System.out.println("Bateria: "
                        + siguiente.getBateria());

                System.out.println("Cantidad de pasos: "
                        + siguiente.getCamino().size());

            } else {

                System.out.println("Movimiento no permitido.");
            }


            DistanciasFloyd floyd = new DistanciasFloyd(grafo);

            Lugar inicio = grafo.getInicio();
            Lugar starbucks = grafo.buscarLugar("Starbucks");
            Lugar auditorio = grafo.buscarLugar("Auditorio");
            Lugar aula613 = grafo.buscarLugar("Aula 613");
            Lugar molinete = grafo.buscarLugar("Molinete Lima 3");

            System.out.println("\nDISTANCIAS MINIMAS - FLOYD");

            System.out.println("Aula 348 -> Starbucks: "
                    + floyd.obtenerDistancia(inicio, starbucks));

            System.out.println("Aula 348 -> Auditorio: "
                    + floyd.obtenerDistancia(inicio, auditorio));

            System.out.println("Aula 348 -> Aula 613: "
                    + floyd.obtenerDistancia(inicio, aula613));

            System.out.println("Aula 348 -> Molinete Lima 3: "
                    + floyd.obtenerDistancia(inicio, molinete));


            CotaInferior cota = new CotaInferior(grafo, floyd);

            double resultado = cota.calcularCI(inicial);

            System.out.println("\nCOTA INFERIOR - PRIM");
            System.out.println("Tiempo acumulado: " + inicial.getTiempo());
            System.out.println("Cota inferior: " + resultado);


            Estado sigue = mover.intentarAvanzar(inicial, conexionPrueba, 'C');

            if (sigue != null) {
                System.out.println("Cota después del movimiento: "
                        + cota.calcularCI(sigue));
            }


            Dijkstra dijkstra = new Dijkstra(grafo);

            TramoDijkstra tramo = dijkstra.buscar(
                    grafo.getInicio(),
                    grafo.buscarLugar("Starbucks"),
                    inicial.getVisitados()
            );

            System.out.println("\nRESULTADO DIJKSTRA");

            if (tramo != null) {

                System.out.println("Tiempo estimado: " + tramo.getTiempo());

                for (PasoDijkstra paso : tramo.getPasos()) {

                    Conexion conexion = paso.getConexion();

                    System.out.println(
                            conexion.getOrigen().getNombre()
                                    + " -> "
                                    + conexion.getDestino().getNombre()
                                    + " | Movimiento: "
                                    + paso.getModo()
                    );
                }

            } else {
                System.out.println("No se encontro un camino.");
            }

            GenerarRutaDijkstra generador =
                    new GenerarRutaDijkstra(grafo, mover);

            Estado solucion = generador.generarRuta();

            if (solucion == null) {

                System.out.println(
                        "Dijkstra no pudo encontrar una ruta completa."
                );

            } else {

                System.out.println("Ruta encontrada.");
                System.out.println(
                        "Tiempo total: " + solucion.getTiempo()
                );
                System.out.println(
                        "Bateria restante: " + solucion.getBateria()
                );

                System.out.println("\nRecorrido:");

                for (Paso paso : solucion.getCamino()) {

                    System.out.println(
                            paso.getOrigen().getNombre()
                                    + " -> "
                                    + paso.getDestino().getNombre()
                                    + " | Modo: "
                                    + paso.getModo()
                                    + " | Tiempo acumulado: "
                                    + paso.getTiempoAcumulado()
                                    + " | Bateria: "
                                    + paso.getBateriaRestante()
                    );
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
