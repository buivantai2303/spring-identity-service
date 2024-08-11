package com.ketealare.identityService.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ketealare.identityService.dto.request.AuthenticationRequest;
import com.ketealare.identityService.dto.request.IntrospectRequest;
import com.ketealare.identityService.dto.request.LogoutRequest;
import com.ketealare.identityService.dto.request.RefreshTokenRequest;
import com.ketealare.identityService.dto.response.AuthenticationResponse;
import com.ketealare.identityService.dto.response.IntrospectResponse;
import com.ketealare.identityService.entity.InvalidatedToken;
import com.ketealare.identityService.entity.User;
import com.ketealare.identityService.exception.AppException;
import com.ketealare.identityService.exception.ErrorCode;
import com.ketealare.identityService.repository.InvalidatedTokenRepository;
import com.ketealare.identityService.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESH_DURATION;

    // GET Token from request and verify
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {

        var token = request.getToken();

        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    // Log out service for users
    public void logOut(LogoutRequest request) throws ParseException, JOSEException {

        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);

        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    // Refresh Token
    // spotless: off
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {

        var signJWT = verifyToken(request.getToken(), true); // -> // Check valid token

        var jit = signJWT.getJWTClaimsSet().getJWTID(); // -> Get jwt token Id
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime(); // -> Get expiry time

        // Disable token
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        // Save disable token request to database
        invalidatedTokenRepository.save(invalidatedToken);

        // Refresh new token for user, will find user
        var username = signJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Generate new token
        var token = generateToken(user);

        // Return new token
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    // spotless: on
    // Verify Token from request valid or not?
    private SignedJWT verifyToken(String token, boolean isRefreshToken) throws JOSEException, ParseException {

        // MACVerifier is used to verify the JWT signature to ensure that the token has not been altered and was created
        // by a trusted source.
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // Converts the token string into a SignedJWT object
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationDate = (isRefreshToken)
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESH_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        // The token's signature must be valid and Token has not expired
        if (!(verified && expirationDate.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // If the token already exists in the InvalidatedToken database, action will not be allowed
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    // Using username and password -> generate Token
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)); // -> Find if username exists or not?

        PasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder(10); // -> Encrypt password to a hashed string with 10 characters

        boolean authenticated = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()); // -> Matching between password in database and request password

        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // -> If wrong return error unauthenticated
        }

        var token = generateToken(user); // -> If true will generate new token for this user

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    // Token Structure: Header.Payload.VerifiedSignature
    String generateToken(User user) {

        // Step 1: Create header (contain Algorithms type): using JWSHeader
        // Step 2: Create JWTClaimsSet contain user data and put into Payload (JWTClaimsSet -> toJSONObject)
        // Step 3: Combine Header & Payload using JWSObject (Header, Payload) and Verify Signature

        // Header Token: Contain hash algorithms type
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Payload (ClaimsSet)
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // -> Username
                .issuer("ketaelare.com") // -> Issuer
                .issueTime(new Date()) // -> Created Time
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli() // -> Expiry Time
                        ))
                .jwtID(UUID.randomUUID().toString()) // -> Token ID
                .claim("scope", buildScope(user)) // -> Scope, Role, Permission
                .build();

        // Converting JWTClaimsSet to JSON and put into Payload
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        // Combine Header & Payload
        JWSObject jwsObject = new JWSObject(header, payload);

        // Add VerifiedSignature
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot sign JWT object", e);
            throw new RuntimeException(e);
        }
    }

    // Create and add Scope (Role and Permission) into Token
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }
}
