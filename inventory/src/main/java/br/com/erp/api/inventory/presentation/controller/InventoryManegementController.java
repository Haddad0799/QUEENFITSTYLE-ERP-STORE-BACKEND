package br.com.erp.api.inventory.presentation.controller;

import br.com.erp.api.inventory.application.usecase.AdjustStockUseCase;
import br.com.erp.api.inventory.application.usecase.ConfirmReservationUseCase;
import br.com.erp.api.inventory.application.usecase.GetStockByProductUseCase;
import br.com.erp.api.inventory.application.usecase.GetStockUseCase;
import br.com.erp.api.inventory.application.usecase.ReleaseReservationUseCase;
import br.com.erp.api.inventory.application.usecase.ReserveStockUseCase;
import br.com.erp.api.inventory.application.usecase.RestockUseCase;
import br.com.erp.api.inventory.presentation.dto.InventoryDTO;
import br.com.erp.api.inventory.presentation.dto.PageResponse;
import br.com.erp.api.inventory.presentation.dto.ProductStockDTO;
import br.com.erp.api.inventory.presentation.dto.ReserveStockRequest;
import br.com.erp.api.inventory.presentation.dto.ReserveStockResponse;
import br.com.erp.api.inventory.presentation.dto.RestockRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/erp")
public class InventoryManegementController {

	private static final int MAX_PAGE_SIZE = 100;

	private final ReserveStockUseCase reserveStockUseCase;
	private final ConfirmReservationUseCase confirmReservationUseCase;
	private final ReleaseReservationUseCase releaseReservationUseCase;
	private final AdjustStockUseCase adjustStockUseCase;
	private final RestockUseCase restockUseCase;
	private final GetStockUseCase getStockUseCase;
	private final GetStockByProductUseCase getStockByProductUseCase;

	public InventoryManegementController(ReserveStockUseCase reserveStockUseCase,
										 ConfirmReservationUseCase confirmReservationUseCase,
										 ReleaseReservationUseCase releaseReservationUseCase,
										 AdjustStockUseCase adjustStockUseCase,
										 RestockUseCase restockUseCase,
										 GetStockUseCase getStockUseCase,
										 GetStockByProductUseCase getStockByProductUseCase) {
		this.reserveStockUseCase = reserveStockUseCase;
		this.confirmReservationUseCase = confirmReservationUseCase;
		this.releaseReservationUseCase = releaseReservationUseCase;
		this.adjustStockUseCase = adjustStockUseCase;
		this.restockUseCase = restockUseCase;
		this.getStockUseCase = getStockUseCase;
		this.getStockByProductUseCase = getStockByProductUseCase;
	}

	@GetMapping("/stock/products")
	public ResponseEntity<PageResponse<ProductStockDTO>> getStockByProduct(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String search) {
		Pageable pageable = PageRequest.of(
				Math.max(page, 0),
				Math.clamp(size, 1, MAX_PAGE_SIZE)
		);
		Page<ProductStockDTO> result = getStockByProductUseCase.execute(search, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/skus/{skuId}/stock")
	public ResponseEntity<InventoryDTO> getStock(@PathVariable Long skuId) {
		InventoryDTO dto = getStockUseCase.execute(skuId);
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/skus/{skuCode}/stock/reserve")
	public ResponseEntity<ReserveStockResponse> reserve(@PathVariable String skuCode,
														@RequestBody ReserveStockRequest req) {
		UUID reservationId = reserveStockUseCase.reserve(skuCode, req.quantity());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ReserveStockResponse(reservationId, skuCode, req.quantity()));
	}

	@PostMapping("/skus/reservations/{reservationId}/confirm")
	public ResponseEntity<Void> confirm(@PathVariable String reservationId) {
		confirmReservationUseCase.confirm(UUID.fromString(reservationId));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/skus/reservations/{reservationId}/release")
	public ResponseEntity<Void> release(@PathVariable String reservationId) {
		releaseReservationUseCase.release(UUID.fromString(reservationId));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/skus/{skuId}/stock/adjust")
	public ResponseEntity<Void> adjust(@PathVariable Long skuId, @RequestParam int quantity,
									   @RequestParam(required = false) String reason) {
		adjustStockUseCase.adjust(skuId, quantity, reason);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/skus/{skuId}/stock/inbound")
	public ResponseEntity<Void> inbound(@PathVariable Long skuId, @RequestBody RestockRequest req) {
		restockUseCase.restock(skuId, req.quantity());
		return ResponseEntity.noContent().build();
	}
}
