import java.util.concurrent.locks.ReentrantLock;

public class Mapa {
    // Variáveis de instância
    int n = 10; // tamanho do mapa
    private int[][] map = new int[n][n]; // número atual de pessoas nos locais do mapa
    private Pair[][] mapa = new Pair[n][n]; // número total de pessoas que frenquentaram os locais do mapa
    private ReentrantLock lock; // lock

    /**
     * Construtor de Mapa.
     */
    public Mapa(){
        for(int i = 0;i < n;i++)
          for(int j = 0;j < n;j++) map[i][j] = 0;

        for(int i = 0;i < n;i++)
            for(int j = 0;j < n;j++) mapa[i][j] = new Pair(0,0);

        lock = new ReentrantLock();    
    }

    /**
     * Getter do número atual de pessoas numa posição do mapa.
     * 
     * @param x coordenada x
     * @param y coordanada y
     * @return int
     */
    public int getPessoasSitio(int x,int y){
        try{
            lock.lock();
            return map[x][y];
        } finally{
            lock.unlock();
        }
    }

    /**
     * Setter do número atual de pessoas numa posição do mapa.
     * 
     * @param x coordenada x
     * @param y coordenada y
     * @param qntPessoas quantidade
     */
    public void setPessoasSitio(int x,int y, int qntPessoas){
        try{
            lock.lock();
            map[x][y] = qntPessoas;
        } finally{
            lock.unlock();
        }
    }

    /**
     * Método responsável por incrementar o número de doentes numa dada posição do mapa.
     * 
     * @param x coordenada x
     * @param y coordenada y
     */
    public void incNrDoentes(int x,int y){
        try{
            lock.lock();
            mapa[x][y].incNrDoentes();
        } finally{
            lock.unlock();
        }
    }

    /**
     * Método responsável por incrementar o número de pessoas numa dada posição do mapa.
     * 
     * @param x coordenada x
     * @param y coordenada y
     */
    public void incPessoas(int x,int y){
        try{
            lock.lock();
            map[x][y]++;
            mapa[x][y].incNrUtilizadores();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Método responsável por decrementar o número de pessoas numa dada posição do mapa.
     * 
     * @param x coordenada x
     * @param y coordenada y
     */
    public void decPessoas(int x,int y){
        try{
            lock.lock();
            map[x][y]--;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Método responsável por verificar se uma posição do mapa está vazia.
     * 
     * @param x coordenada x
     * @param y coordenada y
     * @return boolean
     */
    public boolean posVazia(String x, String y){
        try{
            lock.lock();
            return map[Integer.parseInt(x)][Integer.parseInt(y)] == 0;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Método toString.
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Mapa={");

        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++) sb.append(" ").append(map[i][j]);
            sb.append("\n      ");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Método toString do mapa de numero de utilizadores e doentes.
     * 
     * @return String
     */
    public String toStringMapa(){
        StringBuilder sb = new StringBuilder();
        sb.append("Total Doentes/Total Pessoas no local:\n");
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++)
                sb.append("( ").append(i).append(" , ")
                        .append(j).append(" ) : ").append(mapa[i][j]).append("\n");
            sb.append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Construtor de Mapa a partir de um array de bytes.
     * 
     * @param a array de bytes
     */
    public Mapa(byte[] a){
        String b = new String(a);
        String [] aux = b.split("_");
        int m = 0;
        for(int i=0;i<n;i++)
            for(int j = 0;j<n;j++) mapa[i][j] = new Pair(aux[m++]);
    }

    /**
     * Método que transforma um mapa num array bytes.
     * 
     * @return
     */
    public byte[] mapaToBytes(){
        try{
            lock.lock();
            StringBuilder sb = new StringBuilder();

            for(int i=0;i<n;i++)
                for(int j=0;j<n;j++) sb.append(mapa[i][j]).append("_");

            String res = sb.toString();

            return res.getBytes();

        } finally {
            lock.unlock();
        }
    }

}
