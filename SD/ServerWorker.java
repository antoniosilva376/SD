import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;



class ServerWorker implements Runnable {
    // Variáveis de instância
    private UserList users; // Lista de users
    private Mapa mapa; // Mapa do sistema
    private final TaggedConnection tc; // TaggedConnection
    private ReentrantLock lock; // lock
    private Condition condition; // condition

    /**
     * Método construtor de um ServerWorker
     * 
     * @param userList Lista de users
     * @param tc TaggedConnection
     * @param mapa Mapa
     * @param l Lock
     * @param c Condition
     */
    public ServerWorker (UserList userList,TaggedConnection tc,Mapa mapa,ReentrantLock l,Condition c) {
        this.users = userList;
        this.tc = tc;
        this.mapa = mapa;
        this.lock = l;
        this.condition = c;
    }

    /**
     * Método responsável por fazer o ServerWorker correr.
     */
    @Override
    public void run() {
        try(tc){
            for(;;){
                Frame f = tc.receive();
                int tag = f.getTag();
                int r = 0;
                switch (tag) {
                    case 0:
                        trataRegistarUtilizador(f);      
                        break;

                    case 1:
                        trataAutenticarUtilizador(f);
                        break;

                    case 2:
                        atualizaLocalizacao(f);
                        break;

                    case 3:
                        devolveNrPessoas(f);
                        break;

                    case 4:
                        notificaPosLivre(f);
                        break;

                    case 5:
                        informaDoente(f);
                        break;

                    case 6:
                        if (r == 0){
                            notificaContactoDoente(f);
                            r=1;
                        }
                        break;

                    case 7:
                        devolveNrUtiNrDoentes();
                        break;

                    case 8:
                        utilizadorEspecial(f);

                    default:
                        break;
                }
            }

        } catch (Exception e) {}
    }


    /**
     * Método responsável por responder a um pedido de registo. (Pedido 0)
     * 
     * @param f Frame
     * @throws Exception
     */
    public void trataRegistarUtilizador(Frame f) throws Exception {
        User u = new User(new String(f.getBytes()));
        int res = 0;
        User aux = this.users.getUser(u.getNome());
        if(aux == null){
            users.addUser(u);
            users.atualizaContactos(u);
            mapa.incPessoas(Integer.parseInt(u.getX()),Integer.parseInt(u.getY()));

            res = 1;
        }
        String aux2 = Integer.toString(res);
        byte[] aux3 = aux2.getBytes();

        tc.send(new Frame(0,aux3));
    }

    /**
     * Método responsável por responder a um pedido de autenticação. (Pedido 1)
     * 
     * @param f Frame
     * @throws Exception
     */
    public void trataAutenticarUtilizador(Frame f) throws Exception {
        User u = new User(new String(f.getBytes()));
        int res = 0;
        User aux = this.users.getUser(u.getNome());
        if(aux == null){
            res = 1;
        }
        else if(!(u.getPassword().equals(aux.getPassword()))){
            res = 1;
        }
        if(aux.isDoente()){
            res = 2;
        }
        String aux2 = Integer.toString(res);
        byte[] aux3 = aux2.getBytes();

        tc.send(new Frame(1,aux3));
    }

    /**
     * Método responsável por responder a um pedido de atualização de localização. (Pedido 2)
     * 
     * @param f Frame
     * @throws IOException
     */
    public void atualizaLocalizacao(Frame f) throws IOException {
        String s = new String(f.getBytes());
        String[] as = s.split(";");
        User u = users.getUser(as[0]);

        mapa.decPessoas(Integer.parseInt(u.getX()),Integer.parseInt(u.getY()));
        u.setX(as[1]);
        u.setY(as[2]);
        users.replace(u);
        mapa.incPessoas(Integer.parseInt(u.getX()),Integer.parseInt(u.getY()));
        users.atualizaContactos(u);
        try{ 
            lock.lock();
            condition.signalAll();
        } 
        finally { 
            lock.unlock(); 
        }
    }

    /**
     * Método resposável por responder a uma consulta do número de pessoas numa dada localização. (Pedido 3)
     * 
     * @param f Frame
     * @throws Exception
     */
    public void devolveNrPessoas(Frame f) throws Exception {
        String s = new String(f.getBytes()); 
        String[] as = s.split(";");
        String res = String.valueOf(mapa.getPessoasSitio(Integer.parseInt(as[0]), Integer.parseInt(as[1])));
        byte[] aux = res.getBytes();
        tc.send(new Frame(3,aux));
    }

    /**
     * Método responsável por responder a um pedido de notificação de local livre. (Pedido 4)
     * 
     * @param f Frame
     * @throws Exception
     */
    public void notificaPosLivre(Frame f) throws Exception {
        try{
            lock.lock();
            String s = new String(f.getBytes());
            String[] as = s.split(";");
            boolean res;
            while(!(res = mapa.posVazia(as[0],as[1]))){
                condition.await();
            }
            byte[] aux = String.valueOf(res).getBytes();
            tc.send(new Frame(4,aux));
        }
        finally{
            lock.unlock();
        }
    }

    /**
     * Método responsável por responder à confirmação de um doente.  (Pedido 5)
     * 
     * @param f Frame
     * @throws IOException
     */
    public void informaDoente(Frame f) throws IOException {
        String s = new String(f.getBytes());
        User u = users.getUser(s);
        u.setDoente(true);
        users.replace(u); // atualizar o u na userlist
        int x = Integer.parseInt(u.getX());
        int y = Integer.parseInt(u.getY());

        mapa.decPessoas(x,y);
        mapa.incNrDoentes(x,y);
        try{ 
            lock.lock();
            condition.signalAll();
        } 
        finally { 
            lock.unlock(); 
        }
    }

    /**
     * Método responsável por notificar o cliente que esteve em contacto com um doente. (Pedido 6)
     * 
     * @param f Frame
     * @throws Exception
     */
    public void notificaContactoDoente(Frame f) throws Exception {
        String s = new String(f.getBytes());
        User u = users.getUser(s);
        try{
            lock.lock();
            boolean res;
            while(!(res = esteveEmContacto(u))){
                condition.await();
            }
            byte[] aux = String.valueOf(res).getBytes();
            tc.send(new Frame(6,aux));
        }
        finally{
            lock.unlock();
        }
    }

    /**
     * Método responsável por devolver o número de utilizadores e doentes que já frequentaram os locais do sistema. (Pedido 7)
     */
    public void devolveNrUtiNrDoentes() throws Exception {
        byte[] res = mapa.mapaToBytes();
        tc.send(7,res);
    }

    /**
     * Método responsável por verificar se um utilizador é especial. (Pedido 8)
     * @param f Frame
     * @throws Exception
     */
    public void utilizadorEspecial(Frame f) throws Exception {
        String nome = new String(f.getBytes());
        int res = users.getUser(nome).isEspecial() ? 1 : 0;
        byte[] aux = String.valueOf(res).getBytes();
        tc.send(new Frame(8,aux));
    }

    /**
     * Método responsável por verificar se um cliente esteve em contacto com um doente.
     * 
     * @param u Utilizador
     * @return boolean
     * @throws IOException
     */
    public boolean esteveEmContacto(User u) throws IOException {
        List<String> contactos = u.getUsersContacto();
        for(User user : users.getClients().values()){
            if(contactos.contains(user.getNome())) {
                if(user.isDoente()) return true;
            }
        }
        return false;
    }

}

