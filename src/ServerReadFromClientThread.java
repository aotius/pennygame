import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerReadFromClientThread extends Thread {
    private ServerReadFromClientThread next;
    private AtomicInteger batches;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public ServerReadFromClientThread(Socket socket, int batchSize) {
        this.next = null;
        this.batches = new AtomicInteger(0);
        try {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(batchSize);
        } catch (Exception e) {
            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
            e.printStackTrace();
        }
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public void setNext(ServerReadFromClientThread next) {
        this.next = next;
    }

    public void setBatches(int batches) {
        this.batches.set(batches);
    }

    public void addBatch() {
        this.batches.set(this.batches.get() + 1);
    }

    @Override
    public void run() {
        new Thread(() -> {
            while (true) {
                final int batches = this.batches.get();
                if (batches != 0) {
                    System.out.printf("Sending batches to client %d%n", batches);
                    try {
                        outputStream.writeInt(batches);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.batches.set(0);
                }
            }
        }).start();

        while (true) {
            try {
                System.out.println("Player - blocking");

                final int batchComplete = inputStream.readInt();
                System.out.println("Player received message from client");
                if (next == null) {
                    continue;
                }
                System.out.println("Sending batch to the next player");
                next.addBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
