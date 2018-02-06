package cc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.comm.CommDriver;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Small program which gets chosen color from color-chooser and send it via Serial
 * @author Frosty
 *
 */
public class Main {
    static OutputStream outputStream;
    static InputStream inputStream;
    static CommPortIdentifier cid;
    static SerialPort serialPort;

    public static void main(String[] args) {
        // Load Driver
        if (System.getProperty("os.name").contains("Windows")) {
            String driverName = "com.sun.comm.Win32Driver";
            try {
                CommDriver commdriver = (CommDriver) Class.forName(driverName).newInstance();
                commdriver.initialize();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
        List<String> portsList = new ArrayList<>();
        while (ports.hasMoreElements()) {
            CommPortIdentifier portIdentifier = ports.nextElement();
            portsList.add(portIdentifier.getName());
        }
        //  System.out.println("Ports: " + availablePorts);
        //String path = JOptionPane.showInputDialog("Enter a port (" + availablePorts + ")");

        String path = (String) JOptionPane.showInputDialog(null, "Chose COM-Port", "COM-Port", JOptionPane.PLAIN_MESSAGE, null, portsList.toArray(), portsList.toArray()[0]);
        // Open
        openConnection(path);

        // Init Frame

        JFrame frame = new JFrame("USART JColorChooser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = frame.getContentPane();

        final JColorChooser colorChooser = new JColorChooser();

        ColorSelectionModel model = colorChooser.getSelectionModel();
        byte sendBytes[] = new byte[7]; // (x,x,x)
        sendBytes[0] = '(';
        sendBytes[2] = ',';
        sendBytes[4] = ',';
        sendBytes[6] = ')';

        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {

                Color newForegroundColor = colorChooser.getColor();
                sendBytes[1] = (byte) (newForegroundColor.getRed() & 0xFF); // red
                sendBytes[3] = (byte) (newForegroundColor.getGreen() & 0xFF); // green
                sendBytes[5] = (byte) (newForegroundColor.getBlue() & 0xFF); // blue

                try {
                    outputStream.write(sendBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        model.addChangeListener(changeListener);
        contentPane.add(colorChooser, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

        // Start a continious receive Thread
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (inputStream.available() > 0) {
                            System.out.print((char) inputStream.read());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(r).start();
    }

    static void openConnection(String port) {
        // It seems like you have to load the driver for this

        // Try to get the port given in parameter
        try {
            cid = CommPortIdentifier.getPortIdentifier(port);
            System.out.println("Found port: " + port);
        } catch (Exception e) {
            System.err.println("Did not find port " + port + ". Available ports are (if there are any): ");

            Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
            while (ports.hasMoreElements()) {
                CommPortIdentifier portIdentifier = ports.nextElement();
                System.err.println(portIdentifier.getName());
            }

            e.printStackTrace();
            System.exit(1);
        }

        // Open the port
        try {
            String appName = "RGB-Trangsmiter";
            int portOpenDelay = 2000;
            serialPort = (SerialPort) cid.open(appName, portOpenDelay); // TODO: Maybe here should be a check if unix or windows-system
        } catch (PortInUseException e) {
            System.err.println("Port already in use!");
            JOptionPane.showMessageDialog(null, e.toString(), "Error - Port already in use!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Get Output- and Inputstream
        try {
            outputStream = serialPort.getOutputStream();
            inputStream = serialPort.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set parameters for Serial port
        try {
            //serialPort.setSerialPortParams(2000000, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            System.out.println("Sending... " + serialPort.getName());
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

}
