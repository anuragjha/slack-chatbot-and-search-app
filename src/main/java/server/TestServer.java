package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import handlers.FindHandler;
import handlers.Handlers;
import handlers.RequestHandler;
import handlers.ReviewSearchHandler;

public class TestServer {

	private final int PORT;

	private ServerSocket server;
	private ExecutorService executer;
	private volatile boolean shouldRun;
	private HashMap<String, Handlers> pathMapper;
	//private Handlers handler;


	public TestServer(int port)	{
		this.PORT = port;
		this.pathMapper = new HashMap<String, Handlers>();
		try {
			System.out.println("Constructing Server");
			this.server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Error in constructing server");
			System.exit(1);
		}
		this.shouldRun = true;
	}


	public void startup() {
		try {
			this.runServer(this.PORT);
		} catch (IOException ioe) {
			System.out.println("Error in starting the server");
			//Have to convert to server running, 
		}
		finally	{
			try {
				this.server.close();
			} catch (IOException ioe) {
				System.out.println("Error in closing the server");
			}
		}
	}


	private void runServer(int port) throws IOException	{

		this.executer = Executors.newFixedThreadPool(5);
		System.out.println("server started");

		while(shouldRun) {		
			Socket clientSocket = server.accept();
			Runnable RequestHandler = new RequestHandler(clientSocket, this.pathMapper);
			executer.execute(RequestHandler);	
			if(!shouldRun) {
				this.closeServer();
			}
		}
		if(!shouldRun) {
			this.closeServer();
		}

	}

	private void closeServer() {

		try {
			this.executer.shutdown();
			try {
				while(!this.executer.awaitTermination(10, TimeUnit.SECONDS)) {
					System.out.println("awaiting termination");
				}
			} catch (InterruptedException e) {
				System.out.println("Error in closing executer pool");
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally	{
			this.pathMapper = null; //at the end mapper is empty and will; have to be refilled
		}
	}


	public void addMapping(String path, Handlers handler) {
		pathMapper.put(path, handler);
	}


	/**
	 * @return the pathMapper
	 */
	public HashMap<String, Handlers> getPathMapper() {
		return pathMapper;
	}


	public static void main(String[] args)	{
		TestServer ts = new TestServer(8080);
		ts.addMapping("/reviewsearch", new ReviewSearchHandler());
		ts.addMapping("/find", new FindHandler());
		System.out.println("hashmap: " + ts.pathMapper);
		ts.startup();
	}

}
