/*
 * MegaMek - Copyright (C) 2000-2002 Ben Mazur (bmazur@sev.org)
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */

/*
 * Connection.java
 *
 * Created on March 23, 2002, 10:56 PM
 */

package megamek.server;

import java.net.*;
import java.io.*;
import java.util.*;

import megamek.common.*;

/**
 *
 * @author  Ben
 * @version 
 */
public class Connection {
    
    private Server              server;

    private Socket              socket;
    private ObjectInputStream   in;
    private ObjectOutputStream  out;

    private int                 id;

    private Thread              receiver;
    private Thread              sender;
    
    private Vector              sendQueue = new Vector();
    
    private long                bytesSent;


    public Connection(Server server, Socket socket, int id) {
        this.server = server;
        this.socket = socket;
        this.id = id;

        initThreads();
    }
    
    private void initThreads() {
        // start receiver thread
        Runnable receiverRunnable = new Runnable() {
            public void run() {
                while (receiver == Thread.currentThread()) {
                    server.handle(id, readPacket());
                }
            }
        };
        
        receiver = new Thread(receiverRunnable);
        receiver.start();
        
        // start sender thread
        Runnable senderRunable = new Runnable() {
            public void run() {
                while (sender == Thread.currentThread()) {
                    sendFromQueue();
                }
            }
        };
        
        sender = new Thread(senderRunable);
        sender.start();
    }

    public int getId() {
        return id;
    }

    /**
     * Be careful with this...
     */
    public void setId(int id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * Kill off the thread
     */
    public void die() {
        receiver = null;
        sender = null;
        try {
            synchronized (this) {
                notifyAll();
            }
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.err.print( "Error closing connection #" );
            System.err.print( getId() );
            System.err.print( ": " );
            System.err.println( e.getMessage() );
            // We don't need a full stack trace... we're
            // just closing the connection anyway.
            //e.printStackTrace();
        } catch (NullPointerException ex) {
        	; // never initialized, poor thing
        }
    }
    
    /**
     * Adds a packet to the send queue to be send on a seperate thread.
     */
    public synchronized void send(Packet packet) {
        sendQueue.addElement(packet);
        notifyAll();
    }

    /**
     * Waits for a packet to appear in the queue and then sends it.
     */
    private synchronized void sendFromQueue() {
        while (!hasPending()) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        Packet packet = (Packet)sendQueue.elementAt(0);
        sendQueue.removeElementAt(0);
        sendPacket(packet);
    }
    
    /**
     * Returns true if this connection has pending data
     */
    public boolean hasPending() {
        return sendQueue.size() > 0;
    }

    /**
     * Reads a complete net command from the given socket
     */
    private Packet readPacket() {
        try {
            if (in == null) {
                in = new ObjectInputStream(socket.getInputStream());
            }
            Packet packet = (Packet)in.readObject();
//                System.out.println("server(" + id + "): command #" + packet.getCommand() + " received.");
            return packet;
        } catch (IOException ex) {
            System.err.println("server(" + id + "): IO error reading command");
            server.disconnected(this);
            return null;
        } catch (ClassNotFoundException ex) {
            System.err.println("server(" + id + "): class not found error reading command");
            server.disconnected(this);
            return null;
        }
    }

    /**
     * Sends a packet!
     */
    private void sendPacket(Packet packet) {
        try {
            if (out == null) {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
            }
            out.reset(); // write each packet fresh
            out.writeObject(packet);
            out.flush();
            bytesSent += packet.size();
//            System.out.println("server(" + id + "): command #" + packet.getCommand() + " sent with " + packet.getData().length + " data");
        } catch(IOException ex) {
            System.err.println("server(" + id + "): error sending command.  dropping player");
            System.err.println(ex);
            System.err.println(ex.getMessage());
            server.disconnected(this);
        }
    }
    
    /**
     * Returns a very approximate count of how many bytes were sent to this
     * player.
     */
    public long bytesSent() {
        return bytesSent;
    }
}
