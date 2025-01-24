import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.smartcardio.*;
import java.io.IOException;
import java.util.List;
@WebServlet("/scanNFC")
public class NFCReaderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            // Get the terminal factory
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            // Check if any NFC readers are available
            if (terminals.isEmpty()) {
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"No NFC readers found.\"}");
                return;
            }
            // Use the first terminal (NFC reader)
            CardTerminal terminal = terminals.get(0);
            // Wait for a card to be present
            terminal.waitForCardPresent(0);
            // Connect to the card
            Card card = terminal.connect("*");
            CardChannel channel = card.getBasicChannel();
            // Send a command to read the serial number (example APDU command)
            byte[] command = {(byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00};  // This is a common command to get UID
            ResponseAPDU response = channel.transmit(new CommandAPDU(command));
            // Extract the serial number from the response
            byte[] data = response.getData();
            String serialNumber = bytesToHex(data);
            // Return the serial number as JSON
            resp.getWriter().write("{\"status\":\"success\",\"serialNumber\":\"" + serialNumber + "\"}");
            // Disconnect the card
            card.disconnect(false);
        } catch (CardException e) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Error reading NFC tag: " + e.getMessage() + "\"}");
        }
    }
    // Helper method to convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));  // Convert each byte to two-character hex
        }
        return sb.toString();
    }
}