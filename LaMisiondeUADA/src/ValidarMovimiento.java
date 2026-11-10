import java.util.ArrayList;
import java.util.HashSet;

public class ValidarMovimiento {

    private Grafo grafo;
    private HashSet<Lugar> obligatorios;
    private HashSet<Lugar> prohibidos;

    private final double CAPACIDAD_BATERIA = 100;

    public ValidarMovimiento(Grafo grafo) {

        this.grafo = grafo;

        obligatorios = new HashSet<>();
        prohibidos = new HashSet<>();

        for (Lugar lugar : grafo.getLugares().values()) {

            if (lugar.esObligatorio()) {
                obligatorios.add(lugar);
            }

            if (lugar.esProhibido()) {
                prohibidos.add(lugar);
            }
        }
    }

    // 1. Calcular tiempo según el movimiento
    public double calcularTiempo(Conexion conexion, char modo) {

        switch (modo) {

            case 'C':
                return conexion.getSegundos();

            case 'S':
                return conexion.getSegundos() / 2.0;

            case 'P':
                return conexion.getSegundos() * 2.0;

            default:
                return -1;
        }
    }

    // 2. Calcular consumo de batería
    public double calcularConsumo(Conexion conexion, char modo) {

        double consumoBase = conexion.getSegundos() / 60.0;

        switch (modo) {

            case 'C':
                return consumoBase;

            case 'S':
                return consumoBase * 2.2;

            case 'P':
                return consumoBase * 0.45;

            default:
                return -1;
        }
    }

    // 3. Comprobar si un lugar está cerrado
    public boolean estaCerrado(Lugar lugar, double tiempo) {

        for (Ventana ventana : grafo.getVentanas()) {

            if (ventana.getLugar() == lugar &&
                    tiempo >= ventana.getDesde() &&
                    tiempo <= ventana.getHasta()) {

                return true;
            }
        }

        return false;
    }

    // 4. Calcular la suma de los dígitos del número de aula
    public int sumaDigitos(int numero) {

        int suma = 0;

        while (numero > 0) {

            suma = suma + numero % 10;
            numero = numero / 10;
        }

        return suma;
    }

    // 5. Comprobar si se visitaron todos los lugares obligatorios
    public boolean todosObligatoriosVisitados(Estado estado) {

        return estado.getVisitados().containsAll(obligatorios);
    }

    // 6. Comprobar si el recorrido está completo
    public boolean esSolucionCompleta(Estado estado) {

        return estado.getActual() == grafo.getInicio()
                && !estado.getCamino().isEmpty()
                && todosObligatoriosVisitados(estado);
    }

    // 7. Intentar realizar un movimiento
    public Estado intentarAvanzar(Estado nodo, Conexion conexion, char modo) {

        Lugar destino = conexion.getDestino();

        // Verificar que el movimiento esté permitido
        if (!conexion.permiteModo(modo)) {
            return null;
        }

        // No ingresar a lugares prohibidos
        if (prohibidos.contains(destino)) {
            return null;
        }

        // No repetir lugares, excepto el inicio al finalizar
        if (nodo.getVisitados().contains(destino)
                && destino != grafo.getInicio()) {
            return null;
        }

        // Solo volver al inicio cuando se visitaron todos los obligatorios
        if (destino == grafo.getInicio()
                && !todosObligatoriosVisitados(nodo)) {
            return null;
        }

        // Evitar volver al inicio sin haber recorrido nada
        if (destino == grafo.getInicio()
                && nodo.getCamino().isEmpty()) {
            return null;
        }

        double tiempoTramo = calcularTiempo(conexion, modo);
        double consumoTramo = calcularConsumo(conexion, modo);

        double nuevoTiempo = nodo.getTiempo() + tiempoTramo;
        double nuevaBateria = nodo.getBateria() - consumoTramo;

        // Comprobar batería
        if (nuevaBateria < 0) {
            return null;
        }

        // Solo se permite llegar con batería cero al finalizar
        if (nuevaBateria == 0 && destino != grafo.getInicio()) {
            return null;
        }

        // Comprobar ventana de cierre
        if (estaCerrado(destino, nuevoTiempo)) {
            return null;
        }

        // Recargar batería si el destino es un aula
        if (destino.esAula()) {

            int recarga = sumaDigitos(destino.getNumeroAula()) / 5;

            nuevaBateria = Math.min(
                    CAPACIDAD_BATERIA,
                    nuevaBateria + recarga
            );
        }

        // Crear el nuevo estado
        Estado siguiente = new Estado(nodo);

        siguiente.setActual(destino);
        siguiente.setTiempo(nuevoTiempo);
        siguiente.setBateria(nuevaBateria);

        siguiente.agregarVisitado(destino);

        Paso paso = new Paso(
                nodo.getActual(),
                destino,
                modo,
                tiempoTramo,
                consumoTramo,
                nuevoTiempo,
                nuevaBateria
        );

        siguiente.agregarPaso(paso);

        return siguiente;
    }
}