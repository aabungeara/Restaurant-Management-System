package model;

public class User {
    //Attribute
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    //Constructors
    public User(int id, String firstName, String lastName, String email, String passwordHash) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
    }
   //Getters&Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

     public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    //To convert the User object into a line 
    @Override
    public String toString() {
        return id + "," + firstName + "," + lastName + "," + email + "," + passwordHash;
        //return "User{" + "id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", passwordHash=" + passwordHash + '}';
    }
     //To read the text line from the file and convert it into an User object.
    public static User fromString(String line) {
        String[] p = line.split(",");
        return new User(
                Integer.parseInt(p[0]),
                p[1],
                p[2],
                p[3],
                p[4]
        );
    }
}
