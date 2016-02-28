//Copyright Matthew Pottage 2016. Licensed under GNU General Public License 2.0.
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.ArrayList;
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
        threads.execute(new MaintainClients(this));
        while(!serverShutdown)
        try {
            Socket newCon = internalSocket.accept();
            synchronized(this) {
                if(!serverShutdown) {
                    ClientIO cio = new ClientIO(factory.make(), newCon);
                    clients.add(cio);
                    threads.execute(cio);
                }
                else
                    newCon.close();
            }
        } catch(IOException ie) {
            shutdown(); //Unable to acquire new sockets, so stop the server.
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
        for(ClientIO client : clients)
            client.disconnect(); //Let all clients know.
        threads.shutdown(); //Don't add any more threads.
        serverShutdown = true;
    }
    private class ClientIO implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private ClientHandler handler;
        private boolean disconnected = false;
        // h - Server object managing the client connection.
        // s - Socket to the client.
        // Note: If setup fails, the connection is closed.
        public ClientIO(ClientHandler h, Socket s) {
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
                } catch(IOException ie) {}
                handler.disconnected();
                disconnected = true;
            }
        }
        public boolean isDisconnected()
        {   return disconnected;    }
    }
    private class MaintainClients implements Runnable {
        private Server server;
        public MaintainClients(Server s)
        {   server = s;    }
        public void run() {
            while(!server.serverShutdown) {
                synchronized(server) {
                    for(ClientIO cio : server.clients) {
                        if(!cio.isDisconnected())
                            cio.maintain();
                    }
                }
                if(server.factory.requestedShutdown())
                    server.shutdown();
            }
        }
    }
    private ServerSocket internalSocket;
    private ClientFactory factory;
    private ArrayList<ClientIO> clients = new ArrayList<ClientIO>();
    private ExecutorService threads = Executors.newCachedThreadPool();;
    private boolean serverShutdown = false;
}
