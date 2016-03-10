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
package demo;
import jserver.*;
import java.io.IOException;
public class Handler extends ClientHandler {
    Handler() {
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
        Server s = new Server(7000, new Factory());
        s.run();
    }
    static int connectionCount = 0;
    private boolean quit = false;
    private String connectionNo;
    private boolean shownWelcome = false;
    private boolean shownGoodbye = false;
}
