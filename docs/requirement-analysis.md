
# PHÂN TÍCH YÊU CẦU HỆ THỐNG ONLINE SHOP

## 1. Giới thiệu hệ thống

OnlineShop là hệ thống thương mại điện tử cho phép khách hàng xem, tìm kiếm, lọc và sắp xếp sản phẩm, quản lý giỏ hàng và đặt hàng trực tuyến.

Hệ thống có hai nhóm người dùng chính:
- Khách hàng (Customer).
- Quản trị viên (Admin).

## 2. Đối tượng sử dụng

### 2.1. Khách hàng
- Đăng ký, đăng nhập và đăng xuất.
- Xem danh sách sản phẩm.
- Tìm kiếm sản phẩm.
- Lọc sản phẩm theo danh mục và giá.
- Sắp xếp sản phẩm.
- Xem chi tiết sản phẩm.
- Thêm, sửa và xóa sản phẩm trong giỏ hàng.
- Thanh toán và đặt hàng.
- Xem lịch sử và chi tiết đơn hàng.

### 2.2. Quản trị viên
Các chức năng quản trị sản phẩm, danh mục, đơn hàng và thống kê thuộc phạm vi hệ thống. Phần triển khai do thành viên phụ trách Admin thực hiện và cần được xác minh riêng.

## 3. Yêu cầu chức năng

| Mã | Chức năng | Đối tượng |
|---|---|---|
| KH01 | Đăng ký tài khoản | Customer |
| KH02 | Đăng nhập/đăng xuất | Customer |
| KH04 | Xem danh sách sản phẩm | Customer |
| KH05 | Tìm kiếm sản phẩm | Customer |
| KH06 | Lọc sản phẩm | Customer |
| KH07 | Sắp xếp sản phẩm | Customer |
| KH08 | Xem chi tiết sản phẩm | Customer |
| KH09 | Quản lý giỏ hàng | Customer |
| KH10 | Checkout và đặt hàng | Customer |
| KH11 | Xem lịch sử đơn hàng | Customer |
| KH12 | Xem chi tiết đơn hàng | Customer |
| AD02 | Quản lý sản phẩm | Admin |
| AD03 | Quản lý danh mục | Admin |
| AD04 | Quản lý đơn hàng | Admin |
| AD05 | Cập nhật trạng thái đơn hàng | Admin |
| AD06 | Thống kê hoạt động | Admin |

## 4. Yêu cầu phi chức năng

- Giao diện dễ sử dụng và hỗ trợ thiết bị di động.
- Dữ liệu được lưu trữ trong MySQL.
- Sử dụng Spring Boot và Spring Data JPA để xử lý nghiệp vụ.
- Sử dụng Spring Security và JWT để xác thực người dùng.
- Kiểm soát quyền truy cập dữ liệu đơn hàng.
- Sử dụng Transaction để đảm bảo tính toàn vẹn dữ liệu khi Checkout.

## 5. Phạm vi hệ thống

Hệ thống hỗ trợ quản lý sản phẩm, giỏ hàng và đơn hàng.

Chức năng thanh toán hiện tại là tạo đơn hàng với địa chỉ giao hàng, chưa tích hợp cổng thanh toán trực tuyến.

Các chức năng như đăng nhập mạng xã hội, theo dõi giao hàng trực tiếp và tích hợp nhiều đơn vị vận chuyển không nằm trong phạm vi hiện tại.
