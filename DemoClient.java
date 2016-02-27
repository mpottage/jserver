import java.io.*;
class DemoClient extends ClientHandler {
    DemoClient() {
        connectionNo = Integer.toString(++connectionCount);
    }
    public String handle(String in)
    {
        if(in.startsWith("q"))
            quit = true;
        return connectionNo+in.toUpperCase();
    }
    public boolean requestedDisconnect()
    {   return quit;    }
    public static void main(String[] str) throws IOException {
        Server s = new Server(7000, new DemoFactory());
        s.run();
    }
    static int connectionCount = 0;
    private boolean quit = false;
    private String connectionNo;
}
