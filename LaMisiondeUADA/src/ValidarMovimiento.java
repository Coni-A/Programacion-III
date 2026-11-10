
import java.util.ArrayList;
import java.util.HashSet;

// Controla las reglas de cada movimiento antes de crear un nuevo estado.
public class ValidarMovimiento {

    private Grafo grafo;
    private HashSet<Lugar> obligatorios;
    private HashSet<Lugar> prohibidos;

    private final double CAPACIDAD_BATERIA = 100;

    // Reune los lugares obligatorios y prohibidos para validarlos mas rapido.
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
    // Caminando tarda el tiempo base, saltando la mitad y patas arriba el doble.
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
    // Calcula el gasto de bateria tomando como base el tiempo caminando en minutos.
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
    // Comprueba si UADA llegaria durante el cierre de esa estacion.
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
    // Se usa para calcular la recarga de bateria al llegar a un aula.
    public int sumaDigitos(int numero) {

        int suma = 0;

        while (numero > 0) {

            suma = suma + numero % 10;
            numero = numero / 10;
        }

        return suma;
    }

    // 5. Comprobar si se visitaron todos los lugares obligatorios
    // Comprueba si ya se visitaron todas las estaciones obligatorias.
    public boolean todosObligatoriosVisitados(Estado estado) {

        return estado.getVisitados().containsAll(obligatorios);
    }

    // 6. Comprobar si el recorrido está completo
    // La ruta debe volver al inicio despues de visitar todos los obligatorios.
    public boolean esSolucionCompleta(Estado estado) {

        return estado.getActual() == grafo.getInicio()
                && !estado.getCamino().isEmpty()
                && todosObligatoriosVisitados(estado);
    }

    // 7. Intentar realizar un movimiento
    // Valida un paso completo y, si sirve, devuelve una copia del estado actualizado.
    // Si se incumple una regla, devuelve null y Backtracking prueba otro movimiento.
    public Estado intentarAvanzar(Estado nodo, Conexion conexion, char modo) {

        Lugar destino = conexion.getDestino();

        // Verificar que el movimiento esté permitido
        if (conexion.getOrigen() != nodo.getActual() || !conexion.permiteModo(modo)) {
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

        // Actualizamos el tiempo y descontamos el consumo antes de cualquier recarga.
        double nuevoTiempo = nodo.getTiempo() + tiempoTramo;
        double nuevaBateria = nodo.getBateria() - consumoTramo;

        // Comprobar batería
        if (nuevaBateria < -1e-9) {
            return null;
        }

        // Solo se permite llegar con batería cero al finalizar
        if (nuevaBateria <= 1e-9 && destino != grafo.getInicio()) {
            return null;
        }

        // Comprobar ventana de cierre
        if (estaCerrado(destino, nuevoTiempo)) {
            return null;
        }

        // Recargar batería si el destino es un aula
        // La recarga se aplica al llegar, sin superar las 100 unidades.
        if (destino.esAula()) {

            double recarga = sumaDigitos(destino.getNumeroAula()) / 5.0;

            nuevaBateria = Math.min(
                    CAPACIDAD_BATERIA,
                    nuevaBateria + recarga
            );
        }

        // Crear el nuevo estado
        // Copiamos el estado para no modificar el camino anterior del Backtracking.
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
