package auxiliares;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import entidades.RedDHCP;
import java.util.ArrayList;

public class Auxiliares
{

    /**
     * Ruta del archivo empleados.cvs
     */
    private static final String empleadosCSV = "src/dhcp/archivos/ArchivoPlano.csv";

    /**
     * Lee el arcivho empleados.csv ubicado en resources.
     *
     * @return List<String[]> Lista (lineas del csv) de arreglos de Strings
     * (columnas de cada linea csv).
     * @throws InternalServerErrorException Se levanta este error en el caso que
     * no se abra correctamente el archivo empleados.csv .
     */
    public static List<String[]> leerEmpleadosCSV()
    {
        try
        {
            CSVReader csvReader = new CSVReader(new FileReader(empleadosCSV));
            return csvReader.readAll();
        }
        catch (Exception e)
        {
            LoggerS.mensaje("Error en la lectura del archivo plano 'ArchivoPlano.csv'");
            return null;
        }
    }

    public void as()
    {
        List<String[]> datos = leerEmpleadosCSV();

        List<RedDHCP> redesDHCP = new ArrayList<>();

        byte[] rangoIzquierdo;
        byte[] rangoDerecho;
        byte[] mascara;
        byte[] gateway;
        byte[] dns;
        int leaseTime;

        for (String[] fila : datos)
        {
            rangoIzquierdo = ipStringAByte(fila[0].split("\\."));
            rangoDerecho = ipStringAByte(fila[1].split("\\."));
            mascara = ipStringAByte(fila[2].split("\\."));
            gateway = ipStringAByte(fila[3].split("\\."));
            dns = ipStringAByte(fila[4].split("\\."));
            leaseTime = Integer.parseInt(fila[5]);
            redesDHCP.add(new RedDHCP(dns, gateway, mascara, rangoDerecho, rangoDerecho, leaseTime));
        }
    }

    /**
     * @param mac1
     * @param mac2
     * @return boolean
     */
    public static boolean compararMacs(byte[] mac1, byte[] mac2)
    {
        if (mac1 == null || mac2 == null)
        {
            return false;
        }

        for (int i = 0; i < mac1.length; ++i)
        {
            if (mac1[i] != mac2[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @param ip1
     * @param ip2
     * @return boolean
     */
    public static boolean compararIps(byte[] ip1, byte[] ip2)
    {
        for (int i = 0; i < ip1.length; i++)
        {
            if (unsignedToBytes(ip1[i]) != unsignedToBytes(ip2[i]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @param b
     * @return int
     */
    public static int unsignedToBytes(byte b)
    {
        return b & 0xFF;
    }

    /**
     * @param arr
     * @return String
     */
    public static String macToString(byte[] arr)
    {
        String cadena = "";
        String separador = ":";

        for (int i = 0; i < arr.length; i++)
        {
            String conver = "" + Integer.toHexString((int) Byte.toUnsignedLong(arr[i]));
            if (conver.length() == 1)
            {
                cadena += "0" + conver;
            }
            else
            {
                cadena += conver;
            }

            if (i < arr.length - 1)
            {
                cadena += separador;
            }
        }
        return cadena;
    }

    /**
     * @param arr
     * @return String
     */
    public static String ipToString(byte[] arr)
    {
        String cadena = "";
        String separador = ".";

        for (int i = 0; i < arr.length; i++)
        {
            cadena += Integer.toString((int) Byte.toUnsignedLong(arr[i]));
            if (i < arr.length - 1)
            {
                cadena += separador;
            }
        }
        return cadena;
    }

    /**
     * @param macString
     * @return byte[]
     */
    public static byte[] macStringAByte(String[] macString)
    {
        byte[] mac = new byte[6];
        for (int j = 0; j < macString.length; j++)
        {
            mac[j] = Integer.decode("0x" + macString[j]).byteValue();
        }
        return mac;
    }

    /**
     * @param ipString
     * @return byte[]
     */
    public static byte[] ipStringAByte(String[] ipString)
    {
        byte[] ip = new byte[4];
        for (int j = 0; j < ipString.length; j++)
        {
            Integer aux = Integer.parseInt(ipString[j]);
            ip[j] = aux.byteValue();
        }
        return ip;
    }

    /**
     * @param valor
     * @return byte[]
     */
    public static byte[] shortAByte(short valor)
    {
        byte[] res = new byte[2];
        res[1] = (byte) (valor & 0xff);
        res[0] = (byte) ((valor >> 8) & 0xff);
        return res;
    }

    /**
     * @param valor
     * @return byte[]
     */
    public static byte[] shortAByteTam1(short valor)
    {
        byte[] res = new byte[1];
        res[0] = (byte) (valor);
        return res;
    }

    /**
     * @param time
     * @return String
     */
    public static String timeToString(GregorianCalendar time)
    {
        return new SimpleDateFormat("hh:mm:ss").format(time.getTime());
    }

    /**
     * @param ipRango
     * @return long
     */
    public static long ipALong(byte[] ipRango)
    {
        long resultado = 0;
        for (byte ip : ipRango)
        {
            resultado <<= 8;
            resultado |= ip & 0xff;
        }
        return resultado;
    }

}
