package com.eventhive.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

@Service
public class PaymentService {

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

    // PayHere ඉල්ලන විදියට MD5 කේතය හදන Method එක
    public String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Payment Hash එක ජෙනරේට් කරන Method එක
    public String generatePaymentHash(String orderId, double amount, String currency) {
        // Force US Locale to ensure '.' is used as decimal separator instead of ',' in some locales
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(java.util.Locale.US);
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00", symbols);
        String formattedAmount = df.format(amount);

        // Hash එක හදන ෆෝමියුලා එක:
        // md5(merchant_id + order_id + amount + currency + md5(merchant_secret))
        String mId = merchantId != null ? merchantId.trim() : "";
        String mSecret = merchantSecret != null ? merchantSecret.trim() : "";
        
        String hashedSecret = getMd5(mSecret).toUpperCase();
        String hashInput = mId + orderId.trim() + formattedAmount + currency.trim() + hashedSecret;

        return getMd5(hashInput).toUpperCase();
    }
}