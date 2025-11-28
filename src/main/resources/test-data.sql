-- Тестовые данные (опционально)
INSERT INTO users (username, password, email) VALUES (
'testuser', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'test@email.com'
); -- password - password
INSERT INTO user_roles (user_id, roles) VALUES (1, 'USER');
INSERT INTO birthdays (name, date, contact, user_id) VALUES ('Rick Sanchez', '2025-12-25', 'ivan@tg', 1);
INSERT INTO greetings (text, media_url, is_public, likes_count, user_id) VALUES ('С ДР!', 'photo.jpg', true, 5, 1);