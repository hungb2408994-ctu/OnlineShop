
# WORKFLOWS – ONLINE SHOP

## 1. Quy trình đăng ký và đăng nhập

1. Người dùng truy cập hệ thống.
2. Chọn đăng ký hoặc đăng nhập.
3. Nhập thông tin tài khoản.
4. Hệ thống kiểm tra thông tin.
5. Nếu hợp lệ, hệ thống tạo tài khoản hoặc xác thực người dùng.
6. Nếu đăng nhập thành công, hệ thống cấp JWT.
7. Người dùng truy cập các chức năng được cấp quyền.

## 2. Quy trình xem và tìm kiếm sản phẩm

1. Người dùng truy cập trang sản phẩm.
2. Hệ thống truy vấn dữ liệu từ bảng products.
3. Người dùng có thể tìm kiếm theo tên.
4. Người dùng có thể lọc theo danh mục hoặc khoảng giá.
5. Người dùng có thể sắp xếp theo giá.
6. Hệ thống trả về danh sách sản phẩm phù hợp.

## 3. Quy trình quản lý giỏ hàng

1. Người dùng đăng nhập.
2. Chọn sản phẩm cần mua.
3. Hệ thống kiểm tra sản phẩm và số lượng tồn kho.
4. Người dùng thêm sản phẩm vào giỏ hàng.
5. Hệ thống lưu thông tin vào carts và cart_items.
6. Người dùng có thể cập nhật số lượng hoặc xóa sản phẩm.
7. Hệ thống tính lại tổng tiền giỏ hàng.

## 4. Quy trình Checkout

1. Người dùng đăng nhập.
2. Người dùng mở giỏ hàng.
3. Nhập địa chỉ giao hàng.
4. Hệ thống kiểm tra giỏ hàng.
5. Hệ thống kiểm tra số lượng tồn kho từng sản phẩm.
6. Nếu sản phẩm không đủ tồn kho, Checkout thất bại.
7. Nếu hợp lệ, hệ thống tạo đơn hàng trong orders.
8. Lưu các sản phẩm vào order_items.
9. Cập nhật số lượng tồn kho.
10. Xóa các sản phẩm khỏi giỏ hàng.
11. Transaction commit nếu toàn bộ thao tác thành công.
12. Transaction rollback nếu xảy ra lỗi.

## 5. Quy trình thống kê đơn hàng

1. Người dùng đăng nhập.
2. Gửi yêu cầu xem thống kê đơn hàng.
3. Spring Boot gọi Stored Procedure sp_user_order_summary.
4. MySQL tổng hợp số lượng đơn hàng theo trạng thái.
5. MySQL trả kết quả cho Spring Boot.
6. Spring Boot trả dữ liệu thống kê cho người dùng.

## 6. Quy trình tính tổng đơn hàng

1. Người dùng gửi yêu cầu xem tổng tiền đơn hàng.
2. Hệ thống xác thực JWT.
3. Kiểm tra đơn hàng có thuộc người dùng hay không.
4. Nếu không thuộc người dùng, từ chối truy cập.
5. Nếu hợp lệ, Spring Boot gọi Function fn_order_total.
6. MySQL tính tổng quantity * unit_price.
7. Trả kết quả cho người dùng.

## 7. Quy trình ghi nhận lịch sử trạng thái đơn hàng

1. Trạng thái đơn hàng được cập nhật.
2. MySQL kích hoạt Trigger trg_order_status_history.
3. Trigger so sánh trạng thái cũ và mới.
4. Nếu trạng thái thay đổi, Trigger ghi vào order_status_history.
5. Nếu trạng thái không đổi, không tạo bản ghi lịch sử.

### 8. Transaction và Rollback khi Checkout

Hệ thống sử dụng `@Transactional` trong phương thức
`OrderService.checkout()` để đảm bảo tính toàn vẹn dữ liệu.

**Kịch bản kiểm thử:**
- iPhone 16: tồn kho 9, đặt mua 2.
- Samsung S25: tồn kho 14, đặt mua 20.

**Kết quả:**
- Checkout thất bại do Samsung S25 không đủ tồn kho.
- Tồn kho iPhone 16 vẫn là 9.
- Tồn kho Samsung S25 vẫn là 14.
- Không tạo đơn hàng mới.
- Giỏ hàng không bị xóa.

**Kết luận:** Transaction rollback hoạt động đúng,
đảm bảo các thao tác trong checkout được thực hiện
toàn vẹn hoặc hoàn tác khi xảy ra lỗi.