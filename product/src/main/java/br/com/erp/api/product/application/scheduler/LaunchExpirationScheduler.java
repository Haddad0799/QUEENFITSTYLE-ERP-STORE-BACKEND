package br.com.erp.api.product.application.scheduler;

import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.domain.entity.Product;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class LaunchExpirationScheduler {

    private final ProductRepositoryPort productRepository;
    private final ProductCatalogPort productCatalogPublisher;
    private final Duration launchWindow;

    public LaunchExpirationScheduler(ProductRepositoryPort productRepository,
                                     ProductCatalogPort productCatalogPublisher,
                                     @Value("${product.launch.duration:PT720H}") String launchWindowIso) {
        this.productRepository = productRepository;
        this.productCatalogPublisher = productCatalogPublisher;
        this.launchWindow = Duration.parse(launchWindowIso);
    }

    // roda diariamente às 02:00
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireLaunches() {
        Instant cutoff = Instant.now().minus(launchWindow);
        List<Long> expired = productRepository.findIdsByIsLaunchTrueAndLaunchStartedBefore(cutoff);
        for (Long id : expired) {
            productRepository.findById(id).ifPresent(product -> {
                product.changeLaunch(false); // limpa isLaunch e launchStartedAt
                productRepository.update(product);
                // se produto estiver publicado, force publish to update snapshot
                productCatalogPublisher.publishIfPublished(id);
            });
        }
    }
}

