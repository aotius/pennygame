import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClientReadFromServerThread extends Thread {
    private final PennyClient pennyClient;
    private AtomicInteger batches;
    List<Integer> internalScoreboard = Arrays.asList(PennyServer.TOTAL_PENNIES, 0, 0, 0, 0);
    private volatile String timeElapsedTotal;
    private volatile String timeElapsedFirstBatch;


    public ClientReadFromServerThread(PennyClient pennyClient) {
        this.pennyClient = pennyClient;
        this.batches = new AtomicInteger();
        this.timeElapsedTotal = null;
        this.timeElapsedFirstBatch = null;
    }

    public int getBatches() {
        return batches.get();
    }

    public void setBatches(int batches) {
        this.batches.set(batches);
    }

    public String getTimeElapsedTotal() {
        return timeElapsedTotal;
    }

    public void setTimeElapsedTotal(String timeElapsedTotal) {
        this.timeElapsedTotal = timeElapsedTotal;
    }

    public String getTimeElapsedFirstBatch() {
        return timeElapsedFirstBatch;
    }

    public void setTimeElapsedFirstBatch(String timeElapsedFirstBatch) {
        this.timeElapsedFirstBatch = timeElapsedFirstBatch;
    }

    @Override
    public void run() {
        while (true) {
            if (pennyClient.getSocket().isClosed()) {
                return;
            }

            try {
                final String line = pennyClient.getInputStream().readUTF();
                Logger.info(String.format("Input received from server (%s)", line));

                final long[] numbers = Arrays.stream(line.split(",")).mapToLong(Long::parseLong).toArray();

                /*
                0 - New batch
                1 - Scoreboard
                2 - Total Time
                3 - First Batch Time
                 */
                if (numbers[0] == 0) {
                    batches.addAndGet((int) numbers[1]);
                } else if (numbers[0] == 1) {
                    int index = (int) numbers[1];
                    internalScoreboard.set(index + 1, internalScoreboard.get(index + 1) + pennyClient.getBatchSize());
                    internalScoreboard.set(index, internalScoreboard.get(index) - pennyClient.getBatchSize());
                } else if (numbers[0] == 2) {
                    timeElapsedTotal = String.format("%,.2fs", numbers[1] / 1000.0);
                } else if (numbers[0] == 3) {
                    timeElapsedFirstBatch = String.format("%,.2fs", numbers[1] / 1000.0);
                }

            } catch (Exception e) {
                // TODO handle error?
            }
        }
    }
}