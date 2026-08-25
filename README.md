# EventHive Platform 🐝

Welcome to the **EventHive Platform**, a modern, full-stack event management and ticketing system designed to empower organizers and provide a seamless experience for attendees.

![EventHive Banner](https://via.placeholder.com/1200x400/1e293b/f8fafc?text=EventHive+Platform)

## 🌟 Project Overview

EventHive is a comprehensive platform that handles the entire lifecycle of event management, from creation and marketing to ticket sales, payment processing, and at-the-door attendee scanning.

This repository serves as the central hub for the EventHive Platform. Since the system is built with a decoupled architecture, the source code is neatly organized into separate branches:

- **[🖥️ Frontend Branch (Next.js & React)](https://github.com/imalwic/eventhive-platform/tree/frontend)**
- **[⚙️ Backend Branch (Spring Boot & Java)](https://github.com/imalwic/eventhive-platform/tree/backend)**

---

## 🎨 Frontend Features (`frontend` branch)

Built with **Next.js 14, React, Tailwind CSS, and Framer Motion**, the frontend delivers a premium, highly interactive user experience.

- **Dynamic Seat Selection:** A highly interactive visual seat mapping tool for booking.
- **AI-Powered Event Generation:** Organizers can generate compelling event descriptions and venue images automatically using AI.
- **Premium UI/UX:** Glassmorphism design, smooth animations, and rich toast notifications.
- **Admin & Organizer Dashboards:** Comprehensive analytics, subscription management, and user controls.
- **QR Code Scanning:** Built-in scanner interface for fast attendee check-ins.

---

## 🏗️ Backend Architecture (`backend` branch)

Powered by **Java 17, Spring Boot 3, and MySQL**, the backend provides a robust and secure foundation.

- **JWT Authentication:** Secure stateless authentication and Role-Based Access Control (Admin, Organizer, Attendee).
- **Subscription Management:** Automated handling of free and premium tier restrictions.
- **PDF Invoice Generation:** Automated creation and email delivery of professional PDF receipts.
- **AI Integration Hub:** Communicates with external AI APIs for content generation.
- **RESTful API:** Clean, well-documented endpoints handling complex business logic for bookings and event management.

---

## 🚀 How to Run Locally

### 1. Backend Setup
1. Switch to the backend branch: `git checkout backend`
2. Configure your MySQL database details in `application.properties`.
3. Run the Spring Boot application using Maven: `./mvnw spring-boot:run`

### 2. Frontend Setup
1. Switch to the frontend branch: `git checkout frontend`
2. Install dependencies: `npm install`
3. Run the development server: `npm run dev`
4. Access the application at `http://localhost:3000`

---

*EventHive - Empowering your events, from planning to applause.* 🎉
