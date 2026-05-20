/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import repositories.UserRepo;
import model.User;
import util.HashUtil;

/**
 *
 * @author hp
 */
public class UserService {

    public User login(String email, String password) {
        User user = UserRepo.findByEmail(email);
        if (user == null) {
            return null;
        }

        String hashedPassword = HashUtil.md5(password);

        if (!hashedPassword.equals(user.getPasswordHash())) {
            return null;
        }

        return user;
    }

//    public boolean emailExists(String email) {
//        try {
//            return UserRepo.emailExists(email);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    public boolean register(String firstName, String lastName, String email, String password) {

        try {
            if (UserRepo.emailExists(email)) {
                return false;
            }

            String hash = HashUtil.md5(password);

            User user = new User(firstName, lastName, email, hash);

            UserRepo.insertUser(user);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
