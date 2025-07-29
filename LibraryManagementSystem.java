import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;

class Book {
    String title, author, isbn;
    double price;
    boolean isIssued;

    Book(String title, String author, String isbn, double price, boolean isIssued) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.isIssued = isIssued;
    }
}

public class LibraryManagementSystem extends JFrame {
    private JTextField titleField, authorField, isbnField, priceField;
    private DefaultTableModel tableModel;
    private final ArrayList<Book> books = new ArrayList<>();

    public LibraryManagementSystem() {
        setTitle("PUBLIC LIBRARY MANAGEMENT SYSTEM");
        setSize(900, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        createUI();
        setVisible(true);
    }

    private void createUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

       
        JPanel formPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Book Entry Form"));
        formPanel.setBackground(new Color(232, 245, 233));

        Font font = new Font("Segoe UI", Font.BOLD, 13);
        titleField = new JTextField();
        authorField = new JTextField();
        isbnField = new JTextField();
        priceField = new JTextField();

        JButton addButton = new JButton("Add Book");
        styleButton(addButton);

        formPanel.add(createLabeled("Title:", font));
        formPanel.add(titleField);
        formPanel.add(createLabeled("Author:", font));
        formPanel.add(authorField);
        formPanel.add(createLabeled("ISBN:", font));
        formPanel.add(isbnField);
        formPanel.add(createLabeled("Price:", font));
        formPanel.add(priceField);
        formPanel.add(new JLabel(""));
        formPanel.add(addButton);

        add(formPanel, BorderLayout.NORTH);

        // ==== Center Panel: Table ====
        String[] columns = {"Title", "Author", "ISBN", "Price", "Issued"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane tableScroll = new JScrollPane(table);
        add(tableScroll, BorderLayout.CENTER);

        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton issueButton = new JButton("Issue");
        JButton deleteButton = new JButton("Delete Book");
        JButton clearButton = new JButton("Clear All");
        JButton generateButton = new JButton("Generate Report");

        for (JButton btn : new JButton[]{issueButton, deleteButton, clearButton, generateButton}) {
            styleButton(btn);
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.SOUTH);

 
        addButton.addActionListener(e -> addBook());
        issueButton.addActionListener(e -> toggleIssue(table));
        deleteButton.addActionListener(e -> deleteSelected(table));
        clearButton.addActionListener(e -> clearAll());
        generateButton.addActionListener(e -> generateReport());
    }

    private JLabel createLabeled(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(new Color(63, 81, 181));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            Book book = new Book(title, author, isbn, price, false);
            books.add(book);
            tableModel.addRow(new Object[]{title, author, isbn, formatCurrency(price), "No"});

            titleField.setText("");
            authorField.setText("");
            isbnField.setText("");
            priceField.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price.");
        }
    }

    private void toggleIssue(JTable table) {
        int selected = table.getSelectedRow();
        if (selected != -1) {
            Book book = books.get(selected);
            book.isIssued = !book.isIssued;
            tableModel.setValueAt(book.isIssued ? "Yes" : "No", selected, 4);
        } else {
            JOptionPane.showMessageDialog(this, "Select a book to issue/return.");
        }
    }

    private void deleteSelected(JTable table) {
        int selected = table.getSelectedRow();
        if (selected != -1) {
            books.remove(selected);
            tableModel.removeRow(selected);
        } else {
            JOptionPane.showMessageDialog(this, "Select a book to delete.");
        }
    }

    private void clearAll() {
        books.clear();
        tableModel.setRowCount(0);
    }

    private void generateReport() {
        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No books available.");
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("===== Library Book Report =====\n");
        report.append("Date: ").append(java.time.LocalDate.now()).append("\n\n");

        double totalIssuedValue = 0;

        for (Book book : books) {
            report.append(String.format("Title: %s | Author: %s | ISBN: %s | Price: %s | Issued: %s\n",
                    book.title, book.author, book.isbn,
                    formatCurrency(book.price),
                    book.isIssued ? "Yes" : "No"));
            if (book.isIssued) {
                totalIssuedValue += book.price;
            }
        }

        report.append("\n----------------------------------\n");
        report.append("Total Issued Value: ").append(formatCurrency(totalIssuedValue)).append("\n");
        report.append("Thank you for using the library!");

        JOptionPane.showMessageDialog(this, report.toString(), "Report", JOptionPane.INFORMATION_MESSAGE);
        saveReportToFile(report.toString());
    }

    private void saveReportToFile(String content) {
        try (FileWriter writer = new FileWriter("library_report.txt")) {
            writer.write(content);
            JOptionPane.showMessageDialog(this, "Report saved as library_report.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save report.");
        }
    }

    private String formatCurrency(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryManagementSystem::new);
    }
}