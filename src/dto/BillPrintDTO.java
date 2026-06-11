package dto;

import java.util.List;

public class BillPrintDTO {

    private int billId;
    private int tableNumber;
    private String status;
    private double total;
    private List<BillItemDTO> items;

    public BillPrintDTO(int billId, int tableNumber, String status, double total, List<BillItemDTO> items) {
        this.billId = billId;
        this.tableNumber = tableNumber;
        this.status = status;
        this.total = total;
        this.items = items;
    }

    public int getBillId() {
        return billId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public String getStatus() {
        return status;
    }

    public double getTotal() {
        return total;
    }

    public List<BillItemDTO> getItems() {
        return items;
    }

    public int getItemsCount() {
        return items.size();
    }
}
