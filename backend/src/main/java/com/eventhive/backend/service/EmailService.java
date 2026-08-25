package com.eventhive.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import org.springframework.core.io.ByteArrayResource;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Color;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            // ඔයාගේ ඇත්ත ඊමේල් එක මෙතනට දෙන්න
            message.setFrom("gamagepawani40@gmail.com");

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.out.println("Failed to send email to " + to + ". Error: " + e.getMessage());
        }
    }

    public void sendTicketReceipt(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Booking booking, java.util.List<com.eventhive.backend.entity.Ticket> tickets) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, "utf-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Your Tickets for " + booking.getEvent().getTitle());
            helper.setFrom("gamagepawani40@gmail.com");
            
            StringBuilder htmlMsg = new StringBuilder();
            htmlMsg.append("<h2>Hi ").append(user.getName()).append(",</h2>");
            htmlMsg.append("<p>Thank you for your booking! Here are your tickets for <b>").append(booking.getEvent().getTitle()).append("</b>.</p>");
            htmlMsg.append("<p><b>Total Amount:</b> Rs. ").append(booking.getTotalAmount()).append("</p><hr/>");
            
            htmlMsg.append("<div style='display: flex; flex-wrap: wrap; gap: 20px;'>");
            for (com.eventhive.backend.entity.Ticket t : tickets) {
                htmlMsg.append("<div style='border: 1px solid #ccc; padding: 20px; border-radius: 10px; width: 250px; text-align: center;'>");
                htmlMsg.append("<h3>").append(t.getTicketCategory().getName()).append("</h3>");
                if (t.getIsGroupTicket()) {
                    htmlMsg.append("<p style='color: orange;'>Table / Group Ticket</p>");
                } else {
                    htmlMsg.append("<p>Single Ticket</p>");
                }
                // Generate QR Code URL using quickchart.io
                String qrUrl = "https://quickchart.io/qr?text=" + t.getUuid() + "&size=200";
                htmlMsg.append("<img src='").append(qrUrl).append("' alt='QR Code' />");
                htmlMsg.append("<p style='font-size: 10px; color: gray;'>ID: ").append(t.getUuid()).append("</p>");
                htmlMsg.append("</div>");
            }
            htmlMsg.append("</div>");
            htmlMsg.append("<p><br/>Please present this QR code at the entrance.</p>");
            
            helper.setText(htmlMsg.toString(), true); // true indicates HTML
            mailSender.send(mimeMessage);
            System.out.println("Ticket receipt email sent to: " + user.getEmail());
            
        } catch (Exception e) {
            System.out.println("Failed to send ticket receipt to " + user.getEmail() + ". Error: " + e.getMessage());
        }
    }

    public void sendPackageInvoiceEmail(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Subscription subscription) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, true, "utf-8");
            
            helper.setTo(user.getEmail());
            helper.setBcc("gamagepawani40@gmail.com"); // Send a copy to the admin
            helper.setSubject("EventHive - Package Purchase Receipt (" + subscription.getPackageName() + ")");
            helper.setFrom("gamagepawani40@gmail.com");
            
            StringBuilder htmlMsg = new StringBuilder();
            htmlMsg.append("<h2>Hi ").append(user.getName()).append(",</h2>");
            htmlMsg.append("<p>Thank you for purchasing the <b>").append(subscription.getPackageName()).append("</b> package on EventHive!</p>");
            htmlMsg.append("<p><b>Amount:</b> LKR ").append(subscription.getPrice()).append("</p>");
            htmlMsg.append("<p><b>Payment Method:</b> ").append(subscription.getPaymentMethod()).append("</p>");
            
            if ("BANK_TRANSFER".equalsIgnoreCase(subscription.getPaymentMethod())) {
                htmlMsg.append("<p style='color: orange;'><b>Status:</b> Pending Admin Approval. We have received your payment slip and will activate your package shortly.</p>");
            } else {
                htmlMsg.append("<p style='color: green;'><b>Status:</b> Active. Your package is now ready to use!</p>");
            }
            htmlMsg.append("<p><br/>Thank you for choosing EventHive!</p>");
            
            helper.setText(htmlMsg.toString(), true);
            
            // Generate and attach PDF
            byte[] pdfBytes = generateInvoicePdf(user, subscription);
            helper.addAttachment("Invoice_" + subscription.getPackageName() + ".pdf", new ByteArrayResource(pdfBytes));
            
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Failed to send package invoice email to " + user.getEmail() + ". Error: " + e.getMessage());
        }
    }
    
    public byte[] generateInvoicePdf(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Subscription subscription) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(255, 51, 102)); // Primary color
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            
            // Header Section
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 1});
            
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("EventHive", titleFont));
            leftCell.addElement(new Paragraph("Premium Event Management", subtitleFont));
            headerTable.addCell(leftCell);
            
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph invoiceText = new Paragraph("INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.LIGHT_GRAY));
            invoiceText.setAlignment(Element.ALIGN_RIGHT);
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            Paragraph dateText = new Paragraph("Date: " + dateStr, subtitleFont);
            dateText.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(invoiceText);
            rightCell.addElement(dateText);
            headerTable.addCell(rightCell);
            document.add(headerTable);
            
            // Divider
            document.add(new Paragraph(" "));
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(Color.LIGHT_GRAY);
            document.add(ls);
            document.add(new Paragraph(" "));
            
            // Billed To
            PdfPTable billedTable = new PdfPTable(1);
            billedTable.setWidthPercentage(100);
            PdfPCell billedCell = new PdfPCell();
            billedCell.setBorder(Rectangle.NO_BORDER);
            billedCell.addElement(new Paragraph("BILLED TO:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.GRAY)));
            billedCell.addElement(new Paragraph(user.getName(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK)));
            billedCell.addElement(new Paragraph(user.getEmail(), tableBodyFont));
            billedTable.addCell(billedCell);
            document.add(billedTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            
            // Invoice Items Table
            PdfPTable itemTable = new PdfPTable(3);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{2, 1, 1});
            itemTable.setSpacingBefore(10f);
            
            // Table Header
            String[] headers = {"Description", "Event Limit", "Amount (LKR)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(new Color(41, 41, 41));
                cell.setPadding(10f);
                cell.setBorder(Rectangle.NO_BORDER);
                itemTable.addCell(cell);
            }
            
            // Table Body
            String desc = subscription.getPackageName() + " Package Subscription (" + subscription.getPaymentMethod() + ")";
            PdfPCell cell1 = new PdfPCell(new Phrase(desc, tableBodyFont));
            cell1.setPadding(10f);
            cell1.setBorderColor(Color.LIGHT_GRAY);
            
            String eventLimit = subscription.getMaxEvents() == -1 ? "Unlimited" : String.valueOf(subscription.getMaxEvents());
            PdfPCell cell2 = new PdfPCell(new Phrase(eventLimit, tableBodyFont));
            cell2.setPadding(10f);
            cell2.setBorderColor(Color.LIGHT_GRAY);
            
            PdfPCell cell3 = new PdfPCell(new Phrase(String.format("%,.2f", subscription.getPrice()), tableBodyFont));
            cell3.setPadding(10f);
            cell3.setBorderColor(Color.LIGHT_GRAY);
            
            itemTable.addCell(cell1);
            itemTable.addCell(cell2);
            itemTable.addCell(cell3);
            document.add(itemTable);
            
            // Totals
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{3, 1});
            
            PdfPCell empty = new PdfPCell();
            empty.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(empty);
            
            PdfPCell totalCell = new PdfPCell();
            totalCell.setBorder(Rectangle.NO_BORDER);
            totalCell.setPaddingTop(15f);
            totalCell.addElement(new Paragraph("Total: LKR " + String.format("%,.2f", subscription.getPrice()), totalFont));
            
            boolean isPending = "BANK_TRANSFER".equalsIgnoreCase(subscription.getPaymentMethod());
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, isPending ? new Color(255, 140, 0) : new Color(0, 153, 0));
            totalCell.addElement(new Paragraph(isPending ? "Status: PENDING APPROVAL" : "Status: PAID IN FULL", statusFont));
            totalTable.addCell(totalCell);
            
            document.add(totalTable);
            
            // Footer
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(ls);
            Paragraph footer = new Paragraph("Thank you for your business. For any inquiries, please contact support@eventhive.com", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10f);
            document.add(footer);
            
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public void sendPackageStatusEmail(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Subscription subscription, boolean isApproved) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, "utf-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject(isApproved ? "EventHive - Payment Approved!" : "EventHive - Payment Rejected");
            helper.setFrom("gamagepawani40@gmail.com");
            
            StringBuilder htmlMsg = new StringBuilder();
            htmlMsg.append("<h2>Hi ").append(user.getName()).append(",</h2>");
            
            if (isApproved) {
                htmlMsg.append("<p>Great news! Your payment for the <b>").append(subscription.getPackageName()).append("</b> package has been <b style='color: green;'>Approved</b>.</p>");
                htmlMsg.append("<p>You can now go to your dashboard and start creating events!</p>");
            } else {
                htmlMsg.append("<p>Unfortunately, your payment for the <b>").append(subscription.getPackageName()).append("</b> package has been <b style='color: red;'>Rejected</b>.</p>");
                htmlMsg.append("<p>This might be due to an invalid or unclear payment slip. Please purchase the package again with a valid proof of payment.</p>");
            }
            htmlMsg.append("<p><br/>Thank you,<br/>EventHive Admin Team</p>");
            
            helper.setText(htmlMsg.toString(), true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Failed to send package status email to " + user.getEmail() + ". Error: " + e.getMessage());
        }
    }
}