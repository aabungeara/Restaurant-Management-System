package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;



@Entity
@Table(name = "tables")
public class RestaurantTable {

    //Attribute
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "table_number")
    private int tableNumber;
    private int capacity;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //Constructors

    public RestaurantTable() {
    }

    public RestaurantTable(int tableNumber, int capacity, User user) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.user = user;
    }

    //Getters&Setters
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RestaurantTable{");
        sb.append("id=").append(id);
        sb.append(", tableNumber=").append(tableNumber);
        sb.append(", capacity=").append(capacity);
        sb.append(", userEmail=").append(user.getEmail());
        sb.append('}');
        return sb.toString();
    }

}
