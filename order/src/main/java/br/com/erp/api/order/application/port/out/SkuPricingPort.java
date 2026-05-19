package br.com.erp.api.order.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SkuPricingPort {

    Map<Long, BigDecimal> findSellingPrices(List<Long> skuIds);
}
