package telemed.main;

import frds.broker.ClientRequestHandler;
import frds.broker.ipc.mq.MqClientRequestHandler;

import java.io.IOException;

public class HomeClientMq extends HomeClientTemplate {
  public HomeClientMq(String[] args, int port) {
    super(args, port);
  }

  @Override
  public ClientRequestHandler createClientRequestHandler(String hostname, int port, boolean useTLS) {
    return new MqClientRequestHandler();
  }

  public static void main(String[] args) throws IOException {
    new HomeClientMq(args, 37321);
  }

}
