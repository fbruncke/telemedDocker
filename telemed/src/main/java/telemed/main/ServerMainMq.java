package telemed.main;

import frds.broker.Invoker;
import frds.broker.ServerRequestHandler;
import frds.broker.ipc.mq.MqServerRequestHandler;
import saip.storage.mongo.MongoXDSAdapter;
import telemed.domain.TeleMed;
import telemed.doubles.FakeObjectXDSDatabase;
import telemed.marshall.json.TeleMedJSONInvoker;
import telemed.server.TeleMedServant;
import telemed.storage.XDSBackend;

public class ServerMainMq {

  public static void main(String[] args) throws Exception {
    // Command line argument parsing and validation
    if (args.length < 3) {
      explainAndDie();
    }
    new ServerMainMq(args[0], args[1], args[2]); // No error handling!
  }

  private static void explainAndDie() {
    System.out.println("Usage: ServerMainMq {db} {tls} {pehack}");
    System.out.println("       db = 'memory' is the in-memory db");
    System.out.println("       db = {host} is MongoDB on 'host:27017'");
    System.out.println("       tls = 'false' is default and communication is unencrypted.");
    System.out.println("       pehack = 'true'/'false'; if 'true' then client timestamp is overwritten");
    System.exit(-1);
  }

  public ServerMainMq(String databaseType, String useTlsFlag, String PEHackEnabled) {
    int port = 4567;
    // Define the server side delegates
    XDSBackend xds;
    if (databaseType.equals("memory")) {
      xds = new FakeObjectXDSDatabase();
    } else {
      xds = new MongoXDSAdapter(databaseType, 27017);
    }
    // Create the TeleMed servant
    TeleMed tsServant = new TeleMedServant(xds);
    if (PEHackEnabled.equals("true")) {
      // To avoid changing the general TeleMed
      // implementation, we use a Decorator
      // pattern to change the behavior of
      // timestamping a bit to introduce the
      // Performance Enginering hack
      tsServant = new PEHackDecorator(tsServant);
    }
    boolean useTls = useTlsFlag.equals("true");

    // Create server side implementation of Broker roles
    Invoker invoker = new TeleMedJSONInvoker(tsServant);
    ServerRequestHandler srh = new MqServerRequestHandler(invoker);
    srh.start();

    // Welcome
    System.out.println("=== TeleMed MQ based Server Request Handler (port:"
            + port + ", pehack: "+ PEHackEnabled+ ") ===");
    System.out.println(" Use ctrl-c to terminate!"); 
  }
}
