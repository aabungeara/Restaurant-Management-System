package service;

import java.util.List;
import model.Bill;
import repositories.BillRepo;
import util.Session;

public class BillService {

    public List<Bill> getBills() {

        return BillRepo.getAllBills(
                Session.getUserId()
        );
    }

    public void addBill(Bill bill) {

        bill.setUserId(
                Session.getUserId()
        );

        BillRepo.insertBill(bill);
    }

    public void updateBill(Bill bill) {

        BillRepo.updateBill(bill);
    }

    public void deleteBill(int id) {

        BillRepo.deleteBill(id);
    }

    public boolean billExistsForTable(int tableId, int userId) {
        return BillRepo.billExistsForTable(tableId, userId);
    }

    public boolean pendingBillExistsForTable(int tableId, int userId) {
        return BillRepo.pendingBillExistsForTable(tableId, userId);
    }
}
