import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class LoginSystemGame {
    private final Object databaseLock = new Object();  // Resource 1: Database Lock
    private final Object sessionLock = new Object();   // Resource 2: Session Lock
    private int score = 0;  // Keep track of the score
    private boolean isGameOver = false;
    private int multiplier = 1;  // Multiplier for score

    // Getter for the score
    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public int getMultiplier() {
        return multiplier;
    }

    // Function to handle player choice for login resource acquisition
    public void loginUser(int userId, boolean acquireInCorrectOrder, JTextArea textArea) {
        if (acquireInCorrectOrder) {
            textArea.append("[INFO] User " + userId + " is attempting to log in safely.\n");
            // Correct order: database first, then session
            synchronized (databaseLock) {
                textArea.append("[SUCCESS] User " + userId + " locked the database.\n");
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

                textArea.append("[SUCCESS] User " + userId + " is trying to lock the session.\n");
                synchronized (sessionLock) {
                    textArea.append("[SUCCESS] User " + userId + " locked the session and completed login successfully!\n\n");
                    score += multiplier;  // Increase score for successful login with multiplier
                    multiplier++;  // Increase multiplier after a successful safe login
                }
            }
        } else {
            textArea.append("[WARNING] User " + userId + " is attempting a risky login process.\n");
            // Incorrect order: session first, then database (this might cause a deadlock)
            synchronized (sessionLock) {
                textArea.append("[SUCCESS] User " + userId + " locked the session.\n");
                try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

                textArea.append("[SUCCESS] User " + userId + " is trying to lock the database.\n");
                synchronized (databaseLock) {
                    textArea.append("[SUCCESS] User " + userId + " locked the database and completed login successfully!\n\n");
                    score += 1;  // Increase score for successful risky login, but no multiplier
                }
            }
        }

        // Simulate a deadlock event if certain conditions are met
        if (Math.random() < 0.2) {  // 20% chance of deadlock occurring
            textArea.append("[DEADLOCK] Deadlock occurred! User " + userId + " caused a deadlock.\n");
            isGameOver = true;
        }
    }

    // Method to reset the game
    public void resetGame() {
        score = 0;
        isGameOver = false;
        multiplier = 1;  // Reset multiplier
    }
}

public class DeadlockPreventionGameGUI extends JFrame {
    private LoginSystemGame game;
    private JTextArea textArea;
    private JButton safeButton;
    private JButton riskyButton;
    private JButton restartButton;

    public DeadlockPreventionGameGUI() {
        game = new LoginSystemGame();
        setTitle("Deadlock Prevention Game");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        safeButton = new JButton("Database first, then Session (Safe)");
        riskyButton = new JButton("Session first, then Database (Risky)");
        restartButton = new JButton("Restart Game");
        
        buttonPanel.add(safeButton);
        buttonPanel.add(riskyButton);
        buttonPanel.add(restartButton);
        add(buttonPanel, BorderLayout.SOUTH);

        safeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playGame(true);
            }
        });

        riskyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playGame(false);
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
    }

    private void playGame(boolean acquireInCorrectOrder) {
        if (!game.isGameOver()) {
            // Get user ID from input dialog
            String input = JOptionPane.showInputDialog(this, "Enter User ID:");
            if (input != null && !input.trim().isEmpty()) {
                int userId;
                try {
                    userId = Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid User ID. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Call loginUser directly in the event dispatch thread
                game.loginUser(userId, acquireInCorrectOrder, textArea);
                
                // Show current score and multiplier after the login attempt
                textArea.append("Current score: " + game.getScore() + " | Current Multiplier: " + game.getMultiplier() + "\n");

                // Check for game over condition
                if (game.isGameOver()) {
                    JOptionPane.showMessageDialog(this, "Game Over! Your final score is: " + game.getScore(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    safeButton.setEnabled(false);
                    riskyButton.setEnabled(false);
                }
            }
        }
    }

    private void resetGame() {
        game.resetGame();
        textArea.setText("");  // Clear the text area
        safeButton.setEnabled(true);
        riskyButton.setEnabled(true);
        textArea.append("=== Welcome to the Interactive Deadlock Prevention Game ===\n");
        textArea.append("Your goal is to log in users safely without causing deadlock.\n");
        textArea.append("Current Multiplier: " + game.getMultiplier() + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DeadlockPreventionGameGUI gameGUI = new DeadlockPreventionGameGUI();
                gameGUI.setVisible(true);
            }
        });
    }
}