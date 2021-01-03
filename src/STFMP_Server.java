import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class STFMP_Server {
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(9999)) {

            System.out.println("Port 9999");

            while (true){
                System.out.println("Waiting to accept a client");
                Socket connection = serverSocket.accept();

                System.out.println("Read request from the clients");
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                //Get Encryption
                String encryptedRequest = scanner.nextLine();
                System.out.println("Receiving: "+encryptedRequest);
                //Creating Request
                STFMP_Request request = STFMP_Request.fromEncryptedString(encryptedRequest);
                System.out.println("Send response to client");

                if(request.getAction().equals(STFMP_Actions.WRITE)){
                    STFMPWrite(connection,request);
                }else if(request.getAction().equals(STFMP_Actions.VIEW)){
                    STFMPView(connection,request);
                }else if(request.getAction().equals(STFMP_Actions.CLOSE)){
                    STFMPClose(connection,scanner,inputStream);
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static void sendResponse(Socket connection, STFMP_Response response) throws IOException {
        OutputStream outputStream = connection.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        String encryptedResponse = response.encryptedResponse();
        System.out.println("Responding: "+encryptedResponse);
        printWriter.write(encryptedResponse);
        printWriter.flush();
    }

    private static void STFMPWrite(Socket connection, STFMP_Request request) throws IOException{

        STFMP_Response response;
        String params = request.getParams();
        if (params.split("#").length != 2) {
            response = new STFMP_Response(Constants.PROTOCOL_VERSION, STFMP_Status.INVALID, STFMP_Message.INVALID);
        }else{
            System.out.println("Writing...");
            String filename = request.getFilename();
            String content = request.getContent();
            STFMP_Actions.writeFile(filename, content);
            response = new STFMP_Response(Constants.PROTOCOL_VERSION, STFMP_Status.OK, STFMP_Message.SUCCESS);
        }
        sendResponse(connection,response);
    }

    private static void STFMPView(Socket connection, STFMP_Request request) throws IOException {
        System.out.println("Searching");
        STFMP_Response response;
        String params = request.getParams();

        if (params.split("#").length !=  1 || params.split("#")[0] == null) {
            response = new STFMP_Response(Constants.PROTOCOL_VERSION, STFMP_Status.INVALID, STFMP_Message.INVALID);
        }else{
            String filename = request.getFilename();
            filename = filename.trim();
            if(STFMP_Actions.searchFile(filename) == 200){
                String content = STFMP_Actions.readFile(filename);
                response = new STFMP_Response(Constants.PROTOCOL_VERSION, STFMP_Status.OK,content);
            }else{
                response = new STFMP_Response(Constants.PROTOCOL_VERSION, STFMP_Status.NOT_FOUND, STFMP_Message.NOT_FOUND);
            }


        }
        sendResponse(connection,response);
    }

    private static void STFMPClose(Socket connection, Scanner scanner,InputStream inputStream) throws IOException {
        System.out.println("Connection is closed");
        STFMP_Response response = new STFMP_Response(Constants.PROTOCOL_VERSION, STFMP_Status.OK, STFMP_Message.CLOSE);
        sendResponse(connection,response);
        scanner.close();
        inputStream.close();
        connection.close();
    }

}
