/* autogenerated by Processing revision 1293 on 2024-06-11 */
/* modified by Rowan Ackerman thereafter */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class IPA_Keyboard_Router_Server {



sServer sJava;
sServer sPython;
HashMap<String, sClient> ipClients = new HashMap<String, sClient>();
HashMap<String, sClient> emailClients = new HashMap<String, sClient>();
int val = 0;

public void setup() {
  // Starts a server on port 8000 to connect to the Clients
  sJava = new sServer(this, 8000);

  // Starts a server on port 8001 to connect to the Python server
  sPython = new sServer(this, 8001);
  
  println("Server started on " + sServer.ip());
}

public void draw() {
  throw new Error();
}

public void clientEvent(sClient C) {
  int dataIn = C.read();

  // If appropriate and possible, send the 2nd byte from a python client to the right java client
  if (dataIn == 92) { // from python
    dataIn = C.read();
    String email = C.readString();
    println(email + " sent " + dataIn);
    if (emailClients.containsKey(email)) {
      emailClients.get(email).write(dataIn);
    }
  }
  
  // If appropriate and possible, move sClient C from ipClients to emailClients
  if (dataIn == 96) { // from java
    if (ipClients.containsKey(C.ip())) {
      String email = C.readString();
      println(email + " linked to " + C.ip());
      emailClients.put(email, ipClients.get(C.ip()));
      ipClients.remove(C.ip());
    }
  }
}

// New Client C just connected to Server S
public void serverEvent(sServer S, sClient C) {
  if (S.getPort() != 8000) return;
  ipClients.put(C.ip(), C);
  println(C.ip() + " connected");
}

  static public void main(String[] passedArgs) {
    runSketch();
  }

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
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

    // Give it to me in GMT time.
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    System.out.println(sdf.format(currentTime) + str);
  }
}
