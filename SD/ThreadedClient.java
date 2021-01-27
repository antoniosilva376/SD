import java.net.Socket;

public class ThreadedClient {
    /**
     * Main do cliente
     */
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        Demultiplexer m = new Demultiplexer(new TaggedConnection(s));
        m.start();
        TextUI text = new TextUI(m);
        try{
            text.run(s);
        } catch (Exception ignored){}

    }
}


