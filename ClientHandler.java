abstract class ClientHandler {
    public abstract String handle(String in);
    //public String popMessage();
    public boolean requestedDisconnect()
    {   return false;   }
    public String disconnectMessage()
    {   return "";  }
    public void disconnected() {}
};
