MERGE INTO users (id, username, email, plan, created_at) KEY(id)
VALUES (1, 'free_user', 'free.user@aiproxy.com', 'FREE', CURRENT_TIMESTAMP);

MERGE INTO users (id, username, email, plan, created_at) KEY(id)
VALUES (2, 'pro_user', 'pro.user@aiproxy.com', 'PRO', CURRENT_TIMESTAMP);

MERGE INTO users (id, username, email, plan, created_at) KEY(id)
VALUES (3, 'enterprise_user', 'enterprise.user@aiproxy.com', 'ENTERPRISE', CURRENT_TIMESTAMP);

MERGE INTO quotas (id, user_id, tokens_used, tokens_limit, reset_date, last_updated) KEY(id)
VALUES (1, 1, 0, 50000, DATE '2026-05-01', CURRENT_TIMESTAMP);

MERGE INTO quotas (id, user_id, tokens_used, tokens_limit, reset_date, last_updated) KEY(id)
VALUES (2, 2, 0, 500000, DATE '2026-05-01', CURRENT_TIMESTAMP);

MERGE INTO quotas (id, user_id, tokens_used, tokens_limit, reset_date, last_updated) KEY(id)
VALUES (3, 3, 0, 9223372036854775807, DATE '2026-05-01', CURRENT_TIMESTAMP);


