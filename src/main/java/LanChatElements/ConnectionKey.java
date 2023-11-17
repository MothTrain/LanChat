package LanChatElements;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.Inet4Address;

/**
 * An instance of the Connection Key is used to convey the information
 * required to make a new connection and can express the information in
 * condensed formats to be more user-friendly.
 */
public class ConnectionKey {
    
    /**
     * The port that the listening node is listening on
     */
    public final int port;
    
    /**
     * The IPv4 address of the listening node
     *
     * @see Inet4Address
     */
    public final String IP;
    
    /**
     * Holds a dash ({@code "-"}) delineated string composed of 2 sections.
     * The first section contains the last octet of the listening node's
     * IPv4 address converted to base 36. The second section is the port
     * of the listening node in base-36. <br>
     * Because the key only contains the last octet of the IPv4 address, which
     * is the only octet that changes on a <b>simple</b> LAN network, the
     * {@link #uncondensedConnectionKey} may need to be used, which contains
     * all 4 octets of the IPv4 address
     *
     * @see #toBase36(String)
     * @see Inet4Address
     */
    public final String ConnectionKey;
    
    /**
     * Holds a dash ({@code "-"}) delineated string composed of 5
     * sections. The first 4 sections are the 4 octets of the IPv4
     * address of the listening node in base-36. The fifth section
     * is the port of the listening node in base-36
     *
     * @see #toBase36(String)
     * @see Inet4Address
     */
    public final String uncondensedConnectionKey;
    
    /**
     * Creates a connection key object including the connection key
     * strings from an IPv4 and port of the listening node
     *
     * @param port Port of the listening node
     * @param ip IPv4 of the listening node
     *
     * @see Inet4Address
     */
    public ConnectionKey(int port, String ip) {
        this.port = port;
        IP = ip;
        
        ConnectionKey =
                toBase36(ip.split("\\.")[3]) +
                "-" +
                toBase36(String.valueOf(port));
        
        
        StringBuilder uncondensedConnectionKey = new StringBuilder();
        
        for (String i : ip.split("\\.")) {
            uncondensedConnectionKey.append(toBase36(i));
            uncondensedConnectionKey.append("-");
        }
        uncondensedConnectionKey.append
                (toBase36(
                        String.valueOf(port)));
        
        this.uncondensedConnectionKey = uncondensedConnectionKey.toString();
    }
    
    /**
     * Creates a connection key from a connection key string (accepting both
     * connection key formats) and deduces the IPv4 address and port from the
     * key. <br>
     * In the case that the key is in the {@link #ConnectionKey condensed}
     * form, the method will deduce the first 3 octets of the IPv4 address
     * using the first 3 octets of the executing computer's IP address. The
     * deduction of the computer's IP may result in the throwing of an IOException
     * and on larger/more complex networks, the first 3 octets may vary even on
     * the same LAN network so the {@link #uncondensedConnectionKey uncondensed}
     * form may have to be used.
     *
     * @param connectionKey The connection key of the node
     * @throws IOException If an IOException occurs, while deducing the computer's IP
     *
     * @see #ConnectionKey
     * @see #uncondensedConnectionKey
     * @see Inet4Address
     */
    public ConnectionKey(String connectionKey) throws IOException {
        String[] splitKey = connectionKey.split("-");
        ConnectionKey key;
        
        if (splitKey.length == 5) {
            StringBuilder IP = new StringBuilder();
            
            for (int i = 0; i < 4; i++) {
                IP.append(toBase10(splitKey[i]));
                IP.append(".");
            }
            IP.deleteCharAt(IP.length()-1);
            
            int port = Integer.parseInt(toBase10(splitKey[4]));
            
            key = new ConnectionKey(port, IP.toString());
            
        } else if (splitKey.length == 2) {
            String[] selfIP = getSelfIP().split("\\.");
            StringBuilder IP = new StringBuilder();
            
            for (int i=0; i<3; i++) {
                IP.append(selfIP[i]);
                IP.append(".");
            }
            IP.append(toBase10(splitKey[0]));
            
            int port = Integer.parseInt(
                    toBase10(splitKey[1]));
            
            key = new ConnectionKey(port, IP.toString());
        } else {throw new IllegalArgumentException("Malformed connection key");}
        
        this.IP = key.IP;
        this.port = key.port;
        this.ConnectionKey = key.ConnectionKey;
        this.uncondensedConnectionKey = key.uncondensedConnectionKey;
    }
    
    /**
     * Creates a Connection key with just a port. The IPv4 address is
     * decided by the IPv4 of the executing computer
     *
     * @param port The port of the listening node
     * @throws IOException If an IOException occurs while deducing the
     * IP address
     *
     * @see Inet4Address
     */
    public ConnectionKey(int port) throws IOException {
        ConnectionKey key = new ConnectionKey(port, getSelfIP());
        
        this.IP = key.IP;
        this.port = key.port;
        this.ConnectionKey = key.ConnectionKey;
        this.uncondensedConnectionKey = key.uncondensedConnectionKey;
    }
    
    /**
     * Converts a string containing a decimal number to a string of a
     * base-36 number in the form of 0-9,A-Z
     *
     * @param decimalNumber A base-10 number
     * @return The base-36 form of the input
     */
    static private String toBase36(String decimalNumber) {
        BigInteger decimal = new BigInteger(decimalNumber);
        
        final String base36Chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        StringBuilder base36 = new StringBuilder();
        
        while (decimal.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] quotientAndRemainder = decimal.divideAndRemainder(BigInteger.valueOf(36));
            BigInteger quotient = quotientAndRemainder[0];
            BigInteger remainder = quotientAndRemainder[1];
            
            base36.insert(0, base36Chars.charAt(remainder.intValue()));
            
            decimal = quotient;
        }
        
        return base36.toString();
    }
    
    /**
     * Converts a string of a base-36 number to a string of a base-10 number
     *
     * @param base36Number A base-36 number
     * @return The base-19 form of the input
     *
     * @see #toBase36(String)
     */
    static private String toBase10(String base36Number) {
        final String base36Chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        String reversedInput = new StringBuilder(base36Number).reverse().toString();
        
        int decimalValue = 0;
        int base = 1;
        
        for (char c : reversedInput.toCharArray()) {
            int charValue = base36Chars.indexOf(c);
            decimalValue += charValue * base;
            base *= 36;
        }
        
        return Integer.toString(decimalValue);
    }
    
    /**
     * Gets the local IPv4 address of the machine using a {@code socket}
     *
     * @return The IPv4 address of the machine
     * @throws IOException If an IOException is thrown during execution
     *
     * @see Inet4Address
     * @see DatagramSocket
     */
    static private String getSelfIP() throws IOException {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(Inet4Address.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        }
    }
    
    /**
     * Return a random port in the range between 916-952, an
     * unreserved series of ports
     *
     * @return A random port
     */
    static public int getRandomPort() {
        return (int) Math.floor((Math.random()*(952-916))+916);
    }
}