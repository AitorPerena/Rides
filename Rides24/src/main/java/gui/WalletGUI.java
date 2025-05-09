package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import businessLogic.BLFacade;
import domain.Driver;
import domain.User;

public class WalletGUI extends JFrame {
    private User user;
    private JLabel balanceLabel;
    private ResourceBundle bundle;

    public WalletGUI(User user) {
        this.user = user;
        this.bundle = ResourceBundle.getBundle("Etiquetas");
        setTitle(bundle.getString("WalletGUI.Title"));
        initializeUI();
    }

    private void initializeUI() {
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 1, 10, 10)); 

        if (user.getWallet() == null) {
            JOptionPane.showMessageDialog(this, 
                bundle.getString("WalletGUI.NoWalletError"), 
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        balanceLabel = new JLabel(
            MessageFormat.format(
                bundle.getString("WalletGUI.BalanceLabel"),
                user.getWallet().getBalance()
            ), 
            JLabel.CENTER
        );
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(balanceLabel);

        JButton addFundsButton = new JButton(bundle.getString("WalletGUI.AddFundsButton"));
        addFundsButton.addActionListener(this::addFunds);
        add(addFundsButton);

        if (user instanceof Driver) {
            JButton withdrawButton = new JButton(bundle.getString("WalletGUI.WithdrawButton"));
            withdrawButton.addActionListener(this::withdrawFunds);
            add(withdrawButton);
        }

        JButton closeButton = new JButton(bundle.getString("WalletGUI.CloseButton"));
        closeButton.addActionListener(e -> dispose());
        add(closeButton);
    }

    private void addFunds(ActionEvent e) {
        String amountStr = JOptionPane.showInputDialog(
            this, 
            MessageFormat.format(
                bundle.getString("WalletGUI.AmountPrompt"),
                bundle.getString("WalletGUI.AddFundsTitle").toLowerCase()
            ), 
            bundle.getString("WalletGUI.AddFundsTitle"), 
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (amountStr == null || amountStr.trim().isEmpty()) return;
        
        try {
            float amount = Float.parseFloat(amountStr);
            if (amount > 0) {
                BLFacade facade = MainGUI.getBusinessLogic();
                if (facade.addFunds(user.getEmail(), amount)) {
                    user.getWallet().addFunds(amount);
                    balanceLabel.setText(
                        MessageFormat.format(
                            bundle.getString("WalletGUI.BalanceLabel"), 
                            user.getWallet().getBalance()
                        )
                    );
                    JOptionPane.showMessageDialog(
                        this, 
                        bundle.getString("WalletGUI.AddFundsSuccess"), 
                        bundle.getString("Success"), 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    showOperationError("añadir");
                }
            } else {
                JOptionPane.showMessageDialog(
                    this, 
                    bundle.getString("WalletGUI.PositiveAmountError"), 
                    bundle.getString("Error"), 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, 
                bundle.getString("WalletGUI.AmountError"), 
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void withdrawFunds(ActionEvent e) {
        String amountStr = JOptionPane.showInputDialog(
            this, 
            MessageFormat.format(
                bundle.getString("WalletGUI.AmountPrompt"),
                bundle.getString("WalletGUI.WithdrawTitle").toLowerCase()
            ), 
            bundle.getString("WalletGUI.WithdrawTitle"), 
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (amountStr == null || amountStr.trim().isEmpty()) return;
        
        try {
            float amount = Float.parseFloat(amountStr);
            if (amount > 0) {
                BLFacade facade = MainGUI.getBusinessLogic();
                if (facade.withdrawFunds(user.getEmail(), amount)) {
                    user.getWallet().deductFunds(amount);
                    balanceLabel.setText(
                        MessageFormat.format(
                            bundle.getString("WalletGUI.BalanceLabel"), 
                            user.getWallet().getBalance()
                        )
                    );
                    JOptionPane.showMessageDialog(
                        this, 
                        bundle.getString("WalletGUI.WithdrawSuccess"), 
                        bundle.getString("Success"), 
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    showOperationError("retirar");
                }
            } else {
                JOptionPane.showMessageDialog(
                    this, 
                    bundle.getString("WalletGUI.PositiveAmountError"), 
                    bundle.getString("Error"), 
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, 
                bundle.getString("WalletGUI.AmountError"), 
                bundle.getString("Error"), 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showOperationError(String operation) {
        JOptionPane.showMessageDialog(
            this, 
            MessageFormat.format(
                bundle.getString("WalletGUI.OperationError"),
                operation
            ), 
            bundle.getString("Error"), 
            JOptionPane.ERROR_MESSAGE
        );
    }
}