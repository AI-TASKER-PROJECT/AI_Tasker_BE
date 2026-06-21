# Quản lý Hợp đồng & NDA

Q: Khi nào hợp đồng nháp được tạo?
A: Hệ thống sẽ tự động tạo hợp đồng nháp ngay khi Doanh nghiệp hoàn tất việc chọn chuyên gia.

Q: Làm thế nào để hợp đồng chính thức có hiệu lực (active)?
A: Hợp đồng chỉ kích hoạt (active) khi cả Doanh nghiệp và Chuyên gia cùng nhấn Accept.

Q: Nếu chỉ có một bên nhấn Accept thì hợp đồng đã active chưa?
A: Chưa. Hợp đồng bắt buộc phải được cả hai bên cùng nhấn Accept thì mới chính thức chuyển sang trạng thái active.

Q: Khi nào hệ thống tự động sinh văn bản NDA?
A: Ngay sau khi hợp đồng chuyển sang trạng thái active, hệ thống sẽ tự động sinh văn bản NDA.

Q: Sau khi hệ thống sinh NDA thì hai bên phải làm gì?
A: Hệ thống sẽ gửi thông báo và yêu cầu cả Doanh nghiệp lẫn Chuyên gia đều phải vào xác nhận văn bản NDA này.

# Triển khai & Nghiệm thu dự án

Q: Ai là người thiết lập cột mốc (milestone) và tiêu chí nghiệm thu?
A: Doanh nghiệp sẽ chịu trách nhiệm thiết lập các cột mốc (milestone) và tiêu chí nghiệm thu (acceptance criteria) cho dự án.

Q: Chuyên gia có cần thiết lập tiêu chí nghiệm thu không?
A: Không. Việc thiết lập milestone và tiêu chí nghiệm thu hoàn toàn do Doanh nghiệp thực hiện trên hệ thống.

Q: Quy trình bàn giao sản phẩm diễn ra như thế nào?
A: Chuyên gia sẽ tiến hành bàn giao sản phẩm trên hệ thống, sau đó Doanh nghiệp có nhiệm vụ kiểm tra và nghiệm thu sản phẩm đó.

Q: Nếu Chuyên gia bàn giao sản phẩm mà Doanh nghiệp không phản hồi thì sao?
A: Nếu Doanh nghiệp im lặng quá 3 ngày kể từ khi Chuyên gia bàn giao, hệ thống sẽ tự động chuyển trạng thái sang đã phê duyệt.

Q: Thời gian hệ thống tự động duyệt sản phẩm bàn giao là bao lâu?
A: Thời gian là 3 ngày. Quá hạn 3 ngày im lặng từ phía Doanh nghiệp, hệ thống sẽ tự động nghiệm thu sản phẩm cho Chuyên gia.