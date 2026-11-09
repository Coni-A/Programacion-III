public class Lugar {

    private String nombre;
    private int numeroAula;
    private int x;
    private int y;
    private boolean obligatorio;
    private boolean prohibido;

    public Lugar(String nombre, int numeroAula, int x, int y,
                 boolean obligatorio, boolean prohibido) {

        this.nombre = nombre;
        this.numeroAula = numeroAula;
        this.x = x;
        this.y = y;
        this.obligatorio = obligatorio;
        this.prohibido = prohibido;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNumeroAula() {
        return numeroAula;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean esObligatorio() {
        return obligatorio;
    }

    public boolean esProhibido() {
        return prohibido;
    }

    public boolean esAula() {
        return numeroAula > 0;
    }
}
