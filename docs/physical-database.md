# Physical Database Design – OnlineShop

## 1. Tổng quan

| Thuộc tính | Giá trị |
|---|---|
| Database | `online_shop` |
| DBMS | MySQL |
| Storage Engine | InnoDB |
| Primary Key | BIGINT AUTO_INCREMENT |
| Tiền tệ | DECIMAL(38,2) |
| Character Set | utf8mb4 |
| Transaction | InnoDB Transaction |

## 2. Danh sách bảng

| STT | Bảng | Chức năng |
|---|---|---|
| 1 | users | Lưu tài khoản người dùng |
| 2 | roles | Lưu danh sách vai trò |
| 3 | user_roles | Liên kết người dùng và vai trò |
| 4 | categories | Lưu danh mục sản phẩm |
| 5 | product_models | Lưu nhóm sản phẩm |
| 6 | products | Lưu sản phẩm |
| 7 | carts | Lưu giỏ hàng |
| 8 | cart_items | Lưu chi tiết giỏ hàng |
| 9 | orders | Lưu đơn hàng |
| 10 | order_items | Lưu chi tiết đơn hàng |
| 11 | order_status_history | Lưu lịch sử thay đổi trạng thái đơn hàng |

## 3. Chi tiết cấu trúc bảng

### 3.1. Bảng users

| Cột | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| user_id | BIGINT | PK, AUTO_INCREMENT | Mã người dùng |
| full_name | VARCHAR | NOT NULL | Họ tên |
| email | VARCHAR | UNIQUE, NOT NULL | Email |
| password | VARCHAR | NOT NULL | Mật khẩu đã mã hóa |
| phone | VARCHAR | | Số điện thoại |
| created_at | TIMESTAMP | | Ngày tạo |
| role | VARCHAR(20) | DEFAULT 'USER' | Vai trò |

### 3.2. Bảng roles

| Cột | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| role_id | BIGINT | PK, AUTO_INCREMENT | Mã vai trò |
| role_name | VARCHAR | UNIQUE | Tên vai trò |

### 3.3. Bảng user_roles

| Cột | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| user_id | BIGINT | PK, FK | Mã người dùng |
| role_id | BIGINT | PK, FK | Mã vai trò |

### 3.4. Bảng categories

| Cột | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| category_id | BIGINT | PK, AUTO_INCREMENT | Mã danh mục |
| category_name | VARCHAR | NOT NULL | Tên danh mục |
| description | TEXT | | Mô tả |

### 3.5. Bảng product_models

| Cột | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| product_model_id | BIGINT | PK, AUTO_INCREMENT | Mã nhóm sản phẩm |
| category_id | BIGINT | NOT NULL | Mã danh mục |
| description | TEXT | | Mô tả |
| image_url | VARCHAR | | Đường dẫn ảnh |
| product_name | VARCHAR | | Tên nhóm sản phẩm |

**Lưu ý:** `category_id` hiện chưa có khóa ngoại được khai báo trong database.

### 3.6. Bảng products

| Cột | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| product_id | BIGINT | PK, AUTO_INCREMENT | Mã sản phẩm |
| name | VARCHAR | NOT NULL | Tên sản phẩm |
| description | TEXT | | Mô tả |
| price | DECIMAL(38,2) | CHECK >= 0 | Giá bán |
| stock_quantity | INT | CHECK >= 0 | Số lượng tồn kho |
| image_url | VARCHAR | | Ảnh sản phẩm |
| category_id | BIGINT | FK | Mã danh mục |
| created_at | TIMESTAMP | | Ngày tạo |
| variant_name | VARCHAR | | Phiên bản |
| product_model_id | BIGINT | FK | Mã nhóm sản phẩm |

### 3.7. Bảng carts

| Cột | Kiểu dữ liệu | Ràng buộc | Ý nghĩa |
|---|---|---|---|
| cart_id | BIGINT | PK, AUTO_INCREMENT | Mã giỏ hàng