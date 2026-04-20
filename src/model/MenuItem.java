package model;

public class MenuItem {
    //Attribute
    private int id;
    private String name;
    private double price;
    private String category;
    //Constructors
    public MenuItem(int id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }
    //Getters&Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    //To convert the MenuItem object into a line 
    @Override
    public String toString() {
        return id + "," + name + "," + price + "," + category;
    }
     //To read the text line from the file and convert it into an MenuItem object.
    public static MenuItem fromString(String line){
        String p[] = line.split(",");
        
        if (p.length != 4) {
            throw new IllegalArgumentException("Invalid menu item line: " + line);
        }
        
        return new MenuItem(
                Integer.parseInt(p[0]),
                p[1],
                Double.parseDouble(p[2]),
                p[3]
        );
    }
}
