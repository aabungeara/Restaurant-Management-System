/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import repositories.TableRepo;
import java.util.List;
import model.RestaurantTable;
import util.Session;

/**
 *
 * @author hp
 */
public class TableService {
    
    public List<RestaurantTable> getUserTables() {
        return TableRepo.getAllTables(Session.getUserId());
    }

    public void addTable(RestaurantTable table) throws Exception {
        TableRepo.insertTable(table, Session.getUserId());
    }

    public void updateTable(RestaurantTable table) throws Exception {
        TableRepo.updateTable(table, Session.getUserId());
    }

    public void deleteTable(int id) throws Exception {
        TableRepo.deleteTable(id, Session.getUserId());
    }

    public boolean isDuplicate(int tableNumber, int currentId) throws Exception {
        return TableRepo.tableNumberExists(tableNumber, currentId, Session.getUserId());
    }
}
