package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import businessLogic.BLFacade;
import domain.Notification;
import domain.User;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewNotificationsGUI extends JFrame {
    private JTable unreadTable;
    private JTable readTable;
    private User loggedInUser;
    private JButton markAsRead;

    public ViewNotificationsGUI(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        setTitle("Notificaciones");
        setSize(700, 500);
        setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();

        BLFacade facade = MainGUI.getBusinessLogic();
        List<Notification> notifications = facade.getNotificationsForUser(loggedInUser);
        
        
        
        DefaultTableModel unreadModel = createNotificationTableModel(false, notifications);
        DefaultTableModel readModel = createNotificationTableModel(true, notifications);
        
        unreadTable = new JTable(unreadModel);
        unreadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane unreadScrollPane = new JScrollPane(unreadTable);
        tabbedPane.addTab("No le�das", unreadScrollPane);
        
        readTable = new JTable(readModel);
        JScrollPane readScrollPane = new JScrollPane(readTable);
        tabbedPane.addTab("Le�das", readScrollPane);
        
        markAsRead = new JButton("Marcar como leído");
        markAsRead.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		int selectedRow = unreadTable.getSelectedRow();
        		if(selectedRow >= 0) {
        			Integer notificationId = (Integer) unreadModel.getValueAt(selectedRow, 0);
        			boolean success = facade.markNotificationAsRead(notificationId);
        			if(success) {
        				//Actualizar tablas
        				List<Notification> updatedNotifications = facade.getNotificationsForUser(loggedInUser);
        				unreadModel.setDataVector(getTableData(false, updatedNotifications), getColumnNames());
        				readModel.setDataVector(getTableData(true, updatedNotifications), getColumnNames());
        				JOptionPane.showMessageDialog(ViewNotificationsGUI.this, "Notificación marcada como leída.");
        				
        			}else {
        				JOptionPane.showMessageDialog(ViewNotificationsGUI.this, "Selecciona una notificación para marcar como leída");
        			}
        		}
        	}
        });
        
        add(tabbedPane, BorderLayout.CENTER);
        add(markAsRead, BorderLayout.SOUTH);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    private DefaultTableModel createNotificationTableModel(boolean readStatus, List<Notification> notifications) {
    	String[] columnNames = {"ID", "Mensaje", "Leído"};
    	Object[][] data = getTableData(readStatus, notifications);
    	
    	DefaultTableModel model = new DefaultTableModel(data, columnNames) {
    		@Override
    		public Class <?> getColumnClass(int columnIndex){
    			if (columnIndex == 2) return Boolean.class;
    			return super.getColumnClass(columnIndex);
    		}
    		
    		@Override
    		public boolean isCellEditable(int row, int column) {
    			return column ==2;
    			
    		}
    	
    	};
    	return model;
    }
    
    private Object[][] getTableData(boolean readStatus, List<Notification> notifications){
    	return notifications.stream().filter(n -> n.isRead()==readStatus).map(n -> new Object[] {n.getNotificationId(), n.getMessage(), n.isRead()}).toArray(Object[][]::new);

    }
    private String[] getColumnNames() {
    	return new String[] {"ID", "Mensaje", "Leído"};
    }
}