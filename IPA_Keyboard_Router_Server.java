/* autogenerated by Processing revision 1293 on 2024-06-11 */
/* modified by Rowan Ackerman thereafter */
/**(c) 2024 by Rowan Ackerman
 * All parts of this and other files in this repository not autogenerated
 * or in their current form a part of the Processing library are under
 * the copyright of Rowan Ackerman.
*/

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class IPA_Keyboard_Router_Server {

public static final long DELAY_HOURS = 1;
public static final long DELAY_MILLIS = 1000 * 60 * 60 * DELAY_HOURS;
// public static final long DELAY_MILLIS = 1000 * 60 * 2;

sServer sJava;
sServer sPython;
HashMap<String, sClient> clients = new HashMap<String, sClient>();

Random rand = new Random();

Timer timer = new Timer();
TimerTask removeUnused = new TimerTask() {
  @Override
  public void run() {
    kickAndUpdate(clients);
  }

  private void kickAndUpdate(HashMap<String, sClient> clients) {
    for (Map.Entry<String, sClient> clientMap : clients.entrySet()) {
      sClient client = clientMap.getValue();
      String key = clientMap.getKey();
      if (client.timeoutNextHour) {
        clients.remove(key);
        client.write(253); //timedout
        println("Kicked " + key);
        client = null;
        return;
      }
      client.timeoutNextHour = true;
    }
  }
};

public void setup() {
  // Starts a server on port 8000 to connect to the Clients
  sJava = new sServer(8000, C -> clientEvent(C));

  // Starts a server on port 8001 to connect to the Python server
  sPython = new sServer(8001, C -> backendEvent(C));
  
  println("Server started on " + sServer.ip());

  timer.schedule(removeUnused, DELAY_MILLIS, DELAY_MILLIS);
}

public void draw() {
  throw new Error();
}

public int clientEvent(sClient C) {
  int dataIn = C.read();
  String linkingKey;
  String key;

  switch (dataIn) {
    // If appropriate and possible, add sClient C to keyClients
    case 96: // old client connect
      linkingKey = C.readString();
      println(linkingKey + " for linking to " + C.ip());
      if (clients.containsKey(linkingKey)) //linkingKey exists
        if (!clients.get(linkingKey).activationTimedout()) //and isn't timed out
          return 251; //alert client
      clients.put(linkingKey, C);
      return 254; //success
    
    case 0: // new client connect / renew
      linkingKey = C.readString(6);
      key = C.readString();
      println(linkingKey + " for linking to " + key);
      if (clients.containsKey(linkingKey)) //linkingKey exists
        if (!clients.get(linkingKey).activationTimedout()) //and isn't timed out
          return 251; //alert client
      C.key = key;
      clients.put(linkingKey, C);
      if (!clients.containsKey(key)) // don't overwrite an existing entry with that key
        clients.put(key, C);
      return 254; //success
  }

  return 255; //unknown error
}

public int backendEvent(sClient B) {
  int dataIn = B.read();
  String key;

  switch (dataIn) {
    case 0: // linking
      String linkingKey = B.readString();
      if (!clients.containsKey(linkingKey)) return 1; //nokey
      if (clients.get(linkingKey).activationTimedout()) {
        clients.remove(linkingKey);
        return 3; //expired
      }
      if (!clients.get(linkingKey).write(252)) {
        clients.remove(linkingKey);
        return 2; //noclient
      }
      key = clients.get(linkingKey).key;
      if (key == null) { //must have been an old client
        key = rand64Str(18);
        clients.put(key, clients.remove(linkingKey));
        println(key + " linked to " + clients.get(key).ip());
      }
      else //must have been a new client
        clients.remove(linkingKey); //don't need to add it, since it was already added or extant
      B.write(key);
      return 0; //success

    // If appropriate and possible, send the 2nd byte from the backend to the right client
    case 1: // sending a key
      dataIn = B.read();
      key = B.readString();
      println(key + " sent " + dataIn);
      if (!clients.containsKey(key)) return 1; //nokey
      if (clients.get(key).write(dataIn)) return 0; //success
      clients.remove(key); return 2; //noclient
  }

  return 255; //unknown error
}

  static public void main(String[] passedArgs) {
    runSketch();
  }

  // Modified from the Processing source code
  static void runSketch() {
    final String name = "IPA_Keyboard_Router_Server";
    
    final IPA_Keyboard_Router_Server sketch;
    try {
      Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(name);
      sketch = (IPA_Keyboard_Router_Server) c.getDeclaredConstructor().newInstance();
    } catch (RuntimeException re) {
      // Don't re-package runtime exceptions
      throw re;
    } catch (Exception e) {
      // Package non-runtime exceptions so we can throw them freely
      throw new RuntimeException(e);
    }

    sketch.setup();
  }

  static void println(String str) {
    final Date currentTime = new Date();

    final SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss\t");

    // Give it to me in GMT time.
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    System.out.println(sdf.format(currentTime) + str);
  }

  String rand64Str(int length) {
    final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
    String out = "";
    for (int i = 0; i < length; i++) {
      out += ALPHABET[rand.nextInt(64)];
    }
    return out;
  }
}
