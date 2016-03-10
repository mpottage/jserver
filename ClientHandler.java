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
