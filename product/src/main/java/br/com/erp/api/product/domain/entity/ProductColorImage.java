package br.com.erp.api.product.domain.entity;

public class ProductColorImage {

    private Long id;
    private final Long productId;
    private final Long colorId;
    private final String imageKey;
    private final int order;
    public static final int MAX_IMAGES_PER_COLOR = 5;

    public ProductColorImage(Long productId, Long colorId, String imageKey, int order) {
        this.productId = productId;
        this.colorId = colorId;
        this.imageKey = imageKey;
        this.order = order;
    }

    public ProductColorImage(Long id, Long productId, Long colorId, String imageKey, int order) {
        this.id = id;
        this.productId = productId;
        this.colorId = colorId;
        this.imageKey = imageKey;
        this.order = order;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getColorId() { return colorId; }
    public String getImageKey() { return imageKey; }
    public int getOrder() { return order; }
}