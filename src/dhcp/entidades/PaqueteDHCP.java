package entidades;

import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPResponseFactory;

import auxiliares.Auxiliares;
import auxiliares.LoggerS;

/**
 * Esta clase contiene atributos y métodos de un Paquete DHCP
 *
 * @author Kenneth Leonel, Cristian Dacamara, Luis Montenegro, Juan Pablo Ortiz
 * @version 1.0
 */
public class PaqueteDHCP {

    private static final byte DHO_DHCP_REQUESTED_ADDRESS = 50; //Requested Ip Adress

    private static final byte DHO_SUBNET_MASK = 1; //Mascara de red
    private static final byte DHO_ROUTERS = 3; //GATEWAY
    private static final byte DHO_DOMAIN_NAME_SERVERS = 6; //DNS

    private DHCPPacket dhcpPack;

    public PaqueteDHCP() {
        dhcpPack = new DHCPPacket();
    }

    public PaqueteDHCP(DatagramPacket paquete) {
        dhcpPack = DHCPPacket.getPacket(paquete);
    }

    /**
     * se utiliza la funcion DHCPResponseFactory.makeDHCPOffer() de la libreria
     * DHCPPacket para construir un paquete de tipo DHCP Offer.
     *
     * @param discover
     * @param offeredAddress
     * @param leaseTime
     * @param message
     * @param options
     * @param IPServidor
     */
    public void construirPaqueteOffer(PaqueteDHCP discover, byte[] ipOfrecida, int leaseTime, String message, InetAddress IPServidor, byte[] mascara, byte[] gateway, byte[] dns) {

        InetAddress offeredAddress = null;
        DHCPOption[] opciones = new DHCPOption[3];

        try {
            offeredAddress = Inet4Address.getByAddress(ipOfrecida);

            opciones[0] = DHCPOption.newOptionAsInetAddress(DHO_SUBNET_MASK, Inet4Address.getByAddress(mascara));
            opciones[1] = DHCPOption.newOptionAsInetAddress(DHO_ROUTERS, Inet4Address.getByAddress(gateway));
            opciones[2] = DHCPOption.newOptionAsInetAddress(DHO_DOMAIN_NAME_SERVERS, Inet4Address.getByAddress(dns));

            LoggerS.mensaje("\n-------------------------------------- OFFER - Enviado --------------------------------------\n" +
                            "| Paquete recibido: DHCP-Discover | MAC: " + Auxiliares.macToString(discover.getMacCliente()) + " |\n" +
                            "| IP ofrecida: " + offeredAddress.getHostAddress() + " | Tiempo de arrendamiento: " + leaseTime + "s |\n" +
                            "---------------------------------------------------------------------------------------------");

            dhcpPack = DHCPResponseFactory.makeDHCPOffer(discover.getDHCPPacket(), offeredAddress, leaseTime, IPServidor, message, opciones);
        } catch (UnknownHostException e) {
            LoggerS.mensaje(PaqueteDHCP.class.getName() + ": " + e);
        }
    }

    /**
     * se utiliza la funcion DHCPResponseFactory.makeDHCPAck() de la libreria
     * DHCPPacket para construir un paquete de tipo DHCP Ack.
     *
     * @param request
     * @param ipOfrecida
     * @param leaseTime
     * @param message
     * @param IPServidor
     * @param mascara
     * @param gateway
     * @param dns
     */
    public void construirPaqueteACK(PaqueteDHCP request, byte[] ipOfrecida, int leaseTime, String message, InetAddress IPServidor, byte[] mascara, byte[] gateway, byte[] dns) {

        InetAddress offeredAddress = null;
        DHCPOption[] opciones = new DHCPOption[3];

        try {
            offeredAddress = Inet4Address.getByAddress(ipOfrecida);

            opciones[0] = DHCPOption.newOptionAsInetAddress(DHO_SUBNET_MASK, Inet4Address.getByAddress(mascara));
            opciones[1] = DHCPOption.newOptionAsInetAddress(DHO_ROUTERS, Inet4Address.getByAddress(gateway));
            opciones[2] = DHCPOption.newOptionAsInetAddress(DHO_DOMAIN_NAME_SERVERS, Inet4Address.getByAddress(dns));

            LoggerS.mensaje("\n-------------------------------------- ACK - Enviado --------------------------------------\n" +
                            "| Paquete recibido: DHCP-request | Tiempo de arrendamiento: " + leaseTime + " |\n" +
                            "| +++++++++++++++++++++++++++++++++ Parámetros Asignados +++++++++++++++++++++++++++++++++ |\n" +
                            "| Dirección IP solicitada: " + offeredAddress.getHostAddress() + " Mascara: " + Auxiliares.ipToString(mascara) + " |\n" +
                            "| DNS: " + Auxiliares.ipToString(dns) +" |\n" +
                            "-------------------------------------------------------------------------------------------");
            dhcpPack = DHCPResponseFactory.makeDHCPAck(request.getDHCPPacket(), offeredAddress, leaseTime, IPServidor, message, opciones);
        } catch (UnknownHostException e) {
            LoggerS.mensaje(PaqueteDHCP.class.getName() + ": " + e);
        }
    }

