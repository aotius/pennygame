import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public final class ClientReadFromServerThread extends Thread {
    private final Socket socket;
    private final DataInputStream inputStream;
    private int batches;
    ArrayList<Integer> internalScoreboard = new ArrayList<>();
    private String timeElapsed = null;


    public ClientReadFromServerThread(Socket socket, DataInputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        internalScoreboard.addAll(Arrays.asList(PennyServer.TOTAL_PENNIES,0,0,0,0));
    }

    public int getBatches() {
        return batches;
    }

    public void setBatches(int batches) {
        this.batches = batches;
    }

    public String getTimeElapsed() {
        return timeElapsed;
    }

    @Override
    public void run() {
        while (true) {
            if (socket.isClosed()) {
                return;
            }
            try {
                String line = inputStream.readUTF();
                System.out.println("input rcv from server: " + line);
                long[] numbers = Arrays.stream(line.split(",")).mapToLong(Long::parseLong).toArray();
                if (numbers[0] == 0) {
                    System.out.printf("CRT has received new batch(s) %d%n", numbers[1]);
                    batches += numbers[1];
                } else if (numbers[0] == 1) {
                    System.out.println("Update scoreboard");
                    int index = (int) numbers[1];
                    internalScoreboard.set(index + 1, internalScoreboard.get(index + 1) + PennyServer.BATCH_SIZE);
                    internalScoreboard.set(index, internalScoreboard.get(index) - PennyServer.BATCH_SIZE);
                } else if (numbers[0] == 2) {
                    timeElapsed = String.format("%,ds", numbers[1] / 1000);
                }
            } catch (Exception e) {
                // TODO handle error?
            }
        }
    }
}