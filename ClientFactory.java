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
