package gui;

import configuration.ConfigXML;
import dataAccess.DataAccess;
import businessLogic.BLFacade;
import businessLogic.BLFacadeImplementation;
import javax.swing.*;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class ApplicationLauncher {
    public static void main(String[] args) {
        ConfigXML c = ConfigXML.getInstance();
        
        try {
            BLFacade appFacadeInterface;
            
            if (c.isBusinessLogicLocal()) {
                // Local mode - initialize DataAccess directly
                DataAccess da = new DataAccess();
                da.open();
                da.createAdminIfNotExists();
                da.close();
                if (c.isDatabaseInitialized()) {
                    da.initializeDB();
                }
                appFacadeInterface = new BLFacadeImplementation(da);
            } else {
                // Remote mode - connect via WS
                String serviceName = "http://" + c.getBusinessLogicNode() + ":" + 
                                    c.getBusinessLogicPort() + "/ws/" + 
                                    c.getBusinessLogicName() + "?wsdl";
                URL url = new URL(serviceName);
                QName qname = new QName("http://businessLogic/", "BLFacadeImplementationService");
                Service service = Service.create(url, qname);
                appFacadeInterface = service.getPort(BLFacade.class);
            }
            
            // Set the BLFacade instance BEFORE creating the GUI
            MainGUI.setBussinessLogic(appFacadeInterface);
            
            // Now create and show the GUI
            SwingUtilities.invokeLater(() -> {
                MainGUI mainGUI = new MainGUI();
                mainGUI.setVisible(true);
            });
            
        } catch (Exception e) {
            System.err.println("Error initializing application: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error al iniciar la aplicación: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}