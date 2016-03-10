/* TCP Server.
 * Copyright (C) 2016  Matthew Pottage
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; at version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package jserver;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.ArrayList;
/**
 * A flexible TCP Server supporting multiple clients, but without encryption.
 * <p>
 * Higher-level interaction with clients is done by {@link ClientHandler}
 * instances (one for each connection); which are generated by the instance of
 * {@link ClientFactory} supplied in the {@link #Server(int, ClientFactory)
 * constructor}.
 * <p>
 * Text recieved from a client is forwarded to the {@code ClientHandler}
 * instance attached to the connection, by calling {@link
 * ClientHandler#handle(String)}.  It can then process the input and optionally
 * return a response to the client.
 * <p>
 * Messages can be pushed to a client at any time via {@link
 * ClientHandler#popMessage}.
 * <p>
 * There are multiple ways to have a connection to a client closed:
 * <ul>
 * <li>{@link ClientHandler#requestedDisconnect} returns {@code true} for the
 * attached instance.
 * <li>The connection to the client is lost.
 * <li>The {@code Server} is shut down.
 * </ul>
 * <p>
 * When a client is disconnected {@link ClientHandler#disconnected} is called on
 * the associated {@code ClientHandler}.
 * <p>
 * The server will ensure that no two threads simultaneously access the same
 * instance of a {@code ClientHandler}.
 * It is assumed though that different {@code ClientHandler} instances may be
 * safely accessed simultaneously from different threads. This means that if
 * there is any shared mutable state between instances, then sychronizing access
 * to the shared state is necessary.
 * @see ClientHandler
 * @see ClientFactory
 */
public class Server implements Runnable {
    /**
     * @param port Port to run the server on. If 0 a random port is chosen.
     * @param cf   Factory to make {@link ClientHandler}s. Each active connection is
     * assigned a handler, to push messages, request disconnection and handle
     * input from the client.
     * @throws IOException When creating the socket to run the server on fails.
     */
    public Server(int port, ClientFactory cf) throws IOException
    {
        internalSocket = new ServerSocket(port);
        factory = cf;
    }
    /**
     * Start and run the server.
     * <p>
     * The server accepts new connections, and calls {@link
     * ClientFactory#make()} to get a {@link ClientHandler} for each. Any input
     * from the connection is forwarded to the specific {@link
     * ClientHandler#handle(String)}, which can then return output to be sent to
     * the client.
     * <p>
     * Output for the client can also be sent at any time via {@link
     * ClientHandler#popMessage}. This is given priority over any request to end
     * the connection (so a goodbye can be sent correctly) via {@link
     * ClientHandler#requestedDisconnect()}.
     * <p>
     * To stop the server, call {@link #shutdown}.
     * @see ClientHandler
     */
    public void run() {
        //Maintain clients (disconnect and push messages).
        threads.execute(new Maintenance());
        //Recieve connections.
        while(!serverShutdown)
        try {
            Socket newCon = internalSocket.accept();
            synchronized(this) {
                if(!serverShutdown) {
                    ManageClient mc = new ManageClient(factory.make(), newCon);
                    clients.add(mc);
                    threads.execute(mc);
                }
                else
                    newCon.close();
            }
        } catch(IOException ie) {
            shutdown(); //Unable to acquire new sockets, so stop the server.
        } catch(RuntimeException e) {
            shutdown();
            throw e;
        }
    }
    /**
     * Stops the server.
     * This terminates any active connections and cannot be undone.
     */
    public synchronized void shutdown() {
        try {
            internalSocket.close();
        } catch(IOException ie) {}
        for(ManageClient client : clients)
            client.disconnect(); //Let all clients know.
        threads.shutdown(); //Don't add any more threads.
        serverShutdown = true;
    }
    private static class ManageClient implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private ClientHandler handler;
        private boolean disconnected = false;
        // h - Server object managing the client connection.
        // s - Socket to the client.
        // Note: If setup fails, the connection is closed.
        public ManageClient(ClientHandler h, Socket s) {
            handler = h;
            socket  = s;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch(IOException ie) {
                disconnect(); //setup failed.
            }
        }
        public void run() {
            //Note: If recieving input fails, the connection is closed.
            while(!disconnected)
            try {
                maintain();
                String user = in.readLine();
                if(user!=null) {
                    synchronized(this) {
                        String res = handler.handle(user);
                        if(res!=null)
                            out.println(res);
                    }
                }
                else
                    disconnect(); //End of input.
            }
            catch(IOException ie) {
                disconnect(); //Connection lost.
            }
            catch(RuntimeException re) { //handler raised exception.
                disconnect(); //Handler failed, prevent the client from hanging.
                throw re;
            }
        }
        // Pushes any messages from the handler to the client, then disconnects
        //  if the handler requests it.
        public synchronized void maintain() {
            String msg = handler.popMessage();
            while(msg!=null) {
                out.println(msg);
                msg = handler.popMessage();
            }
            if(!disconnected && handler.requestedDisconnect())
                disconnect();
        }
        // Disconnect early from the client.
        public synchronized void disconnect() {
            if(!disconnected) {
                try {
                    socket.close();
                    handler.disconnected();
                } catch(IOException ie) {
                } catch(RuntimeException e) { //Handler failed.
                    e.printStackTrace();
                }
                disconnected = true;
            }
        }
        public boolean isDisconnected()
        {   return disconnected;    }
    }
    class Maintenance implements Runnable {
        public void run() {
            while(!serverShutdown) {
                synchronized(Server.this) {
                    for(int i=0; i<clients.size(); ++i) {
                        ManageClient mc = clients.get(i);
                        if(mc.isDisconnected()) {
                            clients.remove(i);
                            --i;
                        }
                        else
                            mc.maintain();
                    }
                    if(factory.requestedShutdown())
                        shutdown();
                }
            }
        }
    }
    private ServerSocket internalSocket;
    private ClientFactory factory;
    private ArrayList<ManageClient> clients = new ArrayList<ManageClient>();
    private ExecutorService threads = Executors.newCachedThreadPool();;
    private boolean serverShutdown = false;
}