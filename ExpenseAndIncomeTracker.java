import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Double.parseDouble;

public class ExpenseAndIncomeTracker {
    //Variable for the main frame and ui componenets
    private JFrame frame;
    private JPanel titleBar;
    private JLabel titleLabel;
    private JLabel closeLabel;
    private JLabel minimizeLabel;
    private  JPanel dashboardPanel;
    private JPanel buttonsPanel;
    private JButton addTransactionButton;
    private JButton removeTransactionButton;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    //variable to store totalAmount
    private double totalAmount=0.0;
    //ArrayList to store data panel values
    private ArrayList<String> dataPanelValues=new ArrayList<>();
    //Variables for form dragging;
    private boolean isDragging=false;
    private Point mouseOffset;

  //Constructor
    public ExpenseAndIncomeTracker() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true);//Remove form border and default close and minimize icons(buttons)

        //Set Custom border to the frame
        frame.getRootPane().setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(52,73,94)));
        titleBar = new JPanel();
        titleBar.setLayout(null);
        titleBar.setBackground(new Color(52,73,94));
        titleBar.setPreferredSize(new Dimension(frame.getWidth(),30));
        frame.add(titleBar,BorderLayout.NORTH);



        //create  and set up a title label;
        titleLabel = new JLabel("Expense And Income Tracker");
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setBounds(10,0,250,30);
        titleBar.add(titleLabel);


        //Create and set up close Label
        closeLabel = new JLabel("x");
        closeLabel.setForeground(Color.white);
        closeLabel.setFont(new Font("Arial", Font.BOLD, 17));
        closeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        closeLabel.setBounds(frame.getWidth()-80,0,30,30);
        closeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        titleBar.add(closeLabel);

        //add mouse listeners for close label interactions
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                closeLabel.setForeground(Color.red);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setForeground(Color.white);
            }
        });

        //Create and set up minimize Label
        minimizeLabel = new JLabel("-");
        minimizeLabel.setForeground(Color.white);
        minimizeLabel.setFont(new Font("Arial", Font.BOLD, 17));
        minimizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        minimizeLabel.setBounds(frame.getWidth()-50,0,30,30);
        minimizeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        titleBar.add(minimizeLabel);

//add mousse listener for minimize label
        minimizeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               frame.setState(JFrame.ICONIFIED);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                minimizeLabel.setForeground(Color.red);
            }
            @Override
            public void mouseExited(MouseEvent e) {
               minimizeLabel.setForeground(Color.white);
            }
        });
