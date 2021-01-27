import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer {
    // Variáveis de instância.
    private TaggedConnection tc; // TaggedConnection
    private ReentrantLock l = new ReentrantLock(); // lock
    private Map<Integer, FrameValue> map = new HashMap<>(); // Map dos pedidos identificados pela sua tag
    private IOException exception = null;

    /**
     * Definição da classe FrameValue.
     */
    private class FrameValue {
        //Variáveis de instância
        int waiters = 0; // número pedidos à espera
        Queue<byte[]> queue = new ArrayDeque<>(); // fila
        Condition c = l.newCondition(); // condition
    }

    /**
     * Getter de um FrameValue.
     * 
     * @param tag Identificador.
     * @return
     */
    private FrameValue get(int tag) {
        FrameValue fv;
        try{
            l.lock();
            fv = map.get(tag);
            if (fv == null) {
                fv = new FrameValue();
            map.put(tag, fv);
            } 
        }finally {
            l.unlock();
        }
        return fv;
    }

    /**
     * Construtor de Demultiplixer.
     * 
     * @param conn TaggedConnection
     * @throws IOException
     */
    public Demultiplexer(TaggedConnection conn) throws IOException {
        this.tc = conn;
    }

    /**
     * Método responsável por fazer o Demultiplexer correr.
     */
    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    Frame frame = tc.receive();
                    l.lock();
                    try {
                        FrameValue fv = get(frame.getTag());
                        if (fv == null) {
                            fv = new FrameValue();
                            map.put(frame.getTag(), fv);
                        }
                        fv.queue.add(frame.getBytes());
                        fv.c.signal();
                    } finally {
                        l.unlock();
                    }
                }
            } catch (IOException e) {
                l.lock();
                try {
                    exception = e;
                    map.forEach((k, v) -> v.c.signalAll());
                } finally {
                    l.unlock();
                }
            }
        }).start();
    }

    /**
     * Método responsável por reencaminhar o pedido para a TaggedConnection.
     * 
     * @param frame Frame
     * @throws Exception
     */
    public void send(Frame frame) throws Exception {
        tc.send(frame);
    }

    /**
     * Método responsável por reencaminhar o pedido para a TaggedConnection.
     * 
     * @param tag Tag
     * @param b Data
     * @throws Exception
     */
    public void send(int tag, byte[] b) throws Exception {
        tc.send(tag, b);
    }

    /**
     * Método responsável por receber a resposta do servidor.
     * 
     * @param tag Identificador do pedido
     * @return array de bytes
     * @throws IOException
     * @throws InterruptedException
     */
    public byte[] receive(int tag) throws IOException, InterruptedException {
        l.lock();
        FrameValue fv;
        try {
            fv = map.get(tag);
            if (fv == null) {
                fv = new FrameValue();
                map.put(tag, fv);
            }
            fv.waiters++;
            while (true) {
                if (!fv.queue.isEmpty()) {
                    fv.waiters--;
                    byte[] reply = fv.queue.poll();
                    if (fv.waiters == 0 && fv.queue.isEmpty())
                        map.remove(tag);
                    return reply;
                }
                if (exception != null) {
                    throw exception;
                }
                fv.c.await();
            }
        } finally {
            l.unlock();
        }
    }

    /**
     * Método responsável por fechar a TaggedConnection.
     * @throws IOException
     */
    public void close() throws IOException {
        tc.close();
    }
}