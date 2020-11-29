package entidades;

import static java.lang.Thread.sleep;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.dhcp4java.DHCPConstants;

import auxiliares.Auxiliares;
import auxiliares.LoggerS;
/*
import entidades.IpArriendo;
import entidades.PaqueteDHCP;
import entidades.RedDHCP;
*/

/**
 * Esta clase contiene la implementación del servidor DHCP
 *
 * @author Kenneth Leonel, Cristian Dacamara, Luis Montenegro, Juan Pablo Ortiz
 * @version 1.0
 */
public class ServidorDHCP {

    private static final int PUERTO_CLIENTE = 68;
    private static final int PUERTO_SERVIDOR = 67;
    private static final long SEGUNDO = 1000;
    private static final String BROADCAST = "255.255.255.255";
    private static final String IP_VACIA = "0.0.0.0";

    private static List<RedDHCP> listaRedes;
    private static Queue<PaqueteDHCP> centroPaquetes;
    private static InetAddress ipServidor;


    /*
        Constructor servidorDHCP, abrimos el puerto 67, y cada paquete
        que se reciba se guarda en la cola centroPaquetes
    */
    public ServidorDHCP() {
        try {
            ipServidor = Inet4Address.getLocalHost();
            listaRedes = Auxiliares.obtenerRedesPorCSV();
            centroPaquetes = new LinkedList<>();

            DatagramSocket socket = new DatagramSocket(PUERTO_SERVIDOR);
            DatagramPacket paquete = new DatagramPacket(new byte[socket.getSendBufferSize()],
                    socket.getSendBufferSize());

            while (true) {
                socket.receive(paquete);

                PaqueteDHCP dhcp = new PaqueteDHCP(paquete);
                centroPaquetes.add(dhcp);
            }
        } catch (IOException ex) {
            LoggerS.mensaje("Error en el socket puerto servidor: " + ex);
        }
    }

