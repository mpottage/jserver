//Copyright Matthew Pottage 2016. Licensed under GNU General Public License 2.0.
/**
 * Used to make {@code ClientHandler} instances for a {@code Server}.
 * @see Server
 * @see ClientHandler
 */
public abstract class ClientFactory {
    /**
     * @return A new {@code ClientHandler} instance.
     */
    public abstract ClientHandler make();
    /**
     * Cause a {@code Server} to shut down early.
     * @return Whether any {@link Server} attached to this factory should be
     * shut down early.
     */
    public boolean requestedShutdown()
    {   return false;   }
}
