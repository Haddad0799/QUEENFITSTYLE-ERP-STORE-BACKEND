package br.com.erp.api.inventory.domain.entity;

import br.com.erp.api.inventory.domain.exception.InsufficientReservationException;
import br.com.erp.api.inventory.domain.exception.InsufficientStockException;

public class SkuStock {

    private Long id;
    private final Long skuId;
    private int quantity;
    private int reserved;
    private int minQuantity;

    public SkuStock(Long skuId, int quantity, int minQuantity) {
        this.skuId = skuId;
        this.quantity = quantity;
        this.reserved = 0;
        this.minQuantity = minQuantity;
    }

    public static SkuStock restore(Long id, Long skuId, int quantity, int reserved, int minQuantity) {
        SkuStock stock = new SkuStock(skuId, quantity, minQuantity);
        stock.id = id;
        stock.reserved = reserved;
        return stock;
    }

    // Quantidade disponível para venda (descontando reservas)
    public int getAvailable() {
        return quantity - reserved;
    }

    public boolean isBelowMinimum() {
        return getAvailable() <= minQuantity;
    }

    // Entrada manual de estoque
    public void addStock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");
        this.quantity += amount;
    }

    // Reserva (item adicionado ao carrinho)
    public void reserve(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");
        if (getAvailable() < amount) throw new InsufficientStockException(skuId, amount, getAvailable());
        this.reserved += amount;
    }

    // Libera reserva (carrinho abandonado ou cancelamento)
    public void releaseReservation(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");
        if (reserved < amount) throw new InsufficientReservationException(skuId, amount, reserved);
        this.reserved -= amount;
    }

    // Confirma saída (pedido pago — desconta do estoque e libera reserva)
    public void confirmOutbound(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva");
        if (reserved < amount) throw new InsufficientReservationException(skuId, amount, reserved);
        this.reserved -= amount;
        this.quantity -= amount;
    }

    public void updateMinQuantity(int minQuantity) {
        if (minQuantity < 0) throw new IllegalArgumentException("Quantidade mínima não pode ser negativa");
        this.minQuantity = minQuantity;
    }

    public void adjust(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa");
        this.quantity = quantity;
    }

    public Long getId()          { return id; }
    public Long getSkuId()       { return skuId; }
    public int getQuantity()     { return quantity; }
    public int getReserved()     { return reserved; }
    public int getMinQuantity()  { return minQuantity; }
}