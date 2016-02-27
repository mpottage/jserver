abstract class ClientFactory {
    public abstract ClientHandler make();
    public boolean requestedShutdown()
    {   return false;   }
}
