/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Server - basic network server implementation
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-2007 Ben Fry and Casey Reas
  The previous version of this code was developed by Hernando Barragan

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.function.Function;


/**
 *
 * A server sends and receives data to and from its associated clients
 * (other programs connected to it). When a server is started, it begins
 * listening for connections on the port specified by the <b>port</b>
 * parameter. Computers have many ports for transferring data and some are
 * commonly used so be sure to not select one of these. For example, web
 * servers usually use port 80 and POP mail uses port 110.
 *
 * @webref server
 * @usage application
 * @webBrief The server class is used to create server objects which send
 * and receives data to and from its associated clients (other programs connected to it)
 * @instanceName server    any variable of type Server
 */
public class sServer implements Runnable {
  Function<sClient, Integer> clientEvent;

  volatile Thread thread;
  ServerSocket server;
  int port;

  protected final Object clientsLock = new Object[0];
  /** Number of clients currently connected. */
  public int clientCount;
  /** Array of client objects, useful length is determined by clientCount. */
  public sClient[] clients;


  /**
   * @param parent typically use "this"
   * @param port port used to transfer data
   */
  public sServer(int port, Function<sClient, Integer> clientEvent) {
    this(port, null, clientEvent);
  }


  /**
   * @param parent typically use "this"
   * @param port port used to transfer data
   * @param host when multiple NICs are in use, the ip (or name) to bind from
   */
  public sServer(int port, String host, Function<sClient, Integer> clientEvent) {
    this.port = port;
    this.clientEvent = clientEvent;

    try {
      if (host == null) {
        server = new ServerSocket(this.port);
      } else {
        server = new ServerSocket(this.port, 10, InetAddress.getByName(host));
      }
      clients = new sClient[10];

      thread = new Thread(this);
      thread.start();
    } catch (IOException e) {
      thread = null;
      throw new RuntimeException(e);
    }
  }


  /**
   *
   * Disconnect a particular client.
   *
   * @webref server
   * @webBrief Disconnect a particular client
   * @param client the client to disconnect
   */
  public void disconnect(sClient client) {
    client.stop();
    synchronized (clientsLock) {
      int index = clientIndex(client);
      if (index != -1) {
        removeIndex(index);
      }
    }
  }


  protected void removeIndex(int index) {
    synchronized (clientsLock) {
      clientCount--;
      // shift down the remaining clients
      for (int i = index; i < clientCount; i++) {
        clients[i] = clients[i + 1];
      }
      // mark last empty var for garbage collection
      clients[clientCount] = null;
    }
  }


  protected void disconnectAll() {
    synchronized (clientsLock) {
      for (int i = 0; i < clientCount; i++) {
        try {
          clients[i].stop();
        } catch (Exception e) {
          // ignore
        }
        clients[i] = null;
      }
      clientCount = 0;
    }
  }


  protected void addClient(sClient client) {
    synchronized (clientsLock) {
      if (clientCount == clients.length) {
        clients = (sClient[]) expand(clients);
      }
      clients[clientCount++] = client;
    }
  }
   /**
  * @nowebref
  */
  static public Object expand(Object list) {
    int len = Array.getLength(list);
    int newSize = len > 0 ? len << 1 : 1;
    Class<?> type = list.getClass().getComponentType();
    Object temp = Array.newInstance(type, newSize);
    System.arraycopy(list, 0, temp, 0,
                     Math.min(Array.getLength(list), newSize));
    return temp;
  }


  protected int clientIndex(sClient client) {
    synchronized (clientsLock) {
      for (int i = 0; i < clientCount; i++) {
        if (clients[i] == client) {
          return i;
        }
      }
      return -1;
    }
  }


  /**
   *
   * Returns <b>true</b> if this server is still active and hasn't run
   * into any trouble.
   *
   * @webref server
   * @webBrief Return <b>true</b> if this server is still active
   */
  public boolean active() {
    return thread != null;
  }


  static public String ip() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return null;
    }
  }

  //Written by Rowan Ackerman
  /**
   *
   * Returns the port on which the Server is listening.
   *
   * @webref server
   * @usage application
   * @webBrief Returns the port of the Server as an <b>int</b>
   */
  public int getPort() {
    return port;
  }


  // the last index used for available. can't just cycle through
  // the clients in order from 0 each time, because if client 0 won't
  // shut up, then the rest of the clients will never be heard from.
  int lastAvailable = -1;

  /**
   *
   * Returns the next client in line with a new message.
   *
   * @webref server
   * @webBrief Returns the next client in line with a new message
   * @usage application
   */
  public sClient available() {
    synchronized (clientsLock) {
      int index = lastAvailable + 1;
      if (index >= clientCount) index = 0;

      for (int i = 0; i < clientCount; i++) {
        int which = (index + i) % clientCount;
        sClient client = clients[which];
        //Check for valid client
        if (!client.active()){
          removeIndex(which);  //Remove dead client
          i--;                 //Don't skip the next client
          //If the client has data make sure lastAvailable
          //doesn't end up skipping the next client
          which--;
          //fall through to allow data from dead clients
          //to be retreived.
        }
        if (client.available() > 0) {
          lastAvailable = which;
          return client;
        }
      }
    }
    return null;
  }


  /**
   *
   * Disconnects all clients and stops the server.
   *
   * <h3>Advanced</h3>
   * Use this to shut down the server if you finish using it while your sketch
   * is still running. Otherwise, it will be automatically be shut down by the
   * host PApplet using dispose(), which is identical.
   * @webref server
   * @webBrief Disconnects all clients and stops the server
   * @usage application
   */
  public void stop() {
    dispose();
  }


  /**
   * Disconnect all clients and stop the server: internal use only.
   */
  public void dispose() {
    thread = null;

    if (clients != null) {
      disconnectAll();
      clientCount = 0;
      clients = null;
    }

    try {
      if (server != null) {
        server.close();
        server = null;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  @Override
  public void run() {
    while (Thread.currentThread() == thread) {
      try {
        Socket socket = server.accept();
        sClient client = new sClient(socket, clientEvent);
        synchronized (clientsLock) {
          addClient(client);
        }
      } catch (SocketException e) {
        //thrown when server.close() is called and server is waiting on accept
        System.err.println("Server SocketException: " + e.getMessage());
        thread = null;
      } catch (IOException e) {
        e.printStackTrace();
        thread = null;
      }
    }
  }


  /**
   *
   * Writes a value to all the connected clients. It sends bytes out from the
   * Server object.
   *
   * @webref server
   * @webBrief Writes data to all connected clients
   * @param data data to write
   */
  public void write(int data) {  // will also cover char
    synchronized (clientsLock) {
      int index = 0;
      while (index < clientCount) {
        if (clients[index].active()) {
          clients[index].write(data);
          index++;
        } else {
          removeIndex(index);
        }
      }
    }
  }


  public void write(byte data[]) {
    synchronized (clientsLock) {
      int index = 0;
      while (index < clientCount) {
        if (clients[index].active()) {
          clients[index].write(data);
          index++;
        } else {
          removeIndex(index);
        }
      }
    }
  }


  public void write(String data) {
    synchronized (clientsLock) {
      int index = 0;
      while (index < clientCount) {
        if (clients[index].active()) {
          clients[index].write(data);
          index++;
        } else {
          removeIndex(index);
        }
      }
    }
  }
}
