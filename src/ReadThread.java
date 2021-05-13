import java.io.ObjectInputStream;
import java.net.Socket;

public final class ReadThread extends Thread {
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private int batches;

    public ReadThread(Socket socket, ObjectInputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
    }

    public int getBatches() {
        return batches;
    }

    @Override
    public void run() {
        while (true) {
            if (socket.isClosed()) {
                return;
            }
            try {
                inputStream.read();
                final int incoming = inputStream.readInt();
                batches = incoming;
            } catch (Exception e) {
            }
        }
    }
}