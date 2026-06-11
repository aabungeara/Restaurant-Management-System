package service;

import dto.BillItemDTO;
import dto.BillPrintDTO;
import java.util.ArrayList;
import java.util.List;
import model.Bill;
import model.Order;
import repositories.OrderRepo;

public class BillPrintService {

    public static BillPrintDTO buildBill(Bill bill) {

        List<Order> orders = OrderRepo.getOrdersByTable(bill.getTable().getId(),
        bill.getUserId());

        List<BillItemDTO> items = new ArrayList<>();

        for (Order o : orders) {

            items.add(new BillItemDTO(
                    o.getItem().getName(),
                    o.getQuantity(),
                    o.getItem().getPrice()
            ));
        }

        // ✅ احسب total من DTO نفسه
        double total = items.stream()
                .mapToDouble(BillItemDTO::getTotal)
                .sum();

        return new BillPrintDTO(
                bill.getId(),
                bill.getTable().getTableNumber(),
                bill.getPaymentStatus(),
                total,
                items
        );
    }
}
