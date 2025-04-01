package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import businessLogic.BLFacade;
import dataAccess.DataAccess;
import domain.User;
import java.util.List;

public class AddReviewGUI extends JFrame {
    private JComboBox<User> userComboBox;
    private JSpinner ratingSpinner;
    private JTextArea commentArea;
    private JButton submitButton;
    private User loggedInUser;

    public AddReviewGUI(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        setTitle("Añadir Reseña");
        setSize(400, 300);
        setLayout(new GridLayout(5, 1));

        userComboBox = new JComboBox<>();
        BLFacade facade = MainGUI.getBusinessLogic();
        List<User> users = facade.getAllUsers(); 
        for (User user : users) {
            if (!user.getEmail().equals(loggedInUser.getEmail())) { 
                userComboBox.addItem(user);
            }
        }
        add(new JLabel("Selecciona el usuario a reseñar:"));
        add(userComboBox);

        ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        add(new JLabel("Puntuación (1-5):"));
        add(ratingSpinner);

        commentArea = new JTextArea();
        add(new JLabel("Comentario:"));
        add(new JScrollPane(commentArea));

        submitButton = new JButton("Enviar Reseña");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                User reviewedUser = (User) userComboBox.getSelectedItem();
                int rating = (int) ratingSpinner.getValue();
                String comment = commentArea.getText();

                BLFacade facade = MainGUI.getBusinessLogic();
                boolean success = facade.addReview(loggedInUser, reviewedUser, rating, comment);

                if (success) {
                    JOptionPane.showMessageDialog(AddReviewGUI.this, "Rese�a a�adida con �xito");
                } else {
                    JOptionPane.showMessageDialog(AddReviewGUI.this, "Error al a�adir la rese�a");
                }
            }
        });
        add(submitButton);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}