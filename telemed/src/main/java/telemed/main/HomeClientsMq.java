package telemed.main;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class HomeClientsMq extends TimerTask {
  public static void main(String[] args) throws IOException {
    new Timer("HomeClientsMq")
        .scheduleAtFixedRate(new HomeClientsMq(), 0, 500);
  }

  @Override
  public void run() {
    var random = new Random();
    new HomeClientMq(new String[]{
        "store",
        "123456",
        String.valueOf(random.nextGaussian() * 20.0 + 120.0),
        String.valueOf(random.nextGaussian() * 10.0 + 80.0),
        "localhost",
        "false"
    }, 0);
  }
}
