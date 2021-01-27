import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class User {
  // Variáveis de instância
  private String nome; // nome
  private String password; //password
  private String x; // coordenada x
  private String y; // coordenada y
  private boolean especial; // booleano utilizador especial
  private boolean doente; // booleano doente
  private List<String> usersContacto; // lista de utilizadores que esteve em contacto
  private ReentrantLock lock; // lock

  /**
   * Construtor de User.
   * 
   * @param nome nome
   * @param password password
   * @param x coordenada x
   * @param y coordenada y
   * @param especial booleano especial
   * @param doente booleano doente
   * @param usersContacto lista de contactos
   */
  public User(String nome, String password, String x, String y, boolean especial, boolean doente, List<String> usersContacto) {
    this.nome = nome;
    this.password = password;
    this.x = x;
    this.y = y;
    this.especial = especial;
    this.doente = doente;
    this.usersContacto = usersContacto;
    this.lock = new ReentrantLock();
  }

  /**
   * Getter de nome.
   * 
   * @return String
   */
  public String getNome() {
    try {
      lock.lock();
      return this.nome;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Getter de password.
   * 
   * @return String
   */
  public String getPassword() {
    try {
      lock.lock();
      return this.password;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Getter de x.
   * 
   * @return String
   */
  public String getX() {
    try {
      lock.lock();
      return this.x;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Getter de y.
   * 
   * @return String
   */
  public String getY() {
    try {
      lock.lock();
      return this.y;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Setter de x.
   * 
   * @param novoX x
   */
  public void setX(String novoX) {
    try {
      lock.lock();
      this.x = novoX;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Setter de y.
   * 
   * @param novoY y
   */
  public void setY(String novoY) {
    try {
      lock.lock();
      this.y = novoY;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Método toString.
   */
  public String toString() {
    try {
      lock.lock();
      StringBuilder sb = new StringBuilder();

      sb.append("Cliente = {nome= ").append(nome).append(" ; password= ").append(password).append(" ; localizaçao= ")
          .append(x).append(" , ").append(y).append(" ; especial= ").append(especial).append(" ; doente= ")
          .append(doente).append(" ; listaDeContactos= ").append(usersContacto).append("}\n");
      return sb.toString();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Construtor de User a partide uma String.
   * 
   * @param s String
   */
  public User(String s) {
    String[] as = s.split(";");
    this.nome = as[0];
    this.password = as[1];
    this.x = as[2];
    this.y = as[3];
    if(as[4].equals("true")){ 
      this.especial = true;
    }
    if(as[5].equals("true")){ 
      this.doente = true;
    }
    else {this.doente = false;}
    this.usersContacto = new ArrayList<String>();
    for(int i = 7 ; i < Integer.valueOf(as[6]) + 7 ; i++){
      this.usersContacto.add(as[i]);
    }
    this.lock = new ReentrantLock();
  }

  /**
   * Método responsável por criar um array de bytes a partir de um User.
   * 
   * @return array de bytes
   */
  public byte[] getBytes() {
    try {
      lock.lock();
      StringBuilder sb = new StringBuilder();
      sb.append(nome).append(";").append(password).append(";").append(x).append(";").append(y).append(";").append(especial)
                     .append(";").append(doente).append(";").append(usersContacto.size());
      for(String s : usersContacto){
        sb.append(";").append(s);
      }
      return sb.toString().getBytes();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Getter de doente.
   * 
   * @return boolean
   */
  public boolean isDoente() {
    return doente;
  }

  /**
   * Setter de doente.
   * 
   * @param doente boolean
   */
  public void setDoente(boolean doente) {
    this.doente = doente;
  }

  /**
   * Getter de usersContacto
   * @return lista de nomes
   */
  public List<String> getUsersContacto() {
    return usersContacto;
  }

  /**
   * Setter de usersContacto
   * 
   * @param usersContacto lista de nomes
   */
  public void setUsersContacto(List<String> usersContacto) {
    this.usersContacto = usersContacto;
  }

  /**
   * Getter de especial
   * 
   * @return boolean
   */
  public boolean isEspecial() {
    return especial;
  }

  /**
   * Setter de especial
   * 
   * @param especial boolean
   */
  public void setEspecial(boolean especial) {
    this.especial = especial;
  }

}
