/**
 * Used to manage connections to clients (ignoring IO issues).
 * Input/output and message transmission are handled by {@code Server}.
 * @see Server
 */
public abstract class ClientHandler {
    /**
     * Handle input from the client.
     * @param in Last input sent from the remote client to the server.
     * @return String to send back to the remote client.
     */
    public abstract String handle(String in);
    /**
     * Send messages to the client, without recieving input from the client.
     * @return Message (if any) to send to the client. Value null means no message.
     */
    public String popMessage()
    {   return null;    }
    /**
     * Disconnect from the client early.
     * @return Whether to disconnect early.
     */
    public boolean requestedDisconnect()
    {   return false;   }
    /**
     * Called after the client has disconnected.
     */
    public void disconnected() {}
};
