import lombok.SneakyThrows;
import org.example.CrptApi;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

public class CrptApiTest {

    @Test
    @SneakyThrows
    public void checkLockLogic() {
        CrptApi crptApi = new CrptApi(ChronoUnit.SECONDS, 5L, 3L);

        CompletableFuture<Void> future1 = CompletableFuture.supplyAsync(crptApi::execute);
        CompletableFuture<Void> future2 = CompletableFuture.supplyAsync(crptApi::execute);
        CompletableFuture<Void> future3 = CompletableFuture.supplyAsync(crptApi::execute);
        CompletableFuture<Void> future4 = CompletableFuture.supplyAsync(crptApi::execute);
        CompletableFuture<Void> future5 = CompletableFuture.supplyAsync(crptApi::execute);
        CompletableFuture<Void> future6 = CompletableFuture.supplyAsync(crptApi::execute);
        CompletableFuture<Void> future7 = CompletableFuture.supplyAsync(crptApi::execute);

        CompletableFuture.allOf(future1, future2, future3, future4, future5, future6, future7).get();

    }
}