//Mouse listener for window dragging
        titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isDragging=true;
                mouseOffset=e.getPoint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging=false;
            }
        });

  //Mouse motion listener for window dragging
        titleBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(isDragging){
                    //when the mouse is dragged this event is triggered
                    //Get the current location of the mouse on the screen
                   Point newLocation=e.getLocationOnScreen();
                   //Calculate the new window Location by adjusting for the initial mouse offset
                   newLocation.translate(-mouseOffset.x, -mouseOffset.y);
                   frame.setLocation(newLocation);
                }
            }
        });

 //Create and set up the dashboard panel
        dashboardPanel = new JPanel();
         dashboardPanel.setLayout(new FlowLayout(FlowLayout.CENTER,20,20));
         dashboardPanel.setBackground(new Color(236,240,241));
         frame.add(dashboardPanel,BorderLayout.CENTER);
         //Calculate total amount and populate data panel values
          totalAmount=TransactionValueCalculation.getTotalValue(TransactionDAO.getAllTransactions());
          dataPanelValues.add(String.format("$%,.2f",TransactionValueCalculation.getTotalExpenses(TransactionDAO.getAllTransactions())));
        dataPanelValues.add(String.format("$%,.2f",TransactionValueCalculation.getTotalIncomes(TransactionDAO.getAllTransactions())));
        dataPanelValues.add("$"+totalAmount);





         //Add data panels for Expense,Income,and Total
        addDataPanel("Expense",0);
        addDataPanel("Income",1);
        addDataPanel("Total",2);

       //Create and set up button panel
       addTransactionButton = new JButton("Add Transaction");
       addTransactionButton.setBackground(new Color(41,120,185));
       addTransactionButton.setForeground(Color.white);
       addTransactionButton.setFocusPainted(false);
       addTransactionButton.setBorderPainted(false);
       addTransactionButton.setFont(new Font("Arial", Font.BOLD, 14));
       addTransactionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
       addTransactionButton.addActionListener((e)->showAddTransactionDialog());

       removeTransactionButton = new JButton("Remove Transaction");
       removeTransactionButton.setBackground(new Color(231,76,60));
       removeTransactionButton.setForeground(Color.white);
       removeTransactionButton.setFocusPainted(false);
       removeTransactionButton.setBorderPainted(false);
       removeTransactionButton.setFont(new Font("Arial", Font.BOLD, 14));
       removeTransactionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
       removeTransactionButton.addActionListener((e)->{
        removeSelectedTransaction();
       });

       buttonsPanel=new JPanel();
       buttonsPanel.setLayout(new BorderLayout(10,5));
       buttonsPanel.add(addTransactionButton,BorderLayout.NORTH);
       buttonsPanel.add(removeTransactionButton,BorderLayout.SOUTH);
       dashboardPanel.add(buttonsPanel);

       //set up the transaction table
        String[] columnNames = {"ID","Type","Description","Amount"};
        tableModel = new DefaultTableModel(columnNames,0);
        transactionTable = new JTable(tableModel);
        configureTransaction();
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setPreferredSize(new Dimension(750,300));
        dashboardPanel.add(scrollPane);
        frame.setVisible(true);

    }
    //Remove the selected transaction from the table and database
     private void removeSelectedTransaction(){
        int selectedRow=transactionTable.getSelectedRow();
        if(selectedRow!=-1){
            int transactionID=(int)transactionTable.getValueAt(selectedRow,0);
            String type=transactionTable.getValueAt(selectedRow,1).toString();

            String amountStr= transactionTable.getValueAt(selectedRow,3).toString();
           double amount= parseDouble(amountStr.replace("$","").replace(" ","").replace(",",""));
           //Update totalAmount based on the type of the transaction
           if(type.equals("Income")){
               totalAmount-=amount;
           }else {
               totalAmount+=amount;
           }
           //Repaint the total panel to reflect the updated total amount
           JPanel totalPanel= (JPanel) dashboardPanel.getComponent(2);
           totalPanel.repaint();

           //determine the index of the data panel to update(0 for expense ,1 for income)
           int indexToUpdate=type.equals("Income")?1:0;

           //update the data panel value and repaint it
           String currentValue=dataPanelValues.get(indexToUpdate);
           double currentAmount= Double.parseDouble(currentValue.replace("$","")

                   .replace(",","")
                   .replace("--","-"));

            double updateAmount;
            if(type.equals("Income")){
                updateAmount=currentAmount-amount;
            }else{
                updateAmount=currentAmount+amount;
            }
            dataPanelValues.set(indexToUpdate,String.format("$%,.2f",updateAmount));
//repaint corresponding data panel
           JPanel dataPanel= (JPanel) dashboardPanel.getComponent(indexToUpdate);
           dataPanel.repaint();
           //remove transaction from table model
           tableModel.removeRow(selectedRow);
           //remove transaction from database
           removeTransactionFromDataBase(transactionID);
        }
     }
    //Remove a transaction from the database
    private void removeTransactionFromDataBase(int transactionId) {

        try {
            Connection connection=DataBaseConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement("DELETE FROM `transaction_table` WHERE `Id`=?");
            ps.setInt(1,transactionId);
            ps.executeLargeUpdate();
            System.out.println("Transaction removed");
        } catch (SQLException ex) {
            Logger.getLogger(ExpenseAndIncomeTracker.class.getName()).log(Level.SEVERE,null,ex);
        }
    }

    //Displays the dialog for adding a new transaction
    private void showAddTransactionDialog(){
        //Create JDialog for adding transaction
        JDialog dialog = new JDialog(frame, "Add Transaction", true);
        dialog.setSize(350,250);
        dialog.setLocationRelativeTo(frame);

        //Create a panel to hold the components in a grid layout
        JPanel dialogPanel = new JPanel(new GridLayout(4,0,10,10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialogPanel.setBackground(Color.LIGHT_GRAY);

        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense","Income"});
        typeComboBox.setBackground(Color.WHITE);
        typeComboBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField();
       descriptionField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();
        amountField.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    //Create and configure the "ADD" button
        JButton addTransactionButton = new JButton("Add Transaction");
        addTransactionButton.setBackground(new Color(41,120,185));
        addTransactionButton.setForeground(Color.white);
        addTransactionButton.setFocusPainted(false);
        addTransactionButton.setBorderPainted(false);
        addTransactionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addTransactionButton.addActionListener((e)-> {
             addTransaction(typeComboBox,descriptionField,amountField);
        });
  //Add componenet to the dialog panel
        dialogPanel.add(typeLabel);
        dialogPanel.add(typeComboBox);
        dialogPanel.add(descriptionLabel);
        dialogPanel.add(descriptionField);
        dialogPanel.add(amountLabel);
        dialogPanel.add(amountField);
        dialogPanel.add(new JLabel(""));//adding space
        dialogPanel.add(addTransactionButton);

        DataBaseConnection.getConnection();
        dialog.add(dialogPanel);
        dialog.setVisible(true);

    }
    //Add new transaction to the database
    private void addTransaction(JComboBox<String> typeComboBox,JTextField descriptionField,JTextField amountField) {
        //Retrieve transaction details from the input field
        String type= (String) typeComboBox.getSelectedItem();
        String description=descriptionField.getText();
        String amount=amountField.getText();
        double newAmount = parseDouble(amount.replace("$", "").replace("", "").replace(",", ""));
        //Update the total amount based on the transaction type(income or expense)
        if(type.equals("Income")){//if the transaction was income
            totalAmount+=newAmount;
        }else {//if the transaction was expense
            totalAmount-=newAmount;
        }
        //Update the displayed total amount on the dashboard panel
         JPanel totalPanel = (JPanel)dashboardPanel.getComponent(2);
        totalPanel.repaint();
        //Determine the index of the data panel to update based on the transaction type
        int indexToUpdate = type.equals("Income")? 1:0;
        //Retrieve the current value of the data panel
        String currentValue= dataPanelValues.get(indexToUpdate);

        double currentAmount= parseDouble(currentValue.replace("$", "").replace(" ", "").replace(",", ""));
        //calculate the updated amount based on the transaction type
        double updateAmount=currentAmount+(type.equals("Income")?newAmount:-newAmount);
        //Update the data panel with the new amount
        dataPanelValues.set(indexToUpdate,String.format("$%,.2f",updateAmount));
        //Update the displayed data panel on the dashboard panel
        JPanel dataPanel =(JPanel) dashboardPanel.getComponent(indexToUpdate);
        dataPanel.repaint();
        try{
            Connection connection=DataBaseConnection.getConnection();
            String insertQuery = "INSERT INTO `transaction_table` (`transaction_type`, `description`, `amount`) VALUES (?, ?, ?)";
            PreparedStatement ps=connection.prepareStatement(insertQuery);
            ps.setString(1,type);
            ps.setString(2,description);
            ps.setDouble(3, parseDouble(amount));
            ps.executeUpdate();
            System.out.println("Data inserted successfully");
            tableModel.setRowCount(0);
            populateTableTransactions();
        }catch (SQLException ex){
            System.out.println(" Error = Data insertion failed");
        }

    }
    //Populate Table Transactions
    private void populateTableTransactions(){
        for(Transaction transaction:TransactionDAO.getAllTransactions()){
            Object[] rowData = {transaction.getId(),transaction.getType(),transaction.getDescription(),transaction.getAmount()};
            tableModel.addRow(rowData);
        }
    }


    //configure the appearance and behavior of the transaction table
    private void configureTransaction(){
        transactionTable.setBackground(new Color(236,240,241));
        transactionTable.setRowHeight(30);
        transactionTable.setShowGrid(false);//the hide the rows we set from displaying until we make it appear using button
        transactionTable.setBorder(null);
        transactionTable.setDefaultRenderer(Object.class,new TransactionTableCellRender());
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        populateTableTransactions();
        JTableHeader tableHeader=transactionTable.getTableHeader();
        tableHeader.setForeground(Color.red);
        tableHeader.setFont(new Font("Arial",Font.BOLD,18));
        tableHeader.setDefaultRenderer(new GradientHeaderRender());
    }

    //***********
 //add a data panel to the dashboard panel
    private void addDataPanel(String title,int index){
        //Create a new JPanel for the data panel
        JPanel dataPanel=new JPanel(){
            //Override the paintComponent method to customize the appearance
            @Override
            protected void paintComponent(Graphics g){
                //call the paintComponent method of the superclass
                super.paintComponent(g);
                Graphics2D g2d= (Graphics2D) g;
                //make the drawing smooth
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                //check if the title is "Total" to determine the content to display
                if(title.equals("Total")){
                    //if the title is "Total" ,draw the data panel with the total amount
                    drawDataPanel(g2d,title,String.format("$%,.2f",totalAmount),getWidth(),getHeight());
                }else{
                    //if the title is not "Total" draw the data panel with the corresponding value from the list
                    drawDataPanel(g2d,title,dataPanelValues.get(index),getWidth(),getHeight());
                }
            }
        };
     // Set the layout, size,background color,and border for the data panel
        dataPanel.setLayout(new GridLayout(2,1));
        dataPanel.setPreferredSize(new Dimension(170,100));
        dataPanel.setBackground(new Color(255,255,255));
        dataPanel.setBorder(new LineBorder(new Color(149,165,166),2));
        dashboardPanel.add(dataPanel);
    }

    //Draw a data panel with specified title and value
    private void drawDataPanel(Graphics g,String title,String value,int width,int height){
        Graphics2D g2d=(Graphics2D)g;
        //draw the panel
        g2d.setColor(new Color(255,255,255));
        g2d.fillRoundRect(0,0,width,height,20,20);

        g2d.setColor(new Color(236,240,241));
        g2d.fillRect(0,0,width,40);
        //draw title
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Arial",Font.BOLD,20));
        g2d.drawString(title,20,30);

     // draw value
        g2d.setColor(Color.black);
        g2d.setFont(new Font("Arial",Font.PLAIN,16));
        g2d.drawString(value,20,75);



    }

    //Custom cell renderer for the transaction table

}

