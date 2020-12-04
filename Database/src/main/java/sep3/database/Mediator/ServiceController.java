package sep3.database.Mediator;


import com.google.gson.Gson;
import sep3.database.Persistance.UserDAO;
import sep3.database.Persistance.UserDAOImpl;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceController implements Runnable
{
    private int PORT = 8443;
    private boolean running;
    private ServerSocket welcomeSocket;
    private UserDAO userDAO;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    public ServiceController() throws IOException
    {
        gson = new Gson();
        this.userDAO = new UserDAOImpl();
        this.running = true;
        running = true;
        //removed ssl
        //welcomeSocket = (SSLServerSocketFactory.getDefault()).createServerSocket(PORT);
        welcomeSocket = new ServerSocket(PORT);
    }


    @Override
    public void run()
    {


        try
        {
            System.out.println("ServerSocket is ready for connection...");
            Socket socket = welcomeSocket.accept();
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Client Connected");
            while (running)
            {
                byte[] lenbytes = new byte[1024];
                int read = inputStream.read(lenbytes, 0, lenbytes.length);
                String request = new String(lenbytes, 0, read);

                System.out.println("Received from client: " + request);
                CommandLine commandLine = gson.fromJson(request, CommandLine.class);
                System.out.println(commandLine.getCommand());
                if (commandLine.getCommand().equals("REQUEST-UserCredentials"))
                {
                    CommandLine commandLine1 = new CommandLine();
                    String response = gson.toJson(userDAO.getAllAccount());
                    commandLine1.setSpecificOrder(response);
                    commandLine1.setCommand("UserCredentials");
                    String sendBack = gson.toJson(commandLine1);
                    byte[] responseAsBytes = sendBack.getBytes();
                    outputStream.write(responseAsBytes, 0, responseAsBytes.length);
                    System.out.println("Done sending user credentials");
                }

            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