    /**
     * Se utiliza la funcion DHCPResponseFactory.makeDHCPNak() de la libreria
     * DHCPPacket para construir un paquete de tipo DHCP Nack.
     * @param request
     * @param message
     * @param IPServidor
     */
    public void construirPaqueteNACK(PaqueteDHCP request, String message, InetAddress IPServidor) {

        LoggerS.mensaje("\n-------------------------------------- NACK - Enviado --------------------------------------\n" +
                        "| Estado del reporte: Ejecución negada |\n" +
                        "--------------------------------------------------------------------------------------------");
        dhcpPack = DHCPResponseFactory.makeDHCPNak(request.getDHCPPacket(), IPServidor, message);
    }

    public String DHCPDiscoverToString()
    {
        return "\n-------------------------------------- Discover - Recibido --------------------------------------\n" +
                "| ID: " + dhcpPack.getXid() + " | MAC: " + Auxiliares.macToString(getMacCliente()) + " |\n" +
                "-------------------------------------------------------------------------------------------------";
    }

    public String DHCPRequestToString(byte[] mascara, byte[] dns)
    {
        return "\n-------------------------------------- Request - Recibido --------------------------------------\n" +
                "| ID: " + dhcpPack.getXid() + " | MAC: " + Auxiliares.macToString(getMacCliente()) + " |\n" +
                "| Relay Agent: " + Auxiliares.ipToString(getIpAgenteRelay()) + " |\n" +
                "| ++++++++++++++++++++++++++++++++++ Parámetros Solicitados ++++++++++++++++++++++++++++++++++ |\n" +
                "| Dirección IP solicitada: " + Auxiliares.ipToString(getIpSolicitada()) + " Mascara: " + Auxiliares.ipToString(mascara) + " |\n" +
                "| DNS: " + Auxiliares.ipToString(dns) +" |\n" +
                "------------------------------------------------------------------------------------------------";
    }

    public String DHCPReleaseToString()
    {
        return "\n-------------------------------------- Release - Recibido --------------------------------------\n" +
                "| IP a Liberar: " + Auxiliares.ipToString(getIpCliente()) + " | MAC: " + Auxiliares.macToString(getMacCliente()) + " |\n" +
                "------------------------------------------------------------------------------------------------";
    }

    /**
     * @return byte[]
     */
    public byte[] getDirGateway() {
        return dhcpPack.getGiaddrRaw();
    }

    /**
     * @return Byte
     */
    public Byte getDHCPMessageType() {
        return dhcpPack.getDHCPMessageType();
    }

    /**
     * @return byte[]
     */
    public byte[] getBuffer() {
        return dhcpPack.serialize();
    }

    /**
     * @return int
     */
    public int getBufferSize() {
        return dhcpPack.serialize().length;
    }

    /**
     * @return byte[]
     */
    public byte[] getMacCliente() {
        return dhcpPack.getChaddr();
    }

    /**
     * @return byte[]
     */
    public byte[] getIpCliente() {
        return dhcpPack.getCiaddrRaw();
    }

    /**
     * @return DHCPPacket
     */
    public DHCPPacket getDHCPPacket() {
        return dhcpPack;
    }

    /**
     * @return byte[]
     */
    public byte[] getIpSolicitada() {
        return dhcpPack.getOptionAsInetAddr(DHO_DHCP_REQUESTED_ADDRESS).getAddress();
    }

    /**
     * @return byte[]
     */
    public byte[] getIpAgenteRelay() {
        return dhcpPack.getGiaddrRaw();
    }

    /**
     * @return String
     */
    @Override
    public String toString() {
        return ""; //dhcpPack.toString();
    }

}
