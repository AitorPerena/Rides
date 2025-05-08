package gui;

import java.awt.Color;
import java.net.URL;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import configuration.ConfigXML;
import dataAccess.DataAccess;
import businessLogic.BLFacade;
import businessLogic.BLFacadeImplementation;

public class ApplicationLauncher {
    public static void main(String[] args) {
        ConfigXML c = ConfigXML.getInstance();
        Locale.setDefault(new Locale(c.getLocale()));

        try {
            BLFacade appFacadeInterface;
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

            if (c.isBusinessLogicLocal()) {
                DataAccess da = new DataAccess();
                if (c.isDatabaseInitialized()) {
                    da.initializeDB();
                }
                appFacadeInterface = new BLFacadeImplementation(da);
            } else {
                String serviceName = "http://" + c.getBusinessLogicNode() + ":" + c.getBusinessLogicPort() + "/ws/" + c.getBusinessLogicName() + "?wsdl";
                URL url = new URL(serviceName);
                QName qname = new QName("http://businessLogic/", "BLFacadeImplementationService");
                Service service = Service.create(url, qname);
                appFacadeInterface = service.getPort(BLFacade.class);
            }

            // Configurar la lógica de negocio ANTES de crear la GUI
            MainGUI.setBussinessLogic(appFacadeInterface);
            
            // Iniciar la interfaz gráfica
            SwingUtilities.invokeLater(() -> {
                MainGUI mainGUI = new MainGUI();
                mainGUI.setVisible(true);
            });

        } catch (Exception e) {
            System.err.println("Error in ApplicationLauncher: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error al iniciar la aplicación: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}