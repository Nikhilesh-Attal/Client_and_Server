import java.io.*;
import java.net.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import javax.swing.*;
import java.util.Scanner;

public class Server extends JFrame{

    ServerSocket server;
    Socket socket;
    BufferedReader br;
    PrintWriter pr;

    private volatile boolean isReading = true;

    private JLabel heading = new JLabel("Server");
    private JTextArea messageArea = new JTextArea();
    private JTextField messageInput = new JTextField();
    private Font font = new Font("Roboto", Font.PLAIN, 20);
    Scanner scan = new Scanner(System.in);

    public Server() {
        try {
            System.out.println("Enter the port number");
            int port = scan.nextInt();
            server = new ServerSocket(port);
            System.out.println("Server is waiting for connection...");
            socket = server.accept();
            System.out.println("Client connected!");

            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pr = new PrintWriter(socket.getOutputStream(), true); // Auto-flush enabled

            createGUI();
            handleEvents(); // Handle events like sending messages via GUI
            startReading();
            startWriting();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void createGUI() {
        this.setTitle("Server"); // Set the title of the application window
        this.setSize(700, 800); // Set dimensions of the window
        //this.setLocationRelativeTo(null); // Center the window on the screen
        this.setLocation(0, (Toolkit.getDefaultToolkit().getScreenSize().height - this.getHeight()) / 2);   // Position the server window on the left side of the screen

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the application on clicking the close button

        heading.setFont(new Font("Roboto", Font.BOLD, 20)); // Set heading font and make it bold
        messageArea.setFont(font);
        messageInput.setFont(font);
        messageArea.setEnabled(false);
        messageArea.setForeground(Color.BLACK);     // Set the text color for the message area to black

        heading.setHorizontalAlignment(SwingConstants.CENTER); // Align heading text to the center
        heading.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 10)); // Add padding around the heading

        // Add an icon to the application with fixed dimensions
        ImageIcon icon = new ImageIcon("D:\\chat_logo.jpg");
        Image scaledIcon = icon.getImage().getScaledInstance(100, 50, Image.SCALE_SMOOTH); // Resize icon
        heading.setIcon(new ImageIcon(scaledIcon));
        heading.setHorizontalTextPosition(SwingConstants.CENTER);
        heading.setVerticalTextPosition(SwingConstants.BOTTOM);

        // Add components to the window using BorderLayout
        this.setLayout(new BorderLayout());
        this.add(heading, BorderLayout.NORTH);
        this.add(new JScrollPane(messageArea), BorderLayout.CENTER); // Add scrolling capability to the message area
        this.add(messageInput, BorderLayout.SOUTH);

        this.setVisible(true); // Make the window visible
    }

    public void handleEvents() {
        messageInput.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String msgToSend = messageInput.getText();

                if (e.getKeyCode() == KeyEvent.VK_ENTER) { // Check if the Enter key is pressed
                    messageArea.append("Server: " + msgToSend + "\n"); // Display the sent message in the message area
                    pr.println(msgToSend); // Send the message to the server
                    messageInput.setText(""); // Clear the input field
                }
                // Stop reading and writing if the exit message is sent
                if (msgToSend.equalsIgnoreCase("bye") || msgToSend.equalsIgnoreCase("exit") || msgToSend.equalsIgnoreCase("quit") || msgToSend.equalsIgnoreCase("see you later")) {
                    isReading = false; // Signal to stop reading
                    try {
                        socket.close(); // Close the socket
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    messageInput.setEnabled(false); // Disable input field
                }
            }
        });
    }

    public void startReading() {
        // Thread to read messages sent by the server
        Runnable r1 = () -> {
            try {
                while (isReading) {
                    String msg = br.readLine();
                    if (msg == null || msg.equalsIgnoreCase("bye") || msg.equalsIgnoreCase("quit") || msg.equalsIgnoreCase("see you later") || msg.equalsIgnoreCase("exit")) {
                        JOptionPane.showMessageDialog(this, "Client terminated the chat.");
                        messageInput.setEnabled(false); // Disable the input field
                        socket.close(); // Close the socket
                        break;
                    }
                    messageArea.append("Client: " + msg + "\n"); // Display the server's message in the message area
                }
            } catch (IOException e) {
                if (isReading) { // Distinguish between intentional closure and unexpected errors
                    System.out.println("Error while reading: " + e.getMessage());
                } else {
                    System.out.println("Connection closed.");
                }
            }
        };
        new Thread(r1).start();
    }

    /*This part of code is use when we use console to take input
       instead of using swing or poppup window
     */
    public void startWriting() {
        // Thread for sending data to the server
        Runnable r2 = () -> {
            try {
                BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String content = read.readLine();
                    pr.println(content);
                    if (content.equalsIgnoreCase("bye") || content.equalsIgnoreCase("exit") || content.equalsIgnoreCase("quit") || content.equalsIgnoreCase("see you later")) {
                        System.out.println("Server terminated the chat.");
                        break;
                    }
                }
                socket.close();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        };
        new Thread(r2).start();
    }

    public static void main(String[] args) {
        System.out.println("Server is ready to talk...");
        new Server();
    }
}