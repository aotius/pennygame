import java.io.DataInputStream;
import java.net.Socket;

public final class ReadThread extends Thread {
    private final Socket socket;
    private final DataInputStream inputStream;
    private int batches;

    public ReadThread(Socket socket, DataInputStream inputStream) {
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
                System.out.println("CRT has received new batch(s)");
                batches += inputStream.readInt();
            } catch (Exception e) {
                // TODO handle error?
            }
        }
    }
}