import java.io.*;
class DemoClient extends ClientHandler {
    DemoClient() {
        connectionNo = Integer.toString(++connectionCount);
        System.out.println("Connected "+connectionNo);
    }
    public String handle(String in) {
        if(in.startsWith("q"))
            quit = true;
        return connectionNo+in.toUpperCase();
    }
    public boolean requestedDisconnect()
    {   return quit;    }
    public void disconnected()
    {   System.out.println("Disconnected "+connectionNo);   }
    public String popMessage()
    {
        if(!shownWelcome) {
            shownWelcome = true;
            return "Welcome to the UPPERCASE server.";
        }
        else if(quit && !shownGoodbye) {
            shownGoodbye = true;
            return "Goodbye.";
        }
        else
            return null;
    }
    public static void main(String[] str) throws IOException {
        Server s = new Server(7000, new DemoFactory());
        s.run();
    }
    static int connectionCount = 0;
    private boolean quit = false;
    private String connectionNo;
    private boolean shownWelcome = false;
    private boolean shownGoodbye = false;
}
