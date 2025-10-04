    package com.pm.authservice.controller;
    
    import com.pm.authservice.dto.LoginRequestDTO;
    import com.pm.authservice.dto.LoginResponseDTO;
    import com.pm.authservice.service.AuthService;
    import io.swagger.v3.oas.annotations.Operation;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    
    import java.util.Optional;
    
    @RestController
    // @RequestMapping() // Can be used to prefix all endpoints with /auth if needed
    public class AuthController {
    
        private final AuthService authService; // Inject AuthService to handle business logic
    
        public AuthController(AuthService authService) {
            this.authService = authService;
        }
    
        /**
         * Endpoint to login user and generate JWT tokens.
         * @param loginRequestDTO - contains email & password from client
         * @return ResponseEntity with access & refresh tokens if successful,
         *         UNAUTHORIZED if credentials are wrong,
         *         INTERNAL_SERVER_ERROR if something goes wrong on server side
         */
        @Operation(summary="Generate token on user login")
        @PostMapping("/login")
        public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
    
            // Initialize Optional for token response
            Optional<LoginResponseDTO> tokenOptional = Optional.empty();
    
            try {
                // Call AuthService to authenticate user and generate tokens
                tokenOptional = authService.authenticate(loginRequestDTO);
    
                // 1️⃣ If authentication fails (user not found or password wrong)
                if (tokenOptional.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
                }
            } catch(Exception ex) {
                // 2️⃣ Handle any unexpected server error
                System.out.println("Can't authenticate the user with email " + loginRequestDTO.getEmail());
                ex.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500
            }
    
            // 3️⃣ Authentication successful, return tokens in body
            return ResponseEntity.ok(tokenOptional.get());
        }





        @Operation(summary = "Validate token")
        @GetMapping("/validate/access")
        public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
            // 1. Check if Authorization header exists and starts with "Bearer "
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // If not present or invalid format, reject the request
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2. Extract only the token part (remove "Bearer " prefix → 7 characters)
            String token = authHeader.substring(7);

            // 3. Ask AuthService to validate the token
            boolean isValid = authService.validateAccessToken(token);

            // 4. If valid → return 200 OK, else → return 401 Unauthorized
            return isValid
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
        @Operation(summary = "Validate token")
        @GetMapping("/validate/refresh")
        public ResponseEntity<Void> validateRefreshToken(@RequestHeader("Authorization") String authHeader) {
            // 1. Check if Authorization header exists and starts with "Bearer "
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // If not present or invalid format, reject the request
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
    
            // 2. Extract only the token part (remove "Bearer " prefix → 7 characters)
            String token = authHeader.substring(7);
    
            // 3. Ask AuthService to validate the token
            boolean isValid = authService.validateRefreshToken(token);
    
            // 4. If valid → return 200 OK, else → return 401 Unauthorized
            return isValid
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    
    
    
    
    }
