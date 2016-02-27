import java.net.*;
import java.lang.Exception.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.*;
class Server implements Runnable {
    public Server(int port, ClientFactory cf) throws IOException
    {
        internalSocket = new ServerSocket(port);
        factory = cf;
    }
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
            shutdown();
        }
    }
    public synchronized void shutdown() {
        try {
            internalSocket.close();
        } catch(IOException ie) {}
        for(ClientIO client : clients)
            client.disconnect();
        threads.shutdown();
        serverShutdown = true;
    }
    private class ClientIO implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private ClientHandler handler;
        private boolean disconnected = false;
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
            while(!disconnected)
            try {
                String user = in.readLine();
                if(user!=null) {
                    synchronized(this) {
                        String res = handler.handle(user);
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
        public synchronized void maintain() {
            if(!disconnected && handler.requestedDisconnect())
                disconnect();
        }
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