    /**
     * @param args
     *
    */
    public static void procesarSolicitudes() {

        PaqueteDHCP paqueteDhcpRecibio;

        DatagramSocket socket;
        DatagramPacket paquete;
        PaqueteDHCP paqueteDhcpAEnviar;
        try {
            socket = new DatagramSocket(PUERTO_CLIENTE);
            paqueteDhcpAEnviar = new PaqueteDHCP();

            while (true) {
                try {
                    sleep(SEGUNDO / 2);
                    terminarProcesoPaqueteRecibido:
                    while (!centroPaquetes.isEmpty()) {
                        paqueteDhcpRecibio = centroPaquetes.poll();

                        RedDHCP redActual = ObtenerRed(paqueteDhcpRecibio.getIpAgenteRelay());
                        if (redActual == null) {
                            LoggerS.mensaje("No se pudo establecer una conexión con la red.");
                            continue terminarProcesoPaqueteRecibido;
                        }

                        /*  -En caso de que el tipo de paquete que se reciba sea de tipo DISCOVER, se
                            procedera a crear un paquete de tipo offer y se envia ese paquete al cliente.
                            -En caso de que se reciba uno de tipo REQUEST, se verifica que todo esta en
                            orden, en caso de que este todo en orden se crea un paquete de tipo ACK, en caso
                            contrario uno de tipo NACK y por ultimo se envia este paquete al cliente.
                            -En caso de ser tipo RELEASE. se recibe un paquete RELEASE y este procede a liberar
                            la direccion IP entre otros datos que posee el cliente.
                        */
                        switch (paqueteDhcpRecibio.getDHCPMessageType()) {

                            case DHCPConstants.DHCPDISCOVER:
                                LoggerS.mensaje(paqueteDhcpRecibio.DHCPDiscoverToString());

                                byte[] ip = redActual.ipOfertado(paqueteDhcpRecibio.getMacCliente());
                                if (ip == null) {
                                    LoggerS.mensaje("Error en la construcción de DHCP-Discover: No se encontró una ip para la MAC " + Auxiliares.macToString(paqueteDhcpRecibio.getMacCliente()));
                                    continue terminarProcesoPaqueteRecibido;
                                }

                                paqueteDhcpAEnviar.construirPaqueteOffer(paqueteDhcpRecibio, ip, redActual.getTiempoArrendamiento(), null, ipServidor, redActual.getMascara(), redActual.getGateway(), redActual.getServidorDNS());
                                break;

                            case DHCPConstants.DHCPREQUEST:
                                LoggerS.mensaje(paqueteDhcpRecibio.DHCPRequestToString(redActual.getMascara(), redActual.getServidorDNS()));
                                
                                IpArriendo ipAgregada = null;
                                /*
                                    verificarIP, verifica que la ipSolicitada este en la lista de IPs de la red actual.
                                    agregarIP,  llama a la función verificar y si esta disponible agrega la IP
                                */
                                IpArriendo ipSolicitada = redActual.verificarIp(paqueteDhcpRecibio.getIpSolicitada());

                                if (!ipSolicitada.esArrendado()) {
                                    if (!Auxiliares.compararMacs(ipSolicitada.getMac(), paqueteDhcpRecibio.getMacCliente()) && ipSolicitada.esArrendado() && ipSolicitada != null) {
                                        paqueteDhcpAEnviar.construirPaqueteNACK(paqueteDhcpRecibio, null, ipServidor);
                                        break;
                                    } else {
                                        ipAgregada = redActual.agregarIp(paqueteDhcpRecibio.getIpSolicitada());
                                        if (ipAgregada == null) {
                                            paqueteDhcpAEnviar.construirPaqueteNACK(paqueteDhcpRecibio, null, ipServidor);
                                            break;
                                        } else {
                                            redActual.asignarIp(ipAgregada, redActual.getTiempoArrendamiento(), paqueteDhcpRecibio.getMacCliente());
                                        }
                                    }
                                } else {
                                    ipAgregada = redActual.renovarTiempoArrendamiento(paqueteDhcpRecibio.getIpSolicitada(), redActual.getTiempoArrendamiento());
                                    if(ipAgregada == null)
                                        break;
                                }
                                paqueteDhcpAEnviar.construirPaqueteACK(
                                    paqueteDhcpRecibio, 
                                    ipAgregada.getIp(), 
                                    redActual.getTiempoArrendamiento(), 
                                    null, 
                                    ipServidor, 
                                    redActual.getMascara(), 
                                    redActual.getGateway(), 
                                    redActual.getServidorDNS());
                                break;

                            case DHCPConstants.DHCPRELEASE:
                                LoggerS.mensaje(paqueteDhcpRecibio.DHCPReleaseToString());
                                LoggerS.mensaje("\n-------------------------------------- RELEASE --------------------------------------\n" +
                                                "| Liberación del Ip cliente " + Auxiliares.ipToString(paqueteDhcpRecibio.getIpCliente()) + " realizado correctamente|\n" +
                                                "-------------------------------------------------------------------------------------");
                                redActual.liberarIp(paqueteDhcpRecibio.getIpCliente());
                                continue terminarProcesoPaqueteRecibido;

                            default:
                                continue terminarProcesoPaqueteRecibido;
                        }
                        paquete = new DatagramPacket(paqueteDhcpAEnviar.getBuffer(), paqueteDhcpAEnviar.getBufferSize(), InetAddress.getByName(BROADCAST), PUERTO_SERVIDOR);
                        socket.send(paquete);
                    }
                } catch (InterruptedException | IOException ex) {
                    LoggerS.mensaje("Error en centro de procesamiento de paquetes DHCP: " + ex);
                }
            }
        } catch (SocketException ex) {
            LoggerS.mensaje("Error en el socket puerto cliente: " + ex);
        }
    }

    /**
     * Se realiza un for para recoger las redes que se tengan y de esa forma se
     * compara si el gateway es el de la red actual
     *
     * @param dirGateway direccion gateway actual en el cual se esta encontrando en
     *                   la lista. si la encuentra retorna esa red sino retorna null
     * @return RedDHCP
     */
    private static RedDHCP ObtenerRed(byte[] ipAgenteRelay) {
        if (Auxiliares.ipToString(ipAgenteRelay).equals(IP_VACIA)) {
            for (int i = 0; i < listaRedes.size(); i++) {

                if (listaRedes.get(i).ipDentroDelRango(ServidorDHCP.ipServidor.getAddress())) {
                    listaRedes.get(i).asignarIp(listaRedes.get(i).agregarIp(ServidorDHCP.ipServidor.getAddress()), 90000000, new byte[]{0,0,0,0,0,0});
                    return listaRedes.get(i);
                }
            }
        }

        for (int i = 0; i < listaRedes.size(); i++) {
            if (Auxiliares.compararIps(listaRedes.get(i).getGateway(), ipAgenteRelay)) {
                return listaRedes.get(i);
            }
        }
        return null;
    }

}
