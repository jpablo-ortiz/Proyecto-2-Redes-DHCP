import static java.lang.Thread.sleep;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.dhcp4java.DHCPConstants;

import auxiliares.Auxiliares;
import auxiliares.LoggerS;
import entidades.IpArriendo;
import entidades.PaqueteDHCP;
import entidades.RedDHCP;

public class ServidorDHCP
{

    private static final int PUERTO_CLIENTE = 68;
    private static final int PUERTO_SERVIDOR = 67;
    private static final long SEGUNDO = 1000;
    private static final String BROADCAST = "255.255.255.255";
    private static final String IP_VACIA = "0.0.0.0";

    private static List<RedDHCP> listaRedes;
    private static Queue<PaqueteDHCP> centroPaquetes;
    private static InetAddress ipServidor;

    ServidorDHCP()
    {
        DatagramSocket socket;
        try
        {
            socket = new DatagramSocket(PUERTO_SERVIDOR);
            DatagramPacket paquete = new DatagramPacket(new byte[socket.getSendBufferSize()],
                    socket.getSendBufferSize());

            while (true)
            {
                socket.receive(paquete);

                PaqueteDHCP dhcp = new PaqueteDHCP(paquete);
                centroPaquetes.add(dhcp);
            }
        }
        catch (SocketException ex)
        {
            LoggerS.mensaje(ServidorDHCP.class.getName() + ": " + ex);
            // Logger.getLogger(ServidorDHCP.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            LoggerS.mensaje(ServidorDHCP.class.getName() + ": " + ex);
            // Logger.getLogger(ServidorDHCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            ipServidor = Inet4Address.getLocalHost();
            listaRedes = Auxiliares.obtenerRedesPorCSV();
            centroPaquetes = new LinkedList<>();

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    procesarSolicitudes();
                }
            }).start();

            ServidorDHCP servidorDhcp = new ServidorDHCP();

        }
        catch (UnknownHostException ex)
        {
            LoggerS.mensaje(ServidorDHCP.class.getName() + ": " + ex);
            // LoggerS.getLogger(ServidorDHCP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void procesarSolicitudes()
    {

        PaqueteDHCP paqueteDhcpRecibio;

        DatagramSocket socket;
        DatagramPacket paquete;
        PaqueteDHCP paqueteDhcpAEnviar;
        try
        {
            socket = new DatagramSocket(PUERTO_CLIENTE);
            paqueteDhcpAEnviar = new PaqueteDHCP();

            while (true)
            {
                try
                {
                    sleep(SEGUNDO / 2);
                    terminarProcesoPaqueteRecibido:
                    while (!centroPaquetes.isEmpty())
                    {
                        paqueteDhcpRecibio = centroPaquetes.poll();
                        LoggerS.mensaje(paqueteDhcpRecibio.toString()); // Guardar informacion paquete en el log

                        RedDHCP redActual = ObtenerRed(paqueteDhcpRecibio.getDirGateway());
                        if (redActual == null)
                        {
                            LoggerS.mensaje("No se pudo establecer una conexión con la red.");
                            continue terminarProcesoPaqueteRecibido;
                        }

                        // -En caso de que el tipo de paquete que se reciba sea de tipo DISCOVER, se
                        // procedera
                        // a crear un paquete de tipo offer y se envia ese paquete.
                        // -En caso de que se reciba uno de tipo REQUEST, se verifica que todo esta en
                        // orden, en
                        // caso de que este todo en orden se crea un paquete de tipo ACK, en caso
                        // contrario uno
                        // de tipo NACK y por ultimo se envia este paquete.
                        // -En caso de ser tipo RELEASE.
                        switch (paqueteDhcpRecibio.getDHCPMessageType())
                        {

                            case DHCPConstants.DHCPDISCOVER:
                                byte[] ip = redActual.ipOfertado(paqueteDhcpRecibio.getMacCliente());
                                if (ip == null)
                                {
                                    LoggerS.mensaje(
                                            "Error en la construcción de DHCP-Discover: No se encontró una ip para la MAC "
                                            + Auxiliares.macToString(paqueteDhcpRecibio.getMacCliente()));
                                    continue terminarProcesoPaqueteRecibido;
                                }

                                paqueteDhcpAEnviar.construirPaqueteOffer(paqueteDhcpRecibio, ip, redActual.getTiempoArrendamiento(), null, ipServidor, redActual.getMascara(), redActual.getGateway(), redActual.getServidorDNS());
                                break;

                            case DHCPConstants.DHCPREQUEST:
                                IpArriendo ipAgregada = null;
                                if (Auxiliares.ipToString(paqueteDhcpRecibio.getIpCliente()).equals(IP_VACIA))
                                {
                                    IpArriendo ipSolicitada = redActual
                                            .verificarIp(paqueteDhcpRecibio.getIpSolicitada());

                                    if (!Auxiliares.compararMacs(ipSolicitada.getMac(),
                                            paqueteDhcpRecibio.getMacCliente()) && ipSolicitada.esArrendado()
                                            && ipSolicitada != null)
                                    {
                                        paqueteDhcpAEnviar.construirPaqueteNACK(paqueteDhcpRecibio, null, ipServidor);
                                        break;
                                    }
                                    else
                                    {
                                        ipAgregada = redActual.agregarIp(paqueteDhcpRecibio.getIpSolicitada());
                                        if (ipAgregada == null)
                                        {
                                            paqueteDhcpAEnviar.construirPaqueteNACK(paqueteDhcpRecibio, null,
                                                    ipServidor);
                                            break;
                                        }
                                        else
                                        {
                                            redActual.asignarIp(ipAgregada, redActual.getTiempoArrendamiento(),
                                                    paqueteDhcpRecibio.getMacCliente());
                                        }
                                    }
                                }
                                else
                                {
                                    redActual.renovarTiempoArrendamiento(paqueteDhcpRecibio.getIpCliente(),
                                            redActual.getTiempoArrendamiento());
                                }
                                paqueteDhcpAEnviar.construirPaqueteACK(paqueteDhcpRecibio,
                                        Inet4Address.getByAddress(ipAgregada.getIp()),
                                        redActual.getTiempoArrendamiento(), null, null, ipServidor);
                                break;

                            case DHCPConstants.DHCPRELEASE:
                                LoggerS.mensaje(
                                        "--------------------------------------RELEASE--------------------------------------");
                                LoggerS.mensaje("| Liberación del Ip cliente "
                                        + Auxiliares.ipToString(paqueteDhcpRecibio.getIpCliente())
                                        + " realizado correctamente|");
                                LoggerS.mensaje(
                                        "-----------------------------------------------------------------------------------");
                                redActual.liberarIp(paqueteDhcpRecibio.getIpCliente());
                                continue terminarProcesoPaqueteRecibido;

                            default:
                                LoggerS.mensaje("----- PAQUETE INVÁLIDO ------");
                                LoggerS.mensaje("| Paquete recibido invalido |");
                                LoggerS.mensaje("-----------------------------");
                                continue terminarProcesoPaqueteRecibido;
                        }
                        paquete = new DatagramPacket(paqueteDhcpAEnviar.getBuffer(), paqueteDhcpAEnviar.getBufferSize(),
                                InetAddress.getByName(BROADCAST), PUERTO_CLIENTE);
                        socket.send(paquete);
                    }
                }
                catch (InterruptedException | IOException ex)
                {
                    LoggerS.mensaje(ServidorDHCP.class.getName() + ": " + ex);
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Se realiza un for para recoger las redes que se tengan y de esa forma se
     * compara si el gateway es el de la red actual
     *
     * @param dirGateway direccion gateway actual en el cual se esta encontrando
     * en la lista. si la encuentra retorna esa red sino retorna null
     * @return RedDHCP
     */
    private static RedDHCP ObtenerRed(byte[] dirGateway)
    {
        for (int i = 0; i < listaRedes.size(); i++)
        {
            if (Auxiliares.compararIps(listaRedes.get(i).getGateway(), dirGateway))
            {
                return listaRedes.get(i);
            }
        }
        return null;
    }
}
