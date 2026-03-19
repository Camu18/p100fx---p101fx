# p100fx / p101 – Data-Driven UI & Multi-Language System

---

## 📌 Giới thiệu / Introduction

### 🇻🇳 Tiếng Việt

Dự án này xuất phát từ một phương pháp lập trình được xây dựng từ những năm 1990 với PowerBASIC.

Ý tưởng cốt lõi:

* Không hardcode giao diện (UI)
* Tách biệt **thiết kế – dữ liệu – thực thi**
* Điều khiển chương trình bằng **file tham số bên ngoài**

Dự án hiện tại là một thử nghiệm áp dụng lại tư duy đó trên **Java / JavaFX**.

---

### 🇬🇧 English

This project originates from a programming approach developed in the 1990s using PowerBASIC.

Core ideas:

* No hardcoded UI
* Separation of **design – data – execution**
* Control the application using **external parameter files**

This project is an experiment to bring that approach into **Java / JavaFX**.

---

## 🎯 Nhiệm vụ hệ thống / System Mission

### 🔹 p100fx – Thiết kế UI / UI Designer

**🇻🇳**

* Thiết kế giao diện theo lưới (pixel / cell)
* Xác định vị trí bằng hệ neo (posp, posb)
* Gán loại object bằng mã (`$`, `#`, …)
* Lưu vào parametfile

**🇬🇧**

* Design UI using grid (pixel / cell)
* Define positions using anchor system (posp, posb)
* Assign object types via encoded values (`$`, `#`, …)
* Save into parametfile

---

### 🔹 p101 – Đa ngôn ngữ / Multi-Language

**🇻🇳**

* Tách text khỏi UI
* Dùng key ngắn (`@1`, `@2`)
* Ánh xạ theo ngôn ngữ

**🇬🇧**

* Separate text from UI
* Use compact keys (`@1`, `@2`)
* Map keys to language files

Ví dụ / Example:

| Key | English | Vietnamese |
| --- | ------- | ---------- |
| @1  | insert  | Thêm vào   |

---

### 🔹 mread – Runtime Engine

**🇻🇳**
Class đọc parametfile và khởi tạo giao diện khi chạy.

**🇬🇧**
A runtime class that reads parametfile and initializes the UI.

---

## 🧠 Ý tưởng cốt lõi / Core Idea

```text id="coreflow"
Design (p100fx + p101)
        ↓
Parametfile + Font files
        ↓
mread()
        ↓
Runtime UI
```

👉 **🇻🇳** UI được tạo từ dữ liệu
👉 **🇬🇧** UI is built from data

---

## 🔧 Nguyên lý mread / How mread Works

### 1. Quét dữ liệu / Scan

* 🇻🇳 Quét tuần tự
* 🇬🇧 Sequential scan

---

### 2. Nhận diện object / Object Detection

* `$` → Panel → posp
* `#` → Button / TextField → posb
* `@n` → text key

---

### 3. Gán vị trí / Position Mapping

```text id="posmap"
row = index / columns
col = index % columns
x = col * cellWidth
y = row * cellHeight
```

---

### 4. Lấy text / Resolve Text

```text id="lang"
l = 1 → English
l = 2 → Vietnamese
```

```text id="map"
@1 → insert / Thêm vào
```

---

### 5. Render UI

* 🇻🇳 Tạo component và hiển thị
* 🇬🇧 Create components and render

---

## 🔄 Quy trình / Workflow

```text id="workflow"
1. Design UI (p100fx)
2. Define text (p101)
3. Save parametfile + font files
4. Run application
5. mread() loads data
6. UI is built dynamically
```

---

## 🧩 Nguyên tắc / Principles

* 🇻🇳 Data-driven, không hardcode

* 🇬🇧 Data-driven, no hardcoding

* 🇻🇳 Tách biệt layout / text / logic

* 🇬🇧 Separation of layout / text / logic

---

## 🚀 Ưu điểm / Advantages

* ✔ Không cần sửa code khi đổi UI / No recompilation for UI changes
* ✔ Đa ngôn ngữ đơn giản / Easy multi-language
* ✔ Nhẹ / Lightweight
* ✔ Mở rộng dễ / Scalable

---

## 🔮 Định hướng / Vision

**🇻🇳**
Xây dựng một hệ thống UI điều khiển hoàn toàn bằng dữ liệu.

**🇬🇧**
Build a UI system fully driven by data.

---

## 🤝 Đóng góp / Contribution

**🇻🇳**

Đây là một ý tưởng mở.
Dự án không nhằm trình diễn, mà để cùng xây dựng.

Nếu bạn quan tâm:

* data-driven UI
* hệ thống nhẹ
* cách tiếp cận khác

👉 Bạn có thể tham gia và phát triển theo cách riêng của mình.

---

**🇬🇧**

This is an open idea.

The project is not meant to demonstrate, but to be explored and developed together.

If you are interested in:

* data-driven UI
* lightweight systems
* alternative approaches

👉 You are welcome to explore and build your own way.

---

## 🧭 Ghi chú / Note

**🇻🇳**
Dự án cung cấp ý tưởng và cấu trúc, không phải hướng dẫn từng bước.

**🇬🇧**
This project provides ideas and structure, not step-by-step instructions.

---

## 👤 Tác giả / Author
Thanh Lam Nguyen

**🇻🇳**
Tiếp nối từ hệ thống PowerBASIC trước đây.

**🇬🇧**
A continuation of a previous PowerBASIC system.

---

**“Mọi vấn đề đều có thể giải quyết bằng dữ liệu và toán học.”**
**“Everything can be solved with data and mathematics.”**
