package br.com.erp.api.auth.presentation.controller;

import br.com.erp.api.auth.infrastructure.security.JwtService;
import br.com.erp.api.auth.presentation.dto.ServiceTokenRequest;
import br.com.erp.api.auth.presentation.dto.ServiceTokenResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class ServiceAuthController {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final int    EXPIRES_IN   = 3600;

    private final JwtService jwtService;
    private final String     serviceClientId;
    private final String     serviceClientSecret;

    public ServiceAuthController(JwtService jwtService,
                                 @Value("${service.client-id}") String serviceClientId,
                                 @Value("${service.client-secret}") String serviceClientSecret) {
        this.jwtService          = jwtService;
        this.serviceClientId     = serviceClientId;
        this.serviceClientSecret = serviceClientSecret;
    }

    @PostMapping("/service/token")
    public ResponseEntity<?> issueToken(@Valid @RequestBody ServiceTokenRequest request) {
        if (!credentialsAreValid(request)) {
            return unauthorized();
        }

        String accessToken = jwtService.generateServiceToken(request.clientId());
        return ResponseEntity.ok(new ServiceTokenResponse(accessToken, EXPIRES_IN));
    }

    private boolean credentialsAreValid(ServiceTokenRequest request) {
        return serviceClientId.equals(request.clientId())
                && serviceClientSecret.equals(request.clientSecret());
    }

    private ResponseEntity<ProblemDetail> unauthorized() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Credenciais de serviço inválidas"
        );
        problem.setTitle("Credenciais inválidas");
        problem.setType(URI.create("https://example.com/probs/invalid-credentials"));
        problem.setProperty("timestamp", LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, PROBLEM_JSON);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(problem);
    }
}
