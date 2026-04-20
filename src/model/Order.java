package model;

public class Order {
    //Attribute
    private int id;
    private int tableId;
    private int itemId;
    private int quantity;
    private String status;
    //to display
    private int tableNumber;
    private String itemName;

    //Constructors
    public Order(int id, int tableId, int itemId, int quantity, String status) {
        this.id = id;
        this.tableId = tableId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.status = status;
    }
    //Getters&Setters
    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    //To convert the Order object into a line 
    @Override
    public String toString() {
        return id + "," + tableId + "," + itemId + "," + quantity + "," + status;
    }
    //To read the text line from the file and convert it into an Order object.
    public static Order fromString(String line) {
        String p[] = line.split(",");

        if (p.length != 5) {
            throw new IllegalArgumentException("Invalid order line: " + line);
        }

        return new Order(
                Integer.parseInt(p[0]),
                Integer.parseInt(p[1]),
                Integer.parseInt(p[2]),
                Integer.parseInt(p[3]),
                p[4]
        );
    }
}
