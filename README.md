# java-file-transfer-system
A Java TCP client-server file transfer system with support for resumable downloads and duplicate file handling.

# Java File Transfer System (Client-Server)

## 📌 Overview

This project is a Java-based client-server file transfer system built using TCP sockets.
It allows users to upload and download files between a client and a server with support for file listing, duplicate handling, and resumable downloads.

---

## 🚀 Features

* 📂 View server files
* 📁 View client files
* ⬆️ Upload files to server
* ⬇️ Download files from server
* 🔁 Resume interrupted downloads
* ⚠️ Duplicate file detection
* 🧠 Automatic renaming for duplicate files

---

## 🏗️ Technologies Used

* Java
* TCP Sockets
* Multithreading
* File I/O Streams

---

## ⚙️ How to Run

### 1. Compile the files

```
javac Server.java
javac Client.java
```

### 2. Run the server

```
java Server
```

### 3. Run the client (in another terminal)

```
java Client
```

---

## 🧪 How It Works

### Server

* Runs on port `4000`
* Handles multiple clients using threads
* Stores files in `server_files/`

### Client

* Connects to server (`localhost:4000`)
* Provides a menu interface:

    1. View server files
    2. View client files
    3. Upload file
    4. Download file
    5. Exit

---

## 🔄 File Transfer Logic

### Upload

* Checks for duplicate files on server
* User can choose to overwrite or create a copy
* Copies are renamed automatically (`_copy1`, `_copy2`, ...)

### Download

* Supports resume functionality
* Continues downloading from last saved byte
* Handles duplicate files on client side

---

## 📌 Notes

* Default server port: `4000`
* Default host: `localhost`
* Ensure both server and client directories exist (auto-created)

---

## 👨‍💻 Author

Mamoun

---
