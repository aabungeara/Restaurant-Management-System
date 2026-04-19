package model;

public class RestaurantTable {
    private int id;
    private int tableNumber;
    private int capacity;

    public RestaurantTable(int id, int tableNumber, int capacity) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return id + "," + tableNumber + "," + capacity;
    }
    
    public static RestaurantTable fromString(String line){
        String p[] = line.split(",");
        if (p.length != 3) {
            throw new IllegalArgumentException("Invalid table line: " + line);
        }
        return new RestaurantTable(
                Integer.parseInt(p[0]),
                Integer.parseInt(p[1]),
                Integer.parseInt(p[2])
        );
    }
}
