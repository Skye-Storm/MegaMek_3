/*
 * Copyright (c) 2005 - Ben Mazur (bmazur@sev.org)
 * Copyright (c) 2018-2022 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.common.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import megamek.common.annotations.Nullable;
import megamek.common.net.enums.PacketCommand;
import megamek.common.net.enums.PacketMarshallerMethod;
import megamek.common.net.marshall.AbstractPacketMarshaller;
import megamek.common.net.marshall.PacketMarshallerFactory;
import megamek.common.util.CircularIntegerBuffer;
import org.apache.logging.log4j.LogManager;

/**
 * Generic bidirectional connection between client and server
 */
public abstract class AbstractConnection {
    private static PacketMarshallerFactory marshallerFactory = PacketMarshallerFactory.getInstance();

    protected static final PacketMarshallerMethod[] PACKET_MARSHALLER_METHODS = PacketMarshallerMethod.values();

    /**
     * Peer Host Non null in case if it's a client connection
     */
    private String host;

    /**
     * Peer port != 0 in case if it's a client connection
     */
    private int port;

    /**
     * Connection state
     */
    private boolean open;

    /**
     * The socket for this connection.
     */
    private Socket socket;

    /**
     * The connection ID
     */
    private int id;

    /**
     * Bytes send during the connection lifecycle
     */
    private long bytesSent;

    /**
     * Bytes received during the connection lifecycle
     */
    private long bytesReceived;

    /**
     * Queue of <code>Packets</code> to send
     */
    private SendQueue sendQueue = new SendQueue();

    /**
     * Connection listeners list
     */
    private Vector<ConnectionListener> connectionListeners = new Vector<>();

    /**
     * Buffer of the last commands sent; Used for debugging purposes.
     */
    private CircularIntegerBuffer debugLastFewCommandsSent = new CircularIntegerBuffer(50);

    /**
     * Buffer of the last commands received; Used for debugging purposes.
     */
    private CircularIntegerBuffer debugLastFewCommandsReceived = new CircularIntegerBuffer(50);

    /**
     * Type of marshalling used to represent sent packets
     */
    protected PacketMarshallerMethod marshallingMethod;

    /**
     * Marshaller used to send packets
     */
    protected AbstractPacketMarshaller marshaller;

    /**
     * Indicates the need to compress sent data
     */
    private boolean zipData = true;

    /**
     * Creates new client (connection from client to server) connection
     *
     * @param host target host
     * @param port target port
     * @param id connection ID
     */
    public AbstractConnection(String host, int port, int id) {
        this.host = host;
        this.port = port;
        this.id = id;
        setMarshallingMethod(PacketMarshallerMethod.NATIVE_SERIALIZATION_MARSHALLING);
    }

    /**
     * Creates new Server connection
     *
     * @param socket accepted socket
     * @param id connection ID
     */
    public AbstractConnection(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
        setMarshallingMethod(PacketMarshallerMethod.NATIVE_SERIALIZATION_MARSHALLING);
    }

    /**
     * @return <code>true</code> if it's the Server connection
     */
    public boolean isServer() {
        return host == null;
    }

    /**
     * @return the method of the marshalling used to send packets
     */
    protected PacketMarshallerMethod getMarshallingMethod() {
        return marshallingMethod;
    }

    /**
     * Sets the type of the marshalling used to send packets
     *
     * @param marshallingMethod new marshalling method
     */
    protected void setMarshallingMethod(final PacketMarshallerMethod marshallingMethod) {
        this.marshallingMethod = marshallingMethod;
        marshaller = Objects.requireNonNull(marshallerFactory.getMarshaller(marshallingMethod),
                "Unimplemented marshalling type");
    }

    /**
     * Opens the connection
     *
     * @return <code>true</code> on success, <code>false</code> otherwise
     */
    public synchronized boolean open() {
        if (!open) {
            if (socket == null) {
                try {
                    socket = new Socket(host, port);
                } catch (Exception ignored) {
                    return false;
                }
            }
            open = true;
        }
        return true;
    }

    /**
     * Closes the socket and shuts down the receiver and sender threads
     */
    public void close() {
        synchronized (this) {
            LogManager.getLogger().info("Starting to shut down " + (isServer() ? "server" : "client"));
            sendQueue.reportContents();
            sendQueue.finish();
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception ex) {
                LogManager.getLogger().error("Failed closing connection " + getId(), ex);
            }
            socket = null;
        }

