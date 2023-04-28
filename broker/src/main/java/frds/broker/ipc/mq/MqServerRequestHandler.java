package frds.broker.ipc.mq;

import com.rabbitmq.client.*;
import frds.broker.Invoker;
import frds.broker.ServerRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class MqServerRequestHandler implements ServerRequestHandler {

  private static final String RPC_QUEUE_NAME = "broker_queue";

  private final Logger logger;
  private final Connection connection;
  private final Channel channel;
  private Invoker invoker;

  public MqServerRequestHandler(Invoker invoker) {
    logger = LoggerFactory.getLogger(MqServerRequestHandler.class);
    this.invoker = invoker;

    var factory = new ConnectionFactory();
    factory.setHost("localhost");

    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void start() {
    var factory = new ConnectionFactory();
    factory.setHost("localhost");

    try {
      channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
      channel.basicQos(1);

      DeliverCallback deliveryCallback = (consumerTag, delivery) -> {
        var replyProperties = new AMQP.BasicProperties.Builder()
            .correlationId(delivery.getProperties().getCorrelationId())
            .build();

        var request = new String(delivery.getBody(), StandardCharsets.UTF_8);
        logger.info("request={}", request);

        var response = invoker.handleRequest(request);
        logger.info("response={}", response);

        channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProperties, response.getBytes(StandardCharsets.UTF_8));
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };

      channel.basicConsume(RPC_QUEUE_NAME, false, deliveryCallback, consumerTag -> {});
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    try {
      channel.close();
      connection.close();
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setPortAndInvoker(int port, Invoker invoker) {
    this.invoker = invoker;
  }
}
