import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class STFMP_Client {
    public static void main(String[] args) {
        try (Socket connection = new Socket("localhost", 9999);){

            System.out.println(" Connection to the server established.");

            // Send request to the server
            System.out.println("Sending data to server");

            STFMP_Request request = new STFMP_Request(Constants.PROTOCOL_VERSION, STFMP_Actions.CLOSE,null);
            OutputStream outputStream = connection.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            //Encryption
            String encryptedRequest = request.encryptedRequest();
            printWriter.write(encryptedRequest);
            printWriter.flush();
            System.out.println("Requesting:" + encryptedRequest);

            // Read data from the server
            InputStream inputStream = connection.getInputStream();
            System.out.println("Retrieving from server...");
            Scanner scanner = new Scanner(inputStream);
            String encryptedResponse = scanner.nextLine();
            System.out.println("Retrieving:" + encryptedResponse);
            //Decryption
            STFMP_Response response = STFMP_Response.fromEncryptedString(encryptedResponse);
            System.out.println(" Result: " + response.getMessage());

            scanner.close();
            connection.close();
        } catch (IOException e) {
            System.out.println("Can't connect to the server. " + e.getMessage());
        }
    }
}