        processConnectionEvent(new DisconnectedEvent(this));
    }

    /**
     * @return if the socket for this connection has been closed.
     */
    public boolean isClosed() {
        return (socket == null) || socket.isClosed();
    }

    /**
     * @return the connection ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the connection ID
     *
     * @param id new connection ID
     * @note Be careful with using this method
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the address this socket is or was connected to
     */
    public String getInetAddress() {
        if (socket != null) {
            return socket.getInetAddress().toString();
        }
        return "Unknown";
    }

    /**
     * @return <code>true</code> if this connection compress the sent data
     */
    public boolean isCompressed() {
        return zipData;
    }

    /**
     * Sets the compression
     *
     * @param compress
     */
    public void setCompression(boolean compress) {
        zipData = compress;
    }

    /**
     * Adds a packet to the send queue to be sent on a separate thread.
     */
    public synchronized void send(Packet packet) {
        sendQueue.addPacket(new SendPacket(packet));
        // Send right now
        flush();
    }

    /**
     * Send the packet now, on a separate thread; This is the blocking call.
     */
    public void sendNow(SendPacket packet) {
        try {
            sendNetworkPacket(packet.getData(), packet.isCompressed());
            debugLastFewCommandsSent.push(packet.getCommand().ordinal());
        } catch (Exception ex) {
            LogManager.getLogger().error("", ex);
        }
    }

    /**
     * @return <code>true</code> if there are pending packets
     */
    public synchronized boolean hasPending() {
        return sendQueue.hasPending();
    }

    /**
     * @return a very approximate count of how many bytes were sent
     */
    public synchronized long bytesSent() {
        return bytesSent;
    }

    /**
     * @return a very approximate count of how many bytes were received
     */
    public synchronized long bytesReceived() {
        return bytesReceived;
    }

    /**
     * Adds the specified connection listener to receive connection events from connection.
     *
     * @param listener the connection listener.
     */
    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.addElement(listener);
    }

    /**
     * Removes the specified connection listener.
     *
     * @param listener the connection listener.
     */
    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.removeElement(listener);
    }

    /**
     * Reports receive exception to the <code>System.err</code>
     *
     * @param ex <code>Exception</code>
     * @param packet <code>Packet</code>
     */
    protected void reportSendException(Exception ex, SendPacket packet) {
        System.err.print(getConnectionTypeAbbreviation());
        System.err.print(" error sending command #");
        System.err.print(packet.getCommand());
        System.err.print(": ");
        System.err.println(ex.getMessage());
        reportLastCommands();
    }

    /**
     * Reports receive exception to the <code>System.err</code>
     *
     * @param ex <code>Exception</code>
     */
    protected void reportReceiveException(Exception ex) {
        StringBuffer message = new StringBuffer();
        reportReceiveException(ex, message);
        System.err.println(message);
    }

    /**
     * Appends the receive exception report to the given
     * <code>StringBuffer</code>
     *
     * @param ex <code>Exception</code>
     */
    protected void reportReceiveException(Exception ex, StringBuffer buffer) {
        System.err.print(getConnectionTypeAbbreviation());
        System.err.print(" error reading command: ");
        System.err.println(ex.getMessage());
        reportLastCommands();
    }

    /**
     * Appends the last commands sent/received to the given
     * <code>StringBuffer</code>
     */
    protected synchronized void reportLastCommands() {
        reportLastCommands(true);
        System.err.println();
        reportLastCommands(false);
        System.err.println();
        sendQueue.reportContents();
    }

    /**
     * Appends the last commands sent or received to the given
     * <code>StringBuffer</code> dependig on the <code>sent</code> parameter
     *
     * @param sent indicates which commands (sent/received) should be reported
     */
    protected void reportLastCommands(boolean sent) {
        CircularIntegerBuffer buf = sent ? debugLastFewCommandsSent
                : debugLastFewCommandsReceived;
        System.err.print("    Last ");
        System.err.print(buf.length());
        System.err.print(" commands that were ");
        System.err.print(sent ? "sent" : "received");
        System.err.print(" (oldest first): ");
        System.err.println(buf);
    }

    /**
     * @return the connection type abbreviation (client/server) that used in the debug messages and
     * so on.
     */
    protected String getConnectionTypeAbbreviation() {
        return isServer() ? "s:" : "c:";
    }

    /**
     * @return an input stream
     * @throws IOException
     */
    protected InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    /**
     * @return an output stream
     * @throws IOException
     */
    protected OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }


    protected int getSendBufferSize() throws SocketException {
        return socket.getSendBufferSize();
    }

    protected int getReceiveBufferSize() throws SocketException {
        return socket.getReceiveBufferSize();
    }

    /**
     * Process all incoming data, blocking on the input stream until new input is available. This
     * method should not be synchronized as it should only deal with the input side of things.
     * Without creating separate read/write locks, making this method synchronized would not allow
     * synchronous reads and writes.
     */
    public void update() {
        try {
            INetworkPacket np;
            while ((np = readNetworkPacket()) != null) {
                processPacket(np);
            }
        } catch (SocketException | EOFException ignored) {
            // Do nothing, happens when the socket closes
            close();
        } catch (IOException ex) {
            LogManager.getLogger().error("", ex);
            close();
        } catch (Exception ex) {
            LogManager.getLogger().error("", ex);
            reportReceiveException(ex);
            close();
        }
    }

    /**
     * Send all queued packets. This method is synchronized since it deals with the non-thread-safe
     * send queue.
     */
    public synchronized void flush() {
        SendPacket packet = null;
        try {
            while ((packet = sendQueue.getPacket()) != null) {
                processPacket(packet);
            }
        } catch (Exception ex) {
            if (packet == null) {
                LogManager.getLogger().error("Attempted to process null packet", ex);
            } else {
                reportSendException(ex, packet);
            }
            close();
        }
    }

    /**
     * process a received packet
     */
    protected void processPacket(INetworkPacket np) throws Exception {
        AbstractPacketMarshaller pm = Objects.requireNonNull(marshallerFactory.getMarshaller(np.getMarshallingMethod()),
                "Unknown marshalling type");
        byte[] data = np.getData();
        bytesReceived += data.length;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        InputStream in;
        if (np.isCompressed()) {
            in = new GZIPInputStream(bis);
        } else {
            in = bis;
        }
        Packet packet = pm.unmarshall(in);
        if (packet != null) {
            debugLastFewCommandsReceived.push(packet.getCommand().ordinal());
            processConnectionEvent(new PacketReceivedEvent(this, packet));
        }
    }

    /**
     * process a packet to be sent
     */
    protected void processPacket(SendPacket packet) throws Exception {
        sendNow(packet);
    }

    /**
     * Reads a complete <code>NetworkPacket</code>. Must not block, must return null instead
     *
     * @return the <code>NetworkPacket</code> that was sent, or null if it can't be read.
     */
    protected abstract @Nullable INetworkPacket readNetworkPacket() throws Exception;

    /**
     * Sends the data. This must not be blocked for too long
     *
     * @param data data to send
     * @param zipped should the data be compressed
     * @throws Exception if there's an issue with sending the packet
     */
    protected abstract void sendNetworkPacket(byte[] data, boolean zipped) throws Exception;

    /**
     * Wrapper around a <code>LinkedList</code> for keeping a queue of packets
     * to send. Note that this implementation is not synchronized.
     */
    protected static class SendQueue {
        private LinkedList<SendPacket> queue = new LinkedList<>();
        private boolean finished = false;

        public void addPacket(SendPacket packet) {
            queue.add(packet);
        }

        public void finish() {
            queue.clear();
            finished = true;
        }

        /**
         * Waits for a packet to appear in the queue and then returns it.
         *
         * @return the first available packet in the queue or null if none
         */
        public SendPacket getPacket() {
            return finished ? null : queue.poll();
        }

        /**
         * @return true if this connection has pending data
         */
        public boolean hasPending() {
            return !queue.isEmpty();
        }

        public void reportContents() {
            System.err.print("Contents of Send Queue: ");
            for (SendPacket p : queue) {
                System.err.print(p.command);
            }
            System.err.println();
        }
    }

    /**
     * Processes game events occurring on this connection by dispatching them to
     * any registered GameListener objects.
     *
     * @param event the game event.
     */
    protected void processConnectionEvent(ConnectionEvent event) {
        for (Enumeration<ConnectionListener> e = connectionListeners.elements(); e.hasMoreElements(); ) {
            ConnectionListener l = e.nextElement();
            switch (event.getType()) {
                case ConnectionEvent.CONNECTED:
                    l.connected((ConnectedEvent) event);
                    break;
                case ConnectionEvent.DISCONNECTED:
                    l.disconnected((DisconnectedEvent) event);
                    break;
                case ConnectionEvent.PACKET_RECEIVED:
                    l.packetReceived((PacketReceivedEvent) event);
                    break;
                default:
                    LogManager.getLogger().error("Received unknown connection event type of " + event.getType());
                    break;
            }
        }
    }

    private class SendPacket implements INetworkPacket {
        private final PacketCommand command;
        private boolean zipped = false;
        private byte[] data;

        public SendPacket(Packet packet) {
            command = packet.getCommand();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStream out;
            try {
                if (zipData && (packet.getData() != null)) {
                    out = new GZIPOutputStream(bos);
                    zipped = true;
                } else {
                    out = bos;
                }
                marshaller.marshall(packet, out);
                out.close();
                data = bos.toByteArray();
                bytesSent += data.length;
            } catch (Exception ex) {
                LogManager.getLogger().error("", ex);
            }
        }

        public PacketCommand getCommand() {
            return command;
        }

        @Override
        public PacketMarshallerMethod getMarshallingMethod() {
            return marshallingMethod;
        }

        @Override
        public boolean isCompressed() {
            return zipped;
        }

        @Override
        public byte[] getData() {
            return data;
        }
    }
}
