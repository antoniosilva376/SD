import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TaggedConnection implements AutoCloseable {
    // Variáveis de instância
    private final DataInputStream dis; // DataInputStream
    private final DataOutputStream dos; // DataOutputStream
    private final Lock rl = new ReentrantLock(); // readLock
    private final Lock wl = new ReentrantLock(); // writeLock

    /** 
     * Método construtor de uma TaggedConnection.
     * 
     * @param socket Socket
     * @throws IOException
     */
    public TaggedConnection(Socket socket) throws IOException {
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * Método responsável por escrever no socket um array de bytes identificado pela sua tag.
     * 
     * @param tag Tag da frame
     * @param data Conteúdo da frame
     * @throws Exception
     */
    public void send(int tag, byte[] data) throws Exception {
        this.send(new Frame(tag, data));
    }

    /**
     * Método responsável por escrever no socket uma frame.
     * 
     * @param f Frame completa
     * @throws Exception 
     */
    public void send(Frame f) throws Exception {
        try {
            wl.lock();
            this.dos.writeInt(f.getTag());
            this.dos.writeInt(f.getBytes().length);
            this.dos.write(f.getBytes());
            this.dos.flush();
        }
        finally {
            wl.unlock();
        }
    }

    /**
     * Método responsável por ler do socket uma frame.
     * 
     * @return Frame lida
     * @throws IOException
     */
    public Frame receive() throws IOException {
        int tag;
        byte[] data;
        try {
            rl.lock();
            tag = this.dis.readInt();
            int n = this.dis.readInt();
            data = new byte[n];
            this.dis.readFully(data);
        }
        finally {
            rl.unlock();
        }
        return new Frame(tag,data);
    }

    /**
     * Método que fecha os leitores e escritores do socket
     */
    @Override
    public void close() throws IOException {
        this.dis.close();
        this.dos.close();
    }
}