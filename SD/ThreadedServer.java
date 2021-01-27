import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadedServer {
    /**
     * Main do servidor
     */
    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        UserList userList = new UserList();
        Mapa mapa = new Mapa();
        ReentrantLock l = new ReentrantLock();
        Condition c = l.newCondition();

        final int WORKERS_PER_CONNECTION = 2;

        while (true) {
            Socket socket = serverSocket.accept();
            TaggedConnection tc = new TaggedConnection(socket);


            for (int i = 0; i < WORKERS_PER_CONNECTION; ++i)
                new Thread(new ServerWorker(userList, tc, mapa,l,c)).start();
        }
    }
}
