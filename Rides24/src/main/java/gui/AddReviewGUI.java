package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import businessLogic.BLFacade;
import domain.User;

public class AddReviewGUI extends JFrame {
    private JComboBox<User> userComboBox;
    private JSpinner ratingSpinner;
    private JTextArea commentArea;
    private JButton submitButton;
    private User loggedInUser;
    private ResourceBundle bundle;

    public AddReviewGUI(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.bundle = ResourceBundle.getBundle("Etiquetas");
        
        setTitle(bundle.getString("AddReviewGUI.Title"));
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
        add(new JLabel(bundle.getString("AddReviewGUI.SelectUser")));
        add(userComboBox);

        ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        add(new JLabel(bundle.getString("AddReviewGUI.Rating")));
        add(ratingSpinner);


        commentArea = new JTextArea();
        add(new JLabel(bundle.getString("AddReviewGUI.Comment")));
        add(new JScrollPane(commentArea));

        submitButton = new JButton(bundle.getString("AddReviewGUI.Submit"));
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submitReview();
            }
        });
        add(submitButton);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void submitReview() {
        User reviewedUser = (User) userComboBox.getSelectedItem();
        int rating = (int) ratingSpinner.getValue();
        String comment = commentArea.getText().trim();

        BLFacade facade = MainGUI.getBusinessLogic();
        boolean success = facade.addReview(loggedInUser, reviewedUser, rating, comment);

        if (success) {
            JOptionPane.showMessageDialog(this, 
                bundle.getString("AddReviewGUI.Success"),
                bundle.getString("Success"), 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                bundle.getString("AddReviewGUI.Error"),
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}