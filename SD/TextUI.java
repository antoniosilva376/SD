import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TextUI {

    // Scanner para leitura
    private Scanner scin;
    Demultiplexer m;

    /**
     * Construtor.
     *
     * Cria o TextUI
     */
    public TextUI(Demultiplexer m) throws IOException {
        scin = new Scanner(System.in);
        this.m = m;
    }

    /**
     * Executa o menu principal e invoca o método correspondente à opção
     * seleccionada.
     * 
     * @throws IOException
     */
    public void run(Socket s) throws IOException {
        System.out.println("Bem vindo");
        this.menuPrincipal(s);
        System.out.println("Até breve!...");
    }

    // Métodos auxiliares - Estados da UI

    /**
     * Estado - Menu Principal
     * 
     * @throws IOException
     */
    private void menuPrincipal(Socket s) throws IOException {

        Menu menu = new Menu(new String[] { "Registar Utilizador", "Autenticar" });

        // Registar os handlers
        menu.setHandler(1, () -> {
            try {
                registar();
            } catch (Exception ignored) {
            }
        });

        menu.setHandler(2, () -> {
            try {
                login(s);
            } catch (Exception ignored) {
            }
        });

        menu.run(s,0);

    }

    /**
     * Menu de um Utilizador
     * 
     * @param s Socket
     * @param nome Nome do utilizador
     * @param password Password do utilizador
     * @throws IOException
     */
    private void menuUtilizador(Socket s, String nome, String password) throws IOException {

        Menu menu = new Menu(new String[] { "Atualizar localização", "Consultar número de pessoas numa localização",
                "Pedir para ser notificado quando uma localização ficar vazia",
                "Informar o sistema que estou doente", "Pedir para ser notificado quando estive em contacto com um doente" ,
                "Visualizar mapa com número total de pessoas e doentes por local"});

        menu.setPreCondition(6, ()->verificaEspecial(nome));        

        // Registar os handlers
        menu.setHandler(1, () -> {
            try {
                atualizaLocalizacao(nome);
            } catch (Exception ignored) {}
        });
        menu.setHandler(2, () -> {
            try {
                devolveNrPessoas();
            } catch (Exception ignored) {}
        });
        menu.setHandler(3, () -> {
            try {
                notificarPosLivre();
            } catch (Exception ignored) {}
        });
        menu.setHandler(4, () -> {
            try {
                informarDoente(s,nome);
            } catch (Exception ignored) {}
        });
        menu.setHandler(5, () -> {
            try {
                notificaContactoDoente(nome);
            } catch (Exception ignored) {}
        });
        menu.setHandler(6, () -> {
            try {
                devolveNrUtiNrDoentes(nome);
            } catch (Exception ignored) {}
        });


        menu.run(s,1);

    }

    /**
     * Método responsável por pedir as informações necessarias ao cliente, para o seu registo
     * 
     * @throws Exception
     */
    private void registar() throws Exception {

        System.out.println("Introduza o seu nome: ");
        String name = scin.next();
        System.out.println("Introduza a sua password: ");
        String password = scin.next();
        System.out.println("Introduza coordenada x: ");
        String x = scin.next();
        System.out.println("Introduza coordenada y: ");
        String y = scin.next();
        System.out.println("É um utilizador especial? Pressione '1' caso seja ou '0' caso contrário");
        int especial = scin.nextInt();
        User u = new User(name, password, x, y,(especial == 1 ? true : false),false,new ArrayList<String>());
        m.send(new Frame(0, u.getBytes()));
        byte[] aux = m.receive(0);
        String aux2 = new String(aux);
        if (aux2.equals(Integer.toString(1))) {
            System.out.println("Utilizador registado com sucesso! ");
        } else {
            System.out.println("Nome já existente, por favor tente outra vez ");
        }
    }

    /**
     * Método responsável por pedir as informações necessarias ao cliente, para a sua autenticação
     * 
     * @throws Exception
     */
    private void login(Socket s) throws Exception {

        int r = 1;
        while (r == 1) {
            System.out.println("Introduza o seu nome: ");
            String name = scin.next();
            System.out.println("Introduza a sua password: ");
            String password = scin.next();
            User u = new User(name, password, "-1", "-1", false, false, new ArrayList<String>());
            m.send(new Frame(1, u.getBytes()));
            byte[] aux = m.receive(1);
            String aux2 = new String(aux);
            if (aux2.equals("0")) {
                r = 0;
                menuUtilizador(s, name, password);
            }
            else {
                if(aux2.equals("1")){
                    System.out.println("Credenciais erradas, se deseja sair pressine '0', se deseja reintroduzir pressione '1'");
                } else {
                    System.out.println("Este utilizador foi infetado! Não é possivel efetuar o login. Se deseja sair pressine '0', se deseja reintroduzir pressione '1'");
                }
                r = scin.nextInt();
            }
        }
    }

    /**
     * Método responsável por pedir as informações necessarias ao cliente, para a atualização da sua localização
     * 
     * @param nome Nome do utilizador
     */
    private void atualizaLocalizacao(String nome) {

        try {
            System.out.println("Introduza a coordenada x:");
            String x = scin.next();
            System.out.println("Introduza a coordenada y:");
            String y = scin.next();
            String st = new String(nome + ";" + x + ";" + y);
            m.send(new Frame(2, st.getBytes()));
            System.out.println("Localização atualizada.");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Método responsável por pedir as informações necessarias ao cliente, para a consulta do número de pessoas na localização pedida
     */
    private void devolveNrPessoas() {

        try {
            System.out.println("Introduza a coordenada x: ");
            String x = scin.next();
            System.out.println("Introduza a coordenada y: ");
            String y = scin.next();
            String st = new String(x + ";" + y);
            m.send(new Frame(3, st.getBytes()));
            byte[] reply = m.receive(3);
            String sr = new String(reply);
            System.out.println("Na posição " + x + " " + y + " encontram-se " + sr + " pessoas.");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    /**
     * Método responsável por pedir as informações necessarias ao cliente, para ser notificado do acontecimento da localização pedida ficar livre.
     */
    private void notificarPosLivre() {
        try {
            System.out.println("Introduza a coordenada x: ");
            String x = scin.next();
            System.out.println("Introduza a coordenada y: ");
            String y = scin.next();
            new Thread(() -> {
                String st = new String(x + ";" + y);
                try {
                    m.send(4, st.getBytes());
                    m.receive(4);
                    System.out.println("A posição " + x + " " + y + " ficou vazia.");
                } catch (Exception e) {}

            }).start();
        } catch(Exception e){
        }
    }

    /**
     * Método responsável por enviar para o servidor a informação de que o utilizador está doente.
     * 
     * @param s Socket
     * @param nome Nome do utilizador
     */
    private void informarDoente(Socket s, String nome){
        try{
            m.send(5,nome.getBytes());
            System.out.println("Sistema informado, obrigado pela preocupação!");
            s.close();
        } catch(Exception e){}
    }
        
    /**
     * Método responsável por notificar o utilizador se esteve em contacto com um doente.
     * 
     * @param nome Nome do utilizador
     */
    private void notificaContactoDoente(String nome){
        try {
            new Thread(() -> {
                try {
                    m.send(6, nome.getBytes());
                    m.receive(6);
                    System.out.println("Atenção! Esteve em contacto com um doente!");
                } catch (Exception e) {}

            }).start();
        } catch(Exception e){
            e.getMessage();
        }
    }

    /**
     * Método responsável por apresentar ao cliente o número total de utilizadores e doentes que frequentaram os locais do mapa.
     * 
     * @param nome Nome do utilizador
     */
    private void devolveNrUtiNrDoentes(String nome){
        try{
            m.send(7,nome.getBytes());
            byte[] res = m.receive(7);
            Mapa ma = new Mapa(res);
            System.out.println("Mapa pedido:");
            System.out.println(ma.toStringMapa());
        } catch (Exception e){}
    }

    /**
     * Método responsável por apresentar a opção especial apenas aos utilizadores especiais.
     * 
     * @param nome Nome do utilizador
     * @return boolean
     */
    private boolean verificaEspecial(String nome){
        try{
            m.send(8,nome.getBytes());
            byte[] res = m.receive(8);
            String aux = new String(res);
            return aux.equals("1");

        } catch (Exception e){}
        return false;
    }



}