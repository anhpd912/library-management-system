# Script thuyết trình (đọc trực tiếp)

## Mở đầu
Em xin giới thiệu Library Management System — hệ thống quản lý thư viện xây bằng Spring Boot 4, bảo mật bằng JWT stateless. Hệ thống có 6 nghiệp vụ chính: mượn, trả, gia hạn, đặt trước, quản lý phạt, và dashboard thống kê.

## 1. Luồng Mượn sách
Khi user mượn sách, hệ thống check đầu tiên: user có khoản phạt chưa thanh toán không — nếu có, chặn ngay với mã 402, bắt user trả phạt trước. Tiếp theo, hệ thống ưu tiên kiểm tra xem user có đang được giữ sách từ một reservation không — nếu có thì lấy bản đó. Nếu không, tìm một bản sách đang AVAILABLE. Cuối cùng tạo bản ghi mượn với hạn trả 14 ngày.

## 2. Luồng Trả sách
Khi trả sách, có 3 trường hợp: trả bình thường đúng hạn — không phạt gì. Trả trễ hạn — tự tính phạt theo số ngày trễ nhân 5,000 đồng. Và đây là điểm mới em vừa bổ sung: nếu sách bị hư hỏng, thủ thư có thể nhập thêm phí hư hỏng cộng vào phạt; nếu sách bị mất hẳn, hệ thống tự tính phạt bằng đúng giá trị cuốn sách, và bản sách đó sẽ bị đánh dấu LOST, loại khỏi vòng lưu thông luôn, không cho mượn lại nữa.

## 3. Luồng Gia hạn
Đây là tính năng tự phục vụ cho reader. Để gia hạn được, phải thỏa đủ 5 điều kiện: sách đang mượn chưa trả, chưa quá hạn, chưa gia hạn quá 2 lần, không có ai khác đang đặt trước cuốn này, và reader chỉ được gia hạn sách của chính mình. Nếu đủ điều kiện, hạn trả được cộng thêm 14 ngày.

## 4. Luồng Đặt trước
Khi sách hết bản để mượn, user có thể đặt trước. Khi có người trả sách, hệ thống tự động giữ bản đó cho người đặt trước đầu tiên trong vòng 24 giờ. Nếu họ mượn trong 24h đó — coi như hoàn thành. Nếu không, hệ thống tự giải phóng sách về lại cho người khác.

## 5. Dashboard & background job
Hệ thống có 2 tác vụ tự động chạy ngầm. Một job chạy lúc nửa đêm để rà soát toàn bộ sách quá hạn, tự động chuyển trạng thái và tính phạt. Một job khác chạy mỗi giờ để kiểm tra các lượt đặt trước đã hết 24h chưa, nếu hết thì giải phóng sách. Dashboard thì tổng hợp số liệu: tổng sách, số đang mượn, top 5 sách mượn nhiều nhất trong tháng, và tổng tiền phạt đang chờ thu / đã thu.

## Kết
Tổng kết lại, hệ thống xử lý đầy đủ vòng đời một cuốn sách trong thư viện — từ lúc mượn, gia hạn, đặt trước, đến các tình huống phát sinh như trễ hạn, hư hỏng, hay mất sách. Em xin demo trực tiếp ngay sau đây.
