import java.util.HashMap;

sServer sJava;
sServer sPython;
HashMap<String, sClient> ipClients = new HashMap<String, sClient>();
HashMap<String, sClient> emailClients = new HashMap<String, sClient>();
int val = 0;

void setup() {
  size(400, 200);
  
  String[] config = loadStrings("config.txt");
  try {
    config[0] = config[0];
  } catch(NullPointerException e) {
    config = new String[] {"8000", "8001"};
    saveStrings("config.txt", config);
  }
  
  // Starts a server on port 8000
  sJava = new sServer(this, int(config[0]));
  sPython = new sServer(this, int(config[1]));
  
  println(sServer.ip());
  
  noLoop();
}

void draw() {}

void clientEvent(sClient C) {
  int dataIn = C.read();
  
  // If appropriate and possible, send the 2nd byte from a python client to the right java client
  if (dataIn == 92) { // from python
    dataIn = C.read();
    String email = C.readString();
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
void serverEvent(sServer S, sClient C) {
  if (S.getPort() != 8000) return;
  ipClients.put(C.ip(), C);
  loop();
}

// Client C disconnected
void disconnectEvent(sClient C) {
  noLoop();
}
