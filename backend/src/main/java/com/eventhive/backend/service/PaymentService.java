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
        // PayHere එකෙන් ගාණ ඉල්ලන්නේ දශම ස්ථාන 2කට (උදා: 15000.00)
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedAmount = df.format(amount);

        // Hash එක හදන ෆෝමියුලා එක:
        // md5(merchant_id + order_id + amount + currency + md5(merchant_secret))
        String hashedSecret = getMd5(merchantSecret).toUpperCase();
        String hashInput = merchantId + orderId + formattedAmount + currency + hashedSecret;

        return getMd5(hashInput).toUpperCase();
    }
}