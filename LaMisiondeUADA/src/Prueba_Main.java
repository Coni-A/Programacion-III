import java.io.IOException;

public class Prueba_Main {

    public static void main(String[] args) {

        try {

            Grafo grafo = Archivos.cargar("LaMisiondeUADA/mapa.txt");
            System.out.println("Cantidad de lugares: "
                    + grafo.getLugares().size());

            System.out.println("Cantidad de conexiones: "
                    + grafo.getConexiones().size());

            System.out.println("Cantidad de ventanas: "
                    + grafo.getVentanas().size());

            System.out.println("Lugar de inicio: "
                    + grafo.getInicio().getNombre());

            System.out.println("\nLUGARES OBLIGATORIOS:");

            for (Lugar lugar : grafo.getLugares().values()) {

                if (lugar.esObligatorio()) {
                    System.out.println(lugar.getNombre());
                }
            }

        } catch (IOException e) {

            System.out.println("Error al leer el archivo: " + e.getMessage());

        }
    }
}
