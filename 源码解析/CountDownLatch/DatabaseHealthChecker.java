package countdownlatch;

import java.util.concurrent.CountDownLatch;

/**
 * Created by 11981 on 2018/4/10.
 */
public class DatabaseHealthChecker extends BaseHealthChecker{
    public DatabaseHealthChecker(CountDownLatch latch){
        super("Database Service", latch);
    }

    @Override
    public void verifyService(){
        System.out.println("Checking " + this.getServiceName());
        try {
            Thread.sleep(5000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println(this.getServiceName() + "is UP");
    }
}
