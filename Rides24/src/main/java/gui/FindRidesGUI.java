package gui;

import businessLogic.BLFacade;
import configuration.UtilDate;

import com.toedter.calendar.JCalendar;
import domain.Ride;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class FindRidesGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private JComboBox<String> jComboBoxOrigin = new JComboBox<>();
    private DefaultComboBoxModel<String> originLocations = new DefaultComboBoxModel<>();

    private JComboBox<String> jComboBoxDestination = new JComboBox<>();
    private DefaultComboBoxModel<String> destinationCities = new DefaultComboBoxModel<>();

    private JLabel jLabelOrigin = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.LeavingFrom"));
    private JLabel jLabelDestination = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.GoingTo"));
    private final JLabel jLabelEventDate = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.RideDate"));
    private final JLabel jLabelEvents = new JLabel(ResourceBundle.getBundle("Etiquetas").getString("CreateRideGUI.Rides"));

    private JButton jButtonClose = new JButton(ResourceBundle.getBundle("Etiquetas").getString("Close"));

    // Code for JCalendar
    private JCalendar jCalendar1 = new JCalendar();
    private Calendar calendarAnt = null;
    private Calendar calendarAct = null;
    private JScrollPane scrollPaneEvents = new JScrollPane();

    private List<Date> datesWithRidesCurrentMonth = new Vector<>();

    private JTable tableRides = new JTable();
    private DefaultTableModel tableModelRides;

    private String[] columnNamesRides = new String[]{
            ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.Driver"),
            ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.NPlaces"),
            ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.Price")
    };

    public FindRidesGUI() {
        this.getContentPane().setLayout(null);
        this.setSize(new Dimension(700, 500));
        this.setTitle(ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.FindRides"));

        jLabelEventDate.setBounds(new Rectangle(457, 6, 140, 25));
        jLabelEvents.setBounds(172, 229, 259, 16);

        this.getContentPane().add(jLabelEventDate, null);
        this.getContentPane().add(jLabelEvents);

        jButtonClose.setBounds(new Rectangle(274, 419, 130, 30));

        jButtonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
            }
        });

        BLFacade facade = MainGUI.getBusinessLogic();
        List<String> origins = facade.getDepartCities();

        tableModelRides = new DefaultTableModel(null, columnNamesRides) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableRides = new JTable(tableModelRides);

        // Añadir la tabla a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(tableRides);
        scrollPane.setBounds(172, 257, 346, 150);
        this.getContentPane().add(scrollPane);

        // Llenar el ComboBox de origen
        for (String location : origins) {
            originLocations.addElement(location);
        }

        jLabelOrigin.setBounds(new Rectangle(6, 56, 92, 20));
        jLabelDestination.setBounds(6, 81, 61, 16);
        getContentPane().add(jLabelOrigin);
        getContentPane().add(jLabelDestination);

        jComboBoxOrigin.setModel(originLocations);
        jComboBoxOrigin.setBounds(new Rectangle(103, 50, 172, 20));

        // Llenar el ComboBox de destino basado en el origen seleccionado
        List<String> aCities = facade.getDestinationCities((String) jComboBoxOrigin.getSelectedItem());
        for (String aciti : aCities) {
            destinationCities.addElement(aciti);
        }

        jComboBoxOrigin.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                destinationCities.removeAllElements();
                BLFacade facade = MainGUI.getBusinessLogic();

                List<String> aCities = facade.getDestinationCities((String) jComboBoxOrigin.getSelectedItem());
                for (String aciti : aCities) {
                    destinationCities.addElement(aciti);
                }
                tableModelRides.getDataVector().removeAllElements();
                tableModelRides.fireTableDataChanged();
            }
        });

        jComboBoxDestination.setModel(destinationCities);
        jComboBoxDestination.setBounds(new Rectangle(103, 80, 172, 20));
        jComboBoxDestination.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                paintDaysWithEvents(jCalendar1, datesWithRidesCurrentMonth, new Color(210, 228, 238));

                BLFacade facade = MainGUI.getBusinessLogic();
                datesWithRidesCurrentMonth = facade.getThisMonthDatesWithRides(
                        (String) jComboBoxOrigin.getSelectedItem(),
                        (String) jComboBoxDestination.getSelectedItem(),
                        jCalendar1.getDate()
                );
                paintDaysWithEvents(jCalendar1, datesWithRidesCurrentMonth, Color.CYAN);
            }
        });

        this.getContentPane().add(jButtonClose, null);
        this.getContentPane().add(jComboBoxOrigin, null);
        this.getContentPane().add(jComboBoxDestination, null);

        jCalendar1.setBounds(new Rectangle(300, 50, 225, 150));

        // Code for JCalendar
        jCalendar1.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertychangeevent) {
                if (propertychangeevent.getPropertyName().equals("locale")) {
                    jCalendar1.setLocale((Locale) propertychangeevent.getNewValue());
                } else if (propertychangeevent.getPropertyName().equals("calendar")) {
                    calendarAnt = (Calendar) propertychangeevent.getOldValue();
                    calendarAct = (Calendar) propertychangeevent.getNewValue();

                    DateFormat dateformat1 = DateFormat.getDateInstance(1, jCalendar1.getLocale());

                    int monthAnt = calendarAnt.get(Calendar.MONTH);
                    int monthAct = calendarAct.get(Calendar.MONTH);

                    if (monthAct != monthAnt) {
                        if (monthAct == monthAnt + 2) {
                            calendarAct.set(Calendar.MONTH, monthAnt + 1);
                            calendarAct.set(Calendar.DAY_OF_MONTH, 1);
                        }

                        jCalendar1.setCalendar(calendarAct);
                    }

                    try {
                        tableModelRides.setDataVector(null, columnNamesRides);
                        tableModelRides.setColumnCount(4); // another column added to allocate ride objects

                        BLFacade facade = MainGUI.getBusinessLogic();
                        List<Ride> rides = facade.getRides(
                                (String) jComboBoxOrigin.getSelectedItem(),
                                (String) jComboBoxDestination.getSelectedItem(),
                                UtilDate.trim(jCalendar1.getDate())
                        );

                        if (rides.isEmpty()) {
                            jLabelEvents.setText(ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.NoRides") + ": " + dateformat1.format(calendarAct.getTime()));
                        } else {
                            jLabelEvents.setText(ResourceBundle.getBundle("Etiquetas").getString("FindRidesGUI.Rides") + ": " + dateformat1.format(calendarAct.getTime()));
                        }

                        for (Ride ride : rides) {
                            Vector<Object> row = new Vector<>();
                            row.add(ride.getDriver().getEmail());  // email del conductor
                            row.add(ride.getnPlaces());  // Asientos disponibles
                            row.add(ride.getPrice());  // Precio
                            row.add(ride);  // Objeto Ride para referencia
                            tableModelRides.addRow(row);
                        }

                        datesWithRidesCurrentMonth = facade.getThisMonthDatesWithRides(
                                (String) jComboBoxOrigin.getSelectedItem(),
                                (String) jComboBoxDestination.getSelectedItem(),
                                jCalendar1.getDate()
                        );
                        paintDaysWithEvents(jCalendar1, datesWithRidesCurrentMonth, Color.CYAN);

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    tableRides.getColumnModel().getColumn(0).setPreferredWidth(170);
                    tableRides.getColumnModel().getColumn(1).setPreferredWidth(30);
                    tableRides.getColumnModel().getColumn(2).setPreferredWidth(30);
                    tableRides.getColumnModel().removeColumn(tableRides.getColumnModel().getColumn(3)); // not shown in JTable
                }
            }
        });

        this.getContentPane().add(jCalendar1, null);

        datesWithRidesCurrentMonth = facade.getThisMonthDatesWithRides(
                (String) jComboBoxOrigin.getSelectedItem(),
                (String) jComboBoxDestination.getSelectedItem(),
                jCalendar1.getDate()
        );
        paintDaysWithEvents(jCalendar1, datesWithRidesCurrentMonth, Color.CYAN);
    }

    public static void paintDaysWithEvents(JCalendar jCalendar, List<Date> datesWithEventsCurrentMonth, Color color) {
        Calendar calendar = jCalendar.getCalendar();

        int month = calendar.get(Calendar.MONTH);
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int offset = calendar.get(Calendar.DAY_OF_WEEK);

        if (Locale.getDefault().equals(new Locale("es")))
            offset += 4;
        else
            offset += 5;

        for (Date d : datesWithEventsCurrentMonth) {
            calendar.setTime(d);

            Component o = (Component) jCalendar.getDayChooser().getDayPanel()
                    .getComponent(calendar.get(Calendar.DAY_OF_MONTH) + offset);
            o.setBackground(color);
        }

        calendar.set(Calendar.DAY_OF_MONTH, today);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
    }

    private void jButton2_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }
}