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

public class PaqueteDHCP
{

    private static final byte DHO_DHCP_REQUESTED_ADDRESS = 50;

    private static final byte DHO_SUBNET_MASK = 1; // getValueAsInetAddr() //Mascara de red
    private static final byte DHO_ROUTERS = 3; // getValueAsInetAddrs() //GATEWAY
    private static final byte DHO_DOMAIN_NAME_SERVERS = 6; // getValueAsInetAddrs() //DNS

    private DHCPPacket dhcpPack;

    /**
     * se utiliza la funcion DHCPResponseFactory.makeDHCPOffer() de la libreria
     * DHCPPacket para crear un paquete de tipo DHCP Offer.
     *
     * @param discover
     * @param offeredAddress
     * @param leaseTime
     * @param message
     * @param options
     * @param IPServidor
     */
    public void construirPaqueteOffer(PaqueteDHCP discover, byte[] ipOfrecida, int leaseTime, String message, InetAddress IPServidor, byte[] mascara, byte[] gateway, byte[] dns)
    {
        InetAddress offeredAddress = null;
        DHCPOption[] opciones = new DHCPOption[3];

        try
        {
            offeredAddress = Inet4Address.getByAddress(ipOfrecida);
            opciones[0] = DHCPOption.newOptionAsInetAddress(DHO_SUBNET_MASK, Inet4Address.getByAddress(mascara));
            opciones[1] = DHCPOption.newOptionAsInetAddress(DHO_ROUTERS, Inet4Address.getByAddress(gateway));
            opciones[2] = DHCPOption.newOptionAsInetAddress(DHO_DOMAIN_NAME_SERVERS, Inet4Address.getByAddress(dns));

            LoggerS.mensaje(" ");
            LoggerS.mensaje("--------------------- OFFER ----------------------");
            LoggerS.mensaje("| Paquete recibido: DHCP-Discover | MAC: " + Auxiliares.macToString(discover.getMacCliente()) + " |");
            LoggerS.mensaje("| IP ofrecida: " + offeredAddress.getHostAddress() + " | Tiempo de arrendamiento: " + leaseTime + "s |");
            LoggerS.mensaje("--------------------------------------------------");
            LoggerS.mensaje(" ");

            dhcpPack = DHCPResponseFactory.makeDHCPOffer(discover.getDHCPPacket(), offeredAddress, leaseTime, IPServidor, message, opciones);
        }
        catch (UnknownHostException e)
        {
            LoggerS.mensaje(PaqueteDHCP.class.getName() + ": " + e);
        }
    }

    /**
     * @param request
     * @param offeredAddress
     * @param leaseTime
     * @param message
     * @param options
     * @param IPServidor
     */
    public void construirPaqueteACK(PaqueteDHCP request, InetAddress offeredAddress, int leaseTime, String message,
            DHCPOption[] options, InetAddress IPServidor)
    {

        LoggerS.mensaje(" ");
        LoggerS.mensaje("--------------------- ACK ------------------------");
        LoggerS.mensaje(
                "| Paquete recibido: DHCP-request | Dirección IP asignada: " + offeredAddress.getHostAddress() + " |");
        LoggerS.mensaje("| Tiempo de arrendamiento: " + leaseTime + " |");
        LoggerS.mensaje("--------------------------------------------------");
        LoggerS.mensaje(" ");
        dhcpPack = DHCPResponseFactory.makeDHCPAck(request.getDHCPPacket(), offeredAddress, leaseTime, IPServidor,
                message, options);
    }

    /**
     * @param request
     * @param message
     * @param IPServidor
     */
    public void construirPaqueteNACK(PaqueteDHCP request, String message, InetAddress IPServidor)
    {
        LoggerS.mensaje(" ");
        LoggerS.mensaje("--------------------- NACK ------------------------");
        LoggerS.mensaje("| Estado del reporte: Ejecución negada |");
        LoggerS.mensaje("---------------------------------------------------");
        LoggerS.mensaje(" ");
        dhcpPack = DHCPResponseFactory.makeDHCPNak(request.getDHCPPacket(), IPServidor, message);
    }

    public PaqueteDHCP()
    {
        dhcpPack = new DHCPPacket();
    }

    public PaqueteDHCP(DatagramPacket paquete)
    {
        dhcpPack = DHCPPacket.getPacket(paquete);
    }

    /**
     * @return byte[]
     */
    public byte[] getDirGateway()
    {
        return dhcpPack.getGiaddrRaw();
    }

    /**
     * @return Byte
     */
    public Byte getDHCPMessageType()
    {
        return dhcpPack.getDHCPMessageType();
    }

    /**
     * @return byte[]
     */
    public byte[] getBuffer()
    {
        return dhcpPack.serialize();
    }

    /**
     * @return int
     */
    public int getBufferSize()
    {
        return dhcpPack.serialize().length;
    }

    /**
     * @return byte[]
     */
    public byte[] getMacCliente()
    {
        return dhcpPack.getChaddr();
    }

    /**
     * @return byte[]
     */
    public byte[] getIpCliente()
    {
        return dhcpPack.getCiaddrRaw();
    }

    /**
     * @return DHCPPacket
     */
    public DHCPPacket getDHCPPacket()
    {
        return dhcpPack;
    }

    /**
     * @return byte[]
     */
    public byte[] getIpSolicitada()
    {
        return dhcpPack.getOptionAsInetAddr(DHO_DHCP_REQUESTED_ADDRESS).getAddress();
    }

}
