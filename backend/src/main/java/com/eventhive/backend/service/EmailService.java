package com.eventhive.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    private static final String FROM_EMAIL = "gamagepawani40@gmail.com";
    private static final String BRAND_COLOR = "#4f46e5";
    private static final String ACCENT_COLOR = "#8b5cf6";

    // ─────────────────────────────────────────────
    // Premium HTML Email Template Builder
    // ─────────────────────────────────────────────
    private String buildHtmlEmail(String title, String preheader, String bodyContent) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>" + title + "</title>" +
            "<style>" +
            "  body { margin:0; padding:0; background:#0f0f0f; font-family: 'Segoe UI', Arial, sans-serif; color:#e0e0e0; }" +
            "  .wrapper { max-width:600px; margin:0 auto; padding:40px 20px; }" +
            "  .card { background:#1a1a2e; border-radius:20px; overflow:hidden; border:1px solid #2d2d4e; }" +
            "  .header { background:linear-gradient(135deg, " + BRAND_COLOR + ", " + ACCENT_COLOR + "); padding:40px 40px 30px; text-align:center; }" +
            "  .logo { font-size:32px; font-weight:900; color:#ffffff; letter-spacing:-1px; }" +
            "  .logo span { opacity:0.85; }" +
            "  .header-sub { color:rgba(255,255,255,0.75); font-size:13px; margin-top:4px; }" +
            "  .body { padding:40px; }" +
            "  .greeting { font-size:22px; font-weight:700; color:#ffffff; margin-bottom:16px; }" +
            "  .text { font-size:15px; line-height:1.7; color:#a0a0b8; margin-bottom:20px; }" +
            "  .highlight-box { background:#0f0f1a; border:1px solid #2d2d4e; border-left:4px solid " + BRAND_COLOR + "; border-radius:12px; padding:20px 24px; margin:24px 0; }" +
            "  .highlight-box .label { font-size:11px; text-transform:uppercase; letter-spacing:1px; color:#6060a0; margin-bottom:4px; }" +
            "  .highlight-box .value { font-size:16px; font-weight:700; color:#ffffff; }" +
            "  .badge { display:inline-block; padding:6px 16px; border-radius:50px; font-size:12px; font-weight:700; text-transform:uppercase; letter-spacing:0.5px; }" +
            "  .badge-success { background:rgba(16,185,129,0.15); color:#10b981; border:1px solid rgba(16,185,129,0.3); }" +
            "  .badge-danger  { background:rgba(239,68,68,0.15);  color:#ef4444; border:1px solid rgba(239,68,68,0.3); }" +
            "  .badge-warning { background:rgba(245,158,11,0.15); color:#f59e0b; border:1px solid rgba(245,158,11,0.3); }" +
            "  .btn { display:inline-block; background:linear-gradient(135deg," + BRAND_COLOR + "," + ACCENT_COLOR + "); color:#fff; text-decoration:none; padding:14px 32px; border-radius:50px; font-weight:700; font-size:14px; margin:24px 0; }" +
            "  .divider { border:none; border-top:1px solid #2d2d4e; margin:28px 0; }" +
            "  .footer { text-align:center; padding:24px 40px; border-top:1px solid #1a1a2e; }" +
            "  .footer p { font-size:12px; color:#404060; margin:4px 0; }" +
            "  .footer a { color:" + BRAND_COLOR + "; text-decoration:none; }" +
            "  .event-meta { display:flex; flex-wrap:wrap; gap:16px; margin:20px 0; }" +
            "  .meta-item { background:#0f0f1a; border:1px solid #2d2d4e; border-radius:10px; padding:12px 16px; flex:1; min-width:120px; }" +
            "  .meta-item .meta-label { font-size:11px; color:#6060a0; text-transform:uppercase; letter-spacing:0.5px; }" +
            "  .meta-item .meta-value { font-size:14px; font-weight:600; color:#e0e0e0; margin-top:4px; }" +
            "</style></head><body>" +
            "<div class='wrapper'>" +
            "  <div class='card'>" +
            "    <div class='header'>" +
            "      <div class='logo'>&#x1F41D; EventHive</div>" +
            "      <div class='header-sub'>Premium Event Management Platform</div>" +
            "    </div>" +
            "    <div class='body'>" + bodyContent + "</div>" +
            "    <div class='footer'>" +
            "      <p>You're receiving this email because you're registered on <a href='http://localhost:3000'>EventHive</a>.</p>" +
            "      <p>&copy; 2026 EventHive. All rights reserved.</p>" +
            "    </div>" +
            "  </div>" +
            "</div></body></html>";
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(FROM_EMAIL);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            System.out.println("Email sent to: " + to);
        } catch (Exception e) {
            System.out.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Simple text email (backward compat)
    // ─────────────────────────────────────────────
    public void sendEmail(String to, String subject, String body) {
        String html = buildHtmlEmail(subject, body,
            "<p class='greeting'>EventHive Notification</p>" +
            "<p class='text'>" + body.replace("\n", "<br/>") + "</p>"
        );
        sendHtml(to, subject, html);
    }

    // ─────────────────────────────────────────────
    // Event Approved Email → Organizer
    // ─────────────────────────────────────────────
    public void sendEventApprovedEmail(com.eventhive.backend.entity.User organizer, com.eventhive.backend.entity.Event event) {
        String formatted = event.getEventDate() != null
            ? event.getEventDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))
            : "TBD";

        String body =
            "<p class='greeting'>&#x1F389; Your Event is Live!</p>" +
            "<p class='text'>Great news, <strong>" + organizer.getName() + "</strong>! Your event has been <span class='badge badge-success'>Approved</span> by the admin and is now publicly visible to attendees.</p>" +
            "<div class='highlight-box'>" +
            "  <div class='label'>Event</div><div class='value'>" + event.getTitle() + "</div>" +
            "</div>" +
            "<table style='width:100%;border-collapse:collapse;margin:0 0 24px;'>" +
            "  <tr>" +
            "    <td style='padding:10px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px;color:#a0a0b8;font-size:13px;'>&#x1F4CD; <strong>" + event.getVenue() + "</strong></td>" +
            "  </tr><tr><td style='height:8px;'></td></tr>" +
            "  <tr>" +
            "    <td style='padding:10px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px;color:#a0a0b8;font-size:13px;'>&#x1F4C5; <strong>" + formatted + "</strong></td>" +
            "  </tr>" +
            "</table>" +
            "<p class='text'>Attendees can now discover and book tickets for your event. Share it with your audience!</p>" +
            "<a href='http://localhost:3000/dashboard' class='btn'>Go to Dashboard</a>" +
            "<hr class='divider'/>" +
            "<p class='text' style='font-size:13px;'>Need help? Reply to this email or visit your dashboard.</p>";

        sendHtml(organizer.getEmail(), "✅ Your Event \"" + event.getTitle() + "\" is Approved!", buildHtmlEmail("Event Approved", "", body));
    }

    // ─────────────────────────────────────────────
    // Event Rejected Email → Organizer
    // ─────────────────────────────────────────────
    public void sendEventRejectedEmail(com.eventhive.backend.entity.User organizer, com.eventhive.backend.entity.Event event) {
        String body =
            "<p class='greeting'>&#x1F615; Event Not Approved</p>" +
            "<p class='text'>Hi <strong>" + organizer.getName() + "</strong>, unfortunately your event has been <span class='badge badge-danger'>Rejected</span> by the admin.</p>" +
            "<div class='highlight-box'>" +
            "  <div class='label'>Event</div><div class='value'>" + event.getTitle() + "</div>" +
            "</div>" +
            "<p class='text'>This may be due to incomplete information or policy violations. Please review your event details and resubmit or contact support for assistance.</p>" +
            "<a href='http://localhost:3000/dashboard' class='btn'>Review & Edit Event</a>" +
            "<hr class='divider'/>" +
            "<p class='text' style='font-size:13px;'>If you think this was a mistake, please reach out to our support team.</p>";

        sendHtml(organizer.getEmail(), "❌ Your Event \"" + event.getTitle() + "\" Was Not Approved", buildHtmlEmail("Event Rejected", "", body));
    }

    // ─────────────────────────────────────────────
    // Event Reminder Email → Attendee (24h before)
    // ─────────────────────────────────────────────
    public void sendEventReminderEmail(com.eventhive.backend.entity.User attendee, com.eventhive.backend.entity.Event event) {
        String formatted = event.getEventDate() != null
            ? event.getEventDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))
            : "Soon";

        String body =
            "<p class='greeting'>&#x23F0; Event Reminder — Tomorrow!</p>" +
            "<p class='text'>Hey <strong>" + attendee.getName() + "</strong>! Just a reminder that you have a ticket for an event happening <strong>tomorrow</strong>. Don't miss it!</p>" +
            "<div class='highlight-box'>" +
            "  <div class='label'>Event</div><div class='value'>" + event.getTitle() + "</div>" +
            "</div>" +
            "<table style='width:100%;border-collapse:collapse;margin:0 0 24px;'>" +
            "  <tr>" +
            "    <td style='padding:10px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px;color:#a0a0b8;font-size:13px;width:50%;'>&#x1F4CD; <strong>" + event.getVenue() + "</strong><br/><span style='font-size:11px;color:#6060a0;'>Location</span></td>" +
            "    <td style='width:8px;'></td>" +
            "    <td style='padding:10px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px;color:#a0a0b8;font-size:13px;'>&#x1F4C5; <strong>" + formatted + "</strong><br/><span style='font-size:11px;color:#6060a0;'>Date &amp; Time</span></td>" +
            "  </tr>" +
            "</table>" +
            "<p class='text'>Please bring your ticket QR code (available in your dashboard) to the entrance. We look forward to seeing you there!</p>" +
            "<a href='http://localhost:3000/dashboard' class='btn'>View My Tickets</a>" +
            "<hr class='divider'/>" +
            "<p class='text' style='font-size:13px;'>&#x1F91D; See you at the event!</p>";

        sendHtml(attendee.getEmail(), "⏰ Reminder: \"" + event.getTitle() + "\" is Tomorrow!", buildHtmlEmail("Event Reminder", "", body));
    }

    // ─────────────────────────────────────────────
    // Ticket Receipt Email (premium redesign)
    // ─────────────────────────────────────────────
    public void sendTicketReceipt(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Booking booking, java.util.List<com.eventhive.backend.entity.Ticket> tickets) {
        StringBuilder ticketCards = new StringBuilder();
        for (com.eventhive.backend.entity.Ticket t : tickets) {
            String qrUrl = "https://quickchart.io/qr?text=" + t.getUuid() + "&size=180&dark=4f46e5&light=0f0f1a";
            ticketCards.append(
                "<div style='background:#0f0f1a;border:1px solid #2d2d4e;border-radius:16px;padding:24px;margin-bottom:16px;text-align:center;'>" +
                "  <p style='font-size:11px;text-transform:uppercase;letter-spacing:1px;color:#6060a0;margin-bottom:4px;'>" + booking.getEvent().getTitle() + "</p>" +
                "  <p style='font-size:18px;font-weight:700;color:#ffffff;margin:0 0 4px;'>" + t.getTicketCategory().getName() + "</p>" +
                "  <p style='font-size:12px;color:#a0a0b8;margin-bottom:16px;'>" + (t.getIsGroupTicket() ? "🎭 Group / Table Ticket" : "🎫 Single Ticket") + "</p>" +
                "  <img src='" + qrUrl + "' alt='QR Code' style='border-radius:12px;border:3px solid #2d2d4e;' />" +
                "  <p style='font-size:10px;color:#404060;margin-top:12px;font-family:monospace;'>" + t.getUuid() + "</p>" +
                "</div>"
            );
        }

        String eventDate = booking.getEvent().getEventDate() != null
            ? booking.getEvent().getEventDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a"))
            : "TBD";

        String body =
            "<p class='greeting'>&#x1F3AB; Your Tickets Are Ready!</p>" +
            "<p class='text'>Hi <strong>" + user.getName() + "</strong>, thank you for your booking. Your tickets for <strong>" + booking.getEvent().getTitle() + "</strong> are confirmed!</p>" +
            "<div class='highlight-box'>" +
            "  <div class='label'>Total Paid</div><div class='value'>Rs. " + String.format("%,.2f", booking.getTotalAmount()) + "</div>" +
            "</div>" +
            "<table style='width:100%;margin:0 0 24px;border-collapse:collapse;'>" +
            "  <tr>" +
            "    <td style='padding:10px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px;color:#a0a0b8;font-size:13px;width:50%;'>&#x1F4CD; <strong>" + booking.getEvent().getVenue() + "</strong></td>" +
            "    <td style='width:8px;'></td>" +
            "    <td style='padding:10px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px;color:#a0a0b8;font-size:13px;'>&#x1F4C5; <strong>" + eventDate + "</strong></td>" +
            "  </tr>" +
            "</table>" +
            "<p style='font-size:14px;font-weight:700;color:#ffffff;margin-bottom:12px;'>Your Tickets</p>" +
            ticketCards +
            "<p class='text'>Present the QR code at the entrance. Each code is unique and valid for one entry.</p>" +
            "<a href='http://localhost:3000/dashboard' class='btn'>View in Dashboard</a>";

        String html = buildHtmlEmail("Your Tickets - " + booking.getEvent().getTitle(), "", body);
        sendHtml(user.getEmail(), "🎫 Your Tickets for \"" + booking.getEvent().getTitle() + "\"", html);
    }

    // ─────────────────────────────────────────────
    // Package Invoice Email (premium redesign)
    // ─────────────────────────────────────────────
    public void sendPackageInvoiceEmail(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Subscription subscription) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            helper.setTo(user.getEmail());
            helper.setBcc(FROM_EMAIL);
            helper.setSubject("🧾 EventHive Invoice - " + subscription.getPackageName() + " Package");
            helper.setFrom(FROM_EMAIL);

            boolean isPending = "BANK_TRANSFER".equalsIgnoreCase(subscription.getPaymentMethod());
            String statusBadge = isPending
                ? "<span class='badge badge-warning'>Pending Approval</span>"
                : "<span class='badge badge-success'>Active</span>";

            String body =
                "<p class='greeting'>&#x1F9FE; Purchase Confirmed!</p>" +
                "<p class='text'>Hi <strong>" + user.getName() + "</strong>, thank you for purchasing the <strong>" + subscription.getPackageName() + "</strong> package on EventHive!</p>" +
                "<div class='highlight-box'>" +
                "  <div class='label'>Package</div><div class='value'>" + subscription.getPackageName() + "</div>" +
                "</div>" +
                "<table style='width:100%;border-collapse:collapse;margin:0 0 24px;'>" +
                "  <tr>" +
                "    <td style='padding:12px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px 8px 0 0;color:#a0a0b8;font-size:13px;'>&#x1F4B0; Amount</td>" +
                "    <td style='padding:12px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:8px 8px 0 0;text-align:right;color:#ffffff;font-weight:700;'>LKR " + String.format("%,.2f", subscription.getPrice()) + "</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style='padding:12px;background:#0f0f1a;border:1px solid #2d2d4e;color:#a0a0b8;font-size:13px;'>&#x1F4B3; Payment</td>" +
                "    <td style='padding:12px;background:#0f0f1a;border:1px solid #2d2d4e;text-align:right;color:#e0e0e0;'>" + subscription.getPaymentMethod() + "</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style='padding:12px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:0 0 8px 8px;color:#a0a0b8;font-size:13px;'>&#x2705; Status</td>" +
                "    <td style='padding:12px;background:#0f0f1a;border:1px solid #2d2d4e;border-radius:0 0 8px 8px;text-align:right;'>" + statusBadge + "</td>" +
                "  </tr>" +
                "</table>" +
                (isPending ? "<p class='text'>We've received your payment slip and will review it shortly. You'll get another email once your package is activated.</p>" :
                             "<p class='text'>Your package is now active! Head to your dashboard to start creating events.</p>") +
                "<p class='text'>A detailed PDF invoice is attached to this email.</p>" +
                "<a href='http://localhost:3000/dashboard' class='btn'>Go to Dashboard</a>";

            String html = buildHtmlEmail("Invoice - " + subscription.getPackageName(), "", body);
            helper.setText(html, true);

            byte[] pdfBytes = generateInvoicePdf(user, subscription);
            helper.addAttachment("EventHive_Invoice_" + subscription.getPackageName() + ".pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Failed to send package invoice email: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Package Status Email (approved / rejected)
    // ─────────────────────────────────────────────
    public void sendPackageStatusEmail(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Subscription subscription, boolean isApproved) {
        String body;
        String subject;
        if (isApproved) {
            subject = "✅ Payment Approved - Your EventHive Package is Active!";
            body = "<p class='greeting'>&#x1F389; Payment Approved!</p>" +
                "<p class='text'>Hi <strong>" + user.getName() + "</strong>, your payment for the <strong>" + subscription.getPackageName() + "</strong> package has been <span class='badge badge-success'>Approved</span>.</p>" +
                "<p class='text'>Your package is now active. You can start creating and publishing events right away!</p>" +
                "<a href='http://localhost:3000/dashboard' class='btn'>Start Creating Events</a>";
        } else {
            subject = "❌ Payment Rejected - EventHive Package";
            body = "<p class='greeting'>&#x1F615; Payment Rejected</p>" +
                "<p class='text'>Hi <strong>" + user.getName() + "</strong>, unfortunately your payment for the <strong>" + subscription.getPackageName() + "</strong> package has been <span class='badge badge-danger'>Rejected</span>.</p>" +
                "<p class='text'>This may be due to an unclear or invalid payment slip. Please purchase the package again with a valid proof of payment.</p>" +
                "<a href='http://localhost:3000/dashboard' class='btn'>Try Again</a>";
        }

        sendHtml(user.getEmail(), subject, buildHtmlEmail(subject, "", body));
    }

    // ─────────────────────────────────────────────
    // PDF Invoice Generator (unchanged)
    // ─────────────────────────────────────────────
    public byte[] generateInvoicePdf(com.eventhive.backend.entity.User user, com.eventhive.backend.entity.Subscription subscription) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(79, 70, 229));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);

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

            document.add(new Paragraph(" "));
            LineSeparator ls = new LineSeparator();
            ls.setLineColor(Color.LIGHT_GRAY);
            document.add(ls);
            document.add(new Paragraph(" "));

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

            PdfPTable itemTable = new PdfPTable(3);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{2, 1, 1});
            itemTable.setSpacingBefore(10f);

            for (String h : new String[]{"Description", "Event Limit", "Amount (LKR)"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(new Color(41, 41, 41));
                cell.setPadding(10f);
                cell.setBorder(Rectangle.NO_BORDER);
                itemTable.addCell(cell);
            }

            String desc = subscription.getPackageName() + " Package (" + subscription.getPaymentMethod() + ")";
            PdfPCell cell1 = new PdfPCell(new Phrase(desc, tableBodyFont));
            cell1.setPadding(10f); cell1.setBorderColor(Color.LIGHT_GRAY);
            String eventLimit = subscription.getMaxEvents() == -1 ? "Unlimited" : String.valueOf(subscription.getMaxEvents());
            PdfPCell cell2 = new PdfPCell(new Phrase(eventLimit, tableBodyFont));
            cell2.setPadding(10f); cell2.setBorderColor(Color.LIGHT_GRAY);
            PdfPCell cell3 = new PdfPCell(new Phrase(String.format("%,.2f", subscription.getPrice()), tableBodyFont));
            cell3.setPadding(10f); cell3.setBorderColor(Color.LIGHT_GRAY);
            itemTable.addCell(cell1); itemTable.addCell(cell2); itemTable.addCell(cell3);
            document.add(itemTable);

            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{3, 1});
            PdfPCell empty = new PdfPCell(); empty.setBorder(Rectangle.NO_BORDER);
            totalTable.addCell(empty);
            PdfPCell totalCell = new PdfPCell();
            totalCell.setBorder(Rectangle.NO_BORDER); totalCell.setPaddingTop(15f);
            totalCell.addElement(new Paragraph("Total: LKR " + String.format("%,.2f", subscription.getPrice()), totalFont));
            boolean isPending = "BANK_TRANSFER".equalsIgnoreCase(subscription.getPaymentMethod());
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, isPending ? new Color(245, 158, 11) : new Color(16, 185, 129));
            totalCell.addElement(new Paragraph(isPending ? "Status: PENDING APPROVAL" : "Status: PAID IN FULL", statusFont));
            totalTable.addCell(totalCell);
            document.add(totalTable);

            document.add(new Paragraph(" "));
            document.add(ls);
            Paragraph footer = new Paragraph("Thank you for your business. For inquiries, contact support@eventhive.com", subtitleFont);
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
}