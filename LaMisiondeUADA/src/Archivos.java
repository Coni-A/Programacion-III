import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Archivos {

    public static Grafo cargar(String ruta) throws IOException {

        Grafo grafo = new Grafo();
        // Abre el archivo para leer el contenido linea por linea
        BufferedReader archivo = new BufferedReader(new FileReader(ruta));

        String linea;

        while ((linea = archivo.readLine()) != null) {

            linea = linea.trim();

            // Ignorar líneas vacías y comentarios
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;

            }
            String[] datos = linea.split(";");

            switch (datos[0]) {

                case "LUGAR":
                    //datos[0]=LUGAR; datos[1]=nombre; datos[2]=aula(0 si no); datos[3]=x; datos[4]=y; datos[5]=obligatorio; datos[6]=prohibido          String nombre = datos[1];
                    String nombre = datos[1];
                    int numeroAula = Integer.parseInt(datos[2]);
                    int x = Integer.parseInt(datos[3]);
                    int y = Integer.parseInt(datos[4]);

                    boolean obligatorio = datos[5].equals("1");
                    boolean prohibido = datos[6].equals("1");

                    Lugar lugar = new Lugar(
                            nombre,
                            numeroAula,
                            x,
                            y,
                            obligatorio,
                            prohibido
                    );

                    grafo.agregarLugar(lugar);

                    break;

                case "ARISTA":
                    //datos[0]=ARISTA; datos[1]=origen; datos[2]=destino; datos[3]=segundos; datos[4]=modos
                    Lugar origen = grafo.buscarLugar(datos[1]);
                    Lugar destino = grafo.buscarLugar(datos[2]);

                    if (origen == null || destino == null) {
                        archivo.close();
                        throw new IllegalArgumentException(
                                "La conexión contiene un lugar inexistente: " + linea
                        );
                    }

                    int segundos = Integer.parseInt(datos[3]);
                    String modos = datos[4];

                    Conexion conexion = new Conexion(
                            origen,
                            destino,
                            segundos,
                            modos
                    );

                    grafo.agregarConexion(conexion);

                    break;

                case "VENTANA":
                    //datos[0]=VENTANA; datos[1]=lugar; datos[2]=desde; datos[3]=hasta
                    Lugar lugarVentana = grafo.buscarLugar(datos[1]);

                    if (lugarVentana == null) {
                        archivo.close();
                        throw new IllegalArgumentException(
                                "La ventana contiene un lugar inexistente: " + linea
                        );
                    }

                    int desde = Integer.parseInt(datos[2]);
                    int hasta = Integer.parseInt(datos[3]);

                    Ventana ventana = new Ventana(
                            lugarVentana,
                            desde,
                            hasta
                    );

                    grafo.agregarVentana(ventana);

                    break;

                case "INICIO":

                    Lugar lugarInicio = grafo.buscarLugar(datos[1]);

                    if (lugarInicio == null) {
                        archivo.close();
                        throw new IllegalArgumentException(
                                "El lugar de inicio no existe: " + datos[1]
                        );
                    }

                    grafo.setInicio(lugarInicio);

                    break;
            }
        }

        archivo.close();

        if (grafo.getInicio() == null) {
            throw new IllegalArgumentException(
                    "No se indicó el lugar de inicio en el archivo."
            );
        }

        return grafo;
    }
}
