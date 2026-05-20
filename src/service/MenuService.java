/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import repositories.MenuItemRepo;
import java.util.List;
import model.MenuItem;
import util.Session;

/**
 *
 * @author hp
 */
public class MenuService {

    public List<MenuItem> getItems() throws Exception {
        return MenuItemRepo.getAllMenuItems(Session.getUserId());
    }

    public void add(MenuItem item) throws Exception {
        MenuItemRepo.insertMenuItem(item, Session.getUserId());
    }

    public void update(MenuItem item) throws Exception {
        MenuItemRepo.updateMenuItem(item, Session.getUserId());
    }

    public void delete(int id) throws Exception {
        MenuItemRepo.deleteMenuItem(id, Session.getUserId());
    }

    public boolean exists(String name, int currentId) throws Exception {
        return MenuItemRepo.menuItemNameExists(name, currentId, Session.getUserId());
    }

}
