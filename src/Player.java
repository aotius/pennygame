import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Player extends Thread {
    private Player next;
    private int batches;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Player(Socket socket, int batchSize) {
        this.next = null;
        this.batches = 0;
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

    public void setNext(Player next) {
        this.next = next;
    }

    public void setBatches(int batches) {
        this.batches = batches;
    }

    public void addBatch() {
        this.batches++;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (batches != 0) {
                    System.out.printf("Sending batches to client %d%n", batches);
                    outputStream.writeInt(batches);
                    batches = 0;
                }

                final int batchComplete = inputStream.readInt();
                System.out.println("Player received message from client");
                if (next == null) {
                    return;
                }
                System.out.println("Sending batch to the next player");
                next.addBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
