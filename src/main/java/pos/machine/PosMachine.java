package pos.machine;

import exception.InvalidBarcodeException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PosMachine {

    // Load all items from the ItemsLoader
    private final List<Item> items = ItemsLoader.loadAllItems();

    private final StringBuilder title = new StringBuilder("***<store earning no money>Receipt***\n");

    private final StringBuilder divider = new StringBuilder("----------------------\n");

    private final StringBuilder footer = new StringBuilder("**********************");

    private boolean isValidBarcodes(List<String> barcodes) {
        return barcodes.stream().allMatch(barcode -> items.stream().anyMatch(item -> item.getBarcode().equals(barcode)));
    }

    private Map<String, Integer> groupIdenticalBarcodes(List<String> barcodes) {
        return barcodes.stream().collect(Collectors.groupingBy(barcode -> barcode, Collectors.summingInt(barcode -> 1)));
    }

    private Item findItemByBarcode(String barcode) {
        return items.stream().filter(item -> item.getBarcode().equals(barcode)).findFirst().orElseThrow();
    }

    private int calculateTotal(Map<String, Integer> groupedBarcodes) {
        return groupedBarcodes.entrySet().stream().mapToInt(entry -> {
            String barcode = entry.getKey();
            int quantity = entry.getValue();
            Item item = findItemByBarcode(barcode);
            return item.getPrice() * quantity;
        }).sum();
    }

    private StringBuilder formatLines(Map<String, Integer> groupedBarcodes, int total) {
        StringBuilder lines = new StringBuilder();
        lines.append(title);
        groupedBarcodes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String barcode = entry.getKey();
                    int quantity = entry.getValue();
                    Item item = findItemByBarcode(barcode);
                    if (item != null) {
                        int subtotal = item.getPrice() * quantity;
                        // Use \n instead of %n to ensure consistent Unix-style line endings matching the test expectation
                        lines.append(String.format("Name: %s, Quantity: %d, Unit price: %d (yuan), Subtotal: %d (yuan)\n",
                                item.getName(), quantity, item.getPrice(), subtotal));
                    }
                });
        lines.append(divider);
        lines.append(String.format("Total: %d (yuan)\n", total));
        lines.append(footer);
        return lines;
    }

    public String printReceipt(List<String> barcodes) {
        if (!isValidBarcodes(barcodes)) {
            throw new InvalidBarcodeException("Invalid barcodes found.");
        }

        Map<String, Integer> groupedBarcodes = groupIdenticalBarcodes(barcodes);
        int total = calculateTotal(groupedBarcodes);
        return formatLines(groupedBarcodes, total).toString();
    }
}
