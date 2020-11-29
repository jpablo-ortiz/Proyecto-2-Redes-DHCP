import entidades.ServidorDHCP;

/**
 * Esta clase contiene el inicio del Servidor DHCP
 *
 * @author Kenneth Leonel, Cristian Dacamara, Luis Montenegro, Juan Pablo Ortiz
 * @version 1.0
 */
public class Main {
    
    /**
     * @param args
     * Obtenemos la IP address del servidor, y obtenemos por el archivo plano los datos necesarios
     * y los guardamos en una lista de redes llamada listaRedes
     *
     * Luego de obtener los datos por el archivo plano,
     * creamos los hilos para obtener multiples solicitudes al tiempo
     * Y llamamos a a procesarSolicitudes
     */
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServidorDHCP.procesarSolicitudes();
            }
        }).start();

        ServidorDHCP servidorDhcp = new ServidorDHCP();
    }

}
