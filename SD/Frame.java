import java.util.concurrent.locks.ReentrantLock;

public class Frame {
    // Variáveis de instância
    private int tag; // identificador do tipo de pedido
    private byte[] bytes; // conteudo do pedido
    private ReentrantLock lock = new ReentrantLock(); // lock

    /**
     * Construtor de uma Frame
     * 
     * @param tag Identificador
     * @param bytes Data 
     */
    Frame(int tag,byte[] bytes){
        this.tag = tag;
        this.bytes = bytes;
    }

    /**
     * Getter de uma tag
     * 
     * @return inteiro
     */
    public int getTag(){
        try{
            lock.lock();
            return this.tag;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Getter de 'bytes'
     * 
     * @return array de bytes
     */
    public byte[] getBytes(){
        try{
            lock.lock();
            return this.bytes;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Método toString
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Frame={ tag=").append(tag).append(" , ").append(new String(bytes)).append(" }");

        return sb.toString();
    }
}
