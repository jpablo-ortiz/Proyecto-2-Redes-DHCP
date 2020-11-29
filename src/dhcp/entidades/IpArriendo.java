package entidades;

import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * Esta clase contiene atributos y métodos de un Ip de arrendamiento
 *
 * @author Kenneth Leonel, Cristian Dacamara, Luis Montenegro, Juan Pablo Ortiz
 * @version 1.0
 */
public class IpArriendo
{

    private boolean arrendado;
    private byte[] mac;
    private byte[] ip;
    public GregorianCalendar tiempoInicio;
    public GregorianCalendar tiempoFinal;

    IpArriendo(byte[] ip, byte[] mac)
    {
        this.arrendado = false;
        this.mac = mac;
        this.ip = ip;
        this.tiempoInicio = null;
        this.tiempoFinal = null;
    }

    IpArriendo(byte[] ip)
    {
        this.arrendado = false;
        this.mac = null;
        this.ip = ip;
        this.tiempoInicio = null;
        this.tiempoFinal = null;
    }

    /**
     * @return boolean
     */
    public boolean isArrendado()
    {
        return this.arrendado;
    }

    /**
     * @return boolean
     */
    public boolean esArrendado()
    {
        return this.arrendado;
    }

    /**
     * @return boolean
     */
    public boolean getArrendado()
    {
        return this.arrendado;
    }

    /**
     * @param arrendado
     */
    public void setArrendado(boolean arrendado)
    {
        this.arrendado = arrendado;
    }

    /**
     * @return byte[]
     */
    public byte[] getMac()
    {
        return this.mac;
    }

    /**
     * @param mac
     */
    public void setMac(byte[] mac)
    {
        this.mac = mac;
    }

    /**
     * @return byte[]
     */
    public byte[] getIp()
    {
        return this.ip;
    }

    /**
     * @param ip
     */
    public void setIp(byte[] ip)
    {
        this.ip = ip;
    }

    /**
     * @return GregorianCalendar
     */
    public GregorianCalendar getTiempoInicio()
    {
        return this.tiempoInicio;
    }

    /**
     * @param tiempoInicio
     */
    public void setTiempoInicio(GregorianCalendar tiempoInicio)
    {
        this.tiempoInicio = tiempoInicio;
    }

    /**
     * @return GregorianCalendar
     */
    public GregorianCalendar getTiempoFinal()
    {
        return this.tiempoFinal;
    }

    /**
     * @param tiempoFinal
     */
    public void setTiempoFinal(GregorianCalendar tiempoFinal)
    {
        this.tiempoFinal = tiempoFinal;
    }

    /**
     * @param arrendado
     * @return IpArriendo
     */
    public IpArriendo arrendado(boolean arrendado)
    {
        this.arrendado = arrendado;
        return this;
    }

    /**
     * @param mac
     * @return IpArriendo
     */
    public IpArriendo mac(byte[] mac)
    {
        this.mac = mac;
        return this;
    }

    /**
     * @param ip
     * @return IpArriendo
     */
    public IpArriendo ip(byte[] ip)
    {
        this.ip = ip;
        return this;
    }

    /**
     * @param tiempoInicio
     * @return IpArriendo
     */
    public IpArriendo tiempoInicio(GregorianCalendar tiempoInicio)
    {
        this.tiempoInicio = tiempoInicio;
        return this;
    }

    /**
     * @param tiempoFinal
     * @return IpArriendo
     */
    public IpArriendo tiempoFinal(GregorianCalendar tiempoFinal)
    {
        this.tiempoFinal = tiempoFinal;
        return this;
    }

    /**
     * @param o
     * @return boolean
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof IpArriendo))
        {
            return false;
        }
        IpArriendo ipArriendo = (IpArriendo) o;
        return arrendado == ipArriendo.arrendado && Objects.equals(mac, ipArriendo.mac)
                && Objects.equals(ip, ipArriendo.ip) && Objects.equals(tiempoInicio, ipArriendo.tiempoInicio)
                && Objects.equals(tiempoFinal, ipArriendo.tiempoFinal);
    }

    /**
     * @return int
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(arrendado, mac, ip, tiempoInicio, tiempoFinal);
    }

    /**
     * @return String
     */
    @Override
    public String toString()
    {
        return "{" + " arrendado='" + isArrendado() + "'" + ", mac='" + getMac() + "'" + ", ip='" + getIp() + "'"
                + ", tiempoInicio='" + getTiempoInicio() + "'" + ", tiempoFinal='" + getTiempoFinal() + "'" + "}";
    }

}
