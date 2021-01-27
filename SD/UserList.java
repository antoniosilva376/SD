import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;

import java.util.concurrent.locks.ReentrantLock;

public class UserList {
	//Variáveis de instância
	private Map<String, User> userList; // map de Users
	private Lock lock = new ReentrantLock(); // lock

	/**
	 * Construtor de UserList.
	 */
	public UserList() {
		userList = new TreeMap<>();
	}

	/**
	 * Método responsável por adicionar um User.
	 * 
	 * @param u User.
	 */
	public void addUser(User u){
		try{
			this.lock.lock();
			this.userList.put(u.getNome(),u);
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Getter de um dado User.
	 * 
	 * @param nome nome do User
	 * @return User
	 */
	public User getUser(String nome){
		try{
			lock.lock();
			return userList.get(nome);
		} finally{
			lock.unlock();
		}
	}

	/**
	 * Getter do map de Users.
	 * 
	 * @return map de Users
	 * @throws IOException
	 */
	public Map<String, User> getClients() throws IOException{
		try{ lock.lock();
			Map<String,User> res = new TreeMap<>();
			res.putAll(this.userList);
			return res;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Método responsável por atualizar os dados de um User.
	 * 
	 * @param u User
	 */
	public void replace(User u){
		try{
			lock.lock();
			userList.replace(u.getNome(),u);
		} finally{
			lock.unlock();
		}
	}

	/**
	 * Método responsável por atualizar os contactos dos Users
	 * @param u User
	 * @throws IOException
	 */
	public void atualizaContactos(User u) throws IOException {
		try{
			lock.lock();
			for(User aux : userList.values()){
				if(!(aux.equals(u)) && aux.getX().equals(u.getX()) && aux.getY().equals(u.getY())){
					aux.getUsersContacto().add(u.getNome());
					u.getUsersContacto().add(aux.getNome());
				} 
			}
		} finally{
			lock.unlock();
		}
	}
}
