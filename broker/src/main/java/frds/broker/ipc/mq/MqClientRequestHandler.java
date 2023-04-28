package frds.broker.ipc.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import frds.broker.ClientRequestHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MqClientRequestHandler implements ClientRequestHandler {

  private static final String RPC_QUEUE_NAME = "broker_queue";

  public MqClientRequestHandler() {
    var factory = new ConnectionFactory();
    factory.setHost("localhost");
  }

  @Override
  public String sendToServerAndAwaitReply(String request) {
    var correlationId = UUID.randomUUID().toString();

    var factory = new ConnectionFactory();
    factory.setHost("localhost");

    try (var connection = factory.newConnection();
         var channel = connection.createChannel()) {
      var replyQueueName = channel.queueDeclare().getQueue();
      var requestProperties = new AMQP.BasicProperties.Builder()
          .correlationId(correlationId)
          .replyTo(replyQueueName)
          .build();

      channel.basicPublish("", RPC_QUEUE_NAME, requestProperties, request.getBytes(StandardCharsets.UTF_8));

      CompletableFuture<String> response = new CompletableFuture<>();
      var cTag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
        if (Objects.equals(delivery.getProperties().getCorrelationId(), correlationId)) {
          response.complete(new String(delivery.getBody(), StandardCharsets.UTF_8));
        }
      }, consumerTag -> {
      });

      var result = response.get();
      channel.basicCancel(cTag);
      return result;

    } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setServer(String hostname, int port) {

  }

  @Override
  public void close() {

  }
}
