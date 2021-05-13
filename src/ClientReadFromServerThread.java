import java.io.DataInputStream;
import java.net.Socket;

public final class ClientReadFromServerThread extends Thread {
    private final Socket socket;
    private final DataInputStream inputStream;
    private int batches;

    public ClientReadFromServerThread(Socket socket, DataInputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
    }

    public int getBatches() {
        return batches;
    }

    public void setBatches(int batches) {
        this.batches = batches;
    }

    @Override
    public void run() {
        while (true) {
            if (socket.isClosed()) {
                return;
            }
            try {
                final int num = inputStream.readInt();
                System.out.printf("CRT has received new batch(s) %d%n", num);
                batches += num;
            } catch (Exception e) {
                // TODO handle error?
            }
        }
    }
}