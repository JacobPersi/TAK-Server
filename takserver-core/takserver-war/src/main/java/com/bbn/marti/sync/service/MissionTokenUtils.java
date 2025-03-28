package com.bbn.marti.sync.service;

import java.security.PrivateKey;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bbn.marti.jwt.JwtUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class MissionTokenUtils {

    public static final String MISSION_NAME_CLAIM = "MISSION_NAME";
    public static final String MISSION_GUID_CLAIM = "MISSION_GUID";


    public enum TokenType {
        SUBSCRIPTION,
        INVITATION,
        ACCESS
    }

    private static final Logger logger = LoggerFactory.getLogger(MissionTokenUtils.class);
    private PrivateKey privateKey = null;
    private SecretKeySpec secretKeySpec = null;

    private void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        secretKeySpec = new SecretKeySpec(privateKey.getEncoded(), privateKey.getAlgorithm());
    }

    private MissionTokenUtils(PrivateKey privateKey)  {
        setPrivateKey(privateKey);
    }

    private static MissionTokenUtils instance = null;

    public static MissionTokenUtils getInstance(PrivateKey privateKey) {
        if (instance == null) {
            instance = new MissionTokenUtils(privateKey);
        }
        return instance;
    }
    
    public String createMissionToken(
            String id, String missionName, TokenType tokenType, long expirationMillis, String issuer, UUID missionGuid)  {

        try {
            Date now = new Date();

            JwtBuilder builder = Jwts.builder()
                    .setId(id)
                    .setIssuedAt(now)
                    .setSubject(tokenType.name())
                    .setIssuer(issuer)
                    .signWith(SignatureAlgorithm.HS256, secretKeySpec)
                    .claim(tokenType.name(), id)
                    .claim(MISSION_NAME_CLAIM, missionName)
            		.claim(MISSION_GUID_CLAIM, missionGuid.toString());

            if (expirationMillis > 0) {
                builder.setExpiration(new Date(now.getTime() + expirationMillis));
            }

            return builder.compact();

        } catch (Exception e) {
            logger.error("Exception in createMissionToken!", e);
            return null;
        }
    }

    public Claims decodeMissionToken(String token) {
        return JwtUtils.getInstance().parseMissionTokenClaims(token);
    }
}
