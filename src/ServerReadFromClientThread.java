import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerReadFromClientThread extends Thread {
    private ServerReadFromClientThread next;
    private AtomicInteger batches;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private List<ServerReadFromClientThread> users;

    public ServerReadFromClientThread(Socket socket, int batchSize, List<ServerReadFromClientThread> users) {
        this.next = null;
        this.batches = new AtomicInteger(0);
        this.users = users;
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

    public int getBatches() {
        return batches.get();
    }

    public void setBatches(int batches) {
        this.batches.set(batches);
    }

    public void addBatch() {
        this.batches.set(this.batches.get() + 1);
    }

    public void broadcast(String msg) {
        if (users.isEmpty()) {
            return;
        }
        this.users.forEach(user -> {
            try {
                user.getOutputStream().writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }



    @Override
    public void run() {
        new Thread(() -> {
            while (true) {
                final int batches = this.batches.get();
                if (batches != 0) {
                    System.out.printf("Sending batches to client %d%n", batches);
                    try {
                        outputStream.writeUTF("0," + batches);
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
                System.out.println("Player received ack from client");
                int userIndex  = 0;
                ServerReadFromClientThread nextTemp = next;
                while (nextTemp != null) {
                    userIndex++;
                    nextTemp = nextTemp.next;
                }
                userIndex = 3 - userIndex;
                broadcast("1," + userIndex);
                System.out.println("Send index of player rcving batch");

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
