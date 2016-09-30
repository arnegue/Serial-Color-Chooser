package cc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.comm.CommDriver;
import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
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

        // Open
        openConnection("COM12");

        // Init Frame

        JFrame frame = new JFrame("USART JColorChooser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = frame.getContentPane();

        final JColorChooser colorChooser = new JColorChooser();

        ColorSelectionModel model = colorChooser.getSelectionModel();

        // If the state changed, get new color and send it via UART
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                Color newForegroundColor = colorChooser.getColor();

                String newRGB = "(" + String.format("%03d", newForegroundColor.getRed()) + ",";
                newRGB += String.format("%03d", newForegroundColor.getGreen()) + ",";
                newRGB += String.format("%03d", newForegroundColor.getBlue()) + ")\n";
                System.out.print("Sending: " + newRGB);

                try {
                    outputStream.write(newRGB.getBytes());
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
        String driverName = "com.sun.comm.Win32Driver";
        try {
            CommDriver commdriver = (CommDriver) Class.forName(driverName).newInstance();
            commdriver.initialize();
        } catch (Exception e2) {
            e2.printStackTrace();
        }

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

        try {
            serialPort = (SerialPort) cid.open("SimpleWriijkteApp", 2000);
        } catch (PortInUseException e) {
            System.err.println("Port already in use!");
            e.printStackTrace();
        }
        try {
            outputStream = serialPort.getOutputStream();
            inputStream = serialPort.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            System.out.println(serialPort.getName());
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

}
