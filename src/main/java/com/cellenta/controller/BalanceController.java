
package com.cellenta.controller;

import javax.swing.*;

public class BalanceController extends JFrame {

    private String phoneNumber;

    public BalanceController(String phoneNumber) {
        this.phoneNumber = phoneNumber;

        setTitle("Balance Page");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel label = new JLabel("Welcome, your number is: " + phoneNumber);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);
    }
}
