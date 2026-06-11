package service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import dto.BillItemDTO;
import dto.BillPrintDTO;

import java.io.FileOutputStream;

public class PdfExportService {

    public static void exportBill(BillPrintDTO bill, String filePath) throws Exception {

        Document document = new Document(PageSize.A4);

        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        // ===== Title =====
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Restaurant Bill", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        // ===== Bill Info =====
        document.add(new Paragraph("Bill ID: " + bill.getBillId()));
        document.add(new Paragraph("Table: " + bill.getTableNumber()));
        document.add(new Paragraph("Status: " + bill.getStatus()));

        document.add(new Paragraph(" "));

        // ===== Table =====
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        table.addCell("Item");
        table.addCell("Qty");
        table.addCell("Price");
        table.addCell("Total");

        for (BillItemDTO item : bill.getItems()) {

            table.addCell(item.getItemName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.valueOf(item.getPrice()));
            table.addCell(String.valueOf(item.getTotal()));
        }

        document.add(table);

        document.add(new Paragraph(" "));

        // ===== Total =====
        Paragraph total = new Paragraph(
                "TOTAL: $" + String.format("%.2f", bill.getTotal())
        );
        total.setAlignment(Element.ALIGN_RIGHT);

        document.add(total);

        document.close();
    }
}