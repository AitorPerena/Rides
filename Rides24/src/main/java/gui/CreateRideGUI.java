package gui;

import java.text.DateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import com.toedter.calendar.JCalendar;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import businessLogic.BLFacade;
import configuration.UtilDate;
import domain.Driver;
import domain.Ride;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

public class CreateRideGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private Driver driver;
    private JTextField fieldOrigin = new JTextField();
    private JTextField fieldDestination = new JTextField();
    private JTextField fieldNewStop = new JTextField();
    private JTextField fieldDistance = new JTextField();

    private JButton btnAddStop = new JButton("Añadir parada");
    private DefaultListModel<String> stopsModel = new DefaultListModel<>();
    private JList<String> stopsList = new JList<>(stopsModel);
    private DefaultListModel<Double> distancesModel = new DefaultListModel<>();
    private JList<Double> distancesList = new JList<>(distancesModel);
    private JCheckBox chkMultiStop = new JCheckBox("Viaje con paradas intermedias");

    private JLabel jLabelOrigin = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.LeavingFrom"));
    private JLabel jLabelDestination = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.GoingTo")); 
    private JLabel jLabelSeats = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.NumberOfSeats"));
    private JLabel jLabRideDate = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.RideDate"));
    private JLabel jLabelPrice = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.Price"));
    private JLabel jLabelNewStop = new JLabel("Nueva parada:");
    private JLabel jLabelDistance = new JLabel("Distancia desde anterior (km):");

    private JTextField jTextFieldSeats = new JTextField();
    private JTextField jTextFieldPrice = new JTextField();

    private JCalendar jCalendar = new JCalendar();
    private Calendar calendarAct = null;
    private Calendar calendarAnt = null;

    private JScrollPane scrollPaneStops = new JScrollPane();
    private JScrollPane scrollPaneDistances = new JScrollPane();

    private JButton jButtonCreate = new JButton(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.CreateRide"));
    private JButton jButtonClose = new JButton(ResourceBundle.getBundle("Etiquetas").getString("Close"));
    private JLabel jLabelMsg = new JLabel();
    private JLabel jLabelError = new JLabel();

    private List<Date> datesWithEventsCurrentMonth;

    public CreateRideGUI(Driver driver) {
        this.driver = driver;
        this.getContentPane().setLayout(null);
        this.setSize(new Dimension(700, 500)); // Aumentamos el tamaño para acomodar nuevos elementos
        this.setTitle(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.CreateRide"));

        // Configuración de componentes existentes
        jLabelOrigin.setBounds(new Rectangle(20, 30, 100, 20));
        fieldOrigin.setBounds(new Rectangle(130, 30, 150, 25));
        
        jLabelDestination.setBounds(new Rectangle(20, 60, 100, 20));
        fieldDestination.setBounds(new Rectangle(130, 60, 150, 25));
        
        jLabelSeats.setBounds(new Rectangle(20, 90, 150, 20));
        jTextFieldSeats.setBounds(new Rectangle(130, 90, 60, 25));
        
        jLabelPrice.setBounds(new Rectangle(20, 120, 150, 20));
        jTextFieldPrice.setBounds(new Rectangle(130, 120, 60, 25));

        jCalendar.setBounds(new Rectangle(350, 30, 300, 150));
        
        // Configuración de componentes para paradas intermedias
        chkMultiStop.setBounds(new Rectangle(20, 160, 200, 25));
        chkMultiStop.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                toggleMultiStopFields(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        
        jLabelNewStop.setBounds(new Rectangle(20, 190, 100, 20));
        fieldNewStop.setBounds(new Rectangle(130, 190, 150, 25));
        
        jLabelDistance.setBounds(new Rectangle(20, 220, 200, 20)); 
        fieldDistance.setBounds(new Rectangle(230, 220, 80, 25)); 
        
        btnAddStop.setBounds(new Rectangle(20, 250, 150, 30));
        btnAddStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addIntermediateStop();
            }
        });
        
        scrollPaneStops.setBounds(new Rectangle(20, 290, 150, 100));
        stopsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneStops.setViewportView(stopsList);
        
        scrollPaneDistances.setBounds(new Rectangle(180, 290, 100, 100));
        distancesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneDistances.setViewportView(distancesList);
        
        // Botones principales
        jButtonCreate.setBounds(new Rectangle(150, 420, 150, 30));
        jButtonCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButtonCreate_actionPerformed(e);
            }
        });
        
        jButtonClose.setBounds(new Rectangle(320, 420, 150, 30));
        jButtonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButtonClose_actionPerformed(e);
            }
        });
        
        jLabelMsg.setBounds(new Rectangle(20, 400, 500, 20));
        jLabelMsg.setForeground(Color.red);
        
        jLabelError.setBounds(new Rectangle(20, 390, 500, 20));
        jLabelError.setForeground(Color.red);

        // Añadir componentes al panel
        this.getContentPane().add(jLabelOrigin);
        this.getContentPane().add(fieldOrigin);
        this.getContentPane().add(jLabelDestination);
        this.getContentPane().add(fieldDestination);
        this.getContentPane().add(jLabelSeats);
        this.getContentPane().add(jTextFieldSeats);
        this.getContentPane().add(jLabelPrice);
        this.getContentPane().add(jTextFieldPrice);
        this.getContentPane().add(jCalendar);
        this.getContentPane().add(chkMultiStop);
        this.getContentPane().add(jLabelNewStop);
        this.getContentPane().add(fieldNewStop);
        this.getContentPane().add(jLabelDistance);
        this.getContentPane().add(fieldDistance);
        this.getContentPane().add(btnAddStop);
        this.getContentPane().add(scrollPaneStops);
        this.getContentPane().add(scrollPaneDistances);
        this.getContentPane().add(jButtonCreate);
        this.getContentPane().add(jButtonClose);
        this.getContentPane().add(jLabelMsg);
        this.getContentPane().add(jLabelError);
        
        // Inicialmente ocultamos los campos de paradas intermedias
        toggleMultiStopFields(false);
        
        BLFacade facade = MainGUI.getBusinessLogic();
        datesWithEventsCurrentMonth = facade.getThisMonthDatesWithRides("a", "b", jCalendar.getDate());
        
        // Configuración del JCalendar (igual que antes)
        jCalendar.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertychangeevent) {
                if (propertychangeevent.getPropertyName().equals("locale")) {
                    jCalendar.setLocale((Locale) propertychangeevent.getNewValue());
                } else if (propertychangeevent.getPropertyName().equals("calendar")) {
                    calendarAnt = (Calendar) propertychangeevent.getOldValue();
                    calendarAct = (Calendar) propertychangeevent.getNewValue();
                    DateFormat dateformat1 = DateFormat.getDateInstance(1, jCalendar.getLocale());
                    
                    int monthAnt = calendarAnt.get(Calendar.MONTH);
                    int monthAct = calendarAct.get(Calendar.MONTH);
                    if (monthAct != monthAnt) {
                        if (monthAct == monthAnt + 2) { 
                            calendarAct.set(Calendar.MONTH, monthAnt + 1);
                            calendarAct.set(Calendar.DAY_OF_MONTH, 1);
                        }
                        jCalendar.setCalendar(calendarAct);
                    }
                    jCalendar.setCalendar(calendarAct);
                }
            }
        });
    }
    
    private void toggleMultiStopFields(boolean visible) {
        jLabelNewStop.setVisible(visible);
        fieldNewStop.setVisible(visible);
        jLabelDistance.setVisible(visible);
        fieldDistance.setVisible(visible);
        btnAddStop.setVisible(visible);
        scrollPaneStops.setVisible(visible);
        scrollPaneDistances.setVisible(visible);
    }
    
    private void addIntermediateStop() {
        String stop = fieldNewStop.getText().trim();
        String distanceStr = fieldDistance.getText().trim();
        
        if (stop.isEmpty() || distanceStr.isEmpty()) {
            jLabelError.setText("Debe especificar una parada y una distancia");
            return;
        }
        
        try {
            double distance = Double.parseDouble(distanceStr);
            if (distance <= 0) {
                jLabelError.setText("La distancia debe ser mayor que 0");
                return;
            }
            
            stopsModel.addElement(stop);
            distancesModel.addElement(distance);
            fieldNewStop.setText("");
            fieldDistance.setText("");
            jLabelError.setText("");
        } catch (NumberFormatException e) {
            jLabelError.setText("La distancia debe ser un número válido");
        }
    }
    
    private void jButtonCreate_actionPerformed(ActionEvent e) {
        jLabelMsg.setText("");
        jLabelError.setText("");
        
        String error = field_Errors();
        if (error != null) {
            jLabelMsg.setText(error);
            return;
        }
        
        try {
            BLFacade facade = MainGUI.getBusinessLogic();
            int inputSeats = Integer.parseInt(jTextFieldSeats.getText());
            float price = Float.parseFloat(jTextFieldPrice.getText());
            Date rideDate = UtilDate.trim(jCalendar.getDate());
            
            if (chkMultiStop.isSelected() && stopsModel.size() == 0) {
                jLabelMsg.setText("Debe añadir al menos una parada intermedia para viajes compuestos");
                return;
            }
            
            Ride r;
            if (chkMultiStop.isSelected()) {
                // Crear viaje con paradas intermedias
                List<String> allStops = new ArrayList<>();
                allStops.add(fieldOrigin.getText());
                for (int i = 0; i < stopsModel.size(); i++) {
                    allStops.add(stopsModel.getElementAt(i));
                }
                allStops.add(fieldDestination.getText());
                
                List<Double> distances = new ArrayList<>();
                for (int i = 0; i < distancesModel.size(); i++) {
                    distances.add(distancesModel.getElementAt(i));
                }
                
                r = facade.createMultiStopRide(driver.getEmail(), allStops, distances, rideDate, inputSeats, price);
            } else {
                // Crear viaje simple
                r = facade.createRide(fieldOrigin.getText(), fieldDestination.getText(), rideDate, inputSeats, price, driver.getEmail());
            }
            
            facade.sendRideReminders(rideDate);
            jLabelMsg.setText(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.RideCreated"));
            
            // Limpiar campos después de crear el viaje
            fieldOrigin.setText("");
            fieldDestination.setText("");
            jTextFieldSeats.setText("");
            jTextFieldPrice.setText("");
            stopsModel.clear();
            distancesModel.clear();
            chkMultiStop.setSelected(false);
            
        } catch (RideMustBeLaterThanTodayException e1) {
            jLabelMsg.setText(e1.getMessage());
        } catch (RideAlreadyExistException e1) {
            jLabelMsg.setText(e1.getMessage());
        } catch (Exception e1) {
            jLabelMsg.setText("Error al crear el viaje: " + e1.getMessage());
            e1.printStackTrace();
        }
    }

    private void jButtonClose_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }
    
    private String field_Errors() {
        try {
            if (fieldOrigin.getText().isEmpty() || fieldDestination.getText().isEmpty() || 
                jTextFieldSeats.getText().isEmpty() || jTextFieldPrice.getText().isEmpty()) {
                return ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.ErrorQuery");
            }
            
            int inputSeats = Integer.parseInt(jTextFieldSeats.getText());
            if (inputSeats <= 0) {
                return ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.SeatsMustBeGreaterThan0");
            }
            
            float price = Float.parseFloat(jTextFieldPrice.getText());
            if (price <= 0) {
                return ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.PriceMustBeGreaterThan0");
            }
            
            return null;
            
        } catch (NumberFormatException e1) {
            return ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.ErrorNumber");
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }
}