import java.util.concurrent.locks.ReentrantLock;

public class Pair {
    // Variáveis de instancia
    private int nrUtilizadores; // numero de utilizadores
    private int nrDoentes; // numero de doentes
    private ReentrantLock l = new ReentrantLock(); // lock

    /**
     * Construtor de Pair.
     */
    public Pair(int n,int m){
        nrDoentes = n;
        nrUtilizadores = m;
    }

    /**
     * ConStrutor de Pair a partir de uma String.
     * 
     * @param a String
     */
    public Pair(String a){
        String[] b = a.split("/");
        String c = b[0].substring(2);
        String d = b[1].substring(0,1);
        nrDoentes = Integer.parseInt(c);
        nrUtilizadores = Integer.parseInt(d);
    }

    /**
     * Método responsável por incrementar o número de utilizadores.
     */
    public void incNrUtilizadores(){
        try{
            l.lock();
            nrUtilizadores++;
        } finally {
            l.unlock();
        }
    }
    
    /**
     * Método responsável por incrementar o número de doentes.
     */
    public void incNrDoentes(){
        try{
            l.lock();
            nrDoentes++;
        } finally {
            l.unlock();
        }
    }

    /**
    * Método toString
    * 
    * @return String
    */
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("[ ").append(nrDoentes).append("/").append(nrUtilizadores).append(" ]");

        return sb.toString();
    }
}
