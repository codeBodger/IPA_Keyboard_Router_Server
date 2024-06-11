/* autogenerated by Processing revision 1293 on 2024-06-11 */
// import processing.core.*;
// import processing.data.*;
// import processing.event.*;
// import processing.opengl.*;

import java.util.HashMap;

// import java.util.HashMap;
// import java.util.ArrayList;
// import java.io.File;
// import java.io.BufferedReader;
// import java.io.PrintWriter;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.io.IOException;

public class IPA_Keyboard_Router_Server /* extends PApplet */ {



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
  
  System.out.println(sServer.ip());
  
  // noLoop();
}

public void draw() {
  throw new Error();
}

public void clientEvent(sClient C) {
  int dataIn = C.read();
  System.out.println(dataIn);

  // If appropriate and possible, send the 2nd byte from a python client to the right java client
  if (dataIn == 92) { // from python
    dataIn = C.read();
    String email = C.readString();
    System.out.println(email);
    if (emailClients.containsKey(email)) {
      emailClients.get(email).write(dataIn);
    }
  }
  
  // If appropriate and possible, move sClient C from ipClients to emailClients
  if (dataIn == 96) { // from java
    if (ipClients.containsKey(C.ip())) {
      emailClients.put(C.readString(), ipClients.get(C.ip()));
      ipClients.remove(C.ip());
    }
  }
}

// New Client C just connected to Server S
public void serverEvent(sServer S, sClient C) {
  if (S.getPort() != 8000) return;
  ipClients.put(C.ip(), C);
  // loop();
}

// Client C disconnected
// public void disconnectEvent(sClient C) {
//   noLoop();
// }


  // public void settings() { size(400, 200); }

  static public void main(String[] passedArgs) {
    // PApplet.runSketch(new String[] { "IPA_Keyboard_Router_Server" }, null);
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
}
